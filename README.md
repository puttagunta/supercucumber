# Parallelization
> This can be done in two ways with the help of the plugin code of this repo. One of the ways is to
parallelizing features and other being parallelizing scenarios. The task, which can be used for this
purpose is,
<pre>CucumberJUnitTestRunner</pre>
> This task is responsible for preparing necessary infrastructure which will be used by gradle's test
task to parallelize the execution.
### How?
> The consumer should have a *test* task which should depend on the task, `junitTestRunner`
which is available in the plugin and which is of type, `CucumberJUnitTestRunner` or have
a task of type `CucumberJUnitTestRunner` in your gradle, which should in turn be a
dependent task of the *test* task.
###### input
> input for this task is given through an extension closure which will be an input for the other task
in the plugin of type, `CucumberTask` or the task, `cucumberTask`. Properties of `cucumberExtensions`
closure are as follows.
<pre>
stepDefinitionsPath 'com.example.stepdefs'
featuresPath 'src/test/resources/com/example'
tags '@WIP'
parallelize ParallelizeOptions.PARALLELIZE_OPTIONS.SCENARIOS
browser 'chrome'
</pre>

###### In order to use this plugin, make sure you have the following in your build.gradle
```
apply plugin: 'supercucumber'

import com.cucumbergoodies.selenium.plugin.ParallelizeOptions
import com.cucumbergoodies.selenium.plugin.tasks.CucumberJUnitTestRunner

buildscript {
    repositories {
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots'
        }
    }
    dependencies {
        classpath 'com.cucumbergoodies.seleium:cucumber-extensions:1.0-SNAPSHOT'
    }
}

task supercucumber(type: CucumberJUnitTestRunner) {
	cucumberExtensions {
		stepDefinitionsPath 'com.example.stepdefs'
		featuresPath 'src/test/resources/com/example'
		tags tagsProp
		parallelize ParallelizeOptions.PARALLELIZE_OPTIONS.SCENARIOS
		browser 'chrome'
	}
}

test.dependsOn supercucumber

test {
    maxParallelForks = Integer.parseInt(String.valueOf(System.properties.get('maxParallelForks')))
    forkEvery = 1
}
```