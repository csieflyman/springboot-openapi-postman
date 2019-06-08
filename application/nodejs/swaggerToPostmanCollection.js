const fs = require('fs')
const request = require('request');
const postmanConverter = require('openapi-to-postmanv2')
const Event = require('postman-collection').Event

swagger2ToOpenapi3()
    .then(openapi3ToPostmanCollection).then(collection => manipulateCollection(collection))
    .catch(() => console.error("convert failure"));

// Swagger 2.0 to OpenAPI 3.0
function swagger2ToOpenapi3() {
    return new Promise((resolve, reject) => {
        const formData = {
            source: fs.readFileSync('apidoc/swagger.yaml', {encoding: 'utf8'}),
            filename: 'swagger.yaml'
        }
        request.post({url: 'https://mermade.org.uk/api/v1/convert', formData: formData}, function(err, res, body) {
            if(err) {
                console.error("swagger2ToOpenapi3 failure: " + err.message);
                reject();
            }
            else {
                body = body.replace('<html><body><pre>', '');
                fs.writeFileSync('apidoc/openapi3.yaml', body);
                console.log("swagger2ToOpenapi3 success");
                resolve();
            }
        });
    });
};

// OpenAPI 3.0 to Postman Collection
function openapi3ToPostmanCollection(){
    return new Promise((resolve, reject) => {
        const openapiData = fs.readFileSync('apidoc/openapi3.yaml');

        postmanConverter.convert({type: 'file', data: 'apidoc/openapi3.yaml'},
          {}, (err, conversionResult) => {
            if (!conversionResult.result) {
              console.error('openapi3ToPostmanCollection failure', conversionResult.reason);
              reject();
            }
            else {
              console.log("openapi3ToPostmanCollection success");
              resolve(conversionResult.output[0].data);
            }
          }
        );
    });
};

function manipulateCollection(collection) {
    moveAllRequestsToRootFolder(collection);
    addRequestBody(collection);
    parameterizeVariables(collection);
    addEvent(collection);
    addMockRequestAtFirstPosition(collection);
    console.log("manipulateCollection success");
    fs.writeFileSync('postman/collection.json', JSON.stringify(collection));
}

function moveAllRequestsToRootFolder(collection) {
    collection.item.forEach(rootFolder => {
        if(rootFolder.item) {
            rootFolder.item.forEach(function(subFolder) {
                if(subFolder.item) {
                    moveRequestToFolder(rootFolder, subFolder.item);
                }
            });
            rootFolder.item = rootFolder.item.filter(item => item.request);
        }
    });
}

function moveRequestToFolder(folder, item) {
    item.forEach(function(obj) {
        if(obj.item) {
            moveRequestToFolder(folder, obj.item); //obj is folder
        }
        else {
            folder.item.push(obj); // obj is request
        }
    });
}

function addRequestBody(collection) {
    collection.item.forEach(folder => {
        if(folder.item) {
            folder.item.filter(folderItem =>
                ['POST', 'PUT'].includes(folderItem.request.method) &&
                folderItem.request.header.find(header => header.key === 'Content-Type' && header.value.includes('application/json')))
                .forEach(folderItem => folderItem.request.body = {mode: "raw", raw: "{{_requestBody}}"});
        }
    });
}

function parameterizeVariables(collection) {
    collection.item.forEach(folder => {
        if(folder.item) {
            folder.item.forEach(function(folderItem) {
                folderItem.request.url.variable.forEach(variable => {
                    variable.value = "{{" + variable.key + "}}";
                });
            });
        }
    });
}

function addEvent(collection) {
    collection.item.forEach(folder => {
        if(folder.item) {
            folder.item.forEach(function(folderItem) {
                folderItem.event.push(
                        {
                            listen: "test",
                            script: {
                                type: "text/javascript",
                                exec: [
                                    "let script = pm.iterationData.get(\"_test\");eval(script);postman.setNextRequest(null);"
                                ]
                            }
                        }
                    );
            });
        }
    });
}

function addMockRequestAtFirstPosition(collection) {
    collection.item.forEach(folder => {
        if(folder.item) {
            folder.item.splice(0, 0, {
                name: 'Mock Request',
                request: {
                    url: {
                        host: ['{{baseUrl}}/postman-blank-page.html']
                    },
                    method: 'GET'
                },
                event: [
                    {
                        listen: "prerequest",
                        script: {
                            type: "text/javascript",
                            exec: [
                                "postman.setNextRequest(pm.iterationData.get(\"_requestName\"));"
                            ]
                        }
                    }
                ]
            });
        }
    });
}



