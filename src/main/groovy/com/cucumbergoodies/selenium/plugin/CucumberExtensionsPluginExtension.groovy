package com.cucumbergoodies.selenium.plugin;

import static com.cucumbergoodies.selenium.plugin.ParallelizeOptions.PARALLELIZE_OPTIONS

class CucumberExtensionsPluginExtension {
	def stepDefinitionsPath
	def featuresPath
	def tags
	def browser
	// Following are for cucumber task alone
	def dryRunFlag = '--no-dry-run'
	def mainClass
	// Following are for JUnit task alone
	def PARALLELIZE_OPTIONS parallelize
}
