const fs = require('fs')
const newman = require('newman');
const async = require('async');

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
    runSpecifiedFolder();
}
else {
    runAllFoldersAndSuitesInSequence();
}

function runSpecifiedFolder() {
    console.log(`start to run folder ${folderNameArg}...`);
    newman.run({
        globals: 'postman/globals.json',
        environment: envFilePath,
        collection: collectionFilePath,
        folder: folderNameArg,
        iterationData: `postman/folder/${folderNameArg}-data.json`,
        reporters: ['json', 'html'],
        reporter: {json: {export: `postman/report/folder/${folderNameArg}-report.json`}, html: {export: `postman/report/folder/${folderNameArg}-report.html`}}
    }, function (err) {
        if(err) {
            console.error(`[ERROR] run folder ${folderNameArg} failure`);
            throw err;
        }
    });
    console.log(`finish running folder ${folderNameArg}`);
}

function runAllFoldersAndSuitesInSequence() {
    async.series([runAllFoldersInParallel, runAllSuitesInParallel], function (err, results) {
        if(err) {
            throw err;
        }
    });
}

function runAllFoldersInParallel(next) {
    console.log('==================== Run Folders Begin ====================');
    const runners = getAllFoldersRunners();
    async.parallel(runners, function(err, results) {
        if (err) {
            console.error('run folders failure!');
            throw err;
        }
        console.log('==================== Run Folders End ====================');
        next(err);
    });
}

function runAllSuitesInParallel(next) {
    console.log('==================== Run Suites Begin ====================');
    const runners = getAllSuitesRunners();
    async.parallel(runners, function(err, results) {
        if (err) {
            console.error('run suites failure!');
            throw err;
        }
        console.log('==================== Run Suites End ====================');
        next(err);
    });
}

function getAllFoldersRunners() {
    return folderNameArray.map(folderName => {
        return function(finish) {
            console.log(`========== Folder ${folderName} Begin ==========`);
            newman.run({
                globals: 'postman/globals.json',
                environment: envFilePath,
                collection: collectionFilePath,
                folder: folderName,
                iterationData: `postman/folder/${folderName}-data.json`,
                reporters: ['json', 'html'],
                reporter: {json: {export: `postman/report/folder/${folderName}-report.json`}, html: {export: `postman/report/folder/${folderName}-report.html`}}
            }, function (err) {
                if(err) {
                    console.error(`[ERROR] run folder ${folderName} failure`);
                }
                finish(err, folderName);
            });
            console.log(`========== Folder ${folderName} End ==========`);
        }
    });
}

function getAllSuitesRunners() {
    return fs.readdirSync('postman/suite').map(fileName => {
        let suiteName = fileName.replace('-data.json', '');
        return function(finish) {
            console.log(`========== Suite ${suiteName} Begin ==========`);
            newman.run({
                globals: 'postman/globals.json',
                environment: envFilePath,
                collection: collectionFilePath,
                iterationData: `postman/suite/${fileName}`,
                reporters: ['json', 'html'],
                reporter: {json: {export: `postman/report/suite/${suiteName}-report.json`}, html: {export: `postman/report/suite/${suiteName}-report.html`}}
            }, function (err) {
                if(err) {
                    console.error(`[ERROR] run suite ${suiteName} failure`);
                }
                finish(err, suiteName);
            });
            console.log(`========== Suite ${suiteName} End ==========`);
        }
    });
}