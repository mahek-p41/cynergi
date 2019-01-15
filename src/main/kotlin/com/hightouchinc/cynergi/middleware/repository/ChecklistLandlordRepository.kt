package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.ChecklistLandlord
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import org.apache.commons.lang3.StringUtils.EMPTY
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
class ChecklistLandlordRepository(
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<ChecklistLandlord> {
   private companion object {
      val logger: Logger = LoggerFactory.getLogger(ChecklistLandlordRepository::class.java)
      val SIMPLE_CHECKLIST_LANDLORD_ROW_MAPPER = ChecklistLandlordRowMapper()
      val PREFIXED_CHECKLIST_LANDLORD_ROW_MAPPER = ChecklistLandlordRowMapper(rowPrefix = "cl_")
   }

   override fun findOne(id: Long): ChecklistLandlord? {
      val found = jdbc.findFirstOrNull("SELECT * FROM checklist_landlord ca WHERE ca.id = :id", Maps.mutable.ofPairs("id" to id), SIMPLE_CHECKLIST_LANDLORD_ROW_MAPPER)

      logger.trace("searching for {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM checklist_landlord WHERE id = :id)", Maps.mutable.ofPairs("id" to id), Boolean::class.java)!!

      logger.trace("Checking if ID: {} exists resulted in {}", id, exists)

      return exists
   }

   override fun insert(entity: ChecklistLandlord): ChecklistLandlord {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   override fun update(entity: ChecklistLandlord): ChecklistLandlord {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   fun mapRowPrefixedRow(rs: ResultSet, row: Int): ChecklistLandlord? =
      rs.getString("cl_id")?.let { PREFIXED_CHECKLIST_LANDLORD_ROW_MAPPER.mapRow(rs, row) }
}

private class ChecklistLandlordRowMapper(
   private val rowPrefix: String = EMPTY
) : RowMapper<ChecklistLandlord> {
   override fun mapRow(rs: ResultSet, rowNum: Int): ChecklistLandlord =
      ChecklistLandlord(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getObject("${rowPrefix}uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("${rowPrefix}time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("${rowPrefix}time_updated", OffsetDateTime::class.java),
         address = rs.getBoolean("${rowPrefix}address"),
         altPhone = rs.getString("${rowPrefix}alt_phone"),
         leaseType = rs.getString("${rowPrefix}lease_type"),
         leaveMessage = rs.getBoolean("${rowPrefix}leave_message"),
         length = rs.getInt("${rowPrefix}length"),
         name = rs.getString("${rowPrefix}name"),
         paidRent = rs.getString("${rowPrefix}paid_rent"),
         phone = rs.getBoolean("${rowPrefix}phone"),
         reliable = rs.getBoolean("${rowPrefix}reliable"),
         rent = rs.getBigDecimal("${rowPrefix}rent")
      )
}
