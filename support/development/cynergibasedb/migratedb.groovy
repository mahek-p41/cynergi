#!/usr/bin/env /opt/cyn/v01/cynmid/groovy/bin/groovy
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')
@Grab(group='org.flywaydb', module='flyway-core', version='5.2.4')
@Grab(group='info.picocli', module='picocli', version='4.6.3')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.32')
@Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.7.32')
@Grab(group='org.slf4j', module='jul-to-slf4j', version='1.7.32')
@Grab(group='commons-io', module='commons-io', version='2.11.0')

import org.flywaydb.core.Flyway
import groovy.cli.picocli.CliBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileSystems
import java.util.zip.ZipFile
import org.apache.commons.io.IOUtils
import org.apache.commons.io.FilenameUtils

static boolean isCst143() {
   try {
      return (InetAddress.getLocalHost().getHostName() == "cst143")
   } catch(Throwable e) {
      return false
   }
}

def migrate(Path dir, String dbUrl, String username, String password, boolean forceClean = false) {
   final isCst143 = isCst143()

   final flyway = Flyway
      .configure()
      .locations("filesystem:$dir")
      .cleanDisabled(!isCst143)
      .cleanOnValidationError(isCst143)
      .table("flyway_schema_history")
      .initSql("SELECT 1")
      .dataSource(dbUrl, username, password)
      .load()

   if (forceClean && isCst143) {
      println "Cleaning db"
      flyway.clean()
   }

   flyway.migrate()
}

// https://relentlesscoding.com/posts/how-to-use-groovys-clibuilder/
final cli = new CliBuilder(name: 'migratedb')

cli.width = 80
cli.with {
   u(longOpt: 'user', args: 1, defaultValue: 'cynergiuser', 'db username')
   p(longOpt: 'password', args: 1, defaultValue: 'cynergiuser2019', 'db password')
   P(longOpt: 'port', args: 1, defaultValue: '5432', 'db port')
   d(longOpt: 'database', defaultValue: 'cynergidb', args: 1, 'database to migrate')
   H(longOpt: 'host', args: 1, defaultValue: 'localhost', 'Host database is running on')
   m(longOpt: 'migrations', args: 1, defaultValue: '/opt/cyn/v01/cynmid/cynergi-middleware.jar', 'location of migration files')
   c(longOpt: 'force-clean', 'Reset the database back to zero')
   h(longOpt: 'help', 'this help message')
}

final options = cli.parse(args)
def exitCode = 0

if (options != null && !options.h) {
   final jarPathMatch = FileSystems.getDefault().getPathMatcher("glob:**/*.jar")

   try {
      final migrationLocation = Path.of(options.m)

      if (Files.exists(migrationLocation)) {
         if (Files.isDirectory(migrationLocation)) {
            migrate(migrationLocation, "jdbc:postgresql://${options.H}:${options.P}/${options.d}", options.u, options.p, options.c)
         } else if (jarPathMatch.matches(migrationLocation)) {
            final migrationJar = new ZipFile(migrationLocation.toFile())
            final flywayTemp = Files.createTempDirectory("flywaytemp")

            migrationJar.entries().findAll { !it.directory && it.name.startsWith("db/migration/postgres") && it.name.endsWith(".sql") }.each { zipEntry ->
               migrationJar.getInputStream(zipEntry).with { inStream ->
                  final tempFile = flywayTemp.resolve(FilenameUtils.getName(zipEntry.name))

                  tempFile.toFile().withOutputStream { outStream -> IOUtils.copy(inStream, outStream) }
               }
            }

            migrate(flywayTemp, "jdbc:postgresql://${options.H}:${options.P}/${options.d}", options.u, options.p, options.c)
            flywayTemp.toFile().deleteDir()
         } else {
            exitCode = 2
         }
      } else {
         println "Migration SQL location ${options.m} does not exist!"

         exitCode = 1
      }
   } catch (Throwable e) {
      println e.getMessage()
      exitCode = 3
   }
} else {
   println cli.usage()
   exitCode = 4
}

System.exit(exitCode)
