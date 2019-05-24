const fs = require('fs')
module.exports = {
    init: init
}

function init() {
    this.envName = process.argv[2] ? process.argv[2] : 'localhost';
    this.envFile = `postman/env/${this.envName}.json`;
    this.env = JSON.parse(fs.readFileSync(this.envFile, {encoding: 'utf8'}));
    this.collectionName = process.argv[3] ? process.argv[3] : 'collection';
    this.collectionFile = `postman/collection/${this.collectionName}.json`;
    this.collection = JSON.parse(fs.readFileSync(this.collectionFile, {encoding: 'utf8'}));
    return this;
}
