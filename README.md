# Gradle common scripts

These plugins are meant at (further) simplifying project build files.
Some may be useful to others, and some could deserve to be standard part of Gradle distribution.

## task-rules plugin

Some task rules that I find handy:

### exec

Pattern: "exec <some groovy code>": Executes the given code within a dynamic task

Sample usage:

    gradle "exec compileJava.classpath.files.each { println it }"
    gradle -Ptype=Wrapper -PgradleVersion=1.3 exec
    
Advanced usage:

    gradle -Ptype=NuGet -PdependsOn=nuget "-Pconfigure=command='push'" exec

### println

Pattern: "println <some groovy code>": Executes the given code within a dynamic task, and print the result

Sample usages:

    gradle "println convention.plugins.base.distsDir"
    gradle "println configurations"
    gradle "println tasks.jar.outputs.files.files"
    
### custom

Pattern: "custom<Task> <some groovy code>": Executes the given task, with the given code as extra configure-ation

Sample usages:

    gradle -Pdebug=true customTest
    gradle "customTest debug true"
    
## functions plugin

Some functions that I find handy:

 - FileCollection getTools() : returns tools.jar path for current JDK
 - void replaceAssemblyAttribute(def file, String name, String value) : helper to replace Assembly attributes in C# AssemblyInfo.cs file
    
## optional plugin

Allows to specify optional/packaged/internal dependencies in your build.
Also removes test dependencies from your published pom.

## plugin-base plugin

Gradle plugin common plugin, with the following features:
 - appends '-SNAPSHOT' to the version unless '-Prelease'
 - '-Psnapshot' enables upload to maven central snapshot repository
 - '-Prelease' enables upload to maven central staging area
 - setup sources for gradle libs in generated eclipse project
 - artifacts setup with jar, groovydoc & sources (mandatory for maven central upload)
    
## plugin-base-ullink plugin

Specific common build file for my gradle plugins (uses the above but also sets common POM info)
    
# License

All these scripts are licensed under the [Creative Commons � CC0 1.0 Universal](http://creativecommons.org/publicdomain/zero/1.0/) license with no warranty (expressed or implied) for any purpose.
