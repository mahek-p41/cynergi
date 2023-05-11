package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.AccountPayableCheckPreviewFilterRequest
import com.cynergisuite.domain.AccountPayableInvoiceInquiryFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableDistDetailReportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryPaymentDTO
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import java.sql.ResultSet
import java.util.UUID

@Singleton
class AccountPayableInvoiceInquiryRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val statusRepository: AccountPayableInvoiceStatusTypeRepository,
   private val typeRepository: AccountPayableInvoiceTypeRepository
) {
   fun selectBaseQuery(): String {
      return """
         SELECT
            apInvoice.id                                       AS apInvoice_id,
            apInvoice.invoice                                  AS apInvoice_invoice,
            apInvoice.invoice_amount                           AS apInvoice_invoice_amount,
            apInvoice.invoice_date                             AS apInvoice_invoice_date,
            type.id                                            AS apInvoice_type_id,
            type.value                                         AS apInvoice_type_value,
            type.description                                   AS apInvoice_type_description,
            type.localization_code                             AS apInvoice_type_localization_code,
            apInvoice.separate_check_indicator                 AS apInvoice_separate_check_indicator,
            poHeader.number                                    AS apInvoice_poHeader_number,
            apInvoice.use_tax_indicator                        AS apInvoice_use_tax_indicator,
            apInvoice.due_date                                 AS apInvoice_due_date,
            apInvoice.expense_date                             AS apInvoice_expense_date,
            apInvoice.discount_date                            AS apInvoice_discount_date,
            apInvoice.discount_amount                          AS apInvoice_discount_amount,
            apInvoice.discount_taken                           AS apInvoice_discount_taken,
            apInvoice.discount_percent                         AS apInvoice_discount_percent,
            status.id                                          AS apInvoice_status_id,
            status.value                                       AS apInvoice_status_value,
            status.description                                 AS apInvoice_status_description,
            status.localization_code                           AS apInvoice_status_localization_code,
            apInvoice.message                                  AS apInvoice_message,
            vend.number                                        AS apInvoice_vendor_number,
            payTo.number                                       AS apInvoice_payTo_number,
            count(*) OVER() AS total_elements
         FROM account_payable_invoice apInvoice
            JOIN purchase_order_header poHeader                      ON poHeader.id = apInvoice.purchase_order_id AND poHeader.deleted = FALSE
            JOIN account_payable_invoice_type_domain type            ON type.id = apInvoice.type_id
            JOIN account_payable_invoice_status_type_domain status   ON status.id = apInvoice.status_id
            JOIN vendor vend                                         ON apInvoice.vendor_id = vend.id AND vend.deleted = FALSE
            JOIN vendor payTo                                        ON apInvoice.pay_to_id = payTo.id AND payTo.deleted = FALSE
      """
   }

   @ReadOnly
   fun fetchInquiry(company: CompanyEntity, filterRequest: AccountPayableInvoiceInquiryFilterRequest): RepositoryPage<AccountPayableInvoiceInquiryDTO, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "vendor" to filterRequest.vendor, "payTo" to filterRequest.payTo)
      val whereClause = StringBuilder(
         "WHERE apInvoice.company_id = :comp_id " +
         "AND vend.number = :vendor " +
         "AND payTo.number = :payTo "
      )
      val sortBy = StringBuilder("ORDER BY ")

      if (filterRequest.invStatus != null) {
         params["invStatus"] = filterRequest.invStatus
         whereClause.append(" AND status.value = :invStatus ")
      }

      if (filterRequest.poNbr != null) {
         params["poNbr"] = filterRequest.poNbr
         whereClause.append(" AND poHeader.number >= :poNbr ")
      }

      if (filterRequest.invNbr != null) {
         params["invNbr"] = filterRequest.invNbr
         whereClause.append(" AND apInvoice.invoice >= :invNbr ")
      }

      if (filterRequest.invDate != null) {
         params["invDate"] = filterRequest.invDate
         whereClause.append(" AND apInvoice.invoice_date >= :invDate ")
      }

      if (filterRequest.dueDate != null) {
         params["dueDate"] = filterRequest.dueDate
         whereClause.append(" AND apInvoice.due_date >= :dueDate ")
      }

      if (filterRequest.invAmount != null) {
         params["invAmount"] = filterRequest.invAmount
         whereClause.append(" AND apInvoice.invoice_amount >= :invAmount ")
      }

      if (filterRequest.sortBy == "apInvoice.invoice") {
         sortBy.append("naturalsort(apInvoice.invoice)")
      }
      else {
         sortBy.append(filterRequest.sortBy)
      }

      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            $whereClause
            $sortBy
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {

            val apInvoiceId = rs.getUuid("apInvoice_id")
            val inquiryDTO = mapInvoice(rs, "apInvoice_")

            inquiryDTO.payments = fetchInquiryPayments(apInvoiceId, company)
            inquiryDTO.glDist = fetchInquiryDistributions(apInvoiceId, company)

            elements.add(inquiryDTO)

         } while (rs.next())
      }
   }

   @ReadOnly
   fun fetchInquiryPayments(apInvoiceId: UUID, company: CompanyEntity): List<AccountPayableInvoiceInquiryPaymentDTO> {
      val paymentDTOs = mutableListOf<AccountPayableInvoiceInquiryPaymentDTO>()

      jdbc.query(
         """
            SELECT
               bank.number                         AS bank_number,
               pmt.payment_number                  AS payment_number,
               pmtDetail.amount                    AS pmtDetail_amount,
               pmt.payment_date                    AS payment_date,
               pmt.amount                          AS payment_amount,
               apInvoice.original_invoice_amount   AS apInvoice_original_amount
            FROM account_payable_payment_detail pmtDetail
               JOIN account_payable_payment pmt ON pmtDetail.payment_number_id = pmt.id
               JOIN account_payable_invoice apInvoice ON pmtDetail.account_payable_invoice_id = apInvoice.id
               JOIN bank ON pmt.bank_id = bank.id AND bank.deleted = FALSE
            WHERE apInvoice.id = :apInvoiceId AND pmt.account_payable_payment_status_id = 1
            ORDER BY bank.number ASC, pmt.payment_number ASC
         """.trimIndent(),
         mapOf("apInvoiceId" to apInvoiceId)
      ) { rs, _ ->
         do {
            paymentDTOs.add(mapPayment(rs))
         } while (rs.next())
      }

      return paymentDTOs
   }

   @ReadOnly
   fun fetchInquiryDistributions(apInvoiceId: UUID, company: CompanyEntity): List<AccountPayableDistDetailReportDTO> {
      val distDTOs = mutableListOf<AccountPayableDistDetailReportDTO>()

      jdbc.query(
         """
            SELECT
               invDist.distribution_profit_center_id_sfk    AS invDist_profit_center,
               invDist.distribution_amount                  AS invDist_amount,
               account.number                               AS invDist_account_number,
               account.name                                 AS invDist_account_name
            FROM account_payable_invoice_distribution invDist
               JOIN account ON invDist.distribution_account_id = account.id AND account.deleted = FALSE
            WHERE invDist.invoice_id = :apInvoiceId
            ORDER BY invDist.distribution_profit_center_id_sfk ASC
         """.trimIndent(),
         mapOf("apInvoiceId" to apInvoiceId)
      ) { rs, _ ->
         do {
            distDTOs.add(mapDistDetail(rs, "invDist_"))
         } while (rs.next())
      }

      return distDTOs
   }

   @ReadOnly
   fun fetchCheckPreview(company: CompanyEntity, filterRequest: AccountPayableCheckPreviewFilterRequest): AccountPayableCheckPreviewDTO {
      //TODO: create check preview report
      val checkPreviewDTO = AccountPayableCheckPreviewDTO()

      return checkPreviewDTO
   }

   fun mapInvoice(rs: ResultSet, columnPrefix: String = EMPTY): AccountPayableInvoiceInquiryDTO {
      val type = typeRepository.mapRow(rs, "${columnPrefix}type_")
      val status = statusRepository.mapRow(rs, "${columnPrefix}status_")

      return AccountPayableInvoiceInquiryDTO(
         invoice = rs.getString("${columnPrefix}invoice"),
         invAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         invDate = rs.getLocalDate("${columnPrefix}invoice_date"),
         type = AccountPayableInvoiceTypeDTO(type),
         separateCheckIndicator = rs.getBoolean("${columnPrefix}separate_check_indicator"),
         poNbr = rs.getInt("${columnPrefix}poHeader_number"),
         useTaxIndicator = rs.getBoolean("${columnPrefix}use_tax_indicator"),
         dueDate = rs.getLocalDate("${columnPrefix}due_date"),
         expenseDate = rs.getLocalDate("${columnPrefix}expense_date"),
         discountDate = rs.getLocalDateOrNull("${columnPrefix}discount_date"),
         discountBasisAmount = rs.getBigDecimalOrNull("${columnPrefix}discount_amount"),
         discountTaken = rs.getBigDecimalOrNull("${columnPrefix}discount_taken"),
         discountPercent = rs.getBigDecimalOrNull("${columnPrefix}discount_percent"),
         status = AccountPayableInvoiceStatusTypeDTO(status),
         payments = null,
         glDist = null,
         message = rs.getString("${columnPrefix}message")
      )
   }

   private fun mapPayment(rs: ResultSet): AccountPayableInvoiceInquiryPaymentDTO {
      return AccountPayableInvoiceInquiryPaymentDTO(
         bankNbr = rs.getInt("bank_number"),
         paymentNbr = rs.getString("payment_number"),
         paid = rs.getBigDecimal("pmtDetail_amount"),
         date = rs.getLocalDate("payment_date"),
         paymentAmt = rs.getBigDecimal("payment_amount"),
         originalAmt = rs.getBigDecimal("apInvoice_original_amount")
      )
   }

   private fun mapDistDetail(rs: ResultSet, columnPrefix: String = EMPTY): AccountPayableDistDetailReportDTO {
      return AccountPayableDistDetailReportDTO(
         accountNumber = rs.getInt("${columnPrefix}account_number"),
         accountName = rs.getString("${columnPrefix}account_name"),
         distProfitCenter = rs.getInt("${columnPrefix}profit_center"),
         distAmount = rs.getBigDecimal("${columnPrefix}amount")
      )
   }
}
