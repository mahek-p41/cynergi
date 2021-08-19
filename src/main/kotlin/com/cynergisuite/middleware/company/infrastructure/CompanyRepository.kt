package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class CompanyRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(CompanyRepository::class.java)

   fun companyBaseQuery() =
      """
      SELECT
         comp.id                  AS id,
         comp.time_created        AS time_created,
         comp.time_updated        AS time_updated,
         comp.name                AS name,
         comp.doing_business_as   AS doing_business_as,
         comp.client_code         AS client_code,
         comp.client_id           AS client_id,
         comp.dataset_code        AS dataset_code,
         comp.federal_id_number   AS federal_id_number
      FROM company comp
   """

   @ReadOnly
   fun findCompanyByStore(store: Store): CompanyEntity? {
      logger.debug("Search for company using store id {}", store.myId())

      val found = jdbc.findFirstOrNull(
         """
         SELECT
           comp.id                  AS id,
           comp.time_created        AS time_created,
           comp.time_updated        AS time_updated,
           comp.name                AS name,
           comp.doing_business_as   AS doing_business_as,
           comp.client_code         AS client_code,
           comp.client_id           AS client_id,
           comp.dataset_code        AS dataset_code,
           comp.federal_id_number  AS federal_id_number
         FROM company comp
              JOIN fastinfo_prod_import.store_vw s
                ON comp.dataset_code = s.dataset
         WHERE s.id = :store_id
               AND s.dataset = :dataset
         """,
         mapOf(
            "store_id" to store.myId(),
            "dataset" to store.myCompany().datasetCode
         ),
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Searching for company by store id {} resulted in {}", store.myId(), found)

      return found
   }

   @ReadOnly
   fun findOne(id: UUID): CompanyEntity? {
      val found =
         jdbc.findFirstOrNull("${companyBaseQuery()} WHERE comp.id = :id", mapOf("id" to id)) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for Company: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findByDataset(datasetCode: String): CompanyEntity? {
      val found = jdbc.findFirstOrNull(
         "${companyBaseQuery()} WHERE dataset_code = :dataset_code",
         mapOf("dataset_code" to datasetCode)
      ) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for Company: {} resulted in {}", datasetCode, found)

      return found
   }

   @ReadOnly
   fun findAll(pageRequest: PageRequest): RepositoryPage<CompanyEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<CompanyEntity>()

      jdbc.query(
         """
         WITH paged AS (
            ${companyBaseQuery()}
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT ${pageRequest.size()}
            OFFSET ${pageRequest.offset()}
         """,
         emptyMap<String, Any>()
      ) { rs, _ ->
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }

         elements.add(mapRow(rs))
      }

      return RepositoryPage(
         requested = pageRequest,
         elements = elements,
         totalElements = totalElements ?: 0
      )
   }

   @ReadOnly
   fun forEach(callback: (CompanyEntity) -> Unit) {
      var result = findAll(StandardPageRequest(page = 1, size = 100, sortBy = "id", sortDirection = "ASC"))

      while (result.elements.isNotEmpty()) {
         for (company in result.elements) {
            callback(company)
         }

         result = findAll(result.requested.nextPage())
      }
   }

   @ReadOnly
   fun exists(id: UUID? = null): Boolean {
      if (id == null) return false

      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM company WHERE id = :id)",
         mapOf("id" to id),
         Boolean::class.java
      )

      logger.trace("Checking if Company: {} exists resulted in {}", id, exists)

      return exists
   }

   @ReadOnly
   fun duplicate(id: UUID? = null, clientId: Int? = null, datasetCode: String? = null): Boolean {
      if (clientId == null && datasetCode == null) return false
      var and = EMPTY
      val params = mapOf("id" to id, "clientId" to clientId, "datasetCode" to datasetCode)
      val query = StringBuilder("SELECT EXISTS(SELECT id FROM company WHERE ")

      if (id != null) {
         query.append(and).append(" id <> :id ")
         and = " AND "
      }

      if (clientId != null) {
         query.append(and).append(" client_id = :clientId ")
      } else if (datasetCode != null) {
         query.append(and).append(" dataset_code = :datasetCode ")
      }

      query.append(")")

      logger.trace("Checking if Company clientId or datasetCode duplicate with query:\n{}\nparams:\n{}", query, params)

      val exists = jdbc.queryForObject(query.toString(), params, Boolean::class.java)

      logger.trace("Checking if Company clientId or datasetCode duplicate, result: {}", exists)

      return exists
   }

   @Transactional
   fun insert(company: CompanyEntity): CompanyEntity {
      logger.debug("Inserting company {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO company(name, doing_business_as, client_code, client_id, dataset_code, federal_id_number)
         VALUES (:name, :doing_business_as, :client_code, :client_id, :dataset_code, :federal_id_number)
         RETURNING
            *
         """,
         mapOf(
            "name" to company.name,
            "doing_business_as" to company.doingBusinessAs,
            "client_code" to company.clientCode,
            "client_id" to company.clientId,
            "dataset_code" to company.datasetCode,
            "federal_id_number" to company.federalIdNumber,
         )
      ) { rs, _ -> mapRow(rs) }
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): CompanyEntity =
      CompanyEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         doingBusinessAs = rs.getString("${columnPrefix}doing_business_as"),
         clientCode = rs.getString("${columnPrefix}client_code"),
         clientId = rs.getInt("${columnPrefix}client_id"),
         datasetCode = rs.getString("${columnPrefix}dataset_code"),
         federalIdNumber = rs.getString("${columnPrefix}federal_id_number")
      )
}
