package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Gary
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import org.eclipse.collections.impl.factory.Maps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Singleton

@Singleton
class <%= entity %>Repository(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<<%= entity %>> {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(<%= entity %>::class.java)
      val SIMPLE_ROW_MAPPER = <%= entity %>RowMapper()
   }

   override fun findOne(id: Long): <%= entity %>? {
      val found = jdbc.findFirstOrNull("SELECT * FROM <%= table %> ca WHERE ca.id = :id", Maps.mutable.ofPairs("id" to id), SIMPLE_ROW_MAPPER)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM <%= table %> WHERE id = :id)", Maps.mutable.ofPairs("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: <%= entity %>): <%= entity %> {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   override fun update(entity: <%= entity %>): <%= entity %> {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }
}

private class <%= entity %>RowMapper : RowMapper<<%= entity %>> {
   override fun mapRow(rs: ResultSet, rowNum: Int): <%= entity %> =
      <%= entity %>(
         id = rs.getLong("id"),
         uuRowId = rs.getObject("uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("time_updated", OffsetDateTime::class.java)
      )
}
