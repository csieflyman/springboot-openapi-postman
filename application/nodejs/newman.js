const fs = require('fs')
const newman = require('newman');
const util = require('./util.js').init();

const collectionFilePath = 'postman/collection/collection.json';
if(!fs.existsSync(collectionFilePath)) {
    console.log('not exists collection.json file. exit...')
    return;
}

collection = JSON.parse(fs.readFileSync(collectionFilePath, {encoding: 'utf8'}));
const folderNameArray = collection.item.map(folder => folder.name);
if(folderNameArray.length == 0) {
    console.log('not exists folders. exit...')
    return;
}

const runnerArray = folderNameArray.map(folderName => {
    return function(callback) {
        newman.run({
            globals: 'postman/globals.json',
            environment: `postman/env/${util.envName}.json`,
            collection: collectionFilePath,
            folder: folderName,
            iterationData: `postman/collection/${folderName}-data.json`,
            reporters: 'json',
            reporter: {json: {export: `postman/report/${folderName}-report.json`}}
        }, function (err) {
            console.log(`collection's folder ${folderName} run ${err ? 'failure' : 'complete!'}`);
            callback(err, folderName);
        });
    }
});

var parallel = require('run-parallel');
parallel(runnerArray, function(err, results) {
    if (err) {
        console.error('collection run failure!');
        throw err;
    }
});
