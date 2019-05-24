const newman = require('newman');
const util = require('./util.js').init();

newman.run({
    globals: 'postman/globals.json',
    environment: `postman/env/${util.envName}.json`,
    collection: util.collectionFile,
    folder: process.argv[4] ? process.argv[4].split(",") : undefined,
    iterationData: `postman/collection/${util.collectionName}-data.json`,
    reporters: 'html',
    reporter: {html: {export: 'postman/report.html'}}
}, function (err) {
    if (err) { throw err; }
    console.log('collection run complete!');
});
