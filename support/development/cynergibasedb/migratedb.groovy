#!/usr/bin/env /opt/cyn/v01/cynmid/groovy/bin/groovy
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.postgresql', module = 'postgresql', version = '42.2.15')
@Grab(group='org.flywaydb', module='flyway-core', version='5.2.4')
@Grab(group='info.picocli', module='picocli', version='4.6.3')
@Grab(group='info.picocli', module='picocli-groovy', version='4.6.3')
@Grab(group='org.slf4j', module='slf4j-simple', version='1.7.32')
@Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.7.32')
@Grab(group='org.slf4j', module='jul-to-slf4j', version='1.7.32')
@Grab(group='commons-io', module='commons-io', version='2.11.0')
@picocli.CommandLine.Command(
   description = "Simple script to handle db migrations"
)
@picocli.groovy.PicocliScript
import org.flywaydb.core.Flyway
import groovy.transform.Field
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.FileSystems
import java.util.zip.ZipFile
import org.apache.commons.io.IOUtils
import org.apache.commons.io.FilenameUtils
import picocli.CommandLine.Option

@Option(names = ["-u", "--user"], defaultValue = "cynergiuser", description = "db username")
@Field String user

@Option(names = ["-p", "--password"], defaultValue = "cynergiuser2019", description = "db password")
@Field String password

@Option(names = ["-P", "--port"], defaultValue = "5432", description = "db port")
@Field String port

@Option(names = ["-d", "--database"], defaultValue = "cynergidb", description = "database to migrate")
@Field String database

@Option(names = ["-H", "--host"], defaultValue = "localhost", description = "Host database is running on")
@Field String host

@Option(names = ["-m", "--migrations"], defaultValue = "/opt/cyn/v01/cynmid/cynergi-middleware.jar", description = "'location of migration files")
@Field String migrations

@Option(names = ["-c", "--clean"], defaultValue = "false", description = "'Reset the database back to zero")
@Field boolean force_clean

@Option(names = ["-h", "--help"], defaultValue = "false", description = "'this help message")
@Field boolean helpRequested

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

def exitCode = 0

if (!helpRequested) {
   if (user != null && password != null && port != null && database != null && host != null && migrations != null) {
      final jarPathMatch = FileSystems.getDefault().getPathMatcher("glob:**/*.jar")

      try {
         final migrationLocation = Path.of(migrations)

         if (Files.exists(migrationLocation)) {
            if (Files.isDirectory(migrationLocation)) {
               migrate(migrationLocation, "jdbc:postgresql://${host}:${port}/${database}", user, password, force_clean)
            } else if (jarPathMatch.matches(migrationLocation)) {
               final migrationJar = new ZipFile(migrationLocation.toFile())
               final flywayTemp = Files.createTempDirectory("flywaytemp")

               migrationJar.entries().findAll { !it.directory && it.name.startsWith("db/migration/postgres") && it.name.endsWith(".sql") }.each { zipEntry ->
                  migrationJar.getInputStream(zipEntry).with { inStream ->
                     final tempFile = flywayTemp.resolve(FilenameUtils.getName(zipEntry.name))

                     tempFile.toFile().withOutputStream { outStream -> IOUtils.copy(inStream, outStream) }
                  }
               }

               migrate(migrationLocation, "jdbc:postgresql://${host}:${port}/${database}", user, password, force_clean)
               flywayTemp.toFile().deleteDir()
            } else {
               exitCode = 2
            }
         } else {
            println "Migration SQL location ${migrations} does not exist!"

            exitCode = 1
         }
      } catch (Throwable e) {
         println e.getMessage()
         exitCode = 3
      }
   } else {
      //TODO What is this below, and what is the equivalent?
      println cli.usage()
      exitCode = 4
   }
}

System.exit(exitCode)
