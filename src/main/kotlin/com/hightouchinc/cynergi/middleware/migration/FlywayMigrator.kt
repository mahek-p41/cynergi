package com.hightouchinc.cynergi.middleware.migration

import io.micronaut.context.annotation.Property
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.discovery.event.ServiceStartedEvent
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlywayMigrator @Inject constructor (
   @Property(name = "flyway.url") private val dbUrl: String,
   @Property(name = "flyway.user") private val dbUser: String,
   @Property(name = "flyway.password") private val dbPassword: String
): ApplicationEventListener<ServiceStartedEvent> {
   private companion object {
       val logger: Logger = LoggerFactory.getLogger(FlywayMigrator::class.java)
   }

   override fun onApplicationEvent(event: ServiceStartedEvent?) {
      logger.info("Migrating database")

      val successfulMigrations = Flyway.configure()
         .locations("classpath:db/migration/postgres")
         .dataSource(dbUrl, dbUser, dbPassword)
         .load()
         .migrate()

      logger.info("Successfully migrated {}", successfulMigrations)
   }
}
