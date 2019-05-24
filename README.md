# spring-postman
Convert Spring Boot Controller to Postman Collection for End-To-End Automated Testing
## Goal:
* Generate postman collection automatically to reduce effort that write end-to-end test.
* Integrate gradle with postman newman to run test locally before dev commit code
* Upload postman collection and env to postman server by running gradle task. Then other dev's postman app would sync to download it.
* QA can write and run test without postman app. even write scipt to customize testing workflow become more flexible

## Workflow
1. Install Node.js packages by executing gradle task "myNpmInstall"
2. Convert spring boot controller to postman collection by executing gradle task "generatePostmanCollection"
3. Prepare postman test data json files (request body, environment variables...etc)
4. Runing test by executing gradle task "newman ${envName} ${collectionName} ${folderName}"
5. Check the test report (html, json format)
6. Upload postman collection and env to postman server by executing gradle task "uploadToPostmanServer"

## Tools
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
		* Postman Collection SDK: allows a developer to work with Postman Collections.
			* https://www.npmjs.com/package/postman-collection
	* Postman API: The Postman API allows you to programmatically access data stored in Postman account with ease
		* https://docs.api.getpostman.com/

## Test data files
* /postman/env/${envName}.json => environment variables config file
* /postman/collection.json => auto-generated postman collection json file
* /postman/collection-order.json => specify request's executing order in collection
* /postman/collection-requestBody.json => request body template file (replace value by collection-data.json)
* /postman/collection-data.json => as parameters for each iteration of a collection run
* /postman/collection-event.json => script assertion


