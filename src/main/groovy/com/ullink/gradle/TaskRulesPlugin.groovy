package com.ullink.gradle

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Rule
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.wrapper.Wrapper

class TaskRulesPlugin implements Plugin<Project> {
    private Logger logger = Logging.getLogger(getClass())

    void apply(Project project) {
        // gradle "exec compileJava.classpath.files.each { println it }"
        // gradle -Ptype=Wrapper -PgradleVersion=1.3 exec
        addRule project, 'Pattern: "exec <some groovy code>": Executes the given code within a dynamic task', { String taskName ->
            final String prefix = "exec"
            if (taskName.startsWith(prefix) && taskName.length() >= prefix.length() && !project.tasks.findByName(taskName)) {
                Task dummyTask = createTask(project, taskName)
                def script = 'return { ' + (taskName - prefix) + ' }'
                def del = Eval.me(script)
                dummyTask << {
                    del.setResolveStrategy Closure.DELEGATE_FIRST
                    del.delegate = project
                    del()
                }
            }
        }

        // gradle "println convention.plugins.base.distsDir"
        // gradle "println configurations"
        // gradle "println tasks.jar.outputs.files.files"
        addRule project, 'Pattern: "println <some groovy code>": Executes the given code within a dynamic task, and print the result', { String taskName ->
            final String prefix = "println"
            if (taskName.startsWith(prefix) && taskName.length() > prefix.length() && !project.tasks.findByName(taskName)) {
                Task dummyTask = createTask(project, taskName)
                def script = 'return { ' + (taskName - prefix) + ' }'
                def del = Eval.me(script)
                dummyTask << {
                    del.setResolveStrategy Closure.DELEGATE_FIRST
                    del.delegate = project
                    println del()
                }
            }
        }

        // gradle "customTest debug true"
        // gradle -Pdebug=true customTest
        addRule project, 'Pattern: "custom<Task> <some groovy code>": Executes the given task, with the given code as extra configure-ation', { String taskName ->
            final String prefix = "custom"
            if (taskName.startsWith(prefix) && taskName.length() > prefix.length() && !project.tasks.findByName(taskName)) {
                String orig = taskName - prefix
                String extra
                int i = orig.indexOf(' ')
                if (i != -1) {
                    extra = orig.substring(i + 1)
                    orig = orig.substring(0, i)
                }
                orig = '' + orig.charAt(0).toLowerCase() + orig.substring(1)
                def task = project.tasks.findByName(orig)

                if (!task) {
                    logger.debug("Skipping missing task '$orig' on project '$project.name'")
                    return
                }

                Map<String, ?> props = project.getProperties()
                task.metaClass.getProperties().each() {
                    def value = props[it.name]
                    if (value && !Project.class.metaClass.hasProperty(null, it.name)) {
                        if (it.type == String.class || it.type == Boolean.class || it.type == boolean.class) {
                            println "set ${it.name} = ${value}"
                            it.setProperty(task, value)
                        }
                        // TODO support other types (numbers)
                    }
                }
                Map args = [dependsOn: task]
                Task dummyTask = project.task(args, taskName)
                if (extra) {
                    def script = 'return { ' + extra + ' }'
                    def del = Eval.me(script)
                    del.setResolveStrategy Closure.DELEGATE_FIRST
                    del.delegate = task
                    del()
                }
                dummyTask
            }
        }
    }

    Class<?> classForNameNoThrow(Project project, String s) {
        // classloader bitches
        try {
            // not much in this one
            return Class.forName(s)
        } catch (Exception e) {
            project.logger.debug "Failed: ${e}"
        }
        try {
            // gradle-core
            return Project.class.getClassLoader().loadClass(s)
        } catch (Exception ee) {
            project.logger.debug "Failed: ${ee}"
        }
        try {
            // gradle-plugins
            return Wrapper.class.getClassLoader().loadClass(s)
        } catch (Exception eee) {
            project.logger.debug "Failed: ${eee}"
        }
    }

    Task createTask(Project project, String name, Map args = [:]) {
        if (project.hasProperty('type')) {
            String type = project.getProperty('type')
            def found = project.tasks.find { (it.class.name - '_Decorated') == type }
            found = found ?: project.tasks.find { it.name == type }
            found = found ?: project.tasks.find { (it.class.simpleName - '_Decorated') == type }
            found = found ?: project.tasks.find {
                (it.class.simpleName - '_Decorated').toLowerCase() == type.toLowerCase()
            }
            found = found ?: classForNameNoThrow(project, type)
            found = found ?: classForNameNoThrow(project, "org.gradle.api.tasks.${type}")
            found = found ?: classForNameNoThrow(project, "org.gradle.api.tasks.${type.toLowerCase()}.${type}")
            if (found) {
                args.type = found instanceof Class ? found : found.class
                project.logger.info "Inline task type: ${args.type}"
            } else {
                throw new GradleException("Failed to find requested task base type ${type}")
            }
        }
        if (project.hasProperty('dependsOn')) {
            args.dependsOn = getProperty('dependsOn')
        }
        Task dummyTask = project.task(args, name)
        if (project.hasProperty('configure')) {
            def script = 'return { ' + getProperty('configure') + ' }'
            def del = Eval.me(script)
            del.setResolveStrategy Closure.DELEGATE_FIRST
            del.delegate = dummyTask
            del()
        }
        Map<String, ?> props = project.getProperties()
        dummyTask.metaClass.getProperties().each() {
            def value = props[it.name]
            if (value && !Project.class.metaClass.hasProperty(null, it.name)) {
                if (it.type == String.class || it.type == Boolean.class || it.type == boolean.class) {
                    println "set ${it.name} = ${value}"
                    it.setProperty(dummyTask, value)
                }
                // TODO support other types (numbers)
            }
        }
        dummyTask
    }

    Rule addRule(Project project, String description, Closure ruleAction) {
        if (!project.tasks.getRules().any { it.description == description }) {
            project.tasks.addRule(description, ruleAction)
        }
    }
}
