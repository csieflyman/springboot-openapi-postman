const fs = require('fs')
const newman = require('newman');

const envName = process.argv[2];
if(!envName) {
    throw '[ERROR] env name is required';
}
const envFilePath = `postman/env/${envName}-env.json`;
if(!fs.existsSync(envFilePath)) {
    throw `[ERROR] ${envFilePath} is not exist.`;
}

const collectionFilePath = 'postman/collection.json';
if(!fs.existsSync(collectionFilePath)) {
    throw `[ERROR] ${collectionFilePath} is not exist.`;
}
collection = JSON.parse(fs.readFileSync(collectionFilePath, {encoding: 'utf8'}));
const folderNameArray = collection.item.filter(item => item.item).map(folder => folder.name);
if(folderNameArray.length == 0) {
    throw `[ERROR] ${collectionFilePath} does not contains any folder`;
}

const folderNameArg = process.argv[3];
if(folderNameArg && !folderNameArray.includes(folderNameArg)) {
    throw `[ERROR] folder ${folderNameArg} is not exist`;
}

console.log(`env file: ${envFilePath}`);
console.log(`collection file: ${collectionFilePath}`);
console.log(`folder: ${folderNameArg ? folderNameArg : ''}`);

if(folderNameArg) {
    console.log(`start to run folder ${folderNameArg}...`);
    newman.run({
        globals: 'postman/globals.json',
        environment: envFilePath,
        collection: collectionFilePath,
        folder: folderNameArg,
        iterationData: `postman/data/${folderNameArg}-data.json`,
        reporters: ['json', 'html'],
        reporter: {json: {export: `postman/report/${folderNameArg}-report.json`}, html: {export: `postman/report/${folderNameArg}-report.html`}}
    }, function (err) {
        if(err) {
            console.error(`[ERROR] run folder ${folderNameArg} failure`);
            throw err;
        }
    });
    console.log(`finish running folder ${folderNameArg}`);
}
else {
    const runnerArray = folderNameArray.map(folderName => {
        return function(callback) {
            console.log(`start to run folder ${folderName}...`);
            newman.run({
                globals: 'postman/globals.json',
                environment: envFilePath,
                collection: collectionFilePath,
                folder: folderName,
                iterationData: `postman/data/${folderName}-data.json`,
                reporters: ['json', 'html'],
                reporter: {json: {export: `postman/report/${folderName}-report.json`}, html: {export: `postman/report/${folderName}-report.html`}}
            }, function (err) {
                if(err) {
                    console.error(`[ERROR] run folder ${folderName} failure`);
                }
                callback(err, folderName);
            });
            console.log(`finish running folder ${folderName}`);
        }
    });

    var parallel = require('run-parallel');
    console.log('start to run collection in parallel');
    parallel(runnerArray, function(err, results) {
        if (err) {
            console.error('run collection failure!');
            throw err;
        }
        console.log('finish running collection in parallel');
    });
}