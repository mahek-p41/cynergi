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
         description: 'Name of the table that the Controller will will be managing via an Entity'
      });
   }

   async prompting() {
      this.log(
         yosay(
            `${chalk.green('cynergi-middleware')} Controller boilerplate generator!`
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
         'Controller.kt.template': `src/main/kotlin/com/hightouchinc/cynergi/middleware/controller/${templateValues.entity}.kt`,
         'Validator.kt.template': `src/main/kotlin/com/hightouchinc/cynergi/middleware/validator/${templateValues.entity}Repository.kt`,
         'ControllerSpecification.groovy.template': `src/test/groovy/com/hightouchinc/cynergi/middleware/controller/${templateValues.entity}ControllerSpecification.groovy`
      };

      this.log(`Generating Controller ${chalk.green(this.options.entity)}`);

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
