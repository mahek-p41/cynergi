'use strict';
const Generator = require('yeoman-generator');
const chalk = require('chalk');
const yosay = require('yosay');
const mkdirp = require('mkdirp');
const os = require('os');

module.exports = class extends Generator {
   constructor(args, opts) {
      super(args, opts);

      this.argument('entity', {
         type: String,
         required: true,
         description: 'Generate a cynergi-middleware Entity, it\'s repository and boilerplate for a test data loader'
      });
   }

   async prompting() {
      this.log(
         yosay(
         `${chalk.red('cynergi-middleware')} Entity boilerplate generator!`
         )
      );
   }

   writing() {
      this.log(`Generating Entity ${chalk.green(this.options.entity)}`);
      [
         {'Entity.kt': 'src/main/kotlin/com/hightouchinc/cynergi/middleware/entity'},
         {'Repository.kt': 'src/main/kotlin/com/hightouchinc/cynergi/middleware/repository'},
         {'TestDataLoader.groovy': 'src/main/test/com/hightouchinc/cynergi/test/data/loader'}
      ].forEach((templateFile, destDir) => {
         this.fs.copyTpl(
            this.templatePath(templateFile),
            this.destinationPath(destDir),
            { entityname: this.options.entity }
         )
      });
   }
};
