package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.NotificationDomainType
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDomainTypeRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) : TypeDomainRepository<NotificationDomainType> {
   private val logger: Logger = LoggerFactory.getLogger(NotificationDomainTypeRepository::class.java)
   private val simpleNotificationDomainTypeRowMapper = NotificationDomainTypeRowMapper()

   override fun findOne(id: Long): NotificationDomainType? {
      val found = jdbc.findFirstOrNull("SELECT * FROM notification_domain_type WHERE id = :id", mapOf("id" to id), simpleNotificationDomainTypeRowMapper)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }
}

private class NotificationDomainTypeRowMapper : RowMapper<NotificationDomainType> {
   override fun mapRow(rs: ResultSet, rowNum: Int): NotificationDomainType =
      NotificationDomainType(
         id = rs.getLong("id"),
         uuRowId = rs.getObject("uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("time_updated", OffsetDateTime::class.java),
         value = rs.getString("value"),
         description = rs.getString("description")
      )
}
