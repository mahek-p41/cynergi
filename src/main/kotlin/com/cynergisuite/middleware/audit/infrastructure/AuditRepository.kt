package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getOffsetDateTimeOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.action.infrastructure.AuditActionRepository
import com.cynergisuite.middleware.audit.status.AuditStatusCount
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.audit.status.infrastructure.AuditStatusRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
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
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditRepository::class.java)

   private fun selectBaseQuery(): String =
   """
      WITH employees AS (
         ${employeeRepository.employeeBaseQuery()}
      )
      SELECT
         a.id                                                          AS a_id,
         a.uu_row_id                                                   AS a_uu_row_id,
         a.time_created                                                AS a_time_created,
         a.time_updated                                                AS a_time_updated,
         a.store_number                                                AS store_number,
         a.number                                                      AS a_number,
         (SELECT count(id) FROM audit_detail WHERE audit_id = a.id)    AS a_total_details,
         (SELECT count(id) FROM audit_exception WHERE audit_id = a.id) AS a_total_exceptions,
         (
          SELECT count(aen.id) > 0
          FROM audit_exception ae
               JOIN audit_exception_note aen ON ae.id = aen.audit_exception_id
          WHERE ae.audit_id = a.id
         )                                                             AS a_exception_has_notes,
         (SELECT max(time_updated)
            FROM (
                 SELECT time_updated FROM audit_detail WHERE audit_id = a.id
                 UNION
                 SELECT time_updated FROM audit_exception WHERE audit_id = a.id
               ) AS m
         )                                                             AS a_last_updated,
         a.inventory_count                                             AS a_inventory_count,
         (SELECT csastd.value
          FROM audit_action csaa JOIN audit_status_type_domain csastd ON csaa.status_id = csastd.id
          WHERE csaa.audit_id = a.id ORDER BY csaa.id DESC LIMIT 1
         )                                                             AS current_status,
         auditAction.id                                                AS auditAction_id,
         auditAction.uu_row_id                                         AS auditAction_uu_row_id,
         auditAction.time_created                                      AS auditAction_time_created,
         auditAction.time_updated                                      AS auditAction_time_updated,
         astd.id                                                       AS astd_id,
         astd.value                                                    AS astd_value,
         astd.description                                              AS astd_description,
         astd.color                                                    AS astd_color,
         astd.localization_code                                        AS astd_localization_code,
         auditActionEmployee.emp_id                                    AS auditActionEmployee_id,
         auditActionEmployee.emp_number                                AS auditActionEmployee_number,
         auditActionEmployee.emp_last_name                             AS auditActionEmployee_last_name,
         auditActionEmployee.emp_first_name_mi                         AS auditActionEmployee_first_name_mi,
         auditActionEmployee.emp_pass_code                             AS auditActionEmployee_pass_code,
         auditActionEmployee.emp_active                                AS auditActionEmployee_active,
         auditActionEmployee.emp_type                                  AS auditActionEmployee_type,
         auditActionEmployee.emp_cynergi_system_admin                  AS auditActionEmployee_cynergi_system_admin,
         auditActionEmployee.dept_id                                   AS auditActionEmployeeDept_id,
         auditActionEmployee.dept_code                                 AS auditActionEmployeeDept_code,
         auditActionEmployee.dept_description                          AS auditActionEmployeeDept_description,
         auditActionEmployee.dept_security_profile                     AS auditActionEmployeeDept_security_profile,
         auditActionEmployee.dept_default_menu                         AS auditActionEmployeeDept_default_menu,
         auditActionEmployee.emp_alternative_store_indicator           AS auditActionEmployee_alternative_store_indicator,
         auditActionEmployee.emp_alternative_area                      AS auditActionEmployee_alternative_area,
         auditStore.id                                                 AS auditStore_id,
         auditStore.name                                               AS auditStore_name,
         auditStore.number                                             AS auditStore_number,
         auditStore.dataset                                            AS auditStore_dataset,
         comp.id                                                       AS comp_id,
         comp.uu_row_id                                                AS comp_uu_row_id,
         comp.time_created                                             AS comp_time_created,
         comp.time_updated                                             AS comp_time_updated,
         comp.name                                                     AS comp_name,
         comp.doing_business_as                                        AS comp_doing_business_as,
         comp.client_code                                              AS comp_client_code,
         comp.client_id                                                AS comp_client_id,
         comp.dataset_code                                             AS comp_dataset_code,
         comp.federal_id_number                                        AS comp_federal_id_number
      FROM audit a
           JOIN company comp ON a.company_id = comp.id
           JOIN audit_action auditAction ON a.id = auditAction.audit_id
           JOIN audit_status_type_domain astd ON auditAction.status_id = astd.id
           JOIN employees auditActionEmployee ON comp.id = auditActionEmployee.comp_id AND auditAction.changed_by = auditActionEmployee.emp_number
           JOIN fastinfo_prod_import.store_vw auditStore ON comp.dataset_code = auditStore.dataset AND a.store_number = auditStore.number
   """

   fun findOne(id: Long, company: Company): AuditEntity? {
      logger.debug("Searching for audit by id {} with company {}", id, company)

      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectBaseQuery()}\nWHERE a.id = :id"
      val found = executeFindSingleQuery(query, params)

      logger.trace("Searching for Audit with ID {} resulted in {}", id, found)

      return found
   }

   fun findOneCreatedOrInProgress(store: StoreEntity): AuditEntity? {
      val params = mutableMapOf<String, Any?>("store_number" to store.number, "statuses" to listOf(CREATED.value, IN_PROGRESS.value))
      val query = "${selectBaseQuery()}\nWHERE a.store_number = :store_number AND astd.value IN (:statuses)"

      logger.debug("Searching for audit in either CREATED or IN_PROGRESS for store {} using {}", store, query)

      val found = executeFindSingleQuery(query, params)

      logger.debug("Searching for audit not completed or canceled for store {} resulted in {}", store, found)

      return found
   }

   private fun executeFindSingleQuery(query: String, params: Map<String, Any?>): AuditEntity? {
      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val audit = this.mapRow(rs)

         do {
            audit.actions.add(mapAuditAction(rs))
         } while(rs.next())

         audit
      }

      if (found != null) {
         loadNextStates(found)
      }

      return found
   }

   fun findAll(pageRequest: AuditPageRequest, company: Company): RepositoryPage<AuditEntity, AuditPageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId(), "limit" to pageRequest.size(), "offset" to pageRequest.offset())
      val storeNumbers = pageRequest.storeNumber
      val status = pageRequest.status
      val whereBuilder = StringBuilder("WHERE a.company_id = :comp_id ")
      val from = pageRequest.from
      val thru = pageRequest.thru

      if (!storeNumbers.isNullOrEmpty()) {
         params["store_numbers"] = storeNumbers
         whereBuilder.append(" AND store_number IN (:store_numbers) ")
      }

      if (from != null && thru != null) {
         params["from"] = from
         params["thru"] = thru
         whereBuilder.append(" AND a.time_created BETWEEN :from AND :thru ")
      }

      if (!status.isNullOrEmpty()) {
         params["current_status"] = status
         whereBuilder.append(" AND current_status IN (:current_status) ")
      }

      val sql = """
         WITH employees AS (
            ${employeeRepository.employeeBaseQuery()}
         ), audits AS (
            WITH status AS (
               SELECT
                  csastd.value AS current_status,
                  csaa.audit_id AS audit_id, csaa.id
               FROM audit_action csaa JOIN audit_status_type_domain csastd ON csaa.status_id = csastd.id
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
               (  SELECT count(aen.id) > 0
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
               a.company_id AS company_id,
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
            LIMIT :limit OFFSET :offset
         )
         SELECT
            a.id                                                AS a_id,
            a.uu_row_id                                         AS a_uu_row_id,
            a.time_created                                      AS a_time_created,
            a.time_updated                                      AS a_time_updated,
            a.store_number                                      AS store_number,
            a.number                                            AS a_number,
            a.total_details                                     AS a_total_details,
            a.total_exceptions                                  AS a_total_exceptions,
            a.current_status                                    AS current_status,
            a.last_updated                                      AS a_last_updated,
            a.inventory_count                                   AS a_inventory_count,
            a.exception_has_notes                               AS a_exception_has_notes,
            auditAction.id                                      AS auditAction_id,
            auditAction.uu_row_id                               AS auditAction_uu_row_id,
            auditAction.time_created                            AS auditAction_time_created,
            auditAction.time_updated                            AS auditAction_time_updated,
            astd.id                                             AS astd_id,
            astd.value                                          AS astd_value,
            astd.description                                    AS astd_description,
            astd.color                                          AS astd_color,
            astd.localization_code                              AS astd_localization_code,
            auditActionEmployee.emp_id                          AS auditActionEmployee_id,
            auditActionEmployee.emp_number                      AS auditActionEmployee_number,
            auditActionEmployee.emp_last_name                   AS auditActionEmployee_last_name,
            auditActionEmployee.emp_first_name_mi               AS auditActionEmployee_first_name_mi,
            auditActionEmployee.emp_pass_code                   AS auditActionEmployee_pass_code,
            auditActionEmployee.emp_active                      AS auditActionEmployee_active,
            auditActionEmployee.emp_type                        AS auditActionEmployee_type,
            auditActionEmployee.emp_cynergi_system_admin        AS auditActionEmployee_cynergi_system_admin,
            auditActionEmployee.emp_alternative_store_indicator AS auditActionEmployee_alternative_store_indicator,
            auditActionEmployee.emp_alternative_area            AS auditActionEmployee_alternative_area,
            auditActionEmployee.dept_id                         AS auditActionEmployeeDept_id,
            auditActionEmployee.dept_code                       AS auditActionEmployeeDept_code,
            auditActionEmployee.dept_description                AS auditActionEmployeeDept_description,
            auditActionEmployee.dept_security_profile           AS auditActionEmployeeDept_security_profile,
            auditActionEmployee.dept_default_menu               AS auditActionEmployeeDept_default_menu,
            auditStore.id                                       AS auditStore_id,
            auditStore.name                                     AS auditStore_name,
            auditStore.number                                   AS auditStore_number,
            auditStore.dataset                                  AS auditStore_dataset,
            comp.id                                             AS comp_id,
            comp.uu_row_id                                      AS comp_uu_row_id,
            comp.time_created                                   AS comp_time_created,
            comp.time_updated                                   AS comp_time_updated,
            comp.name                                           AS comp_name,
            comp.doing_business_as                              AS comp_doing_business_as,
            comp.client_code                                    AS comp_client_code,
            comp.client_id                                      AS comp_client_id,
            comp.dataset_code                                   AS comp_dataset_code,
            comp.federal_id_number                              AS comp_federal_id_number,
            total_elements                                      AS total_elements
         FROM audits a
              JOIN company comp ON a.company_id = comp.id
              JOIN audit_action auditAction ON a.id = auditAction.audit_id
              JOIN audit_status_type_domain astd ON auditAction.status_id = astd.id
              JOIN employees auditActionEmployee ON comp.id = auditActionEmployee.comp_id AND auditAction.changed_by = auditActionEmployee.emp_number
              JOIN fastinfo_prod_import.store_vw auditStore ON comp.dataset_code = auditStore.dataset AND a.store_number = auditStore.number
         ORDER BY a_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
      """.trimIndent()

      logger.trace("Finding all audits using {}\n{}", params, sql)

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

            tempParentEntity.actions.add(mapAuditAction(rs))
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

   fun findAuditStatusCounts(pageRequest: AuditPageRequest, company: Company): List<AuditStatusCount> {
      val status = pageRequest.status
      val params = mutableMapOf<String, Any>("comp_id" to company.myId()!!)
      val storeNumbers = pageRequest.storeNumber
      val whereBuilderForStatusAndTimeCreated = StringBuilder()
      val whereBuilderForCompanyAndStore = StringBuilder("WHERE a.company_id = :comp_id ")
      val from = pageRequest.from
      val thru = pageRequest.thru
      var where = " WHERE "
      var and = EMPTY

      if (from != null && thru != null) {
         params["from"] = from
         params["thru"] = thru
         whereBuilderForStatusAndTimeCreated.append(where).append(and).append(" csaa.time_created BETWEEN :from AND :thru ")
         where = EMPTY
         and = " AND "
      }

      if ( !status.isNullOrEmpty() ) {
         params["statuses"] = status.asSequence().toList()
         whereBuilderForStatusAndTimeCreated.append(where).append(and).append(" csastd.value IN (:statuses) ")
      }

      if ( !storeNumbers.isNullOrEmpty() ) {
         params["store_numbers"] = storeNumbers
         whereBuilderForCompanyAndStore.append(" AND store_number IN (:store_numbers) ")
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
            $whereBuilderForStatusAndTimeCreated
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
            JOIN company comp ON a.company_id = comp.id
            JOIN status status ON status.audit_id = a.id
            JOIN maxStatus ms ON status.id = ms.current_status_id
            JOIN fastinfo_prod_import.store_vw auditStore ON comp.dataset_code = auditStore.dataset AND a.store_number = auditStore.number
         $whereBuilderForCompanyAndStore
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

   fun countAuditsNotCompletedOrCanceled(storeNumber: Int, company: Company): Int =
      jdbc.queryForObject("""
         SELECT COUNT (*)
         FROM (
            SELECT *
            FROM (
                  SELECT a.id, MAX(aa.status_id) AS max_status
                  FROM audit a
                      JOIN audit_action aa ON a.id = aa.audit_id
                  WHERE a.store_number = :store_number AND a.company_id = :comp_id
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
            "comp_id" to company.myId()
         ),
         Int::class.java
      )!!

   @Transactional
   fun insert(entity: AuditEntity): AuditEntity {
      logger.debug("Inserting audit {}", entity)

      val audit = jdbc.insertReturning(
         """
        INSERT INTO audit(store_number, inventory_count, company_id)
         VALUES (
            :store_number,
            (
               SELECT COUNT (i.id)
               FROM fastinfo_prod_import.inventory_vw i
                    JOIN company comp ON i.dataset = comp.dataset_code
               WHERE i.primary_location = :store_number
                     AND i.location = :store_number
                     AND i.status in ('N', 'R')
                     AND comp.id = :company_id
            ),
            :company_id
         )
         RETURNING
            *
         """.trimMargin(),
         mapOf(
            "store_number" to entity.store.number,
            "company_id" to entity.store.company.myId()
         ),
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
               hasExceptionNotes = false,
               inventoryCount = rs.getInt("inventory_count"),
               lastUpdated = null,
               actions = mutableSetOf<AuditActionEntity>()
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

   private fun mapRow(rs: ResultSet): AuditEntity {
      return AuditEntity(
         id = rs.getLong("a_id"),
         uuRowId = rs.getUuid("a_uu_row_id"),
         timeCreated = rs.getOffsetDateTime("a_time_created"),
         timeUpdated = rs.getOffsetDateTime("a_time_updated"),
         store = mapStore(rs),
         number = rs.getInt("a_number"),
         totalDetails = rs.getInt("a_total_details"),
         totalExceptions = rs.getInt("a_total_exceptions"),
         hasExceptionNotes = rs.getBoolean("a_exception_has_notes"),
         inventoryCount = rs.getInt("a_inventory_count"),
         lastUpdated = rs.getOffsetDateTimeOrNull("a_last_updated")
      )
   }

   private fun mapStore(rs: ResultSet): StoreEntity {
      return StoreEntity(
         id = rs.getLong("auditStore_id"),
         number = rs.getInt("auditStore_number"),
         name = rs.getString("auditStore_name"),
         company = mapCompany(rs)
      )
   }

   private fun mapCompany(rs: ResultSet): Company {
      return CompanyEntity(
         id = rs.getLong("comp_id"),
         uuRowId = rs.getUuid("comp_uu_row_id"),
         timeCreated = rs.getOffsetDateTime("comp_time_created"),
         timeUpdated = rs.getOffsetDateTime("comp_time_updated"),
         name = rs.getString("comp_name"),
         doingBusinessAs = rs.getString("comp_doing_business_as"),
         clientCode = rs.getString("comp_client_code"),
         clientId = rs.getInt("comp_client_id"),
         federalIdNumber = rs.getString("comp_federal_id_number"),
         datasetCode = rs.getString("comp_dataset_code")
      )
   }

   private fun mapAuditAction(rs: ResultSet): AuditActionEntity {
      return AuditActionEntity(
         id = rs.getLong("auditAction_id"),
         uuRowId = rs.getUuid("auditAction_uu_row_id"),
         timeCreated = rs.getOffsetDateTime("auditAction_time_created"),
         timeUpdated = rs.getOffsetDateTime("auditAction_time_updated"),
         status = auditStatusRepository.mapRow(rs, "astd_"),
         changedBy = mapAuditActionEmployee(rs)
      )
   }

   private fun mapAuditActionEmployee(rs: ResultSet): EmployeeEntity {
      return EmployeeEntity(
         id = rs.getLong("auditActionEmployee_id"),
         type = rs.getString("auditActionEmployee_type"),
         number = rs.getInt("auditActionEmployee_number"),
         company = mapCompany(rs),
         lastName = rs.getString("auditActionEmployee_last_name"),
         firstNameMi = rs.getString("auditActionEmployee_first_name_mi"),  // FIXME fix query so that it isn't trimming stuff to null when employee is managed by PostgreSQL
         passCode = rs.getString("auditActionEmployee_pass_code"),
         store = mapStore(rs),
         active = rs.getBoolean("auditActionEmployee_active"),
         department = mapAuditActionEmployeeDepartment(rs),
         cynergiSystemAdmin = rs.getBoolean("auditActionEmployee_cynergi_system_admin"),
         alternativeStoreIndicator = rs.getString("auditActionEmployee_alternative_store_indicator"),
         alternativeArea = rs.getInt("auditActionEmployee_alternative_area")
      )
   }

   private fun mapAuditActionEmployeeDepartment(rs: ResultSet): DepartmentEntity? {
      return if (rs.getString("auditActionEmployeeDept_id") != null) {
         DepartmentEntity(
            id = rs.getLong("auditActionEmployeeDept_id"),
            code = rs.getString("auditActionEmployeeDept_code"),
            description = rs.getString("auditActionEmployeeDept_description"),
            securityProfile = rs.getInt("auditActionEmployeeDept_security_profile"),
            defaultMenu = rs.getString("auditActionEmployeeDept_default_menu"),
            company = mapCompany(rs)
         )
      } else {
         null
      }
   }

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
