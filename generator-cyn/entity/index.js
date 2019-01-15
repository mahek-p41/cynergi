'use strict';
const Generator = require('yeoman-generator');
const chalk = require('chalk');
const yosay = require('yosay');
const camelCase = require('camel-case');
const pascalCase = require('pascal-case');

module.exports = class extends Generator {
   constructor(args, opts) {
      super(args, opts);

      this.argument('table', {
         type: String,
         required: true,
         description: 'Name of the table that the Entity will be managing'
      });
   }

   async prompting() {
      this.log(
         yosay(
         `${chalk.green('cynergi-middleware')} Entity boilerplate generator!`
         )
      );
   }

   writing() {
      const templateValues = {
         repository: camelCase(this.options.table),
         entity: pascalCase(this.options.table),
         table: this.options.table
      };
      const templates = {
         'Entity.kt.template': `src/main/kotlin/com/hightouchinc/cynergi/middleware/entity/${templateValues.entity}.kt`,
         'Repository.kt.template': `src/main/kotlin/com/hightouchinc/cynergi/middleware/repository/${templateValues.entity}Repository.kt`,
         'TestDataLoader.groovy.template': `src/test/groovy/com/hightouchinc/cynergi/test/data/loader/${templateValues.entity}.groovy`
      };

      this.log(`Generating Entity ${chalk.green(this.options.entity)}`);

      Object.keys(templates).forEach((key) => {
         const templateFile = key;
         const destDir = templates[key];

         this.fs.copyTpl(
            this.templatePath(templateFile),
            this.destinationPath(destDir),
            templateValues
         );
      });
   }
};
