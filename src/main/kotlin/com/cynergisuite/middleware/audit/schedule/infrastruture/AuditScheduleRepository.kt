package com.cynergisuite.middleware.audit.schedule.infrastruture

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.Repository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.audit.schedule.AuditScheduleEntity
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.infrastructure.DepartmentRepository
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.argument.infrastructure.ScheduleArgumentRepository
import com.cynergisuite.middleware.schedule.infrastructure.ScheduleRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Singleton

@Singleton
class AuditScheduleRepository(
   private val departmentRepository: DepartmentRepository,
   private val scheduleRepository: ScheduleRepository,
   private val scheduleArgumentRepository: ScheduleArgumentRepository,
   private val storeRepository: StoreRepository,
   private val jdbc: NamedParameterJdbcTemplate
) : Repository<AuditScheduleEntity> {
   private val logger: Logger = LoggerFactory.getLogger(AuditScheduleRepository::class.java)

   override fun findOne(id: Long): AuditScheduleEntity? {
      logger.debug("Searching for audit schedule by id {}", id)

      var found: AuditScheduleEntity? = null

      jdbc.query("""
         SELECT 
            auditSched.id               AS auditSched_id,
            auditSched.uu_row_id        AS auditSched_uu_row_id,
            auditSched.time_created     AS auditSched_time_created,
            auditSched.time_updated     AS auditSched_time_updated,
            sched.id                    AS sched_id,
            sched.uu_row_id             AS sched_uu_row_id,
            sched.time_created          AS sched_time_created,
            sched.time_updated          AS sched_time_updated,
            sched.title                 AS sched_title,
            sched.description           AS sched_description,
            sched.schedule              AS sched_schedule,
            sched.command               AS sched_command,
            schedType.id                AS schedType_id,
            schedType.value             AS schedType_value,
            schedType.description       AS schedType_description,
            schedType.localization_code AS schedType_localization_code,
            schedArg.id                 AS schedArg_id,
            schedArg.uu_row_id          AS schedArg_uu_row_id,
            schedArg.time_created       AS schedArg_time_created,
            schedArg.time_updated       AS schedArg_time_updated,
            schedArg.value              AS schedArg_value,
            schedArg.description        AS schedArg_description
         FROM audit_schedule auditSched
              JOIN schedule sched ON auditSched.schedule_id = sched.id
              JOIN schedule_type_domain schedType ON sched.type_id = schedType.id
              JOIN fastinfo_prod_import.store_vw str ON auditSched.store_number = str.number
              JOIN fastinfo_prod_import.department_vw dept ON auditSched.department_access = dept.code
	           LEFT OUTER JOIN schedule_arg schedArg ON sched.id = schedArg.schedule_id
         WHERE auditSched.id = :id
         """.trimIndent(),
         mapOf("id" to id)
      ) { rs ->
         val localAuditSchedule = found ?: mapRow(
            rs = rs,
            store = storeRepository.mapRow(rs, "str_"),
            schedule = scheduleRepository.mapRow(rs, "sched_", "schedType_"),
            departmentAccess = departmentRepository.mapRow(rs, "dept_")
         )

         scheduleArgumentRepository.mapRowOrNull(rs, "schedArg_")?.also { localAuditSchedule.schedule.arguments.add(it) }
         found = localAuditSchedule
      }

      logger.debug("Searching for audit schedule with id {} resulted in {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit_schedule WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Audit Schedule: {} exists resulted in {}", id, exists)

      return exists
   }

   fun findAll(pageRequest: PageRequest): RepositoryPage<AuditScheduleEntity> {
      logger.trace("Fetching All")

      var totalElement: Long? = null
      val elements = mutableListOf<AuditScheduleEntity>()
      var currentAuditSchedule: AuditScheduleEntity? = null

      jdbc.query("""
         SELECT 
            auditSched.id                    AS auditSched_id,
            auditSched.uu_row_id             AS auditSched_uu_row_id,
            auditSched.time_created          AS auditSched_time_created,
            auditSched.time_updated          AS auditSched_time_updated,
            sched.id                         AS sched_id,
            sched.uu_row_id                  AS sched_uu_row_id,
            sched.time_created               AS sched_time_created,
            sched.time_updated               AS sched_time_updated,
            sched.title                      AS sched_title,
            sched.description                AS sched_description,
            sched.schedule                   AS sched_schedule,
            sched.command                    AS sched_command,
            schedType.id                     AS schedType_id,
            schedType.value                  AS schedType_value,
            schedType.description            AS schedType_description,
            schedType.localization_code      AS schedType_localization_code,
            schedArg.id                      AS schedArg_id,
            schedArg.uu_row_id               AS schedArg_uu_row_id,
            schedArg.time_created            AS schedArg_time_created,
            schedArg.time_updated            AS schedArg_time_updated,
            schedArg.value                   AS schedArg_value,
            schedArg.description             AS schedArg_description,
            (SELECT count(id) FROM schedule) AS total_elements
         FROM audit_schedule auditSched
              JOIN schedule sched ON auditSched.schedule_id = sched.id
              JOIN schedule_type_domain schedType ON sched.type_id = schedType.id
              JOIN fastinfo_prod_import.store_vw str ON auditSched.store_number = str.number
              JOIN fastinfo_prod_import.department_vw dept ON auditSched.department_access = dept.code
	           LEFT OUTER JOIN schedule_arg schedArg ON sched.id = schedArg.schedule_id
         ORDER BY sched_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection}
               LIMIT ${pageRequest.size}
               OFFSET ${pageRequest.offset()}
      """.trimIndent()) { rs ->
         val dbScheduleId = rs.getLong("auditSched_id")

         val localAuditSchedule: AuditScheduleEntity = if (currentAuditSchedule?.id != dbScheduleId) {
            val created = mapRow(
               rs = rs,
               store = storeRepository.mapRow(rs, "str_"),
               schedule = scheduleRepository.mapRow(rs, "sched_", "schedType_"),
               departmentAccess = departmentRepository.mapRow(rs, "dept_")
            )

            currentAuditSchedule = created

            created
         } else {
            currentAuditSchedule!!
         }

         scheduleArgumentRepository.mapRowOrNull(rs, "schedArg_")?.also { localAuditSchedule.schedule.arguments.add(it) }

         if (totalElement == null) {
            totalElement = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         elements = elements,
         totalElements = totalElement ?: 0
      )
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
