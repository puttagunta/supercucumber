package com.cucumbergoodies.selenium.plugin.tasks

import groovy.json.JsonOutput
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser

class MergeReports extends DefaultTask {
	@TaskAction
	def generateReports() {
		def jsonReports = project.fileTree(dir: "$project.buildDir/reports/cucumber/json").include '*.json'
		JSONParser jsonParser = new JSONParser();
		def resultantJson = []
		jsonReports.each { File jsonReport ->
			def jsonArray = (JSONArray) jsonParser.parse(new FileReader(jsonReport));
			resultantJson.addAll(jsonArray.flatten());
		}
		File reportsFolder = new File("$project.buildDir/reports/cucumber");
		if (reportsFolder.exists()) {
			File finalJsonReport = new File("$project.buildDir/reports/cucumber/cucumber.json")
			finalJsonReport.write(JsonOutput.toJson(resultantJson))
			def xmlReports = project.fileTree(dir: "$project.buildDir/reports/cucumber/junit").include '*.xml'
			def noOfFailures = 0, noOfSkipped = 0, noOfTests = 0
			def xml = """
					<testsuite failures="0" skipped="0" tests="0"
						name="cucumber.runtime.formatter.JUnitFormatter"
						time="">
					</testsuite>
				"""
			def parser = new XmlParser()
			def resultantXMLReport = parser.parseText(xml)
			xmlReports.each { File xmlReportFile ->
				def testsuite = new XmlParser().parse(xmlReportFile)
				def validFailures = testsuite.@failures && testsuite.@failures.trim().length() > 0
				def validSkipped = testsuite.@skipped && testsuite.@skipped.trim().length() > 0
				def validTests = testsuite.@tests && testsuite.@tests.trim().length() > 0
				noOfFailures += validFailures ? testsuite.@failures.toInteger() : 0
				noOfSkipped += validSkipped ? testsuite.@skipped.toInteger() : 0
				noOfTests += validTests ? testsuite.@tests.toInteger() : 0
				testsuite.children().each {
					resultantXMLReport.append(it)
				}
			}
			resultantXMLReport.@failures = noOfFailures
			resultantXMLReport.@skipped = noOfSkipped
			resultantXMLReport.@tests = noOfTests
			File finalXmlReport = new File("$project.buildDir/reports/cucumber/cucumber.xml")
			finalXmlReport.write(XmlUtil.serialize(resultantXMLReport))
		}
	}
}
