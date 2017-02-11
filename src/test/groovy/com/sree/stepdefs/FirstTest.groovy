package com.sree.stepdefs

import cucumber.api.java.en.Given

class FirstTest {
	@Given(/"^a user testing the scenario$"/)
	public void testScenario() {
		String str = 'sreedhar'
		println str
	}
}