package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.Store
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class CompanyRepository @Inject constructor(
   private val addressRepository: AddressRepository,
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
         comp.federal_id_number   AS federal_id_number,
         comp.deleted             AS deleted,
         address.id               AS address_id,
         address.name             AS address_name,
         address.address1         AS address_address1,
         address.address2         AS address_address2,
         address.city             AS address_city,
         address.state            AS address_state,
         address.postal_code      AS address_postal_code,
         address.latitude         AS address_latitude,
         address.longitude        AS address_longitude,
         address.country          AS address_country,
         address.county           AS address_county,
         address.phone            AS address_phone,
         address.fax              AS address_fax
      FROM company comp
         LEFT JOIN address ON comp.address_id = address.id AND address.deleted = FALSE
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
           comp.federal_id_number   AS federal_id_number,
           comp.deleted             As deleted,
           address.id               AS address_id,
           address.name             AS address_name,
           address.address1         AS address_address1,
           address.address2         AS address_address2,
           address.city             AS address_city,
           address.state            AS address_state,
           address.postal_code      AS address_postal_code,
           address.latitude         AS address_latitude,
           address.longitude        AS address_longitude,
           address.country          AS address_country,
           address.county           AS address_county,
           address.phone            AS address_phone,
           address.fax              AS address_fax
         FROM company comp
            JOIN system_stores_fimvw s
               ON comp.dataset_code = s.dataset
            LEFT JOIN address ON comp.address_id = address.id AND address.deleted = FALSE
         WHERE s.id = :store_id
               AND s.dataset = :dataset
         """,
         mapOf(
            "store_id" to store.myId(),
            "dataset" to store.myCompany().datasetCode
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Searching for company by store id {} resulted in {}", store.myId(), found)

      return found
   }

   @ReadOnly
   fun findOne(id: UUID): CompanyEntity? {
      val found =
         jdbc.findFirstOrNull("${companyBaseQuery()} WHERE comp.id = :id AND comp.deleted = FALSE", mapOf("id" to id)) { rs, _ -> mapRow(rs) }

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
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM (${companyBaseQuery()} WHERE comp.deleted = FALSE) AS p
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
   fun all(): Sequence<CompanyEntity> {
      var result = findAll(StandardPageRequest(page = 1, size = 100, sortBy = "id", sortDirection = "ASC"))

      return sequence {
         while (result.elements.isNotEmpty()) {
            yieldAll(result.elements)

            result = findAll(result.requested.nextPage())
         }
      }
   }

   @ReadOnly
   fun exists(id: UUID? = null): Boolean {
      if (id == null) return false

      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM company WHERE id = :id AND company.deleted = FALSE)",
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
         query.append(and).append(" client_id = :clientId AND deleted = FALSE ")
      } else if (datasetCode != null) {
         query.append(and).append(" dataset_code = :datasetCode AND deleted = FALSE ")
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

      val addressCreated = if (company.address != null) {
         addressRepository.save(company.address)
      } else {
         null
      }

      return jdbc.insertReturning(
         """
         INSERT INTO company(name, doing_business_as, client_code, client_id, dataset_code, federal_id_number, address_id)
         VALUES (:name, :doing_business_as, :client_code, :client_id, :dataset_code, :federal_id_number, :address_id)
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
            "address_id" to addressCreated?.id
         )
      ) { rs, _ -> mapRow(rs, addressCreated) }
   }

   @Transactional
   fun update(existing: CompanyEntity, toUpdate: CompanyEntity): CompanyEntity {
      logger.debug("Updating company {}", toUpdate)
      var addressToDelete: AddressEntity? = null

      val companyAddress = if (existing.address?.id != null && toUpdate.address == null) {
         addressToDelete = existing.address

         null
      } else if (toUpdate.address != null) {
         addressRepository.upsert(toUpdate.address)
      } else {
         null
      }

      val updatedCompany = jdbc.updateReturning(
         """
         UPDATE company
         SET
            name = :name,
            doing_business_as = :doing_business_as,
            client_code = :client_code,
            client_id = :client_id,
            dataset_code = :dataset_code,
            federal_id_number = :federal_id_number,
            address_id = :address_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to toUpdate.id,
            "name" to toUpdate.name,
            "doing_business_as" to toUpdate.doingBusinessAs,
            "client_code" to toUpdate.clientCode,
            "client_id" to toUpdate.clientId,
            "dataset_code" to toUpdate.datasetCode,
            "federal_id_number" to toUpdate.federalIdNumber,
            "address_id" to companyAddress?.id
         )
      ) { rs, _ ->
         mapRow(rs, companyAddress)
      }

      addressToDelete?.let { addressRepository.deleteById(it.id!!) } // delete address if it exists, done this way because it avoids the race condition compilation error

      return updatedCompany
   }

   @Transactional
   fun delete(id: UUID) {
      logger.debug("Deleting Company with id={}", id)

      val rowsAffected =jdbc.softDelete(
         """
            UPDATE company
            SET deleted = TRUE
            WHERE id = :id AND deleted = FALSE
         """,
         mapOf("id" to id),
         "company"
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY, addressPrefix: String = "address_"): CompanyEntity =
      CompanyEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         doingBusinessAs = rs.getString("${columnPrefix}doing_business_as"),
         clientCode = rs.getString("${columnPrefix}client_code"),
         clientId = rs.getInt("${columnPrefix}client_id"),
         datasetCode = rs.getString("${columnPrefix}dataset_code"),
         federalIdNumber = rs.getString("${columnPrefix}federal_id_number"),
         address = addressRepository.mapAddressOrNull(rs, addressPrefix)
      )

   fun mapRow(rs: ResultSet, address: AddressEntity?, columnPrefix: String = EMPTY): CompanyEntity =
      CompanyEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         doingBusinessAs = rs.getString("${columnPrefix}doing_business_as"),
         clientCode = rs.getString("${columnPrefix}client_code"),
         clientId = rs.getInt("${columnPrefix}client_id"),
         datasetCode = rs.getString("${columnPrefix}dataset_code"),
         federalIdNumber = rs.getString("${columnPrefix}federal_id_number"),
         address = address
      )
}
