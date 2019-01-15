'use strict';
const Generator = require('yeoman-generator');
const chalk = require('chalk');
const yosay = require('yosay');

module.exports = class extends Generator {
   prompt() {
      this.log(yosay(
         `Welcome to the ${chalk.red('cynergi-middleware')} boilerplate generator!`
      ));
   }
};
