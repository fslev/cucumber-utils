Feature: Parameter type feature

  Scenario: Test Object conversion to String
  By default, Cucumber-JVM passes argument values from the Gherkin syntax, as Strings.
  Since 'param={}' stores inside scenario props any object it receives as argument, no conversion is needed.
    When param a="[1,2,3]"
    Then This is a String: #[a]