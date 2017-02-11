package com.cucumberextensions.selenium.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class CucumberTestTaskSpec extends Specification {
	static final TASK_NAME = 'cucumberTask'
	static final STEP_DEFS_PATH = 'com.sree.stepdefs'
	static final FEATURES_PATH = 'src/test/resources/com/sree'
	static final TAGS = '@testTag'
	Project project;

	def setup() {
		project = ProjectBuilder.builder().build()
	}

	def 'Applies plugin and sets extension values'() {
		expect:
			project.tasks.findByName(TASK_NAME) == null

		when:
			project.apply plugin: 'supercucumber'
			project.cucumberExtensions {
				stepDefinitionsPath 'com.sree.stepdefs'
				featuresPath 'src/test/resources/com/sree'
				cucumberTaskTags '@testTag'
			}

		then:
			project.extensions.findByName('cucumberExtensions') != null
			Task task = project.tasks.findByName(TASK_NAME)
			task != null
			println TASK_NAME
			task.stepDefinitionsPath == STEP_DEFS_PATH
			task.featuresPath == FEATURES_PATH
			task.cucumberTaskTags == TAGS
	}
}
