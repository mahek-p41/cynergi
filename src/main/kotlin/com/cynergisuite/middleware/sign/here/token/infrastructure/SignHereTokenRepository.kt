package com.cynergisuite.middleware.sign.here.token.infrastructure

import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.cynergisuite.middleware.sign.here.token.SignHereTokenEntity
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
class SignHereTokenRepository(
   private val companyRepository: CompanyRepository,
   private val jdbc: Jdbi,
   private val regionRepository: RegionRepository,
) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(SignHereTokenRepository::class.java)

   private val selectBase =
      """
      SELECT
         sht.id AS sht_id,
         sht.company_id AS sht_company_id,
         sht.store_number_sfk AS sht_store_number_sfk,
         sht.token AS sht_token,
         comp.id AS comp_id,
         comp.time_created AS comp_time_created,
         comp.time_updated AS comp_time_updated,
         comp.name AS comp_name,
         comp.doing_business_as AS comp_doing_business_as,
         comp.client_code AS comp_client_code,
         comp.client_id AS comp_client_id,
         comp.dataset_code AS comp_dataset_code,
         comp.federal_id_number AS comp_federal_id_number,
         comp.include_demo_inventory AS comp_include_demo_inventory,
         compAddress.id                AS comp_address_id,
         compAddress.name              AS comp_address_name,
         compAddress.address1          AS comp_address_address1,
         compAddress.address2          AS comp_address_address2,
         compAddress.city              AS comp_address_city,
         compAddress.state             AS comp_address_state,
         compAddress.postal_code       AS comp_address_postal_code,
         compAddress.latitude          AS comp_address_latitude,
         compAddress.longitude         AS comp_address_longitude,
         compAddress.country           AS comp_address_country,
         compAddress.county            AS comp_address_county,
         compAddress.phone             AS comp_address_phone,
         compAddress.fax               AS comp_address_fax,
         store.id AS store_id,
         store.number AS store_number,
         store.name AS store_name,
         store.dataset AS store_dataset
      FROM sign_here_token sht
           JOIN company comp ON comp.id = sht.company_id
           LEFT JOIN address AS compAddress ON comp.address_id = compAddress.id AND compAddress.deleted = FALSE
           JOIN system_stores_fimvw store ON comp.dataset_code = store.dataset AND sht.store_number_sfk = store.number
      """.trimIndent()

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): SignHereTokenEntity? {
      logger.debug("Finding AWS token by ID with {}", id)

      val signHereTokenRecord = jdbc.findFirstOrNull(
         """
         $selectBase
         WHERE comp.id = :comp_id
               AND sht.id = :id
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "id" to id
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Search for AWS token by ID {} produced {}", id, signHereTokenRecord)

      return signHereTokenRecord
   }

   @ReadOnly
   fun findOneByStoreNumber(storeNumber: Int, company: CompanyEntity): SignHereTokenEntity? {
      logger.debug("Finding AWS token by store number {}", storeNumber)

      val signHereTokenRecord = jdbc.findFirstOrNull(
         """
         $selectBase
         WHERE comp.id = :comp_id
               AND sht.store_number_sfk = :store_number_sfk
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "store_number_sfk" to storeNumber
         )
      ) { rs, _ -> mapRow(rs) }

      logger.debug("Search for AWS token by store {} produced {}", storeNumber, signHereTokenRecord)

      return signHereTokenRecord
   }

   @ReadOnly
   override fun exists(id: Long, company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         """
         SELECT count(sht.id) > 0
         FROM company comp
              JOIN sign_here_token sht ON comp.dataset_code = sht.dataset
         WHERE sht.id = :sht_id AND comp.id = :comp_id
         """.trimIndent(),
         mapOf("sht_id" to id, "comp_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if AWS token: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long, company: CompanyEntity): Boolean =
      !exists(id, company)

   @Transactional
   fun insert(entity: SignHereTokenEntity): SignHereTokenEntity {
      logger.debug("Inserting AWS token {}", entity)

      val token = jdbc.insertReturning(
         """
         INSERT INTO sign_here_token(company_id, store_number_sfk, token)
         VALUES (
            :company_id,
            :store_number_sfk,
            :token
         )
         RETURNING
            *
         """.trimMargin(),
         mapOf(
            "company_id" to entity.company.id,
            "store_number_sfk" to entity.store.myNumber(),
            "token" to entity.token
         )
      ) { rs, _ -> mapInsertUpdateSignHereToken(rs, entity) }

      return token
   }

   @Transactional
   fun update(entity: SignHereTokenEntity): SignHereTokenEntity {
      logger.debug("Updating Agreement Signing {}", entity)

      val updated = jdbc.updateReturning(
         """
         UPDATE sign_here_token
         SET
            store_number_sfk = :store_number_sfk,
            token = :token
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "store_number_sfk" to entity.store.myNumber(),
            "token" to entity.token
         )
      ) { rs, _ ->
         mapInsertUpdateSignHereToken(rs, entity)
      }

      return updated
   }

   private fun mapInsertUpdateSignHereToken(rs: ResultSet, entity: SignHereTokenEntity): SignHereTokenEntity {

      return SignHereTokenEntity(
         id = rs.getUuid("id"),
         company = entity.company,
         store = entity.store,
         token = rs.getString("token"),
      )
   }

   fun mapRow(rs: ResultSet): SignHereTokenEntity {
      val company = companyRepository.mapRow(rs, "comp_", addressPrefix = "comp_address_")

      return SignHereTokenEntity(
         id = rs.getUuid("sht_id"),
         company = company,
         store = mapStore(rs, company, "store"),
         token = rs.getString("sht_token"),
      )
   }

   // TODO Not thinking the region is going to work right. Does it matter here?
   fun mapStore(rs: ResultSet, company: CompanyEntity, columnPrefix: String = StringUtils.EMPTY): StoreEntity =
      StoreEntity(
         id = rs.getLong("${columnPrefix}_id"),
         number = rs.getInt("${columnPrefix}_number"),
         name = rs.getString("${columnPrefix}_name"),
         region = regionRepository.mapRowOrNull(rs, company, "reg_"),
         company = company,
      )
}
