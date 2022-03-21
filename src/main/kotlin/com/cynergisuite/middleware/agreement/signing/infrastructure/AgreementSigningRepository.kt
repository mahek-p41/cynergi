package com.cynergisuite.middleware.agreement.signing.infrastructure

import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.agreement.signing.AgreementSigningEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class AgreementSigningRepository(
   private val companyRepository: CompanyRepository,
   private val jdbc: Jdbi,
   private val regionRepository: RegionRepository,
) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(AgreementSigningRepository::class.java)

   private val selectBase =
      """
      SELECT
         asn.id AS asn_id,
         asn.company_id AS asn_company_id,
         asn.store_number_sfk AS asn_store_number_sfk,
         asn.primary_customer_number AS asn_primary_customer_number,
         asn.secondary_customer_number AS asn_secondary_customer_number,
         asn.agreement_number AS asn_agreement_number,
         asn.agreement_type AS asn_agreement_type,
         asn.status_id AS asn_status_id,
         asn.external_signature_id AS asn_external_signature_id,
         comp.id AS comp_id,
         comp.time_created AS comp_time_created,
         comp.time_updated AS comp_time_updated,
         comp.name AS comp_name,
         comp.doing_business_as AS comp_doing_business_as,
         comp.client_code AS comp_client_code,
         comp.client_id AS comp_client_id,
         comp.dataset_code AS comp_dataset_code,
         comp.federal_id_number AS comp_federal_id_number,
         store.id AS store_id,
         store.number AS store_number,
         store.name AS store_name,
         store.dataset AS store_dataset,
         asstd.id AS status_type_id,
         asstd.value AS status_type_value,
         asstd.description AS status_type_description,
         asstd.localization_code AS status_type_localization_code
      FROM agreement_signing asn
           JOIN company comp ON comp.id = asn.company_id
           JOIN system_stores_fimvw store ON comp.dataset_code = store.dataset AND asn.store_number_sfk = store.number
           JOIN agreement_signing_status_type_domain asstd ON asn.status_id = asstd.id
      """.trimIndent()

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AgreementSigningEntity? {
      logger.debug("Finding Agreement Signing record by ID with {}", id)

      val agreementSigningRecord = jdbc.findFirstOrNull(
         """
         $selectBase
         WHERE comp.id = :comp_id
               AND asn.id = :id
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "id" to id
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Search for Agreement Signing record by ID {} produced {}", id, agreementSigningRecord)

      return agreementSigningRecord
   }

   @ReadOnly
   override fun exists(id: Long, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT count(asn.id) > 0
         FROM company comp
              JOIN agreement_signing asn ON comp.dataset_code = asn.dataset
         WHERE asn.id = :asn_id AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("asn_id" to id, "comp_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if Agreement Signing record: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: CompanyEntity): Boolean =
      !exists(id, company)

   @ReadOnly
   fun findAll(
      pageRequest: AgreementSigningPageRequest,
      company: CompanyEntity
   ): RepositoryPage<AgreementSigningEntity, AgreementSigningPageRequest> {
      var totalElements: Long? = null
      val elements = mutableListOf<AgreementSigningEntity>()
      var companyId = company.id
      val params = mutableMapOf<String, Any?>(
         "comp_id" to companyId,
         "limit" to pageRequest.size(),
         "offset" to pageRequest.offset()
      )

      if (!(pageRequest.storeNumber == null)) {
         params["storeNumber"] = pageRequest.storeNumber!!
      }

      if (!(pageRequest.primaryCustomerNumber == null)) {
         params["primary_customer_number"] = pageRequest.primaryCustomerNumber!!
      }

      if (!(pageRequest.agreementNumber == null)) {
         params["agreement_number"] = pageRequest.agreementNumber!!
      }

      val sql =
         """
      WITH paged AS (
         $selectBase
         WHERE comp.id = :comp_id
               ${if (params.containsKey("storeNumber")) "AND asn.store_number_sfk = :storeNumber" else ""}
               ${if (params.containsKey("primary_customer_number")) "AND asn.primary_customer_number = :primary_customer_number" else ""}
               ${if (params.containsKey("agreement_number")) "AND asn.agreement_number = :agreement_number" else ""}
      )
      SELECT
         p.*,
         count(*) OVER() as total_elements
      FROM paged AS p
      ORDER BY ${pageRequest.sortBy} ${pageRequest.sortDirection}
      LIMIT :limit
         OFFSET :offset
         """.trimIndent()

      logger.debug("Querying agreement_signing {} {} {}", pageRequest, params, sql)

      jdbc.query(sql, params) { rs, _ ->
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

   @Transactional
   fun insert(entity: AgreementSigningEntity): AgreementSigningEntity {
      logger.debug("Inserting Agreement Signing record {}", entity)

      val agreement = jdbc.insertReturning(
         """
         INSERT INTO agreement_signing(company_id, store_number_sfk, primary_customer_number, secondary_customer_number, agreement_number, agreement_type, status_id, external_signature_id)
         VALUES (
            :company_id,
            :store_number_sfk,
            :primary_customer_number,
            :secondary_customer_number,
            :agreement_number,
            :agreement_type,
            :status_id,
            :external_signature_id
         )
         RETURNING
            *
         """.trimMargin(),
         mapOf(
            "company_id" to entity.company.id,
            "store_number_sfk" to entity.store.myNumber(),
            "primary_customer_number" to entity.primaryCustomerNumber,
            "secondary_customer_number" to entity.secondaryCustomerNumber,
            "agreement_number" to entity.agreementNumber,
            "agreement_type" to entity.agreementType,
            "status_id" to entity.statusId,
            "external_signature_id" to entity.externalSignatureId
         )
      ) { rs, _ -> mapInsertUpdateAgreementSigning(rs, entity) }

      return agreement
   }

   @Transactional
   fun update(entity: AgreementSigningEntity): AgreementSigningEntity {
      logger.debug("Updating Agreement Signing {}", entity)

      val updated = jdbc.updateReturning(
         """
         UPDATE agreement_signing
         SET
            store_number_sfk = :store_number_sfk,
            status_id = :status_id,
            external_signature_id = :external_signature_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "store_number_sfk" to entity.store.myNumber(),
            "status_id" to entity.statusId,
            "external_signature_id" to entity.externalSignatureId
         )
      ) { rs, _ ->
         mapInsertUpdateAgreementSigning(rs, entity)
      }

      return updated
   }

   private fun mapInsertUpdateAgreementSigning(rs: ResultSet, entity: AgreementSigningEntity): AgreementSigningEntity {

      return AgreementSigningEntity(
         id = rs.getUuid("id"),
         company = entity.company,
         store = entity.store,
         primaryCustomerNumber = rs.getInt("primary_customer_number"),
         secondaryCustomerNumber = rs.getInt("secondary_customer_number"),
         agreementNumber = rs.getInt("agreement_number"),
         agreementType = rs.getString("agreement_type"),
         statusId = rs.getInt("status_id"),
         externalSignatureId = rs.getString("external_signature_id"),
      )
   }

   fun mapRow(rs: ResultSet): AgreementSigningEntity {
      val company = companyRepository.mapRow(rs, "comp_")

      return AgreementSigningEntity(
         id = rs.getUuid("asn_id"),
         company = company,
         store = mapStore(rs, company, "store"),
         primaryCustomerNumber = rs.getInt("asn_primary_customer_number"),
         secondaryCustomerNumber = rs.getInt("asn_secondary_customer_number"),
         agreementNumber = rs.getInt("asn_agreement_number"),
         agreementType = rs.getString("asn_agreement_type"),
         statusId = rs.getInt("asn_status_id"),
         externalSignatureId = rs.getString("asn_external_signature_id"),
      )
   }

   // TODO Not sure the region is going to work right. Does it matter here?
   fun mapStore(rs: ResultSet, company: CompanyEntity, columnPrefix: String = StringUtils.EMPTY): StoreEntity =
      StoreEntity(
         id = rs.getLong("${columnPrefix}_id"),
         number = rs.getInt("${columnPrefix}_number"),
         name = rs.getString("${columnPrefix}_name"),
         region = regionRepository.mapRowOrNull(rs, company, "reg_"),
         company = company,
      )
}
