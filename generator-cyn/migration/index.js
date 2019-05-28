'use strict';
const Generator = require('yeoman-generator');
const chalk = require('chalk');
const yosay = require('yosay');
const camelCase = require('camel-case');
const pascalCase = require('pascal-case');
const snakeCase = require('snake-case');
const fs = require('fs');
const process = require('process');
const ejs = require('ejs');

module.exports = class extends Generator {
   constructor(args, opts) {
      super(args, opts);

      this.argument('table', {
         type: String,
         required: false,
         description: 'Name of the table for the to be created'
      });

      this.argument('description', {
         type: String,
         required: true,
         description: 'simple description with the jira ticket number'
      });

      this.option('append', {
         type: Boolean,
         alias: 'a',
         default: false,
         description: 'whether to append a table to existing file or create a new one'
      });
   }

   async prompting() {
      this.log(
         yosay(
         `${chalk.green('cynergi-middleware')} Flyway Migration boilerplate generator!`
         )
      );
   }

   writing() {
      const append = this.options.append;
      const migrationScriptPattern = /^V(\d*)__([\W\w]*)\.sql$/;
      const versionNumber = fs.readdirSync(`${process.cwd()}/src/main/resources/db/migration/postgres/`)
         .map(fileName => fileName.match(migrationScriptPattern)[1])
         .map(version => {
            if (append) {
               return parseInt(version);
            } else {
               return parseInt(version) + 1;
            }
         })
         .sort((o1, o2) =>  o2 - o1)[0]
      ;
      const templateValues = {
         repository: camelCase(this.options.table),
         entity: pascalCase(this.options.table),
         table: snakeCase(this.options.table)
      };
      const templates = {
         'Entity.sql.template': `src/main/resources/db/migration/postgres/V${versionNumber}__${this.options.description}.sql`,
      };

      this.log(`Generating Migration ${chalk.green(this.options.table)}`);

      Object.keys(templates).forEach((key) => {
         const templateFile = key;
         const destDir = templates[key];

         if (!append) {
            this.fs.copyTpl(
               this.templatePath(templateFile),
               this.destinationPath(destDir),
               templateValues
            );
         } else {
            const template = this.fs.read(this.templatePath(templateFile));
            const rendered = ejs.render(template, templateValues, {});

            this.fs.append(
               this.destinationPath(destDir),
               `\n${rendered}`
            );
         }
      });
   }
};
