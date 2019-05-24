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
    flatRequestsToPath1Folder(collection);
    sortRequestOrder(collection);
    addEvent(collection);
    addRequestBody(collection);
    parameterizeVariables(collection);
    console.log("manipulateCollection success");
    fs.writeFileSync('postman/collection/collection.json', JSON.stringify(collection));
}

function flatRequestsToPath1Folder(collection) {
    collection.item.forEach(folder => {
        if(folder.item) {
            folder.item.forEach(function(folderItem) {
                if(folderItem.item) {
                    moveRequestToFolder(folder, folderItem.item);
                }
            });
            folder.item = folder.item.filter(item => item.request);
        }
        //console.log(JSON.stringify(collection));
    });
}

function moveRequestToFolder(folder, item) {
    item.forEach(function(obj) {
        if(obj.item)
            moveItems(folder, obj.item); //obj is folder
        else {
            folder.item.push(obj); // obj is request
        }
    });
}

function sortRequestOrder(collection) {
    const filePath = 'postman/collection/collection-order.json';
    if(fs.existsSync(filePath)) {
        console.log("load file: " + filePath);
        const requestOrderArray = JSON.parse(fs.readFileSync('postman/collection/collection-order.json', {encoding: 'utf8'}));
        collection.item.forEach(item => item.item.sort(function(a, b) {
            return requestOrderArray.indexOf(a.name) - requestOrderArray.indexOf(b.name);
        }));
    }
}


function addEvent(collection) {
    const filePath = 'postman/collection/collection-event.json';
    if(fs.existsSync(filePath)) {
        console.log("load file: " + filePath);
        let root = JSON.parse(fs.readFileSync(filePath, {encoding: 'utf8'}));
        collection.event = root.collection;
        collection.item.forEach(folder => {
            let folderName = folder.name;
            if(folder.item && root[folderName]) {
                folder.item.forEach(function(folderItem) {
                    let requestName = folderItem.name;
                    if(root[folderName][requestName]) {
                        folderItem.event = root[folderName][requestName];
                    }
                });
            }
        });
    }
}

function addRequestBody(collection) {
    const filePath = 'postman/collection/collection-requestBody.json';
    if(fs.existsSync(filePath)) {
        console.log("load file: " + filePath);
        let root = JSON.parse(fs.readFileSync(filePath, {encoding: 'utf8'}));
        collection.item.forEach(folder => {
            let folderName = folder.name;
            if(folder.item && root[folderName]) {
                folder.item.forEach(function(folderItem) {
                    let requestName = folderItem.name;
                        if(root[folderName][requestName]) {
                        folderItem.request.body = root[folderName][requestName];
                        folderItem.request.body.raw = JSON.stringify(folderItem.request.body.raw);
                    }
                });
            }
        });
    }
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



