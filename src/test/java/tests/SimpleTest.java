package tests;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features",
        glue = {"ro.cucumber.stepdefs.http", "ro.cucumber.core.config"}, plugin = {"pretty",
                "html:target/cucumber-html-report", "json:target/cucumber-report/report.json"})
public class SimpleTest {
}
