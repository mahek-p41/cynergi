package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.cache.annotation.Cacheable
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
class CompanyRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(CompanyRepository::class.java)
   private val simpleCompanyRowMapper = CompanyRowMapper()

   @Language("PostgreSQL")
   final val selectBase = """
      SELECT
         c.id                  AS id,
         c.uu_row_id           AS uu_row_id,
         c.time_created        AS time_created,
         c.time_updated        AS time_updated,
         c.name                AS name,
         c.doing_business_as   AS doing_business_as,
         c.client_code         AS client_code,
         c.client_id           AS client_id,
         c.dataset_code        AS dataset_code,
         c.federal_tax_number  AS federal_tax_number
      FROM company c
   """

   fun findCompanyByStore(store: StoreEntity): CompanyEntity? {
      logger.debug("Search for company using store id {}", store.id)

      val found = jdbc.findFirstOrNull("""
         SELECT
           c.id                  AS id,
           c.uu_row_id           AS uu_row_id,
           c.time_created        AS time_created,
           c.time_updated        AS time_updated,
           c.name                AS name,
           c.doing_business_as   AS doing_business_as,
           c.client_code         AS client_code,
           c.client_id           AS client_id,
           c.dataset_code        AS dataset_code,
           c.federal_tax_number  AS federal_tax_number
         FROM company c
              JOIN fastinfo_prod_import.store_vw s
                ON c.dataset_code = s.dataset
         WHERE s.id = :store_id
               AND s.dataset = :dataset
         """,
         mapOf(
            "store_id" to store.id,
            "dataset" to store.myCompany().myDataset()
         ),
         RowMapper { rs, _ -> mapRow(rs) }
      )

      logger.debug("Searching for company by store id {} resulted in {}", store.id, found)

      return found
   }

   @Cacheable("company-cache")
   fun findOne(id: Long): CompanyEntity? {
      val found = jdbc.findFirstOrNull("$selectBase WHERE id = :id", mapOf("id" to id), simpleCompanyRowMapper)

      logger.trace("Searching for Company: {} resulted in {}", id, found)

      return found
   }

   fun findByDataset(datasetCode: String): CompanyEntity? {
      val found = jdbc.findFirstOrNull("$selectBase WHERE dataset_code = :dataset_code", mapOf("dataset_code" to datasetCode), simpleCompanyRowMapper)

      logger.trace("Searching for Company: {} resulted in {}", datasetCode, found)

      return found
   }

   fun findAll(pageRequest: PageRequest): RepositoryPage<CompanyEntity, PageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<CompanyEntity>()

      jdbc.query(
         """
         WITH paged AS (
            $selectBase
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

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM company WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Company: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

   fun doesNotExist(company: Company): Boolean {
      val companyId = company.myId()

      return if (companyId != null) {
         !exists(companyId)
      } else {
         false
      }
   }

   fun insert(company: CompanyEntity): CompanyEntity {
      logger.debug("Inserting company {}", company)

      return jdbc.insertReturning("""
         INSERT INTO company(name, doing_business_as, client_code, client_id, dataset_code, federal_tax_number)
         VALUES (:name, :doing_business_as, :client_code, :client_id, :dataset_code, :federal_tax_number)
         RETURNING
            *
         """,
         mapOf(
            "name" to company.name,
            "doing_business_as" to company.doingBusinessAs,
            "client_code" to company.clientCode,
            "client_id" to company.clientId,
            "dataset_code" to company.datasetCode,
            "federal_tax_number" to company.federalTaxNumber
         ),
         RowMapper { rs, _ -> mapRow(rs) }
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
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         name = rs.getString("${columnPrefix}name"),
         doingBusinessAs = rs.getString("${columnPrefix}doing_business_as"),
         clientCode = rs.getString("${columnPrefix}client_code"),
         clientId = rs.getInt("${columnPrefix}client_id"),
         datasetCode = rs.getString("${columnPrefix}dataset_code"),
         federalTaxNumber = rs.getString("${columnPrefix}federal_tax_number")
      )
}
