package com.ullink.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class FunctionsPlugin implements Plugin<Project> {
    void apply(Project project) {
        /* FileCollection */ project.ext.getTools = { ->
            def jHome = System.getProperty('java.home')
            def path = new File(jHome, 'lib/tools.jar')
            if (path.exists()) {
                return project.files(path)
            }
            path = new File(jHome, '../lib/tools.jar')
            if (path.exists()) {
                return project.files(path)
            }
            assert false, "Failed to find tools.jar"
        }

        // TODO: move to msbuild-plugin?
        /* void */ project.ext.replaceAssemblyAttribute = { def file, String name, String value ->
            project.ant.replaceregexp(file: project.file(file), match: /^\[assembly: / + name + /\(".*"\)\]$/, replace: '[assembly: ' + name + '("' + value + '")]', byline: true)
        }
    }
}
