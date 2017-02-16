package com.cucumbergoodies.selenium.plugin

import com.cucumbergoodies.selenium.plugin.tasks.CucumberJUnitTestRunner
import com.cucumbergoodies.selenium.plugin.tasks.MergeReports
import org.gradle.api.tasks.SourceSetContainer

import static com.cucumbergoodies.selenium.plugin.ParallelizeOptions.PARALLELIZE_OPTIONS
import com.cucumbergoodies.selenium.plugin.tasks.CucumberTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class CucumberExtensionsPlugin implements Plugin<Project> {
	static final String CUCUMBER_EXTENSION_NAME = 'cucumberExtensions'

	@Override
	void apply(Project project) {
		println('cucumber goodies')
		project.extensions.create(CUCUMBER_EXTENSION_NAME, CucumberExtensionsPluginExtension)
		addTasks(project)
		SourceSetContainer sourceSets =
				(SourceSetContainer) project.getProperties().get("sourceSets");
		project.configurations {
			cucumberRuntime {
				extendsFrom testRuntime
			}
		}
		sourceSets.getByName("test").getJava()
				.srcDirs(["src/test/java", "$project.buildDir/src/java"]);
	}

	def addTasks(Project project) {
		project.task('cucumberTask', type: CucumberTask) {
			conventionMapping.stepDefinitionsPath = { getStepDefinitionsPath(project) }
			conventionMapping.featuresPath = { getFeaturesPath(project) }
			conventionMapping.tags = { getTags(project) }
			conventionMapping.dryRunFlag = { getDryRunFlag(project) }
		}
		project.task('junitTestRunner', type: CucumberJUnitTestRunner) {
			conventionMapping.parallelize = { getParallelize(project) }
		}
		project.task('mergeReports', type: MergeReports)
	}

	private String getStepDefinitionsPath(Project project) {
		project.hasProperty('stepDefinitionsPath') ? project.stepDefinitionsPath :
				project.extensions.findByName(CUCUMBER_EXTENSION_NAME).stepDefinitionsPath
	}

	private String getFeaturesPath(Project project) {
		project.hasProperty('featuresPath') ? project.featuresPath:
				project.extensions.findByName(CUCUMBER_EXTENSION_NAME).featuresPath
	}

	private String getTags(Project project) {
		project.hasProperty('tags') ? project.tags:
				project.extensions.findByName(CUCUMBER_EXTENSION_NAME).tags
	}

	private String getDryRunFlag(Project project) {
		project.hasProperty('dryRunFlag') ? project.dryRunFlag
				: project.extensions.findByName(CUCUMBER_EXTENSION_NAME).dryRunFlag
	}

	private PARALLELIZE_OPTIONS getParallelize(Project project) {
		project.hasProperty('parallelize') ? project.parallelize
				: project.extensions.findByName(CUCUMBER_EXTENSION_NAME).parallelize
	}
}