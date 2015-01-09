package com.ullink.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

// Extracted from Peter Niederwieser work in spockframework:
// https://github.com/spockframework/spock/blob/4916ace3cbb853193c65db312214dbdf66ce62ae/gradle/publishMaven.gradle
class OptionalPlugin implements Plugin<Project> {
    void apply(Project project) {
        def optionalDeps = []
        def providedDeps = []
        def internalDeps = []

        project.ext {
            optional = { optionalDeps << it; it }
            provided = { providedDeps << it; it }
            internal = { internalDeps << it; it }
            patchPom = { pom ->
                optionalDeps.each { dep ->
                    pom.dependencies.find { matches(it, dep) }.optional = true
                }
                providedDeps.each { dep ->
                    pom.dependencies.find { matches(it, dep) }.scope = "provided"
                }
                internalDeps.each { dep ->
                    pom.dependencies.removeAll { matches(it, dep) }
                }
                // no need to publish test dependencies
                pom.dependencies.removeAll { it.scope == "test" }
            }
        }

        project.afterEvaluate {
            def deployers = []

            project.install {
                deployers << repositories.mavenInstaller
            }

            deployers*.pom.each {
                it.whenConfigured(project.patchPom)
            }
        }
    }

    boolean matches(/*org.apache.maven.model.Dependency*/ mvn, org.gradle.api.artifacts.ModuleDependency dep) {
        if (mvn.groupId != dep.group) {
            return false
        }
        if (dep.artifacts.isEmpty()) {
            return mvn.artifactId == dep.name
        }
        for (org.gradle.api.artifacts.DependencyArtifact art in dep.artifacts) {
            if (mvn.artifactId == art.name) {
                return true
            }
        }
        false
    }
}
