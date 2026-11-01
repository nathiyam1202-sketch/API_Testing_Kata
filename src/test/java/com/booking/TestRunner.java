package com.booking;

import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("com.booking")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.booking")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value =
        "pretty, html:target/cucumber-reports.html, json:target/cucumber.json, junit:target/cucumber.xml, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
public class TestRunner {}
