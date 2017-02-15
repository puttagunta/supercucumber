package com.cucumbergoodies.selenium.plugin.tasks

import com.cucumbergoodies.selenium.plugin.CucumberExtensionsPlugin
import gherkin.formatter.model.Tag

import static com.cucumbergoodies.selenium.plugin.ParallelizeOptions.PARALLELIZE_OPTIONS
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.model.CucumberFeature
import cucumber.runtime.model.CucumberScenario
import cucumber.runtime.model.CucumberScenarioOutline
import gherkin.TagExpression
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CucumberJUnitTestRunner extends DefaultTask {
	@TaskAction
	def start() {
		generateCucumberRunners()
	}

	def generateCucumberRunners() {
		switch (getParallelize()) {
			case PARALLELIZE_OPTIONS.FEATURES:
				getFeaturePathsFilteredByTags().each { featurePath ->
					generateARunner(featurePath)
				}
				break
			case PARALLELIZE_OPTIONS.SCENARIOS:
				generateFeatureFilesPerScenario()
				new File("$project.buildDir/generated-src/features/").listFiles().each { feature ->
					generateARunner(feature.getPath())
				}
				break
			default:
				break
		}
	}

	def getFeaturePathsFilteredByTags() {
		def features = getFeatures()
		def tags = getTags()
		def filteredFeaturePaths = []
		ClassLoader classLoader = getClass().getClassLoader();
		MultiLoader loader = new MultiLoader(classLoader);
		def filters = []
		def featurePaths = features.collect { it.absolutePath }
		if (tags != null) {
			TagExpression tagExpression = new TagExpression(Arrays.asList(tags.split(' ')));
			for (CucumberFeature feature : CucumberFeature.load(loader, featurePaths, filters)) {
				def featureTags = feature.getGherkinFeature().getTags();
				for (Object featureElement : feature.getFeatureElements()) {
					def scenario = null
					if (featureElement instanceof CucumberScenario) {
						scenario = (CucumberScenario) featureElement;
					} else if (featureElement instanceof CucumberScenarioOutline) {
						scenario = (CucumberScenarioOutline) featureElement;
					}
					featureTags.addAll(scenario.getGherkinModel().getTags());
				}
				if (tagExpression.evaluate(featureTags)) {
					filteredFeaturePaths.add(feature.getPath());
				}
			}
		}
		return filteredFeaturePaths;
	}

	def static isTheLineScenarioOrScenarioOutline(line) {
		return line.toLowerCase().startsWith('scenario:') ||
				line.toLowerCase().startsWith('scenario outline:')
	}

	def static getScenarioText(featurePath, lineIndex) {
		def lines = new File(featurePath).readLines()
		def scenarioText = ''
		for (int i = lineIndex - 1; i < lines.size(); i++) {
			def line = lines.get(i).trim()
			scenarioText += line + '\n'
			if (i + 1 < lines.size()) {
				def nextLine = lines.get(i + 1).trim()
				if (nextLine.startsWith('@') ||
						isTheLineScenarioOrScenarioOutline(nextLine)) {
					break;
				}
			}
		}

		return scenarioText
	}

	def static getBackground(featurePath) {
		def featureFile = new File(featurePath)
		def lines = featureFile.readLines()
		def background = ''
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i).trim();
			if (line.toLowerCase().startsWith('background:')) {
				while (!(line.startsWith('@') || isTheLineScenarioOrScenarioOutline(line))) {
					background += line + '\n';
					i = i + 1;
					line = lines.get(i).trim();
				}
			} else if (isTheLineScenarioOrScenarioOutline(line)) {
				break;
			}
		}

		return background;
	}

	def generateFeatureFilesPerScenario() {
		def features = getFeatures()
		def tags = getTags()
		ClassLoader classLoader = getClass().getClassLoader();
		MultiLoader loader = new MultiLoader(classLoader);
		def filters = []
		def featurePaths = features.collect { it.absolutePath }
		new File("$project.buildDir/generated-src/features/").mkdirs()
		if (tags != null) {
			TagExpression tagExpression = new TagExpression(Arrays.asList(tags.split(' ')));
			for (CucumberFeature feature : CucumberFeature.load(loader, featurePaths, filters)) {
				String featureContent = '';
				def featureTags = feature.getGherkinFeature().getTags();
				featureContent += featureTags.collect { it.name }.join(' ')
				featureContent += '\n'
				featureContent += 'Feature: ' + feature.getGherkinFeature().getName() + '\n'
				featureContent += feature.getGherkinFeature().getDescription() + '\n'
				featureContent += getBackground(feature.getPath());
				int fileIndex = 0;
				for (Object featureElement : feature.getFeatureElements()) {
					def scenario = null
					if (featureElement instanceof CucumberScenario) {
						scenario = (CucumberScenario) featureElement;
					} else if (featureElement instanceof CucumberScenarioOutline) {
						scenario = (CucumberScenarioOutline) featureElement;
					}
					def scenarioTags = scenario.getGherkinModel().getTags()
					if (scenarioTags == null) {
						scenarioTags = new ArrayList<Tag>()
					}
					scenarioTags.addAll(featureTags)
					if (tagExpression.evaluate(scenarioTags)) {
						def scenarioText = '';
						scenarioText += scenario.getGherkinModel().getTags().findAll {
							!featureTags.contains(it)
						}.collect({ it.name }).join(' ')
						scenarioText = sprintf('%s\n%s',
								scenarioText,
								getScenarioText(feature.getPath(),
										scenario.getGherkinModel().getLineRange().last))
						scenarioText = sprintf('%s%s', featureContent, scenarioText)
						String featureFileName = feature.getPath().replace('\\', '/').split('/').
								last().split('\\.').first()
						featureFileName = sprintf('%s_%d.feature', featureFileName, fileIndex++)
						File featureFile = new File("$project.buildDir/generated-src/features/" +
								featureFileName)
						featureFile.write(scenarioText);
					}
				}
			}
		}
	}

	def generateARunner(String featurePath) {
		def tags = getTags()
		def browser = getBrowser()
		def templateFile = (tags == null) ? 'CucumberJUnitTestNoTag.java.template' :
				'CucumberJUnitTest.java.template';
		def marphedFeaturePath = featurePath.replace('\\', '/')
		def featureFileName = marphedFeaturePath.substring(marphedFeaturePath.lastIndexOf('/') + 1)
		project.copy {
			def jarFile = project.buildscript.configurations.classpath.find {
				it.name.contains('cucumber-extensions')
			}
			from project.resources.text.fromArchiveEntry(jarFile, 'templates/' + templateFile).asFile()
			into "$project.buildDir/generated-src/java"
			def newFileName = featureFileName.replace('.', '_') + '_' + browser.replace('.', '_')
			expand(featurePath: marphedFeaturePath,
					fileName: featureFileName,
					className: newFileName,
					stepdefs: getStepDefinitionsPath(),
					tags: tags.split(' ').collect { '"' + it + '"' }.join(','))
			rename { file ->
				"Cucumber_${newFileName}.java"
			}
		}
	}

	def getParallelize() {
		project.extensions.findByName(CucumberExtensionsPlugin.CUCUMBER_EXTENSION_NAME)
				.parallelize
	}

	def getBrowser() {
		project.extensions.findByName(CucumberExtensionsPlugin.CUCUMBER_EXTENSION_NAME)
				.browser
	}

	def getStepDefinitionsPath() {
		project.extensions.findByName(CucumberExtensionsPlugin.CUCUMBER_EXTENSION_NAME)
				.stepDefinitionsPath
	}

	def getTags() {
		project.extensions.findByName(CucumberExtensionsPlugin.CUCUMBER_EXTENSION_NAME)
				.tags
	}

	def getFeatures() {
		def featuresPath = project.extensions
				.findByName(CucumberExtensionsPlugin.CUCUMBER_EXTENSION_NAME).featuresPath
		project.fileTree(dir: featuresPath).include('**/features/**/*.feature')
	}
}
