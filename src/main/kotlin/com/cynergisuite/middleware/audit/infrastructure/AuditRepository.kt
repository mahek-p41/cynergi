package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getOffsetDateTimeOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.action.infrastructure.AuditActionRepository
import com.cynergisuite.middleware.audit.status.AuditStatusCount
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.audit.status.infrastructure.AuditStatusRepository
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
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

   private fun selectBaseQuery(params: MutableMap<String, Any?>, dataset: String): String {
      return """
         WITH employees AS (
            ${employeeRepository.selectBaseQuery(params, dataset)}
         )
         SELECT
            a.id AS id,
            a.id AS a_id,
            a.uu_row_id AS a_uu_row_id,
            a.time_created AS a_time_created,
            a.time_updated AS a_time_updated,
            a.store_number AS store_number,
            a.number AS a_number,
            (SELECT count(id) FROM audit_detail WHERE audit_id = a.id) AS a_total_details,
            (SELECT count(id) FROM audit_exception WHERE audit_id = a.id) AS a_total_exceptions,
            (
             SELECT count(aen.id) > 0
             FROM audit_exception ae
                  JOIN audit_exception_note aen ON ae.id = aen.audit_exception_id
             WHERE ae.audit_id = a.id
            ) AS a_exception_has_notes,
            (SELECT max(time_updated)
               FROM (
                    SELECT time_updated FROM audit_detail WHERE audit_id = a.id
                    UNION
                    SELECT time_updated FROM audit_exception WHERE audit_id = a.id
                  ) AS m
            ) AS a_last_updated,
            a.inventory_count as a_inventory_count,
            (SELECT csastd.value
             FROM audit_action csaa JOIN audit_status_type_domain csastd ON csaa.status_id = csastd.id
             WHERE csaa.audit_id = a.id ORDER BY csaa.id DESC LIMIT 1
            ) AS current_status,
            a.dataset AS a_dataset,
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
            aer.e_number AS aer_number,
            aer.e_dataset AS aer_dataset,
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
                     AND a.dataset = s.dataset
              JOIN fastinfo_prod_import.store_vw se
                  ON aer.s_number = se.number
                     AND a.dataset = se.dataset
      """
   }

   fun findOne(id: Long, dataset: String): AuditEntity? {
      logger.debug("Searching for audit by id {} with dataset {}", id, dataset)

      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectBaseQuery(params, dataset)}\nWHERE a.id = :id"
      val found = executeFindSingleQuery(query, params)

      logger.trace("Searching for Audit with ID {} resulted in {}", id, found)

      return found
   }

   fun findOneCreatedOrInProgress(store: StoreEntity): AuditEntity? {
      val params = mutableMapOf<String, Any?>("store_number" to store.number, "statuses" to listOf(CREATED.value, IN_PROGRESS.value))
      val query = "${selectBaseQuery(params, store.dataset)}\nWHERE a.store_number = :store_number AND astd.value IN (:statuses)"

      logger.debug("Searching for audit in either CREATED or IN_PROGRESS for store {} using {}", store, query)

      val found = executeFindSingleQuery(query, params)

      logger.debug("Searching for audit not completed or canceled for store {} resulted in {}", store, found)

      return found
   }

   private fun executeFindSingleQuery(query: String, params: Map<String, Any?>): AuditEntity? {
      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val audit = this.mapRow(rs)

         do {
            auditActionRepository.mapRowOrNull(rs)?.also { audit.actions.add(it) }
         } while(rs.next())

         audit
      }

      if (found != null) {
         loadNextStates(found)
      }

      return found
   }

   fun findAll(pageRequest: AuditPageRequest, dataset: String): RepositoryPage<AuditEntity, AuditPageRequest> {
      val params = mutableMapOf<String, Any?>()
      val storeNumber = pageRequest.storeNumber
      val status = pageRequest.status
      val whereBuilder = StringBuilder("WHERE a.dataset = :dataset ")
      val from = pageRequest.from
      val thru = pageRequest.thru

      if (storeNumber != null) {
         params["store_number"] = storeNumber
         whereBuilder.append(" AND store_number = :store_number ")
      }

      if (from != null && thru != null) {
         params["from"] = from
         params["thru"] = thru
         whereBuilder.append(" AND a.time_created BETWEEN :from AND :thru ")
      }

      if (status != null && status.isNotEmpty()) {
         params["current_status"] = status
         whereBuilder.append(" AND current_status IN (:current_status) ")
      }

      val sql = """
         WITH employees AS (
            ${employeeRepository.selectBaseQuery(params, dataset)}
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
               (SELECT count(id) FROM audit_detail WHERE audit_id = a.id) AS total_details,
               (SELECT count(id) FROM audit_exception WHERE audit_id = a.id) AS total_exceptions,
               (
                SELECT count(aen.id) > 0
                FROM audit_exception ae
                     JOIN audit_exception_note aen ON ae.id = aen.audit_exception_id
                WHERE ae.audit_id = a.id
               ) AS exception_has_notes,
               (SELECT max(time_updated)
                  FROM (
                       SELECT time_updated FROM audit_detail WHERE audit_id = a.id
                       UNION
                       SELECT time_updated FROM audit_exception WHERE audit_id = a.id
                     ) AS m
               ) AS last_updated,
               a.inventory_count AS inventory_count,
               a.dataset AS dataset,
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
            ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
            LIMIT ${pageRequest.size()}
               OFFSET ${pageRequest.offset()}
         )
         SELECT
            a.id AS a_id,
            a.uu_row_id AS a_uu_row_id,
            a.time_created AS a_time_created,
            a.time_updated AS a_time_updated,
            a.store_number AS store_number,
            a.number AS a_number,
            a.total_details AS a_total_details,
            a.total_exceptions AS a_total_exceptions,
            a.current_status AS current_status,
            a.last_updated AS a_last_updated,
            a.inventory_count AS a_inventory_count,
            a.exception_has_notes AS a_exception_has_notes,
            a.dataset AS a_dataset,
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
            aer.e_number AS aer_number,
            aer.e_dataset AS aer_dataset,
            aer.e_last_name AS aer_last_name,
            aer.e_first_name_mi AS aer_first_name_mi,
            aer.e_pass_code AS aer_pass_code,
            aer.e_active AS aer_active,
            aer.e_department AS aer_department,
            aer.e_employee_type AS aer_employee_type,
            aer.e_allow_auto_store_assign AS aer_allow_auto_store_assign,
            s.id AS s_id,
            s.name AS s_name,
            s.number AS s_number,
            s.dataset AS s_dataset,
            se.id AS se_id,
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
                  AND s.dataset = :dataset
              JOIN fastinfo_prod_import.store_vw se
                  ON aer.s_number = se.number
                  AND se.dataset = :dataset
         ORDER BY a_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
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

   fun findAuditStatusCounts(pageRequest: AuditPageRequest, dataset: String): List<AuditStatusCount> {
      val status = pageRequest.status
      val params = mutableMapOf<String, Any>("dataset" to dataset)
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
            JOIN fastinfo_prod_import.store_vw store ON a.store_number = store.number AND store.dataset = :dataset
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

   fun countAuditsNotCompletedOrCanceled(storeNumber: Int, dataset: String): Int =
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
                       AND a.dataset = :dataset
                  GROUP BY a.id
            ) b
            JOIN audit_status_type_domain astd
              ON b.max_status = astd.id
            WHERE astd.VALUE IN (:values)
         ) c
         """.trimIndent(),
         mapOf(
            "store_number" to storeNumber,
            "values" to listOf(CREATED.value, IN_PROGRESS.value),
            "dataset" to dataset
         ),
         Int::class.java
      )!!

   @Transactional
   fun insert(entity: AuditEntity): AuditEntity {
      logger.debug("Inserting audit {}", entity)

      val audit = jdbc.insertReturning(
         """
        INSERT INTO audit(store_number, inventory_count, dataset)
         VALUES (
            :store_number,
            (
               SELECT COUNT (id)
               FROM fastinfo_prod_import.inventory_vw i
               WHERE i.primary_location = :store_number
                     AND i.status in ('N', 'R', 'D')
                     AND i.dataset = :dataset
            ),
            :dataset
         )
         RETURNING
            *
         """.trimMargin(),
         mapOf("store_number" to entity.store.number, "dataset" to entity.store.dataset),
         RowMapper { rs, _ ->
            AuditEntity(
               id = rs.getLong("id"),
               uuRowId = rs.getUuid("uu_row_id"),
               timeCreated = rs.getOffsetDateTime("time_created"),
               timeUpdated = rs.getOffsetDateTime("time_updated"),
               store = entity.store,
               number = rs.getInt("number"),
               totalDetails = 0,
               totalExceptions = 0,
               inventoryCount = rs.getInt("inventory_count"),
               lastUpdated = null,
               dataset = rs.getString("dataset")
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

      // there is nothing to update on an audit as they can just cancel the audit and create a new one, just upsert the actions
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
         number = rs.getInt("a_number"),
         totalDetails = rs.getInt("a_total_details"),
         totalExceptions = rs.getInt("a_total_exceptions"),
         hasExceptionNotes = rs.getBoolean("a_exception_has_notes"),
         inventoryCount = rs.getInt("a_inventory_count"),
         lastUpdated = rs.getOffsetDateTimeOrNull("a_last_updated"),
         dataset = rs.getString("a_dataset")
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
