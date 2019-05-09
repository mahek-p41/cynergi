'use script';
const Generator = require('yeoman-generator');
const chalk = require('chalk');
const yosay = require('yosay');

module.exports = class extends Generator {
   constructor(args, opts) {
      super(args, opts);
   }

   async prompting() {
      this.log(
         yosay(`Welcome to the ${chalk.red('cynergi-middleware')} boilerplate generator`)
      );
      const questions = [{
         type: 'input',
         name: 'name',
         message: 'What is the name of your project?',
         default: this.appname,
         store: true
      }, {
         type: 'input',
         name: 'basePkg',
         message: 'What is the base package?',
         default: 'com.cynergisuite',
         store: true
      }, {
         type: 'input',
         name: 'mainSrc',
         message: "What is the main source directory?",
         default: 'src/main/kotlin',
         store: true
      }, {
         type: 'input',
         name: 'testSrc',
         message: "What is the test source directory?",
         default: 'src/test/groovy',
         store: true
      }, {
         type: 'input',
         name: 'lob',
         message: "What is the name of this line of business?",
         default: 'Cynergi',
         store: true
      }];

      this.answers = await this.prompt(questions);

      for(let i = 0; i < questions.length; i++) {
         const question = questions[i];
         const key = question.name;
         const value = this.answers[key];

         this.config.set(key, value);
         this.config.save();
      }

      this.config.save();
   }
};
