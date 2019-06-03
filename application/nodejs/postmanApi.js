// Upload Collection To Postman Server
module.exports = {
    uploadCollection: uploadCollection,
    uploadEnvironment: uploadEnvironment
//    findCollectionByName: findCollectionByName,
//    getCollection: getCollection,
//    createCollection: createCollection,
//    updateCollection: updateCollection,
//    findEnvironmentByName: findEnvironmentByName,
//    getEnvironment: getEnvironment,
//    createEnvironment: createEnvironment,
//    updateEnvironment: updateEnvironment
};

const request = require('request');
const postmanApiUrl = 'https://api.getpostman.com'
const baseRequest = request.defaults({
  headers: {'X-Api-Key': process.env["X-Api-Key"]}
})
const fs = require('fs');

if(process.env["uploadCollection"] === 'true')
    uploadCollection(JSON.parse(fs.readFileSync('postman/collection.json', {encoding: 'utf8'})));
if(process.env["uploadEnv"] === 'true') {
    fs.readdirSync('postman/env').forEach(fileName => uploadEnvironment(JSON.parse(fs.readFileSync(`postman/env/${fileName}`, {encoding: 'utf8'}))));
}

function getCollection(collectionUid) {
    return sendGetRequest('collections', collectionUid);
}

function createCollection(collection) {
    delete collection.collection.info.version; // no versioning
    return sendPostRequest('collections', collection);
}

function updateCollection(collectionUid, collection) {
    delete collection.collection.info.version; // no versioning
    return sendPutRequest('collections', collectionUid, collection);
}

function findCollectionByName(collectionName) {
    return findObjByName('collections', collectionName);
}

function getEnvironment(environmentUid) {
    return sendGetRequest('environments', environmentUid);
}

function createEnvironment(environment) {
    return sendPostRequest('environments', environment);
}

function updateEnvironment(environmentUid, environment) {
    return sendPutRequest('environments', environmentUid, environment);
}

function findEnvironmentByName(environmentName) {
    return findObjByName('environments', environmentName);
}

function sendGetRequest(endpoint, uid) {
    return new Promise((resolve, reject) => {
        const url = `${postmanApiUrl}/${endpoint}/${uid}`;
        console.log("GET " + url);
        baseRequest.get({url: url, json: true}, function(err, res, body) {
            if(err) {
                console.error(`GET ${url} failure`)
                reject();
            }
            console.log(body);
            resolve(body);
        });
    });
}

function sendPostRequest(endpoint, obj) {
    return new Promise((resolve, reject) => {
        const url = `${postmanApiUrl}/${endpoint}`
        console.log("POST " + url);
        baseRequest.post({url: url, body: obj, json: true}, function(err, res, body) {
            if(err) {
                console.error(`POST ${url} failure`)
                reject();
            }
            console.log(body);
            resolve(body);
        });
    });
}

function sendPutRequest(endpoint, uid, obj) {
    return new Promise((resolve, reject) => {
        const url = `${postmanApiUrl}/${endpoint}/${uid}`;
        console.log("PUT " + url);
        baseRequest.put({url: url, body: obj, json: true}, function(err, res, body) {
            if(err) {
                console.error(`PUT ${url} failure`)
                reject();
            }
            console.log(body);
            resolve(body);
        });
    });
}

function findObjByName(endpoint, objName) {
    return new Promise((resolve, reject) => {
        const url = `${postmanApiUrl}/${endpoint}`;
        console.log("GET " + url);
        baseRequest.get({url: url, json: true}, function(err, res, body) {
            if(err) {
                console.error(`GET ${url} failure`)
                reject();
            }
            //console.log(body);
            let objRes = body[endpoint].find(obj => obj.name === objName);
            //console.log(objRes);
            resolve(objRes);
        });
    });
}

function uploadCollection(collection) {
    uploadObj(collection.info.name, {collection: collection}, findCollectionByName, createCollection, updateCollection);
}

function uploadEnvironment(env) {
    uploadObj(env.environment.name, env, findEnvironmentByName, createEnvironment, updateEnvironment);
}

function uploadObj(objName, obj, findObjByNameFunction, createObjFunction, updateObjFunction) {
    findObjByNameFunction(objName)
    .then((objRes) => {
        if(objRes) {
            updateObjFunction(objRes.uid, obj);
        }
        else {
            createObjFunction(obj);
        }
    })
    .catch(() => console.error(`upload ${objName} To Postman Server failure`));
}