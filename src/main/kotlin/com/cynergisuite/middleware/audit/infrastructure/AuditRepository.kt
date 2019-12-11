package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.action.infrastructure.AuditActionRepository
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.audit.status.AuditStatusCount
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.audit.status.infrastructure.AuditStatusRepository
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepository @Inject constructor(
   private val auditActionRepository: AuditActionRepository,
   private val auditStatusRepository: AuditStatusRepository,
   private val employeeRepository: EmployeeRepository,
   private val jdbc: NamedParameterJdbcTemplate,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditRepository::class.java)
   @Language("PostgreSQL") private val baseFindQuery = """
         WITH employees AS (
            ${employeeRepository.selectBase}
         )
         SELECT
            a.id AS id,
            a.id AS a_id,
            a.uu_row_id AS a_uu_row_id,
            a.time_created AS a_time_created,
            a.time_updated AS a_time_updated,
            a.store_number AS store_number,
            a.number AS a_number,
            (SELECT csastd.value
             FROM audit_action csaa JOIN audit_status_type_domain csastd ON csaa.status_id = csastd.id
             WHERE csaa.audit_id = a.id ORDER BY csaa.id DESC LIMIT 1
            ) AS current_status,
            aa.id AS aa_id,
            aa.uu_row_id AS aa_uu_row_id,
            aa.time_created AS aa_time_created,
            aa.time_updated AS aa_time_updated,
            astd.id AS astd_id,
            astd.value AS astd_value,
            astd.description AS astd_description,
            astd.color AS astd_color,
            astd.localization_code AS astd_localization_code,
            aer.e_id AS aer_id,
            aer.e_time_created AS aer_time_created,
            aer.e_time_updated AS aer_time_updated,
            aer.e_number AS aer_number,
            aer.e_last_name AS aer_last_name,
            aer.e_first_name_mi AS aer_first_name_mi,
            aer.e_pass_code AS aer_pass_code,
            aer.e_active AS aer_active,
            aer.e_department AS aer_department,
            aer.e_employee_type AS aer_employee_type,
            aer.e_allow_auto_store_assign AS aer_allow_auto_store_assign,
            s.id AS s_id,
            s.time_created AS s_time_created,
            s.time_updated AS s_time_updated,
            s.name AS s_name,
            s.number AS s_number,
            s.dataset AS s_dataset,
            se.id AS se_id,
            se.time_created AS se_time_created,
            se.time_updated AS se_time_updated,
            se.name AS se_name,
            se.dataset AS s_dataset
         FROM audit a
              JOIN audit_action aa
                  ON a.id = aa.audit_id
              JOIN audit_status_type_domain astd
                  ON aa.status_id = astd.id
              JOIN employees aer
                  ON aa.changed_by = aer.e_number
              JOIN fastinfo_prod_import.store_vw s
                  ON a.store_number = s.number
              JOIN fastinfo_prod_import.store_vw se
                  ON aer.s_number = se.number
      """.trimMargin()

   fun findOne(id: Long): AuditEntity? {
      logger.debug("Searching for audit by id {}", id)

      val found = jdbc.findFirstOrNull("$baseFindQuery\nWHERE a.id = :id", mapOf("id" to id)) { rs ->
         val audit = this.mapRow(rs)

         do {
            auditActionRepository.mapRowOrNull(rs)?.also { audit.actions.add(it) }
         } while(rs.next())

         audit
      }

      if (found != null) {
         loadNextStates(found)
      }

      logger.trace("Searching for Audit with ID {} resulted in {}", id, found)

      return found
   }

   fun findOneCreatedOrInProgress(store: StoreEntity): AuditEntity? {
      logger.debug("Searching for audit not completed or canceled for store {} and status {}", store)

      val found = jdbc.findFirstOrNull("$baseFindQuery\nWHERE a.store_number = :store_number AND astd.value IN (:statuses)",
         mapOf("store_number" to store.number, "statuses" to listOf(CREATED.value, IN_PROGRESS.value))) { rs ->
         val audit = this.mapRow(rs)

         do {
            auditActionRepository.mapRowOrNull(rs)?.also { audit.actions.add(it) }
         } while(rs.next())

         audit
      }

      if (found != null) {
         loadNextStates(found)
      }

      logger.debug("Searching for audit not completed or canceled for store {} resulted in {}", store, found)

      return found
   }

   fun findAll(pageRequest: AuditPageRequest): RepositoryPage<AuditEntity, AuditPageRequest> {
      val params = mutableMapOf<String, Any>()
      val storeNumber = pageRequest.storeNumber
      val status = pageRequest.status
      val whereBuilder = StringBuilder()
      val from = pageRequest.from
      val thru = pageRequest.thru
      var where = " WHERE "
      var and = EMPTY

      if (storeNumber != null) {
         params["store_number"] = storeNumber
         whereBuilder.append(where).append(" store_number = :store_number ")
         where = EMPTY
         and = " AND "
      }

      if (from != null && thru != null) {
         params["from"] = from
         params["thru"] = thru
         whereBuilder.append(where).append(and).append(" a.time_created BETWEEN :from AND :thru ")
         where = EMPTY
         and = " AND "
      }

      if (status != null && status.isNotEmpty()) {
         params["current_status"] = status
         whereBuilder.append(where).append(and).append(" current_status IN (:current_status) ")
      }

      @Language("PostgreSQL")
      val sql = """
         WITH employees AS (
            ${employeeRepository.selectBase}
         ), audits AS (
            WITH status AS (
               SELECT csastd.value AS current_status,
                      csaa.audit_id AS audit_id, csaa.id
               FROM audit_action csaa
                    JOIN audit_status_type_domain csastd
                         ON csaa.status_id = csastd.id
            ), maxStatus AS (
               SELECT MAX(id) AS current_status_id, audit_id
               FROM audit_action
               GROUP BY audit_id
            )
            SELECT
               a.id AS id,
               a.uu_row_id AS uu_row_id,
               a.time_created AS time_created,
               a.time_updated AS time_updated,
               a.store_number AS store_number,
               a.number AS number,
               s.current_status AS current_status,
               (SELECT count(a.id)
                FROM audit a
                    JOIN status s
                      ON s.audit_id = a.id
                    JOIN maxStatus ms
                      ON s.id = ms.current_status_id
                $whereBuilder) AS total_elements
            FROM audit a
                 JOIN status s
                      ON s.audit_id = a.id
                 JOIN maxStatus ms
                      ON s.id = ms.current_status_id
            $whereBuilder
            ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection}
            LIMIT ${pageRequest.size}
               OFFSET ${pageRequest.offset()}
         )
         SELECT
            a.id AS a_id,
            a.uu_row_id AS a_uu_row_id,
            a.time_created AS a_time_created,
            a.time_updated AS a_time_updated,
            a.store_number AS store_number,
            a.number AS a_number,
            a.current_status AS current_status,
            aa.id AS aa_id,
            aa.uu_row_id AS aa_uu_row_id,
            aa.time_created AS aa_time_created,
            aa.time_updated AS aa_time_updated,
            astd.id AS astd_id,
            astd.value AS astd_value,
            astd.description AS astd_description,
            astd.color AS astd_color,
            astd.localization_code AS astd_localization_code,
            aer.e_id AS aer_id,
            aer.e_time_created AS aer_time_created,
            aer.e_time_updated AS aer_time_updated,
            aer.e_number AS aer_number,
            aer.e_last_name AS aer_last_name,
            aer.e_first_name_mi AS aer_first_name_mi,
            aer.e_pass_code AS aer_pass_code,
            aer.e_active AS aer_active,
            aer.e_department AS aer_department,
            aer.e_employee_type AS aer_employee_type,
            aer.e_allow_auto_store_assign AS aer_allow_auto_store_assign,
            s.id AS s_id,
            s.time_created AS s_time_created,
            s.time_updated AS s_time_updated,
            s.name AS s_name,
            s.number AS s_number,
            s.dataset AS s_dataset,
            se.id AS se_id,
            se.time_created AS se_time_created,
            se.time_updated AS se_time_updated,
            se.name AS se_name,
            se.dataset AS s_dataset,
            total_elements AS total_elements
         FROM audits a
              JOIN audit_action aa
                  ON a.id = aa.audit_id
              JOIN audit_status_type_domain astd
                  ON aa.status_id = astd.id
              JOIN employees aer
                  ON aa.changed_by = aer.e_number
              JOIN fastinfo_prod_import.store_vw s
                  ON a.store_number = s.number
              JOIN fastinfo_prod_import.store_vw se
                  ON aer.s_number = se.number
         ORDER BY a_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection}
      """.trimIndent()

      logger.trace("Finding all audits for {} using {}\n{}", pageRequest, params, sql)

      val repoPage = jdbc.queryPaged<AuditEntity, AuditPageRequest>(sql, params, pageRequest) { rs, elements ->
         var currentId: Long = -1
         var currentParentEntity: AuditEntity? = null

         do {
            val tempId = rs.getLong("a_id")
            val tempParentEntity: AuditEntity = if (tempId != currentId) {
               currentId = tempId
               currentParentEntity = mapRow(rs)
               elements.add(currentParentEntity)
               currentParentEntity
            } else {
               currentParentEntity!!
            }

            tempParentEntity.actions.add(auditActionRepository.mapRow(rs))
         } while(rs.next())
      }

      return repoPage.copy(elements = repoPage.elements.onEach(this::loadNextStates))
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM audit WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Audit: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

   fun findAuditStatusCounts(pageRequest: AuditPageRequest): List<AuditStatusCount> {
      val status = pageRequest.status
      val params = mutableMapOf<String, Any>()
      val whereBuilder = StringBuilder()
      val from = pageRequest.from
      val thru = pageRequest.thru
      var where = " WHERE "
      var and = EMPTY

      if (from != null && thru != null) {
         params["from"] = from
         params["thru"] = thru
         whereBuilder.append(where).append(" csaa.time_created BETWEEN :from AND :thru ")
         where = EMPTY
         and = " AND "
      }

      if ( !status.isNullOrEmpty() ) {
         params["statuses"] = status.asSequence().toList()
         whereBuilder.append(where).append(and).append(" csastd.value IN (:statuses) ")
      }

      return jdbc.query("""
         WITH status AS (
            SELECT
               csastd.id AS current_status_id,
               csastd.value AS current_status,
               csastd.description AS current_status_description,
               csastd.localization_code AS current_status_localization_code,
               csastd.color AS current_status_color,
               csaa.audit_id AS audit_id,
               csaa.id
            FROM audit_action csaa
               JOIN audit_status_type_domain csastd
                 ON csaa.status_id = csastd.id
            $whereBuilder
            ),
            maxStatus AS (
               SELECT MAX(id) AS current_status_id, audit_id
               FROM audit_action
               GROUP BY audit_id
            )
         SELECT
            status.current_status_id AS current_status_id,
            status.current_status AS current_status,
            status.current_status_description AS current_status_description,
            status.current_status_localization_code AS current_status_localization_code,
            status.current_status_color AS current_status_color,
            count(*) AS current_status_count
         FROM audit a
            JOIN status status ON status.audit_id = a.id
            JOIN maxStatus ms ON status.id = ms.current_status_id
            JOIN fastinfo_prod_import.store_vw store ON a.store_number = store.number
         GROUP BY status.current_status,
                  status.current_status_description,
                  status.current_status_localization_code,
                  status.current_status_color,
                  status.current_status_id
         """.trimIndent(),
         params
      ) { rs, _ ->
         AuditStatusCount(
            id = rs.getLong("current_status_id"),
            value = rs.getString("current_status"),
            description = rs.getString("current_status_description"),
            localizationCode = rs.getString("current_status_localization_code"),
            color = rs.getString("current_status_color"),
            count = rs.getInt("current_status_count")
         )
      }
   }

   fun countAuditsNotCompletedOrCanceled(storeNumber: Int): Int =
      jdbc.queryForObject("""
         SELECT COUNT (*)
         FROM (
            SELECT *
            FROM (
                  SELECT a.id, MAX(aa.status_id) AS max_status
                  FROM audit a
                      JOIN audit_action aa
                        ON a.id = aa.audit_id
                  WHERE a.store_number = :store_number
                  GROUP BY a.id
            ) b
            JOIN audit_status_type_domain astd
              ON b.max_status = astd.id
            WHERE astd.VALUE IN (:values)
         ) c
         """.trimIndent(),
         mapOf(
            "store_number" to storeNumber,
            "values" to listOf(CREATED.value, IN_PROGRESS.value)
         ),
         Int::class.java
      )!!

   @Transactional
   fun insert(entity: AuditEntity): AuditEntity {
      logger.debug("Inserting audit {}", entity)

      val audit = jdbc.insertReturning(
         """
         INSERT INTO audit(store_number)
         VALUES (:store_number)
         RETURNING
            *
         """.trimMargin(),
         mapOf("store_number" to entity.store.number),
         RowMapper { rs, _ ->
            AuditEntity(
               id = rs.getLong("id"),
               uuRowId = rs.getUuid("uu_row_id"),
               timeCreated = rs.getOffsetDateTime("time_created"),
               timeUpdated = rs.getOffsetDateTime("time_updated"),
               store = entity.store,
               number = rs.getInt("number")
            )
         }
      )

      entity.actions.asSequence()
         .map { auditActionRepository.insert(audit, it) }
         .forEach { audit.actions.add(it) }

      return audit
   }

   @Transactional
   fun update(entity: AuditEntity): AuditEntity {
      logger.debug("Updating Audit {}", entity)

      // Since it isn't really necessary to update a store number for an audit as they can just cancel the audit and create a new one, just upsert the actions
      val actions = entity.actions.asSequence()
         .map { auditActionRepository.upsert(entity, it) }
         .toMutableSet()

      return entity.copy(actions = actions)
   }

   private fun mapRow(rs: ResultSet): AuditEntity =
      AuditEntity(
         id = rs.getLong("a_id"),
         uuRowId = rs.getUuid("a_uu_row_id"),
         timeCreated = rs.getOffsetDateTime("a_time_created"),
         timeUpdated = rs.getOffsetDateTime("a_time_updated"),
         store = storeRepository.mapRow(rs, "s_"),
         number = rs.getInt("a_number")
      )

   private fun loadNextStates(audit: AuditEntity) {
      val actions = audit.actions.map { action ->
         val actionStatus = auditStatusRepository.findOne(action.status.id)!!

         val changed = action.copy(status = actionStatus)

         changed
      }

      audit.actions.clear()
      audit.actions.addAll(actions)
   }
}
