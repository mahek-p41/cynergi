package com.cynergisuite.middleware.vendor.payment.term.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.schedule.infrastructure.SchedulePageRequest
import com.cynergisuite.middleware.schedule.type.ScheduleType
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.VendorPaymentTermScheduleEntity
import com.cynergisuite.middleware.vendor.payment.term.schedule.infrastructure.VendorPaymentTermScheduleRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorPaymentTermRepository @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val jdbc: NamedParameterJdbcTemplate,
   private val vendorPaymentTermScheduleRepository: VendorPaymentTermScheduleRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermRepository::class.java)
   private fun findOneQuery() = """
      SELECT
         vpt.id                     AS vpt_id,
         vpt.uu_row_id              AS vpt_uu_row_id,
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
         comp.uu_row_id             AS comp_uu_row_id,
         comp.time_created          AS comp_time_created,
         comp.time_updated          AS comp_time_updated,
         comp.name                  AS comp_name,
         comp.doing_business_as     AS comp_doing_business_as,
         comp.client_code           AS comp_client_code,
         comp.client_id             AS comp_client_id,
         comp.dataset_code          AS comp_dataset_code,
         comp.federal_id_number     AS comp_federal_id_number,
         vpts.id                    AS vpts_id,
         vpts.uu_row_id             AS vpts_uu_row_id,
         vpts.time_created          AS vpts_time_created,
         vpts.time_updated          AS vpts_time_updated,
         vpts.payment_term_id       AS vpts_payment_term_id,
         vpts.due_month             AS vpts_due_month,
         vpts.due_days              AS vpts_due_days,
         vpts.due_percent           AS vpts_due_percent,
         vpts.schedule_order_number AS vpts_schedule_order_number,
         count(*) OVER()            AS total_elements
      FROM vendor_payment_term vpt
           JOIN company comp ON vpt.company_id = comp.id
           LEFT OUTER JOIN vendor_payment_term_schedule vpts ON vpt.id = vpts.payment_term_id 
   """

   fun findOne(id: Long, company: Company): VendorPaymentTermEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${findOneQuery()}\nWHERE vpt.id = :id AND comp.id = :comp_id"

      logger.debug("Searching for VendorPaymentTerm using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val vendorPaymentTerm = mapRow(rs)

         do {
            mapRowVendorPaymentTermSchedule(rs)?.also { vendorPaymentTerm.scheduleRecords.add(it) }
         } while(rs.next())

         vendorPaymentTerm
      }

      logger.trace("Searching for VendorPaymentTerm: {} resulted in {}", id, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<VendorPaymentTermEntity, PageRequest> {
      val comp_id = company.myId()
      val params = mutableMapOf<String, Any?>("comp_id" to comp_id, "limit" to page.size(), "offset" to page.offset())
      val sql = """
         WITH paged AS (
            SELECT
         vpt.id                     AS vpt_id,
         vpt.uu_row_id              AS vpt_uu_row_id,
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
         comp.uu_row_id             AS comp_uu_row_id,
         comp.time_created          AS comp_time_created,
         comp.time_updated          AS comp_time_updated,
         comp.name                  AS comp_name,
         comp.doing_business_as     AS comp_doing_business_as,
         comp.client_code           AS comp_client_code,
         comp.client_id             AS comp_client_id,
         comp.dataset_code          AS comp_dataset_code,
         comp.federal_id_number     AS comp_federal_id_number,
               count(*) OVER () AS total_elements
            FROM vendor_payment_term vpt
               JOIN company comp ON vpt.company_id = comp.id
            WHERE comp.id = :comp_id
            ORDER BY vpt_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         )
         SELECT
            p.*,
            vpts.id                    AS vpts_id,
            vpts.uu_row_id             AS vpts_uu_row_id,
            vpts.time_created          AS vpts_time_created,
            vpts.time_updated          AS vpts_time_updated,
            vpts.payment_term_id       AS vpts_payment_term_id,
            vpts.due_month             AS vpts_due_month,
            vpts.due_days              AS vpts_due_days,
            vpts.due_percent           AS vpts_due_percent,
            vpts.schedule_order_number AS vpts_schedule_order_number
         FROM paged AS p
            LEFT OUTER JOIN vendor_payment_term_schedule vpts ON p.vpt_id = vpts.payment_term_id
         ORDER BY vpt_${page.snakeSortBy()}, vpts.id ${page.sortDirection()}
      """

      logger.debug("find all vendor payment terms {}/{}", sql, params)

      return jdbc.queryPaged(sql, params, page) { rs, elements ->
         var currentId = -1L
         var currentParentEntity: VendorPaymentTermEntity? = null

         do {
            val tempId = rs.getLong("vpt_id")
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
            "company_id" to entity.company.myId(),
            "description" to entity.description,
            "number_of_payments" to entity.numberOfPayments,
            "discount_month" to entity.discountMonth,
            "discount_days" to entity.discountDays,
            "discount_percent" to entity.discountPercent
         ),
         RowMapper { rs, _ -> mapDdlRow(rs, entity.company) }
      )

      //TODO Add comment about below
      entity.scheduleRecords
         .map { vendorPaymentTermScheduleRepository.upsert(it, inserted) }
         .forEach { inserted.scheduleRecords.add(it) }

      logger.debug("Inserted VendorPaymentTerm {}", inserted)

      return inserted
   }

   @Transactional
   fun update(entity: VendorPaymentTermEntity): VendorPaymentTermEntity {
      logger.debug("Updating VendorPaymentTerm {}", entity)

      /*
      val existing = jdbc.query("""
         Select * from vendor_payment_term
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id
         ),
         RowMapper { rs, _ -> mapDdlRow(rs, entity.company) }
      )
      */

      val existing = findOne(entity.id!!, entity.company)

      vendorPaymentTermScheduleRepository.deleteNotIn(existing!!, entity.scheduleRecords)

      val updated = jdbc.updateReturning("""
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
            "companyId" to entity.company.myId(),
            "description" to entity.description,
            "numberOfPayments" to entity.numberOfPayments,
            "discountMonth" to entity.discountMonth,
            "discountDays" to entity.discountDays,
            "discountPercent" to entity.discountPercent
         ),
         RowMapper { rs, _ -> mapDdlRow(rs, entity.company) }
      )

      //TODO Add comment about below
      entity.scheduleRecords
         .map { vendorPaymentTermScheduleRepository.upsert(it, updated) }
         .forEach { updated.scheduleRecords.add(it) }

      logger.debug("Updated VendorPaymentTerm {}", updated)

      return updated
   }

   private fun mapRow(rs: ResultSet): VendorPaymentTermEntity {
      return VendorPaymentTermEntity(id = rs.getLong("vpt_id"), company = companyRepository.mapRow(rs, "comp_"), description = rs.getString("vpt_description"), number = rs.getInt("vpt_number"), numberOfPayments = rs.getInt("vpt_number_of_payments"), discountMonth = rs.getIntOrNull("vpt_discount_month"), discountDays = rs.getIntOrNull("vpt_discount_days"), discountPercent = rs.getBigDecimal("vpt_discount_percent"))
   }

   private fun mapDdlRow(rs: ResultSet, company: Company): VendorPaymentTermEntity {
      return VendorPaymentTermEntity(id = rs.getLong("id"), company = company, description = rs.getString("description"), number = rs.getInt("number"), numberOfPayments = rs.getInt("number_of_payments"), discountMonth = rs.getIntOrNull("discount_month"), discountDays = rs.getIntOrNull("discount_days"), discountPercent = rs.getBigDecimal("discount_percent"))
   }

   private fun mapRowVendorPaymentTermSchedule(rs: ResultSet): VendorPaymentTermScheduleEntity? =
      if (rs.getString("vpts_id") != null) {
         VendorPaymentTermScheduleEntity(
            id = rs.getLong("vpts_id"),
            dueMonth = rs.getInt("vpts_due_month"),
            dueDays = rs.getInt("vpts_due_days"),
            duePercent = rs.getBigDecimal("vpts_due_percent"),
            scheduleOrderNumber = rs.getInt("vpts_schedule_order_number")
         )
      } else {
         null
      }
}
