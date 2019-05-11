# Cucumber Utils

# Summary
A Java library meant to help you write organized and clean **Cucumber** tests.  
It takes over the usual dependencies and features needed inside a test framework, such as:  
* the **comparing** mechanism (compare XMLs, JSONs, and Java objects)  
* customized up-to-date **clients** to access various resources, i.e _databases_, _HTTP services_, etc  
* a light and powerful **state-sharing mechanism** between _Cucumber Scenario Steps_  
* other utility methods           

Most of these features can be used directly from code within your test project and are also available as _Cucumber step definitions_.    

#### Maven Central
```
    <dependency>
        <groupId>io.github.fslev</groupId>
        <artifactId>cucumber-utils</artifactId>
        <version>${latest.version}</version>
    </dependency>

Gradle: compile("io.github.fslev:cucumber-utils:${latest.version}")
```  
#### Included dependencies that are worth to mention:

* [**cucumber-java8**](https://github.com/cucumber/cucumber-jvm/tree/master/java8)
* [**cucumber-guice**](https://github.com/cucumber/cucumber-jvm/tree/master/guice)
* [**xml-unit**](https://github.com/xmlunit/xmlunit)
* [**json-compare**](https://github.com/fslev/json-compare)
* [**apache-http-client**](https://github.com/apache/httpcomponents-client)

# Integration
In order to integrate **cucumber-utils** within your test project you must configure the following **glue** package, either inside your IDE Cucumber plugin or inside the code:
```
com.cucumber.utils
```  

# Features
## 1. Compare mechanism
The following types of objects are supported for complex comparison:
* JSONs (String, JsonNode)  
  * dependency: [**json-compare**](https://github.com/fslev/json-compare)
    ```
    String expected = "{\"!b\":val1\",\"a\":\"val2\"}";
    String actual = "{\"a\":\"val2\",\"c\":\"val1\"}";
    cucumbers.compare(expected, actual); //comparison passes
    ```

* XMLs  
  * dependency: [**xml-unit**](https://github.com/xmlunit/xmlunit)
    ```
    String expected = "<struct><int a=1>some .* text</int><boolean>false</boolean></struct>";
    String actual = "<struct><int a=1>some dummy text</int><boolean>false</boolean></struct>";
    cucumbers.compare(expected, actual); //comparison passes
    ```      

* JSON convertible objects  
     
    ```
    List<String> expected = Arrays.asList(new String[]{"a", "b", "c", ".*"});
    List<String> actual = Arrays.asList(new String[]{"c", "a", "c", "b"});
    cucumbers.compare(expected, actual); //comparison passes
    ```   

* Strings, with regex support   
    ```css
    String expected = "some .* text";
    String actual = "some dummy text";
    cucumbers.compare(expected, actual); //comparison passes
    ```  

* By default, when comparing two Java objects, all compare mechanisms from above are invoked in same order, until one comparison passes.  
  If not, the *equals()* method is used as a last compare mechanism.   

## 2. State-sharing mechanism
The state sharing mechanism uses the power of **guice**, **cucumber-guice** and Cucumber **custom** expressions.  
You can share state between different Cucumber steps using *scenario properties*.  

#### Set and use scenario properties  

- within the Scenario, using the **param \<name\>="\<value\>"** Cucumber step
    ```css
    Scenario: Test scenario properties
        Given param animal="r\"a$b\\"b[it"
        And param location="forest"
        And The string with scenario placeholders "The #[animal] is running through the #[location]"
        Then Check filled string equals "The r\"a$b\\"b[it is running through the forest"
    ```

As you can see, in order to use a scenario property value within the Cucumber scenario, you must call it by its name using the special placeholder symbols **#[ ]**, which are magically parsed in your corresponding step definition if it uses the custom cucumber expression **{cstring}**:     
```java
    @Then("Check filled string equals \"{cstring}\"")
    public void check(String str) {
        assertEquals(str, this.str);
    }
```

- from resource file, via **load scenario props from file "\<relativePath/toFile.properties\>"** Cucumber step           
    ```css
    Scenario: Test scenario properties
    * load scenario props from file "placeholders/scenario.properties"
    Then check #[animal]=Rabbit 
    ```
    where, *scenario.properties* file contains:
    ```
    animal=Rabbit
    ```  
    
    ***Note:***  
    __Supported file types__ for setting scenario properties:
    - ***.properties***
    - ***.yaml***
    - ***.property***  
    
    If a scenario property is read from a **.property** file, it means that the name of the property will actually be the name of the file, without the extension:  
    ```css
      Scenario: Test placeholder fill with scenario property file
        * load scenario props from file "placeholders/figure.property"
        And The string with scenario placeholders "This is a #[figure]"
        Then Check filled string equals "This is a circle"
    ```  
    where, *figure.property* file contains:
    ```
    circle
    ```  

- from resource directory, via **load all scenario props from dir "\<relativePath/toDir\>"** Cucumber step           
    ```css
    Given load all scenario props from dir "placeholders/properties"
    Given The string with scenario placeholders "Soda=#[soda], food=#[food], whisky=#[whisky], burger=#[burger], cheese=#[cheese] and ignore=#[ignore]"
    Then Check filled string equals "Soda=Coca-Cola, food=burger, whisky=Johnny Walker, burger=Cheeseburger, cheese=Mozzarela and ignore=#[ignore]" 
    ```
    where, inside the *properties* directory are defined several files containing the corresponding properties.    
    
    ***Note:***  
    The function for reading scenario properties from a directory walks through the whole directory tree structure. It filters only the supported file types.  
    
- programmatically, by injecting the *ScenarioProps* class via **Guice**:    
    ```java
    import com.google.inject.Inject;

    @ScenarioScoped
    public class ParamSteps {
        @Inject
        private ScenarioProps scenarioProps;
        public void setProp(String name, String value) {
            scenarioProps.put(name, value);
        }
    }
    ```
     
## Setup scenario properties from compare
There is another way to initialise a scenario property. This is done via the *compare mechanism*.  
Suppose you want to extract a value from the JSON response received after calling the API of an application.  
In order to do that, you must compare the actual response with an expected one and inside the expected value you must introduce the variable placeholder **~[var.name.here]**.  

Example:  

```css
Scenario: Test scenario properties
When invoke create user HTTP API of application    
Then check response body="{"id":"~[userId]"}" 
```  
    
If inside the step definition the comparison is done via the compare mechanism from above and if the comparison passes, then a new scenario property will be initialised behind the scenes, having the name ***userId***.  
This new scenario property can be used further inside your test scenario:  

```css
...        
When invoke get user HTTP API of applicaiton with user id = #[userId]
Then check HTTP response status = 200
... 
```  
     
## 3. Clients

The following clients are available via Cucumber-Utils:
- HTTP client
- SQL clients (MySQL, PostgreSQL, Sybase, etc -> depending on the sql driver you configure)

These clients are initialized and configured via the builder pattern, in order to allow addition of new settings between Cucumber steps.      
    
### HTTP Client
Example:  
```java
HttpClient client = new HttpClient.Builder()
                .address("http://example.com")
                .path("/user/")
                .method(Method.POST)
                .addHeader("auth", "authCode")
                .addQueryParam("host","google.ro")
                .entity("{\"a\":\"some json value\"}")
                .build();
        HttpResponse response = client.execute();
        String responseAsString = EntityUtils.toString(response.getEntity());
```

## 4. Pre-defined Cucumber utility steps 

- Compare date times
    ```css
    Given DateTime pattern="yyyy-MM-dd HH:mm:ss"
    Then DateTime check period from "2018-02-03 01:00:00" to "2019-02-03 01:00:00" is 1year
    And DateTime check period from "2018-02-03 01:00:00" to "2019-02-02 12:01:10" is 364days
    And DateTime check period from "2019-02-03 01:02:12" to "2019-02-03 23:59:10" is 22hours
    And DateTime check period from "2019-02-03 22:02:12" to "2019-02-03 23:59:10" is 116minutes
    And DateTime check period from "2019-02-03 23:58:12" to "2019-02-03 23:59:10" is 58seconds
    ``` 
    
- Connect to SQL databases, execute queries, compare results and also execute updates
    ```css
    Scenario: Test MYSQL client select
        Given SQL data source from file path "config/database/mysql.properties"
        Then SQL execute query "select * from gift order by person_id asc limit 3" and compare result with
          | person_id | gift                                                 |
          | .*        | .*                                                   |
          | 21189037  | fun & joy for everybody!                             |
          | 21193939  | Leica M9-P Hermes Edition: http://vimeo.com/42108675 |
    ``` 
    ```css
    Scenario: Test POSTGRESQL client simple insert with tabular data
        Given SQL data source from file path "config/database/psql.properties"
        Then SQL INSERT into table "mag" the following data
          | person_id | description       |
          | 14        | http://heheheh.ro |
          | 16        | null              |
          | 17        | wow               |
    ```
    
    