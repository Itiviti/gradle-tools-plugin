package com.ullink.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.tasks.bundling.Jar

class PluginBasePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'groovy'
        project.apply plugin: 'signing'
        project.apply plugin: 'maven'

        project.repositories {
            mavenLocal()
            mavenCentral()
        }

        project.dependencies {
            compile gradleApi()
            compile localGroovy()
        }

        project.groovydoc {
            source = project.sourceSets.main.allSource
        }

        project.task 'groovydocJar', type: Jar, dependsOn: project.tasks.groovydoc, {
            classifier = 'javadoc'
            from 'build/docs/groovydoc'
        }

        project.task 'sourcesJar', type: Jar, {
            from project.sourceSets.main.allSource
            classifier = 'sources'
        }

        project.artifacts {
            archives project.jar
            archives project.groovydocJar
            archives project.sourcesJar
        }

        project.allprojects {
            afterEvaluate { p ->
                if (p.hasProperty("release")) {
                    setupUploadArchives(project, "https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                } else {
                    p.version += "-SNAPSHOT"
                    if (p.hasProperty("snapshot")) {
                        setupUploadArchives(project, "https://oss.sonatype.org/content/repositories/snapshots/")
                    }
                }

                install {
                    repositories {
                        mavenInstaller {
                            customizePom(project, pom)
                        }
                    }
                }
            }
        }
    }

    def customizePom(Project project, pom) {
        pom.project {
            name project.name
            description project.description
        }
        if (project.ext.has('pom')) {
            project.ext.pom(pom)
        }
    }

    def setupUploadArchives(Project project, sonatypeRepositoryUrl) {
        project.signing {
            sign configurations.archives
        }

        project.uploadArchives {
            repositories {
                mavenDeployer {
                    customizePom(pom)
                    beforeDeployment { MavenDeployment deployment ->
                        project.signing.signPom(deployment)
                    }
                    repository(url: sonatypeRepositoryUrl) {
                        authentication(userName: sonatypeUsername, password: sonatypePassword)
                    }
                }
            }
        }
    }
}
