package com.ullink.gradle

import org.gradle.api.Project

class PluginBaseUllinkPlugin extends PluginBasePlugin {
    void apply(Project project) {
        super.apply(project)
        project.group = 'com.ullink.gradle'
        project.sourceCompatibility = 1.6

        project.ext.pom = { p ->
            p.project {
                url "http://github.com/gluck/${project.name}"

                scm {
                    def git = "scm:git@github.com:gluck/${project.name}.git"
                    url git
                    connection git
                    developerConnection git
                }

                licenses {
                    license {
                        name 'Creative Commons � CC0 1.0 Universal'
                        url 'http://creativecommons.org/publicdomain/zero/1.0/'
                        distribution 'repo'
                    }
                }

                developers {
                    developer {
                        id 'fvaldy'
                        name 'Francois Valdy'
                    }
                }
            }
        }
    }
}
