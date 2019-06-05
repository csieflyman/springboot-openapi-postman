# spring-postman
Convert Spring Boot Controllers to a Postman Collection for REST API Automated Testing
## This tool can help you...
* Convert spring boot controllers to a postman collection automatically to reduce effort in writing API test and ensure test cases are up-to-date.
* Integrate gradle with Node.js script provider a convenient way to run test for developers
* Run newman for each folder of the collection in parallel to reduce execution time. 
* You can control more fine-grained testing behaviors by passing postman test data files to change postman's default behaviors
	* You can specify which one request you want to execute, rather than all requests of the collection _(Postman execute all requests of a collection in declared order for each test data. It's inappropriate in most situations)_
	* You can run test with varying request body and script assertion for each test data _(Postman run the same script assertion which be defined in the request for all test data. It's inappropriate in most situations)_
	* You can compose many test data to a test suite for a complicated scenario without duplicating the request. For example, in _create -> get -> update -> get_ scenario, you have to create two _get_ requests in a collection in postman application, but actually all you need to do is just define two "get" test data in the test data file
* Developers can upload tested collection to postman server, then sync it with other team members.
* Testers neither use this tool nor modify collection, they just sync collection with postman application and run test with test data files

## Workflow
1. Developers generate postman collection json file
2. Developers prepare test data files for simple test cases
3. Developers run test with this tool in the localhost or dev envrionment
4. Developers upload collection to postman server if test has passed
5. Testers prepare test data files for complicated test cases
6. Testers use postman application to run test with test data files

## Usage
1. Run gradle task __"myNpmInstall"__ to install Node.js packages
2. Run gradle task __"generatePostmanCollection"__ to generate collection json file
	* _GeneratePostmanCollection_ task = _generateSwaggerDocumentation_ task + _swaggerToPostmanCollection_ task
	* Note: Because this example is a gradle multi-project. If you get an _ClassNotFound_ error in library project, you can run it again successfully while library jar file is exist after first run.
3. Prepare postman files
	* Edit _"/postman/globals.json"_ if you want to define global environment variables.
	* Put your environment json file _"${envName}-env.json"_ info _"/postman/env"_ directory
	* Put your test data json files _"${folderName}-data.json"_ into _"/postman/folder"_ directory
	* Put your test data json files _"${suiteName}-data.json"_ into _"/postman/suite"_ directory if you want to run comlicated test cases by executing requests cross folders within the collection.
4. Run gradle task __"newman"__ to run test and you can checkout test report in _"/postman/report"_ directory
	* You have to assign argument value _${envName}_(required), _${folderName}_(optional, run all folders if empty)
5. Run gradle task __"uploadToPostmanServer"__ to upload collection and env to postman server
	* You have to assign envrionment variable _"POSTMAN_API_KEY"_
	* You can specify the argument _"uploadEnv"_(boolean) and _"uploadCollection"_(boolean)

## Test data file
* You have to specify three pre-defined variables value for each test data
	* _\_requestName(required)_: The request which you what to execute.
	* _\_requestBody(optional)_: The request body of request if need (POST, PUT...etc)
	* _\_test(required, but can be empty string)_: The javascript code executed after the request is sent. For example, you can check the response for assertion. I think of code as data and pass it to postman. When postman run test script `pm.iterationData.get("\_test");eval(script);`,  it will get _"\_test"_ value and run our script code.
* Example
```
[
  {
    "_requestName": "create User",
    "_requestBody": "{\"gender\": \"MALE\",\"age\": 30,\"isMember\": false}",
    "_test": "pm.test('check name required', function(){pm.response.to.have.status(400);});"
  },
  {
    "_requestName": "create User",
    "_requestBody": "{\"name\": \"csieflyman\",\"gender\": \"MALE\",\"age\": 30,\"isMember\": false}",
    "_test": "pm.test('200 ok', function(){pm.response.to.have.status(200);}); pm.test('check userId after create', function(){Number.isInteger(responseBody);}); postman.setEnvironmentVariable(\"userId\", responseBody);"
  },
  {
    "_requestName": "get User",
    "_test": "pm.test('200 ok', function(){pm.response.to.have.status(200);}); pm.test('check username', function(){pm.expect(pm.response.json().name).to.eql('csieflyman');});"
  },
  {
    "_requestName": "update User",
    "_requestBody": "{\"name\": \"csieflyman\",\"gender\": \"MALE\",\"age\": -1,\"isMember\": true}",
    "_test": "pm.test('check age >= 0', function(){pm.response.to.have.status(400);});"
  },
  {
    "_requestName": "update User",
    "_requestBody": "{\"name\": \"csieflyman\",\"gender\": \"MALE\",\"age\": 30,\"isMember\": true}",
    "_test": "pm.test('200 ok', function(){pm.response.to.have.status(200);});"
  },
  {
    "_requestName": "get User",
    "_test": "pm.test('200 ok', function(){pm.response.to.have.status(200);}); pm.test('check data after update', function(){pm.expect(pm.response.json().name).to.eql('csieflyman');pm.expect(pm.response.json().isMember).to.eql(true);});"
  },
  {
    "_requestName": "find Users",
    "_test": "pm.test('200 ok', function(){pm.response.to.have.status(200);});pm.test('check array size', function(){pm.response.json().length === 1});"
  },
  {
    "_requestName": "delete User",
    "_test": "pm.test('200 ok', function(){pm.response.to.have.status(200);});"
  },
  {
    "_requestName": "get User",
    "_test": "pm.test('404 not found', function(){pm.response.to.have.status(404);});"
  }
]
```

## Mock request
* For specify the request you want to execute rather than all requests of the collection, this tool manipulate the generated collection json with the following changes
	* Create a mock request in each folder automatically and put it to the first position.
	* add  `postman.setNextRequest(pm.iterationData.get("\_requestName"));` in the _Pre-request Script_ of the mock request, then postman will jump to your specified request and execute it.
	* add `postman.setNextRequest(null);` in the _Test Script_ of each request, then postman will stop this test iteration after executing request
	* You can send a GET mock request and return 200 OK simply, then ignore the test result.

## This project use the following APIs and tools...
* Springfox: Automated JSON API documentation for API's built with Spring
	* http://springfox.github.io/springfox/
* Gradle Plugin
	* com.benjaminsproule.swagger: Plugin to create Swagger documentation using Gradle
		* https://plugins.gradle.org/plugin/com.benjaminsproule.swagger
	* com.moowork.node: Gradle plugin for executing node scripts
		* https://plugins.gradle.org/plugin/com.moowork.node
* Mermade Swagger 2.0 to OpenAPI 3.0.0 converter
	* https://mermade.org.uk/openapi-converter
* Postman
	* Node.js package
		* OpenAPI 3.0 to Postman Collection v2.1.0 Converter
			* https://github.com/postmanlabs/openapi-to-postman
		* newman: Using Newman as a Library
			* https://www.npmjs.com/package/newman
		* newman-reporter-html
			* https://www.npmjs.com/package/newman-reporter-html
	* Postman API: The Postman API allows you to programmatically access data stored in Postman account with ease
		* https://docs.api.getpostman.com/
