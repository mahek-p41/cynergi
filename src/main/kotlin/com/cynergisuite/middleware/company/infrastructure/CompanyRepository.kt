package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.lang.StringBuilder
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(CompanyRepository::class.java)
   private val simpleCompanyRowMapper = CompanyRowMapper()

   fun companyBaseQuery() = """
      SELECT
         comp.id                  AS id,
         comp.uu_row_id           AS uu_row_id,
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

   fun findCompanyByStore(store: Store): CompanyEntity? {
      logger.debug("Search for company using store id {}", store.myId())

      val found = jdbc.findFirstOrNull("""
         SELECT
           comp.id                  AS id,
           comp.uu_row_id           AS uu_row_id,
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
            "dataset" to store.myCompany().myDataset()
         ),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.debug("Searching for company by store id {} resulted in {}", store.myId(), found)

      return found
   }

   fun findOne(id: Long): CompanyEntity? {
      val found = jdbc.findFirstOrNull("${companyBaseQuery()} WHERE id = :id", mapOf("id" to id), simpleCompanyRowMapper)

      logger.trace("Searching for Company: {} resulted in {}", id, found)

      return found
   }

   fun findByDataset(datasetCode: String): CompanyEntity? {
      val found = jdbc.findFirstOrNull("${companyBaseQuery()} WHERE dataset_code = :dataset_code", mapOf("dataset_code" to datasetCode), simpleCompanyRowMapper)

      logger.trace("Searching for Company: {} resulted in {}", datasetCode, found)

      return found
   }

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
      ) { rs ->
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

   fun exists(id: Long? = null): Boolean {
      if (id == null) return false

      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM company WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Company: {} exists resulted in {}", id, exists)

      return exists
   }

   fun duplicate(id: Long? = null, clientId: Int? = null, datasetCode: String? = null): Boolean {
      if (clientId == null && datasetCode == null) return false
      var and = EMPTY
      val params = mapOf<String, Any?>("id" to id, "clientId" to clientId, "datasetCode" to datasetCode)
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

      val exists = jdbc.queryForObject(query.toString(), params, Boolean::class.java)!!

      logger.trace("Checking if Company clientId or datasetCode duplicate, result: {}", exists)

      return exists
   }

   @Transactional
   fun insert(company: CompanyEntity): CompanyEntity {
      logger.debug("Inserting company {}", company)

      return jdbc.insertReturning("""
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
            "federal_id_number" to company.federalIdNumber
         ),
         RowMapper { rs, _ -> mapRow(rs) }
      )
   }

   @Transactional
   fun update(company: CompanyEntity): CompanyEntity {
      logger.debug("Updating company {}", company)

      return jdbc.updateReturning("""
         UPDATE company
         SET
            name = :name,
            doing_business_as = :doing_business_as,
            client_code = :client_code,
            client_id = :client_id,
            dataset_code = :dataset_code,
            federal_id_number = :federal_id_number
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to company.id,
            "name" to company.name,
            "doing_business_as" to company.doingBusinessAs,
            "client_code" to company.clientCode,
            "client_id" to company.clientId,
            "dataset_code" to company.datasetCode,
            "federal_id_number" to company.federalIdNumber
         ),
         RowMapper { rs, _ ->
            mapRow(rs)
         }
      )
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): CompanyEntity =
      simpleCompanyRowMapper.mapRow(rs, columnPrefix)
}

private class CompanyRowMapper : RowMapper<CompanyEntity> {
   override fun mapRow(rs: ResultSet, rowNum: Int): CompanyEntity =
      mapRow(rs, EMPTY)

   fun mapRow(rs: ResultSet, columnPrefix: String): CompanyEntity =
      CompanyEntity(
         id = rs.getLong("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         doingBusinessAs = rs.getString("${columnPrefix}doing_business_as"),
         clientCode = rs.getString("${columnPrefix}client_code"),
         clientId = rs.getInt("${columnPrefix}client_id"),
         datasetCode = rs.getString("${columnPrefix}dataset_code"),
         federalIdNumber = rs.getString("${columnPrefix}federal_id_number")
      )
}
