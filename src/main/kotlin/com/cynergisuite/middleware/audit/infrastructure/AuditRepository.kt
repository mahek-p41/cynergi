package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getOffsetDateTimeOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.action.infrastructure.AuditActionRepository
import com.cynergisuite.middleware.audit.status.AuditStatusCount
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.audit.status.infrastructure.AuditStatusRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class AuditRepository @Inject constructor(
   private val auditActionRepository: AuditActionRepository,
   private val auditStatusRepository: AuditStatusRepository,
   private val companyRepository: CompanyRepository,
   private val employeeRepository: EmployeeRepository,
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditRepository::class.java)

   private val queryAuditCurrentStatus: String =
      """
         (
         SELECT csastd.value
         FROM audit_action csaa
               JOIN audit_status_type_domain csastd ON csaa.status_id = csastd.id
         WHERE csaa.audit_id = a.id
         ORDER BY csaa.id DESC LIMIT 1
         )
      """

   private fun selectByIdBaseQuery(): String =
      """
      SELECT
         a.id                                                          AS a_id,
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
         (
         SELECT max(time_updated)
         FROM (
               SELECT time_updated FROM audit_detail WHERE audit_id = a.id
               UNION
               SELECT time_updated FROM audit_exception WHERE audit_id = a.id
               ) AS m
         )                                                             AS a_last_updated,
         $queryAuditCurrentStatus                                      AS current_status,
         CASE
         WHEN $queryAuditCurrentStatus IN ('CREATED', 'IN-PROGRESS')
         THEN
            (
            SELECT COUNT (*)
            FROM fastinfo_prod_import.inventory_vw i
            WHERE i.primary_location = a.store_number
                  AND i.location = a.store_number
                  AND i.status in ('N', 'R')
                  AND i.dataset = auditStore.dataset
            )
         ELSE
            (
            SELECT COUNT (*)
            FROM audit_inventory i
            WHERE i.primary_location = a.store_number
                  AND i.location = a.store_number
                  AND i.dataset = auditStore.dataset
                  AND i.audit_id = a.id
            )
         END                                                           AS a_inventory_count,
         auditAction.id                                                AS auditAction_id,
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
         auditActionEmployee.emp_alternative_store_indicator           AS auditActionEmployee_alternative_store_indicator,
         auditActionEmployee.emp_alternative_area                      AS auditActionEmployee_alternative_area,
         auditStore.id                                                 AS auditStore_id,
         auditStore.name                                               AS auditStore_name,
         auditStore.number                                             AS auditStore_number,
         auditStore.dataset                                            AS auditStore_dataset,
         comp.id                                                       AS comp_id,
         comp.time_created                                             AS comp_time_created,
         comp.time_updated                                             AS comp_time_updated,
         comp.name                                                     AS comp_name,
         comp.doing_business_as                                        AS comp_doing_business_as,
         comp.client_code                                              AS comp_client_code,
         comp.client_id                                                AS comp_client_id,
         comp.dataset_code                                             AS comp_dataset_code,
         comp.federal_id_number                                        AS comp_federal_id_number,
         comp.address_id                                               AS address_id,
         comp.address_name                                             AS address_name,
         comp.address_address1                                         AS address_address1,
         comp.address_address2                                         AS address_address2,
         comp.address_city                                             AS address_city,
         comp.address_state                                            AS address_state,
         comp.address_postal_code                                      AS address_postal_code,
         comp.address_latitude                                         AS address_latitude,
         comp.address_longitude                                        AS address_longitude,
         comp.address_country                                          AS address_country,
         comp.address_county                                           AS address_county,
         comp.address_phone                                            AS address_phone,
         comp.address_fax                                              AS address_fax
      FROM audit a
           JOIN company comp ON a.company_id = comp.id AND comp.deleted = FALSE
           JOIN audit_action auditAction ON a.id = auditAction.audit_id
           JOIN audit_status_type_domain astd ON auditAction.status_id = astd.id
           JOIN system_employees_vw auditActionEmployee ON comp.id = auditActionEmployee.comp_id AND auditAction.changed_by = auditActionEmployee.emp_number
           JOIN system_stores_fimvw auditStore ON comp.dataset_code = auditStore.dataset AND a.store_number = auditStore.number
   """

   private fun selectAllBaseQuery(whereClause: String): String =
      """
         WITH audits AS (
            WITH status AS (
               SELECT
                  csastd.value AS current_status,
                  csaa.audit_id AS audit_id, csaa.id
               FROM audit_action csaa JOIN audit_status_type_domain csastd ON csaa.status_id = csastd.id
            ), maxStatus AS (
               SELECT id AS current_status_id, audit_id
               FROM audit_action
               WHERE (status_id, audit_id) IN
                  (
                     SELECT MAX(status_id), audit_id
                     FROM audit_action
                     GROUP BY audit_id
                  )
            )
            SELECT
               a.id AS id,
               a.time_created AS time_created,
               a.time_updated AS time_updated,
               a.store_number AS store_number,
               a.number AS number,
               (SELECT count(id) FROM audit_detail WHERE audit_id = a.id) AS total_details,
               (SELECT count(id) FROM audit_exception WHERE audit_id = a.id) AS total_exceptions,
               (SELECT count(aen.id) > 0
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
               a.company_id AS company_id,
               (SELECT count(a.id)
                FROM audit a
                    JOIN status s ON s.audit_id = a.id
                    JOIN maxStatus ms ON s.id = ms.current_status_id
                    JOIN company comp ON a.company_id = comp.id AND comp.deleted = FALSE
                    JOIN division div ON comp.id = div.company_id AND div.deleted = FALSE
                    JOIN region reg ON div.id = reg.division_id AND reg.deleted = FALSE
                $whereClause) AS total_elements
            FROM audit a
                 JOIN status s ON s.audit_id = a.id
                 JOIN maxStatus ms ON s.id = ms.current_status_id
                 JOIN company comp ON a.company_id = comp.id AND comp.deleted = FALSE
                 JOIN division div ON comp.id = div.company_id AND div.deleted = FALSE
                 JOIN region reg ON div.id = reg.division_id AND reg.deleted = FALSE
            $whereClause
         )
         SELECT
            a.id                                                AS a_id,
            a.time_created                                      AS a_time_created,
            a.time_updated                                      AS a_time_updated,
            a.store_number                                      AS store_number,
            a.number                                            AS a_number,
            a.total_details                                     AS a_total_details,
            a.total_exceptions                                  AS a_total_exceptions,
            $queryAuditCurrentStatus                            AS current_status,
            CASE
            WHEN $queryAuditCurrentStatus IN ('CREATED', 'IN-PROGRESS')
            THEN
               (
               SELECT COUNT (*)
               FROM fastinfo_prod_import.inventory_vw i
               WHERE i.primary_location = a.store_number
                     AND i.location = a.store_number
                     AND i.status in ('N', 'R')
                     AND i.dataset = auditStore.dataset
               )
            ELSE
               (
               SELECT COUNT (*)
               FROM audit_inventory i
               WHERE i.primary_location = a.store_number
                     AND i.location = a.store_number
                     AND i.dataset = auditStore.dataset
                     AND i.audit_id = a.id
               )
            END                                                 AS a_inventory_count,
            a.last_updated                                      AS a_last_updated,
            a.exception_has_notes                               AS a_exception_has_notes,
            auditAction.id                                      AS auditAction_id,
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
            auditStore.id                                       AS auditStore_id,
            auditStore.name                                     AS auditStore_name,
            auditStore.number                                   AS auditStore_number,
            auditStore.dataset                                  AS auditStore_dataset,
            comp.id                                             AS comp_id,
            comp.time_created                                   AS comp_time_created,
            comp.time_updated                                   AS comp_time_updated,
            comp.name                                           AS comp_name,
            comp.doing_business_as                              AS comp_doing_business_as,
            comp.client_code                                    AS comp_client_code,
            comp.client_id                                      AS comp_client_id,
            comp.dataset_code                                   AS comp_dataset_code,
            comp.federal_id_number                              AS comp_federal_id_number,
            comp.address_id                                     AS address_id,
            comp.address_name                                   AS address_name,
            comp.address_address1                               AS address_address1,
            comp.address_address2                               AS address_address2,
            comp.address_city                                   AS address_city,
            comp.address_state                                  AS address_state,
            comp.address_postal_code                            AS address_postal_code,
            comp.address_latitude                               AS address_latitude,
            comp.address_longitude                              AS address_longitude,
            comp.address_country                                AS address_country,
            comp.address_county                                 AS address_county,
            comp.address_phone                                  AS address_phone,
            comp.address_fax                                    AS address_fax,
            total_elements                                      AS total_elements
         FROM audits a
              JOIN company comp ON a.company_id = comp.id
              JOIN division div ON comp.id = div.company_id
              JOIN region reg ON div.id = reg.division_id
              JOIN region_to_store regionStores ON reg.id = regionStores.region_id
              JOIN system_stores_fimvw auditStore ON comp.dataset_code = auditStore.dataset AND a.store_number = auditStore.number
              JOIN audit_action auditAction ON a.id = auditAction.audit_id
              JOIN audit_status_type_domain astd ON auditAction.status_id = astd.id
              JOIN system_employees_vw auditActionEmployee ON comp.id = auditActionEmployee.comp_id AND auditAction.changed_by = auditActionEmployee.emp_number
              JOIN fastinfo_prod_import.store_vw auditStore
                     ON comp.dataset_code = auditStore.dataset
                        AND a.store_number = auditStore.number
               LEFT JOIN region_to_store rts ON rts.store_number = auditStore.number AND rts.company_id = a.company_id
               LEFT JOIN region reg ON reg.id = rts.region_id AND reg.deleted = FALSE
               LEFT JOIN division div ON comp.id = div.company_id AND reg.division_id = div.id AND div.deleted = FALSE
         ORDER BY a.id
      """

   /**
    * The sub-query added to make sure we get unassigned store
    * but won't get the store of region belong to another company
    * which have the same store number
    **/
   private val subQuery =
      """
                           (rts.region_id IS null
                              OR rts.region_id NOT IN (
                                    SELECT region.id
                                    FROM region JOIN division ON region.division_id = division.id AND division.deleted = FALSE
                                    WHERE division.company_id <> :comp_id AND region.deleted = FALSE
                                 ))
   """

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AuditEntity? {
      logger.debug("Searching for audit by id {} with company {}", id, company)

      val params = mutableMapOf<String, Any?>("id" to id)
      val query = "${selectByIdBaseQuery()}\nWHERE a.id = :id"
      val found = executeFindForSingleAudit(query, params)

      logger.trace("Searching for Audit with ID {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findOneCreatedOrInProgress(store: Store): AuditEntity? {
      val params = mutableMapOf("store_number" to store.myNumber(), "current_status" to listOf(CREATED.value, IN_PROGRESS.value))
      val whereClause =
         """ WHERE a.store_number = :store_number
                   AND current_status IN (<current_status>)
         """.trimIndent()
      val query = selectAllBaseQuery(whereClause)

      logger.debug(
         "Searching for one audit in either CREATED or IN_PROGRESS for store {} \n Params {} \n Query {}",
         store,
         params,
         query
      )

      return executeFindForMultipleAudits(query, params).getOrNull(0)
   }

   private fun executeFindForMultipleAudits(query: String, params: MutableMap<String, Any>): List<AuditEntity> {
      val elements = mutableListOf<AuditEntity>()

      logger.trace("{}/{}", query, params)

      jdbc.query(query, params) { rs, _ ->
         var currentId: UUID? = null
         var currentParentEntity: AuditEntity? = null

         do {
            val tempId = rs.getUuid("a_id")
            val tempParentEntity: AuditEntity = if (tempId != currentId) {
               currentId = tempId
               currentParentEntity = mapRow(rs)
               elements.add(currentParentEntity)
               currentParentEntity
            } else {
               currentParentEntity!!
            }
            tempParentEntity.actions.add(mapAuditAction(rs))
         } while (rs.next())
      }

      return elements
   }

   private fun executeFindForSingleAudit(query: String, params: Map<String, Any?>): AuditEntity? {
      logger.trace("Executing find single audit query {}/{}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val audit = this.mapRow(rs)

         do {
            audit.actions.add(mapAuditAction(rs))
         } while (rs.next())

         audit
      }

      if (found != null) {
         loadNextStates(found)
      }

      return found
   }

   @ReadOnly
   fun findAll(pageRequest: AuditPageRequest, user: User): RepositoryPage<AuditEntity, AuditPageRequest> {
      val params = mutableMapOf<String, Any?>(
         "comp_id" to user.myCompany().id,
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset()
      )
      val auditIds = mutableListOf<UUID>()
      val whereClause = StringBuilder(" WHERE a.company_id = :comp_id ")
      val storeNumbers = pageRequest.storeNumber
      val status = pageRequest.status
      val from = pageRequest.from
      val thru = pageRequest.thru

      processAlternativeStoreIndicator(whereClause, params, user, storeNumbers)

      if (from != null && thru != null) {
         params["from"] = from
         params["thru"] = thru
         whereClause.append(" AND a.time_created BETWEEN :from AND :thru ")
      }

      if (!status.isNullOrEmpty()) {
         params["current_status"] = status
         whereClause.append(" AND current_status IN (<current_status>) ")
      }

      whereClause.append(" AND $subQuery ")

      val sql =
         """
         WITH company AS (
            ${companyRepository.companyBaseQuery()}
         ), maxStatus AS (
            SELECT id AS current_status_id, audit_id
               FROM audit_action
               WHERE (status_id, audit_id) IN
                  (
                     SELECT MAX(status_id), audit_id
                     FROM audit_action
                     GROUP BY audit_id
                  )
         ), status AS (
            SELECT
               csastd.value AS current_status,
               csaa.audit_id AS audit_id, csaa.id
            FROM audit_action csaa JOIN audit_status_type_domain csastd ON csaa.status_id = csastd.id
         )
         SELECT
            a.id                                                          AS a_id,
            a.time_created                                                AS a_time_created,
            a.time_updated                                                AS a_time_updated,
            auditStore.id                                                 AS auditStore_id,
            auditStore.number                                             AS auditStore_number,
            auditStore.name                                               AS auditStore_name,
            a.number                                                      AS a_number,
            (SELECT count(id) FROM audit_detail WHERE audit_id = a.id)    AS a_total_details,
            (SELECT count(id) FROM audit_exception WHERE audit_id = a.id) AS a_total_exceptions,
            (SELECT count(aen.id) > 0
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
            )                                                   AS a_last_updated,
            $queryAuditCurrentStatus                            AS a_current_status,
            CASE
            WHEN $queryAuditCurrentStatus IN ('CREATED', 'IN-PROGRESS')
            THEN
               (
               SELECT COUNT (*)
               FROM fastinfo_prod_import.inventory_vw i
               WHERE i.primary_location = a.store_number
                     AND i.location = a.store_number
                     AND i.status in ('N', 'R')
                     AND i.dataset = auditStore.dataset
               )
            ELSE
               (
               SELECT COUNT (*)
               FROM audit_inventory i
               WHERE i.primary_location = a.store_number
                     AND i.location = a.store_number
                     AND i.dataset = auditStore.dataset
                     AND i.audit_id = a.id
               )
            END                                                 AS a_inventory_count,

            comp.id                                             AS comp_id,
            comp.time_created                                   AS comp_time_created,
            comp.time_updated                                   AS comp_time_updated,
            comp.name                                           AS comp_name,
            comp.doing_business_as                              AS comp_doing_business_as,
            comp.client_code                                    AS comp_client_code,
            comp.client_id                                      AS comp_client_id,
            comp.dataset_code                                   AS comp_dataset_code,
            comp.federal_id_number                              AS comp_federal_id_number,
            comp.address_id                                     AS address_id,
            comp.address_name                                   AS address_name,
            comp.address_address1                               AS address_address1,
            comp.address_address2                               AS address_address2,
            comp.address_city                                   AS address_city,
            comp.address_state                                  AS address_state,
            comp.address_postal_code                            AS address_postal_code,
            comp.address_latitude                               AS address_latitude,
            comp.address_longitude                              AS address_longitude,
            comp.address_country                                AS address_country,
            comp.address_county                                 AS address_county,
            comp.address_phone                                  AS address_phone,
            comp.address_fax                                    AS address_fax,
            count(*) OVER() AS total_elements
         FROM audit a
               JOIN company comp ON a.company_id = comp.id AND comp.deleted = FALSE
               JOIN system_stores_fimvw auditStore
                     ON comp.dataset_code = auditStore.dataset
                        AND a.store_number = auditStore.number
               JOIN status s ON s.audit_id = a.id
               JOIN maxStatus ms ON s.id = ms.current_status_id
               LEFT JOIN region_to_store rts ON rts.store_number = auditStore.number AND rts.company_id = a.company_id
               LEFT JOIN region reg ON reg.id = rts.region_id AND reg.deleted = FALSE
               LEFT JOIN division div ON comp.id = div.company_id AND reg.division_id = div.id AND div.deleted = FALSE
         $whereClause
         ORDER BY a_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """.trimIndent()

      logger.trace("Finding all audits using {}\n{}", params, sql)

      val repoPage = jdbc.queryPaged<AuditEntity, AuditPageRequest>(sql, params, pageRequest) { rs, elements ->
         do {
            val audit = mapRow(rs)

            elements.add(audit)

            auditIds.add(audit.id!!)
         } while (rs.next())
      }

      val actions = auditActionRepository.findAll(auditIds)

      return repoPage.copy(
         elements = repoPage.elements.asSequence()
            .onEach { it.actions.addAll(actions.get(it.id!!)) }
            .onEach(this::loadNextStates)
            .toList()
      )
   }

   @ReadOnly
   fun exists(id: UUID): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM audit WHERE id = :id)",
         mapOf("id" to id),
         Boolean::class.java
      )

      logger.trace("Checking if Audit: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: UUID): Boolean = !exists(id)

   @ReadOnly
   fun findAuditStatusCounts(pageRequest: AuditPageRequest, user: User): List<AuditStatusCount> {
      val status = pageRequest.status
      val params = mutableMapOf<String, Any?>("comp_id" to user.myCompany().id)
      val storeNumbers = pageRequest.storeNumber
      val whereClause = StringBuilder("WHERE a.company_id = :comp_id ")
      val from = pageRequest.from
      val thru = pageRequest.thru

      processAlternativeStoreIndicator(whereClause, params, user, storeNumbers)

      if (from != null && thru != null) {
         params["from"] = from
         params["thru"] = thru
         whereClause.append(" AND a.time_created BETWEEN :from AND :thru ")
      }

      if (!status.isNullOrEmpty()) {
         params["statuses"] = status
         whereClause.append(" AND current_status IN (<statuses>) ")
      }

      whereClause.append(" AND $subQuery ")

      val sql =
         """
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
            ),
            maxStatus AS (
               SELECT id AS current_status_id, audit_id
               FROM audit_action
               WHERE (status_id, audit_id) IN
                  (
                     SELECT MAX(status_id), audit_id
                     FROM audit_action
                     GROUP BY audit_id
                  )
            )
         SELECT
            status.current_status_id AS current_status_id,
            status.current_status AS current_status,
            status.current_status_description AS current_status_description,
            status.current_status_localization_code AS current_status_localization_code,
            status.current_status_color AS current_status_color,
            count(*) AS current_status_count
         FROM audit a
            JOIN company comp ON a.company_id = comp.id AND comp.deleted = FALSE
            JOIN system_stores_fimvw auditStore ON comp.dataset_code = auditStore.dataset AND a.store_number = auditStore.number
            JOIN status status ON status.audit_id = a.id
            JOIN maxStatus ms ON status.id = ms.current_status_id
            LEFT JOIN region_to_store rts ON rts.store_number = auditStore.number AND rts.company_id = a.company_id
            LEFT JOIN region reg ON reg.id = rts.region_id AND reg.deleted = FALSE
            LEFT JOIN division div ON comp.id = div.company_id AND reg.division_id = div.id AND div.deleted = FALSE
         $whereClause
         GROUP BY status.current_status,
                  status.current_status_description,
                  status.current_status_localization_code,
                  status.current_status_color,
                  status.current_status_id
         """.trimIndent()

      logger.debug("Loading stats using {}/{}", sql, params)

      return jdbc.query(sql, params) { rs, _ ->
         AuditStatusCount(
            id = rs.getInt("current_status_id"),
            value = rs.getString("current_status"),
            description = rs.getString("current_status_description"),
            localizationCode = rs.getString("current_status_localization_code"),
            color = rs.getString("current_status_color"),
            count = rs.getInt("current_status_count")
         )
      }
   }

   @ReadOnly
   fun countAuditsNotCompletedOrCanceled(storeNumber: Int, company: CompanyEntity): Int =
      jdbc.queryForObject(
         """
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
            WHERE astd.VALUE IN (<values>)
         ) c
         """.trimIndent(),
         mapOf(
            "store_number" to storeNumber,
            "values" to listOf(CREATED.value, IN_PROGRESS.value),
            "comp_id" to company.id
         ),
         Int::class.java
      )

   @Transactional
   fun insert(entity: AuditEntity): AuditEntity {
      logger.debug("Inserting audit {}", entity)

      val audit = jdbc.insertReturning(
         """
         INSERT INTO audit(store_number, company_id)
         VALUES (
            :store_number,
            :company_id
         )
         RETURNING
            *
         """.trimMargin(),
         mapOf(
            "store_number" to entity.store.myNumber(),
            "company_id" to entity.store.myCompany().id
         )
      ) { rs, _ -> mapInsertUpdateAudit(rs, entity) }

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

   private fun mapInsertUpdateAudit(rs: ResultSet, audit: AuditEntity): AuditEntity =
      AuditEntity(
         id = rs.getUuid("id"),
         timeCreated = rs.getOffsetDateTime("time_created"),
         timeUpdated = rs.getOffsetDateTime("time_updated"),
         store = audit.store,
         number = rs.getInt("number"),
         totalDetails = 0,
         totalExceptions = 0,
         hasExceptionNotes = false,
         inventoryCount = 0,
         lastUpdated = null,
         actions = mutableSetOf()
      )

   private fun processAlternativeStoreIndicator(
      whereClause: StringBuilder,
      params: MutableMap<String, Any?>,
      user: User,
      storeNumbers: Set<Int>?
   ) {
      if (!storeNumbers.isNullOrEmpty() && user.myAlternativeStoreIndicator() != "N") {
         params["store_numbers"] = storeNumbers
         whereClause.append(" AND a.store_number IN (<store_numbers>) ")
      }

      when (user.myAlternativeStoreIndicator()) {
         // with value 'A' return all stores
         "N" -> {
            whereClause.append(" AND a.store_number = :store_number ")
            params["store_number"] = user.myLocation().myNumber()
         }
         "R" -> {
            whereClause.append(" AND reg.number = :region_number ")
            params["region_number"] = user.myAlternativeArea()
         }
         "D" -> {
            whereClause.append(" AND div.number = :division_number ")
            params["division_number"] = user.myAlternativeArea()
         }
      }
   }

   private fun mapRow(rs: ResultSet): AuditEntity {
      return AuditEntity(
         id = rs.getUuid("a_id"),
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

   private fun mapStore(rs: ResultSet): Store {
      return StoreEntity(
         id = rs.getLong("auditStore_id"),
         number = rs.getInt("auditStore_number"),
         name = rs.getString("auditStore_name"),
         company = companyRepository.mapRow(rs, "comp_"),
      )
   }

   private fun mapAuditAction(rs: ResultSet): AuditActionEntity {
      return AuditActionEntity(
         id = rs.getUuid("auditAction_id"),
         timeCreated = rs.getOffsetDateTime("auditAction_time_created"),
         timeUpdated = rs.getOffsetDateTime("auditAction_time_updated"),
         status = auditStatusRepository.mapRow(rs, "astd_"),
         changedBy = mapAuditActionEmployee(rs)
      )
   }

   private fun mapAuditActionEmployee(rs: ResultSet): EmployeeEntity {
      return employeeRepository.mapRow(
         rs = rs,
         columnPrefix = "auditActionEmployee_",
         companyColumnPrefix = "comp_",
         departmentColumnPrefix = "auditActionEmployeeDept_",
         storeColumnPrefix = "auditStore_"
      )
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
