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
      this.argument('table', {
         type: String,
         required: true,
         description: 'Name of the table that the Entity will be managing'
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
      const templates = {
         'Entity.kt': `src/main/kotlin/com/hightouchinc/cynergi/middleware/entity/${this.options.entity}.kt`,
         'Repository.kt': `src/main/kotlin/com/hightouchinc/cynergi/middleware/repository/${this.options.entity}Repository.kt`,
         'TestDataLoader.groovy': `src/test/groovy/com/hightouchinc/cynergi/test/data/loader/${this.options.entity}.groovy`
      };
      Object.keys(templates).forEach((key) => {
         const templateFile = key;
         const destDir = templates[key];

         this.fs.copyTpl(
            this.templatePath(templateFile),
            this.destinationPath(destDir),
            this.options
         );
      });
   }
};
