package com.cucumbergoodies.selenium.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction


class CucumberTask extends DefaultTask {
	@Input def stepDefinitionsPath
	@Input def featuresPath
	@Input def cucumberTaskTags
	@Input def dryRunFlag

	CucumberTask() {}

	@TaskAction
	def start() {
		withExceptionHandling {
			project.javaexec({
				classpath project.buildscript.configurations.classpath
				main 'cucumber.api.cli.Main'
				systemProperties System.properties
				args '--plugin', 'html:build/reports/cucumber/html/cucumber-html-report',
						'-f', 'json:build/reports/cucumber/cucumber.json',
						'-f', 'junit:build/reports/cucumber/cucumber.xml',
						'-f', 'pretty',
						'--glue', getStepDefinitionsPath(),
						getFeaturesPath(),
						'--tags', getCucumberTaskTags(),
						getDryRunFlag()
			})
		}
	}

	def withExceptionHandling(Closure c) {
		try {
			c()
		}
		catch (Exception e) {
			logger.error "Failed to execute CucumberTask", e
			throw new GradleException(e.message)
		}
	}
}
