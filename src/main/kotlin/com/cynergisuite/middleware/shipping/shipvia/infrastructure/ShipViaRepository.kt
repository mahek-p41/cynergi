package com.cynergisuite.middleware.shipping.shipvia.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
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
class ShipViaRepository @Inject constructor(
   private val addressRepository: AddressRepository,
   private val companyRepository: CompanyRepository,
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaRepository::class.java)
   fun baseSelectQuery() =
      """
      WITH company AS (
         ${companyRepository.companyBaseQuery()}
      )
      SELECT
         shipVia.id                    AS id,
         shipVia.time_created          AS time_created,
         shipVia.time_updated          AS time_updated,
         shipVia.description           AS description,
         shipVia.number                AS number,
         comp.id                       AS comp_id,
         comp.name                     AS comp_name,
         comp.doing_business_as        AS comp_doing_business_as,
         comp.client_code              AS comp_client_code,
         comp.client_id                AS comp_client_id,
         comp.dataset_code             AS comp_dataset_code,
         comp.federal_id_number        AS comp_federal_id_number,
         comp.address_id               AS address_id,
         comp.address_name             AS address_name,
         comp.address_address1         AS address_address1,
         comp.address_address2         AS address_address2,
         comp.address_city             AS address_city,
         comp.address_state            AS address_state,
         comp.address_postal_code      AS address_postal_code,
         comp.address_latitude         AS address_latitude,
         comp.address_longitude        AS address_longitude,
         comp.address_country          AS address_country,
         comp.address_county           AS address_county,
         comp.address_phone            AS address_phone,
         comp.address_fax              AS address_fax,
         count(*) OVER()               AS total_elements
      FROM ship_via shipVia
           JOIN company comp ON shipVia.company_id = comp.id AND comp.deleted = FALSE
   """

   @ReadOnly fun findOne(id: UUID, company: CompanyEntity): ShipViaEntity? {
      logger.debug("Searching for ShipVia by id {}", id)

      val found = jdbc.findFirstOrNull("${baseSelectQuery()} WHERE shipVia.id = :id AND comp.id = :comp_id AND shipVia.deleted = FALSE", mapOf("id" to id, "comp_id" to company.id)) { rs, _ -> mapRow(rs) }

      logger.trace("Searching for ShipVia: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findAll(pageRequest: PageRequest, company: CompanyEntity): RepositoryPage<ShipViaEntity, PageRequest> {
      return jdbc.queryPaged(
         """
         ${baseSelectQuery()}
         WHERE comp.id = :comp_id AND shipVia.deleted = FALSE
         ORDER BY shipVia.${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         ),
         pageRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs))
         } while (rs.next())
      }
   }

   @ReadOnly fun exists(id: Long, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM ship_via WHERE id = :id AND company_id = :comp_id AND shipVia.deleted = FALSE)",
         mapOf("id" to id, "comp_id" to company.id), Boolean::class.java
      )

      logger.trace("Checking if ShipVia: {}/{} exists resulted in {}", id, company, exists)

      return exists
   }

   @ReadOnly fun exists(description: String, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
            SELECT EXISTS(SELECT id FROM ship_via
            WHERE UPPER(description) = UPPER(:description) AND company_id = :comp_id AND deleted = FALSE)""",
         mapOf("description" to description, "comp_id" to company.id), Boolean::class.java
      )

      logger.trace("Checking if ShipVia: {}/{} exists resulted in {}", description, company, exists)

      return exists
   }

   @Transactional
   fun insert(entity: ShipViaEntity): ShipViaEntity {
      logger.debug("Inserting shipVia {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO ship_via(description, company_id)
         VALUES (
            :description,
            :comp_id
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf<String, Any?>(
            "description" to entity.description,
            "comp_id" to entity.company.id
         )
      ) { rs, _ ->
         ShipViaEntity(
            id = rs.getUuid("id"),
            description = rs.getString("description"),
            number = rs.getInt("number"),
            company = entity.company
         )
      }
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting account with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE ship_via
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "ship_via"
      )

      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) {
         throw NotFoundException(id)
      }
   }

   @Transactional
   fun update(entity: ShipViaEntity): ShipViaEntity {
      logger.debug("Updating shipVia {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE ship_via
         SET
            description = :description
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "description" to entity.description,
            "number" to entity.number
         )
      ) { rs, _ ->
         ShipViaEntity(
            id = rs.getUuid("id"),
            description = rs.getString("description"),
            number = rs.getInt("number"),
            company = entity.company
         )
      }
   }

   private fun mapRow(rs: ResultSet): ShipViaEntity {
      return ShipViaEntity(
         id = rs.getUuid("id"),
         description = rs.getString("description"),
         number = rs.getInt("number"),
         company = CompanyEntity(
            id = rs.getUuid("comp_id"),
            name = rs.getString("comp_name"),
            doingBusinessAs = rs.getString("comp_doing_business_as"),
            clientCode = rs.getString("comp_client_code"),
            clientId = rs.getInt("comp_client_id"),
            datasetCode = rs.getString("comp_dataset_code"),
            federalIdNumber = rs.getString("comp_federal_id_number"),
            address = addressRepository.mapAddressOrNull(rs, "address_")
         )
      )
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): ShipViaEntity {
      return ShipViaEntity(
         id = rs.getUuid("${columnPrefix}id"),
         description = rs.getString("${columnPrefix}description"),
         number = rs.getInt("${columnPrefix}number"),
         company = companyRepository.mapRow(rs, "${columnPrefix}comp_", "${columnPrefix}address_")
      )
   }
}
