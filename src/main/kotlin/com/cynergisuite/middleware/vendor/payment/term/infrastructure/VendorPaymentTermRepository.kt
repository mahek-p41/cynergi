package com.cynergisuite.middleware.vendor.payment.term.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure.VendorPaymentTermScheduleRepository
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
class VendorPaymentTermRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val jdbc: Jdbi,
   private val vendorPaymentTermScheduleRepository: VendorPaymentTermScheduleRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermRepository::class.java)

   fun findOneQuery() =
      """
      WITH company AS (
         ${companyRepository.companyBaseQuery()}
      )
      SELECT
         vpt.id                     AS vpt_id,
         vpt.time_created           AS vpt_time_created,
         vpt.time_updated           AS vpt_time_updated,
         vpt.company_id             AS vpt_company_id,
         vpt.description            AS vpt_description,
         vpt.number                 AS vpt_number,
         vpt.number_of_payments     AS vpt_number_of_payments,
         vpt.discount_month         AS vpt_discount_month,
         vpt.discount_days          AS vpt_discount_days,
         vpt.discount_percent       AS vpt_discount_percent,
         comp.id                    AS comp_id,
         comp.time_created          AS comp_time_created,
         comp.time_updated          AS comp_time_updated,
         comp.name                  AS comp_name,
         comp.doing_business_as     AS comp_doing_business_as,
         comp.client_code           AS comp_client_code,
         comp.client_id             AS comp_client_id,
         comp.dataset_code          AS comp_dataset_code,
         comp.federal_id_number     AS comp_federal_id_number,
         comp.address_id            AS address_id,
         comp.address_name          AS address_name,
         comp.address_address1      AS address_address1,
         comp.address_address2      AS address_address2,
         comp.address_city          AS address_city,
         comp.address_state         AS address_state,
         comp.address_postal_code   AS address_postal_code,
         comp.address_latitude      AS address_latitude,
         comp.address_longitude     AS address_longitude,
         comp.address_country       AS address_country,
         comp.address_county        AS address_county,
         comp.address_phone         AS address_phone,
         comp.address_fax           AS address_fax,
         vpts.id                    AS vpts_id,
         vpts.time_created          AS vpts_time_created,
         vpts.time_updated          AS vpts_time_updated,
         vpts.vendor_payment_term_id       AS vpts_vendor_payment_term_id,
         vpts.due_month             AS vpts_due_month,
         vpts.due_days              AS vpts_due_days,
         vpts.due_percent           AS vpts_due_percent,
         vpts.schedule_order_number AS vpts_schedule_order_number,
         count(*) OVER()            AS total_elements
      FROM vendor_payment_term vpt
           JOIN company comp ON vpt.company_id = comp.id AND comp.deleted = FALSE
           LEFT OUTER JOIN vendor_payment_term_schedule vpts ON vpt.id = vpts.vendor_payment_term_id
   """

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): VendorPaymentTermEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${findOneQuery()}\nWHERE vpt.id = :id AND comp.id = :comp_id AND vpt.deleted = false"

      logger.debug("Searching for VendorPaymentTerm using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val vendorPaymentTerm = mapRow(rs)

         do {
            mapRowVendorPaymentTermSchedule(rs)?.also { vendorPaymentTerm.scheduleRecords.add(it) }
         } while (rs.next())

         vendorPaymentTerm
      }

      logger.trace("Searching for VendorPaymentTerm: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<VendorPaymentTermEntity, PageRequest> {
      val compId = company.id
      val params = mutableMapOf<String, Any?>("comp_id" to compId, "limit" to page.size(), "offset" to page.offset())
      val sql =
         """
         WITH paged AS (
            WITH company AS (
               ${companyRepository.companyBaseQuery()}
            )
            SELECT
               vpt.id                     AS vpt_id,
               vpt.time_created           AS vpt_time_created,
               vpt.time_updated           AS vpt_time_updated,
               vpt.company_id             AS vpt_company_id,
               vpt.description            AS vpt_description,
               vpt.number                 AS vpt_number,
               vpt.number_of_payments     AS vpt_number_of_payments,
               vpt.discount_month         AS vpt_discount_month,
               vpt.discount_days          AS vpt_discount_days,
               vpt.discount_percent       AS vpt_discount_percent,
               comp.id                    AS comp_id,
               comp.time_created          AS comp_time_created,
               comp.time_updated          AS comp_time_updated,
               comp.name                  AS comp_name,
               comp.doing_business_as     AS comp_doing_business_as,
               comp.client_code           AS comp_client_code,
               comp.client_id             AS comp_client_id,
               comp.dataset_code          AS comp_dataset_code,
               comp.federal_id_number     AS comp_federal_id_number,
               comp.address_id            AS address_id,
               comp.address_name          AS address_name,
               comp.address_address1      AS address_address1,
               comp.address_address2      AS address_address2,
               comp.address_city          AS address_city,
               comp.address_state         AS address_state,
               comp.address_postal_code   AS address_postal_code,
               comp.address_latitude      AS address_latitude,
               comp.address_longitude     AS address_longitude,
               comp.address_country       AS address_country,
               comp.address_county        AS address_county,
               comp.address_phone         AS address_phone,
               comp.address_fax           AS address_fax,
               count(*) OVER () AS total_elements
            FROM vendor_payment_term vpt
               JOIN company comp ON vpt.company_id = comp.id AND comp.deleted = FALSE
            WHERE comp.id = :comp_id AND vpt.deleted = false
            ORDER BY vpt_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         )
         SELECT
            p.*,
            vpts.id                    AS vpts_id,
            vpts.time_created          AS vpts_time_created,
            vpts.time_updated          AS vpts_time_updated,
            vpts.vendor_payment_term_id       AS vpts_vendor_payment_term_id,
            vpts.due_month             AS vpts_due_month,
            vpts.due_days              AS vpts_due_days,
            vpts.due_percent           AS vpts_due_percent,
            vpts.schedule_order_number AS vpts_schedule_order_number
         FROM paged AS p
            LEFT OUTER JOIN vendor_payment_term_schedule vpts ON p.vpt_id = vpts.vendor_payment_term_id
         ORDER BY vpt_${page.snakeSortBy()}, vpts.id ${page.sortDirection()}
      """

      logger.debug("find all vendor payment terms {}/{}", sql, params)

      return jdbc.queryPaged(sql, params, page) { rs, elements ->
         var currentId = UUID.randomUUID()
         var currentParentEntity: VendorPaymentTermEntity? = null

         do {
            val tempId = rs.getUuid("vpt_id")
            val tempParentEntity: VendorPaymentTermEntity = if (tempId != currentId) {
               currentId = tempId
               currentParentEntity = mapRow(rs)
               elements.add(currentParentEntity)
               currentParentEntity
            } else {
               currentParentEntity!!
            }
            mapRowVendorPaymentTermSchedule(rs)?.also { tempParentEntity.scheduleRecords.add(it) }
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: VendorPaymentTermEntity): VendorPaymentTermEntity {
      logger.debug("Inserting VendorPaymentTerm {}", entity)

      val inserted = jdbc.insertReturning(
         """
         INSERT INTO vendor_payment_term(company_id, description, number_of_payments, discount_month, discount_days, discount_percent)
         VALUES (
            :company_id,
            :description,
            :number_of_payments,
            :discount_month,
            :discount_days,
            :discount_percent
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company.id,
            "description" to entity.description,
            "number_of_payments" to entity.scheduleRecords.size,
            "discount_month" to entity.discountMonth,
            "discount_days" to entity.discountDays,
            "discount_percent" to entity.discountPercent
         )
      ) { rs, _ -> mapDdlRow(rs, entity.company) }

      // The below will loop through each schedule record to first insert it into vendor_payment_term_schedule,
      // and then add each to the mutable list on VendorPaymentTermEntity.
      entity.scheduleRecords
         .map { vendorPaymentTermScheduleRepository.upsert(it, inserted) }
         .forEach { inserted.scheduleRecords.add(it) }

      logger.debug("Inserted VendorPaymentTerm {}", inserted)

      return inserted
   }

   @Transactional
   fun update(entity: VendorPaymentTermEntity): VendorPaymentTermEntity {
      logger.debug("Updating VendorPaymentTerm {}", entity)

      val existing = findOne(entity.id!!, entity.company)

      vendorPaymentTermScheduleRepository.deleteNotIn(entity.id, entity.scheduleRecords)

      val updated = jdbc.updateReturning(
         """
         UPDATE vendor_payment_term
         SET
            company_id = :companyId,
            description = :description,
            number_of_payments = :numberOfPayments,
            discount_month = :discountMonth,
            discount_days = :discountDays,
            discount_percent = :discountPercent
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "companyId" to entity.company.id,
            "description" to entity.description,
            "numberOfPayments" to entity.scheduleRecords.size,
            "discountMonth" to entity.discountMonth,
            "discountDays" to entity.discountDays,
            "discountPercent" to entity.discountPercent
         )
      ) { rs, _ -> mapDdlRow(rs, entity.company) }

      // The below will loop through each schedule record to first update or delete it in vendor_payment_term_schedule,
      // and then add each to the mutable list on VendorPaymentTermEntity.
      entity.scheduleRecords
         .map { vendorPaymentTermScheduleRepository.upsert(it, updated) }
         .forEach { updated.scheduleRecords.add(it) }

      logger.debug("Updated VendorPaymentTerm {}", updated)

      return updated
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting VendorPaymentTerm with id={}", id)

      vendorPaymentTermScheduleRepository.deleteByVendorPaymentTerm(id)

      val affectedRows = jdbc.softDelete(
         """
         UPDATE vendor_payment_term
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id
         """,
         mapOf("id" to id, "company_id" to company.id),
         "vendor_payment_term"
      )

      logger.info("Affected rows: {}", affectedRows)

      if (affectedRows == 0) throw NotFoundException(id)
   }

   private fun mapRow(rs: ResultSet): VendorPaymentTermEntity {
      return VendorPaymentTermEntity(
         id = rs.getUuid("vpt_id"),
         company = companyRepository.mapRow(rs, "comp_"),
         description = rs.getString("vpt_description"),
         discountMonth = rs.getIntOrNull("vpt_discount_month"),
         discountDays = rs.getIntOrNull("vpt_discount_days"),
         discountPercent = rs.getBigDecimal("vpt_discount_percent")
      )
   }

   fun mapRow(rs: ResultSet, columnPrefix: String = EMPTY): VendorPaymentTermEntity {
      return VendorPaymentTermEntity(
         id = rs.getUuid("${columnPrefix}vpt_id"),
         company = companyRepository.mapRow(rs, "${columnPrefix}comp_", "${columnPrefix}address_"),
         description = rs.getString("${columnPrefix}vpt_description"),
         discountMonth = rs.getIntOrNull("${columnPrefix}vpt_discount_month"),
         discountDays = rs.getIntOrNull("${columnPrefix}vpt_discount_days"),
         discountPercent = rs.getBigDecimal("${columnPrefix}vpt_discount_percent")
      )
   }

   private fun mapDdlRow(rs: ResultSet, company: CompanyEntity): VendorPaymentTermEntity {
      return VendorPaymentTermEntity(
         id = rs.getUuid("id"),
         company = company,
         description = rs.getString("description"),
         discountMonth = rs.getIntOrNull("discount_month"),
         discountDays = rs.getIntOrNull("discount_days"),
         discountPercent = rs.getBigDecimal("discount_percent")
      )
   }

   private fun mapRowVendorPaymentTermSchedule(rs: ResultSet): VendorPaymentTermScheduleEntity? =
      if (rs.getString("vpts_id") != null) {
         VendorPaymentTermScheduleEntity(
            id = rs.getUuid("vpts_id"),
            dueMonth = rs.getIntOrNull("vpts_due_month"),
            dueDays = rs.getInt("vpts_due_days"),
            duePercent = rs.getBigDecimal("vpts_due_percent"),
            scheduleOrderNumber = rs.getInt("vpts_schedule_order_number")
         )
      } else {
         null
      }
}
