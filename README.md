# Gradle tools plugins

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
    
# License

All these scripts are licensed under the [Creative Commons � CC0 1.0 Universal](http://creativecommons.org/publicdomain/zero/1.0/) license with no warranty (expressed or implied) for any purpose.
