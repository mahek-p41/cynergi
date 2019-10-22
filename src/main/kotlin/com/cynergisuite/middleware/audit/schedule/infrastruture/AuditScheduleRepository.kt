package com.cynergisuite.middleware.audit.schedule.infrastruture

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.schedule.AuditScheduleEntity
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.store.StoreEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class AuditScheduleRepository(
   private val scheduleRepository: ScheduleRepository,
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<AuditScheduleEntity> {
   private val logger: Logger = LoggerFactory.getLogger(AuditScheduleRepository::class.java)

   override fun findOne(id: Long): AuditScheduleEntity? {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   override fun exists(id: Long): Boolean {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   fun findAll(pageRequest: PageRequest): RepositoryPage<AuditScheduleEntity> {
      TODO("not implemented")
   }

   override fun insert(entity: AuditScheduleEntity): AuditScheduleEntity {
      logger.debug("Inserting audit schedule {}", entity)

      val schedule = scheduleRepository.insert(entity.schedule)
      val inserted = jdbc.insertReturning("""
         INSERT INTO audit_schedule(store_number, department_access, schedule_id)
         VALUES(:store_number, :department_access, :schedule_id)
         RETURNING 
            *
         """.trimIndent(),
         mapOf(
            "store_number" to entity.store.number,
            "department_access" to entity.departmentAccess.code,
            "schedule_id" to schedule.id
         ),
         RowMapper { rs, _ -> mapRow(rs, entity.store, schedule, entity.departmentAccess) }
      )

      logger.debug("Inserted audit schedule {}", entity)

      return inserted
   }

   override fun update(entity: AuditScheduleEntity): AuditScheduleEntity {
      logger.debug("Updating audit schedule {}", entity)

      val schedule = scheduleRepository.update(entity.schedule)
      val updated = jdbc.updateReturning("""
         UPDATE audit_schedule(store_number, department_access)
         SET 
            store_number = :store_number,
            department_access = :department_access 
         WHERE id = :id
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "store_number" to entity.store.number,
            "department_access" to entity.departmentAccess
         ),
         RowMapper { rs, _ -> mapRow(rs, entity.store, schedule, entity.departmentAccess) }
      )

      logger.debug("Updated audit schedule {}", updated)

      return updated
   }

   private fun mapRow(rs: ResultSet, store: StoreEntity, schedule: ScheduleEntity, departmentAccess: DepartmentEntity): AuditScheduleEntity =
      AuditScheduleEntity(
         id = rs.getLong("id"),
         timeCreated = rs.getOffsetDateTime("time_created"),
         timeUpdated = rs.getOffsetDateTime("time_updated"),
         store =  store,
         departmentAccess = departmentAccess,
         schedule = schedule
      )
}
