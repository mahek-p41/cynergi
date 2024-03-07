package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.area.AreaEntity
import com.cynergisuite.middleware.area.AreaType
import com.cynergisuite.middleware.area.AreaTypeEntity
import com.cynergisuite.middleware.area.toAreaTypeEntity
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.data.annotation.Join
import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.repeatable.JoinSpecifications
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.transaction.Transactional

@JdbcRepository
abstract class AreaRepository @Inject constructor(
   private val jdbc: Jdbi
) : CrudRepository<AreaEntity, UUID> {
   private val logger: Logger = LoggerFactory.getLogger(AreaRepository::class.java)

   @JoinSpecifications(
      Join("areaType")
   )
   abstract fun existsByCompanyAndAreaType(company: CompanyEntity, areaType: AreaTypeEntity): Boolean

   @JoinSpecifications(
      Join("areaType"),
      Join("company")
   )
   abstract fun findByCompanyAndAreaType(company: CompanyEntity, areaType: AreaTypeEntity): AreaEntity?

   @Query(
      """
      SELECT
         a.id                  AS id,
         atd.id                AS area_type_id,
         atd.value             AS area_type_value,
         atd.description       AS area_type_description,
         atd.localization_code AS area_type_localization_code,
         c.id                  AS company_id,
         c.name                AS company_name,
         c.doing_business_as   AS company_doing_business_as,
         c.client_code         AS company_client_code,
         c.client_id           AS company_client_id,
         c.dataset_code        AS company_dataset_code,
         c.federal_id_number   AS company_federal_id_number,
         c.include_demo_inventory AS company_include_demo_inventory,
         add.id                AS company_address_id,
         add.number            AS company_address_number,
         add.name              AS company_address_name,
         add.address1          AS company_address_address1,
         add.address2          AS company_address_address2,
         add.city              AS company_address_city,
         add.state             AS company_address_state,
         add.postal_code       AS company_address_postal_code,
         add.latitude          AS company_address_latitude,
         add.longitude         AS company_address_longitude,
         add.country           AS company_address_country,
         add.county            AS company_address_county,
         add.phone             AS company_address_phone,
         add.fax               AS company_address_fax
      FROM (SELECT
               id,
               value,
               description,
               localization_code
            FROM area_type_domain
            WHERE menu_visible = TRUE) atd
      LEFT OUTER JOIN (
          SELECT id, area_type_id, company_id
          FROM area
          WHERE company_id = :company OR id IS NULL
         ) a ON atd.id = a.area_type_id
      LEFT OUTER JOIN company c ON a.company_id = c.id
      LEFT OUTER JOIN address add ON c.address_id = add.id
      ORDER BY atd.id;
   """,
      nativeQuery = true
   )
   @JoinSpecifications(
      Join("areaType"),
      Join("company"),
      Join("company.address")
   )
   abstract fun findAllVisibleByCompany(company: CompanyEntity): List<AreaEntity>

   @Transactional
   fun enable(company: CompanyEntity, areaTypeId: Int) {
      logger.debug("Enable area {} for company {}", areaTypeId, company.datasetCode)

      jdbc.update(
         """
         INSERT INTO area(company_id, area_type_id)
         VALUES (:company_id, :area_type_id)
         """,
         mapOf(
            "company_id" to company.id,
            "area_type_id" to areaTypeId
         )
      )
   }

   @Transactional
   fun deleteByCompanyAndAreaType(company: CompanyEntity, areaType: AreaTypeEntity) {
      logger.debug("Deleting area {}", areaType)

      jdbc.update(
         """
            DELETE FROM area
            WHERE company_id = :company_id AND area_type_id = :area_type_id
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "area_type_id" to areaType.id
         )
      )
   }

   @Transactional
   fun insert(company: CompanyEntity, areaType: AreaType): AreaEntity {
      logger.debug("Inserting area {}", areaType)

      return jdbc.insertReturning(
         """
         INSERT INTO area(company_id, area_type_id)
         VALUES (:company_id, :area_type_id)
         RETURNING
            *
         """,
         mapOf(
            "company_id" to company.id,
            "area_type_id" to areaType.myId()
         )
      ) { _, _ ->
         AreaEntity(
            areaType = areaType.toAreaTypeEntity(),
            company = company,
         )
      }
   }

   @ReadOnly
   fun exists(areaTypeId: Int): Boolean {
      logger.trace("Checking if Area exists {}")

      val exists = jdbc.queryForObject(
         "SELECT EXISTS (SELECT id FROM area_type_domain WHERE id = :area_type_id)",
         mapOf("area_type_id" to areaTypeId),
         Boolean::class.java
      )

      logger.trace("Area {} existed {}", areaTypeId, exists)

      return exists
   }
}
