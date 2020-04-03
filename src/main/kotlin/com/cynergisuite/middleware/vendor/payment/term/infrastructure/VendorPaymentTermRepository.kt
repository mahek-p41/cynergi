package com.cynergisuite.middleware.vendor.payment.term.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import io.micronaut.spring.tx.annotation.Transactional
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
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermRepository::class.java)
   private fun baseSelectQuery() = """
      SELECT
         vpt.id                 AS vpt_id,
         vpt.uu_row_id          AS vpt_uu_row_id,
         vpt.time_created       AS vpt_time_created,
         vpt.time_updated       AS vpt_time_updated,
         vpt.company_id         AS vpt_company_id,
         vpt.description        AS vpt_description,
         vpt.number             AS vpt_number,
         vpt.number_of_payments AS vpt_number_of_payments,
         vpt.due_month_1        AS vpt_due_month_1,
         vpt.due_month_2        AS vpt_due_month_2,
         vpt.due_month_3        AS vpt_due_month_3,
         vpt.due_month_4        AS vpt_due_month_4,
         vpt.due_month_5        AS vpt_due_month_5,
         vpt.due_month_6        AS vpt_due_month_6,
         vpt.due_days_1         AS vpt_due_days_1,
         vpt.due_days_2         AS vpt_due_days_2,
         vpt.due_days_3         AS vpt_due_days_3,
         vpt.due_days_4         AS vpt_due_days_4,
         vpt.due_days_5         AS vpt_due_days_5,
         vpt.due_days_6         AS vpt_due_days_6,
         vpt.due_percent_1      AS vpt_due_percent_1,
         vpt.due_percent_2      AS vpt_due_percent_2,
         vpt.due_percent_3      AS vpt_due_percent_3,
         vpt.due_percent_4      AS vpt_due_percent_4,
         vpt.due_percent_5      AS vpt_due_percent_5,
         vpt.due_percent_6      AS vpt_due_percent_6,
         vpt.discount_month     AS vpt_discount_month,
         vpt.discount_days      AS vpt_discount_days,
         vpt.discount_percent   AS vpt_discount_percent,
         comp.id                AS comp_id,
         comp.uu_row_id         AS comp_uu_row_id,
         comp.time_created      AS comp_time_created,
         comp.time_updated      AS comp_time_updated,
         comp.name              AS comp_name,
         comp.doing_business_as AS comp_doing_business_as,
         comp.client_code       AS comp_client_code,
         comp.client_id         AS comp_client_id,
         comp.dataset_code      AS comp_dataset_code,
         comp.federal_id_number AS comp_federal_id_number,
         count(*) OVER()        AS total_elements
      FROM vendor_payment_term vpt
           JOIN company comp ON vpt.company_id = comp.id
   """

   fun findOne(id: Long): VendorPaymentTermEntity? {
      logger.debug("Searching for VendorPaymentTerm by id {}", id)

      val found = jdbc.findFirstOrNull("${baseSelectQuery()} WHERE vpt.id = :id", mapOf("id" to id), this::mapRow)

      logger.trace("Searching for VendorPaymentTerm: {} resulted in {}", id, found)

      return found
   }

   fun findAll(pageRequest: PageRequest, company: Company): RepositoryPage<VendorPaymentTermEntity, PageRequest> {
      return jdbc.queryPaged("""
         ${baseSelectQuery()}
         WHERE comp.id = :comp_id
         ORDER BY vpt.${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId(),
            "limit" to pageRequest.size(),
            "offset" to pageRequest.offset()
         ),
         pageRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs))
         } while(rs.next())
      }
   }
   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM vendor_payment_term WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if VendorPaymentTerm: {} exists resulted in {}", id, exists)

      return exists
   }

   @Transactional
   fun insert(entity: VendorPaymentTermEntity): VendorPaymentTermEntity {
      logger.debug("Inserting VendorPaymentTerm {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO vendor_payment_term(company_id, description, number_of_payments, due_month_1, due_month_2, due_month_3, due_month_4, due_month_5, due_month_6, due_days_1, due_days_2, due_days_3, due_days_4, due_days_5, due_days_6, due_percent_1, due_percent_2, due_percent_3, due_percent_4, due_percent_5, due_percent_6, discount_month, discount_days, discount_percent)
         VALUES (
            :company_id,
            :description,
            :number_of_payments,
            :due_month_1,
            :due_month_2,
            :due_month_3,
            :due_month_4,
            :due_month_5,
            :due_month_6,
            :due_days_1,
            :due_days_2,
            :due_days_3,
            :due_days_4,
            :due_days_5,
            :due_days_6,
            :due_percent_1,
            :due_percent_2,
            :due_percent_3,
            :due_percent_4,
            :due_percent_5,
            :due_percent_6,
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
            "due_month_1" to entity.dueMonth1,
            "due_month_2" to entity.dueMonth2,
            "due_month_3" to entity.dueMonth3,
            "due_month_4" to entity.dueMonth4,
            "due_month_5" to entity.dueMonth5,
            "due_month_6" to entity.dueMonth6,
            "due_days_1" to entity.dueDays1,
            "due_days_2" to entity.dueDays2,
            "due_days_3" to entity.dueDays3,
            "due_days_4" to entity.dueDays4,
            "due_days_5" to entity.dueDays5,
            "due_days_6" to entity.dueDays6,
            "due_percent_1" to entity.duePercent1,
            "due_percent_2" to entity.duePercent2,
            "due_percent_3" to entity.duePercent3,
            "due_percent_4" to entity.duePercent4,
            "due_percent_5" to entity.duePercent5,
            "due_percent_6" to entity.duePercent6,
            "discount_month" to entity.discountMonth,
            "discount_days" to entity.discountDays,
            "discount_percent" to entity.discountPercent
         ),
         RowMapper { rs, _ -> mapDdlRow(rs, entity.company) }
      )
   }

   @Transactional
   fun update(entity: VendorPaymentTermEntity): VendorPaymentTermEntity {
      logger.debug("Updating VendorPaymentTerm {}", entity)

      return jdbc.updateReturning("""
         UPDATE vendor_payment_term
         SET
            company_id = :companyId,
            description = :description,
            number_of_payments = :numberOfPayments,
            due_month_1 = :dueMonth1,
            due_month_2 = :dueMonth2,
            due_month_3 = :dueMonth3,
            due_month_4 = :dueMonth4,
            due_month_5 = :dueMonth5,
            due_month_6 = :dueMonth6,
            due_days_1 = :dueDays1,
            due_days_2 = :dueDays2,
            due_days_3 = :dueDays3,
            due_days_4 = :dueDays4,
            due_days_5 = :dueDays5,
            due_days_6 = :dueDays6,
            due_percent_1 = :duePercent1,
            due_percent_2 = :duePercent2,
            due_percent_3 = :duePercent3,
            due_percent_4 = :duePercent4,
            due_percent_5 = :duePercent5,
            due_percent_6 = :duePercent6,
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
            "dueMonth1" to entity.dueMonth1,
            "dueMonth2" to entity.dueMonth2,
            "dueMonth3" to entity.dueMonth3,
            "dueMonth4" to entity.dueMonth4,
            "dueMonth5" to entity.dueMonth5,
            "dueMonth6" to entity.dueMonth6,
            "dueDays1" to entity.dueDays1,
            "dueDays2" to entity.dueDays2,
            "dueDays3" to entity.dueDays3,
            "dueDays4" to entity.dueDays4,
            "dueDays5" to entity.dueDays5,
            "dueDays6" to entity.dueDays6,
            "duePercent1" to entity.duePercent1,
            "duePercent2" to entity.duePercent2,
            "duePercent3" to entity.duePercent3,
            "duePercent4" to entity.duePercent4,
            "duePercent5" to entity.duePercent5,
            "duePercent6" to entity.duePercent6,
            "discountMonth" to entity.discountMonth,
            "discountDays" to entity.discountDays,
            "discountPercent" to entity.discountPercent
            ),
         RowMapper { rs, _ -> mapDdlRow(rs, entity.company) }
      )
   }

   private fun mapRow(rs: ResultSet): VendorPaymentTermEntity {
      return VendorPaymentTermEntity(
         id = rs.getLong("vpt_id"),
         uuRowId = rs.getUuid("vpt_uu_row_id"),
         company = companyRepository.mapRow(rs, "comp_"),
         description = rs.getString("vpt_description"),
         number = rs.getInt("vpt_number"),
         numberOfPayments = rs.getInt("vpt_number_of_payments"),
         dueMonth1 = rs.getIntOrNull("vpt_due_month_1"),
         dueMonth2 = rs.getIntOrNull("vpt_due_month_2"),
         dueMonth3 = rs.getIntOrNull("vpt_due_month_3"),
         dueMonth4 = rs.getIntOrNull("vpt_due_month_4"),
         dueMonth5 = rs.getIntOrNull("vpt_due_month_5"),
         dueMonth6 = rs.getIntOrNull("vpt_due_month_6"),
         dueDays1 = rs.getIntOrNull("vpt_due_days_1"),
         dueDays2 = rs.getIntOrNull("vpt_due_days_2"),
         dueDays3 = rs.getIntOrNull("vpt_due_days_3"),
         dueDays4 = rs.getIntOrNull("vpt_due_days_4"),
         dueDays5 = rs.getIntOrNull("vpt_due_days_5"),
         dueDays6 = rs.getIntOrNull("vpt_due_days_6"),
         duePercent1 = rs.getBigDecimal("vpt_due_percent_1"),
         duePercent2 = rs.getBigDecimal("vpt_due_percent_2"),
         duePercent3 = rs.getBigDecimal("vpt_due_percent_3"),
         duePercent4 = rs.getBigDecimal("vpt_due_percent_4"),
         duePercent5 = rs.getBigDecimal("vpt_due_percent_5"),
         duePercent6 = rs.getBigDecimal("vpt_due_percent_6"),
         discountMonth = rs.getIntOrNull("vpt_discount_month"),
         discountDays = rs.getIntOrNull("vpt_discount_days"),
         discountPercent = rs.getBigDecimal("vpt_discount_percent")
      )
   }

   private fun mapDdlRow(rs: ResultSet, company: Company): VendorPaymentTermEntity {
      return VendorPaymentTermEntity(
         id = rs.getLong("id"),
         uuRowId = rs.getUuid("uu_row_id"),
         company = company,
         description = rs.getString("description"),
         number = rs.getInt("number"),
         numberOfPayments = rs.getInt("number_of_payments"),
         dueMonth1 = rs.getIntOrNull("due_month_1"),
         dueMonth2 = rs.getIntOrNull("due_month_2"),
         dueMonth3 = rs.getIntOrNull("due_month_3"),
         dueMonth4 = rs.getIntOrNull("due_month_4"),
         dueMonth5 = rs.getIntOrNull("due_month_5"),
         dueMonth6 = rs.getIntOrNull("due_month_6"),
         dueDays1 = rs.getIntOrNull("due_days_1"),
         dueDays2 = rs.getIntOrNull("due_days_2"),
         dueDays3 = rs.getIntOrNull("due_days_3"),
         dueDays4 = rs.getIntOrNull("due_days_4"),
         dueDays5 = rs.getIntOrNull("due_days_5"),
         dueDays6 = rs.getIntOrNull("due_days_6"),
         duePercent1 = rs.getBigDecimal("due_percent_1"),
         duePercent2 = rs.getBigDecimal("due_percent_2"),
         duePercent3 = rs.getBigDecimal("due_percent_3"),
         duePercent4 = rs.getBigDecimal("due_percent_4"),
         duePercent5 = rs.getBigDecimal("due_percent_5"),
         duePercent6 = rs.getBigDecimal("due_percent_6"),
         discountMonth = rs.getIntOrNull("discount_month"),
         discountDays = rs.getIntOrNull("discount_days"),
         discountPercent = rs.getBigDecimal("discount_percent")
      )
   }
}
