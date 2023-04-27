package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.VendorStatisticsFilterRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceInquiryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDate

@Singleton
class VendorStatisticsRepository @Inject constructor(
   private val accountPayableInvoiceInquiryRepository: AccountPayableInvoiceInquiryRepository,
   private val jdbc: Jdbi
) {
   @ReadOnly
   fun calculatePaid(vendorNumber: Int, dateRange: Pair<LocalDate, LocalDate>, company: CompanyEntity): BigDecimal {
      return jdbc.queryForObject(
         """
            SELECT COALESCE(SUM(apPmtDetail.amount), 0)
            FROM account_payable_payment_detail apPmtDetail
               JOIN account_payable_payment apPmt ON apPmtDetail.payment_number_id = apPmt.id
               JOIN account_payable_invoice apInv ON apPmtDetail.account_payable_invoice_id = apInv.id
               JOIN vendor ON apInv.vendor_id = vendor.id
            WHERE vendor.company_id = :company_id
               AND vendor.number = :vendorNumber
               AND apPmt.account_payable_payment_status_id = 1
               AND apPmt.payment_date BETWEEN :from AND :thru
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "vendorNumber" to vendorNumber,
            "from" to dateRange.first,
            "thru" to dateRange.second
         ),
         BigDecimal::class.java
      )
   }

   @ReadOnly
   fun calculateUnpaidAmounts(vendorNumber: Int, company: CompanyEntity): List<Pair<BigDecimal, LocalDate>> {
      return jdbc.query(
         """
            SELECT
               apInv.invoice_amount - apInv.discount_taken - apInv.paid_amount AS unpaid_amount,
               apInv.due_date AS due_date
            FROM account_payable_invoice apInv
               JOIN vendor ON apInv.vendor_id = vendor.id
            WHERE vendor.company_id = :company_id
               AND vendor.number = :vendorNumber
               AND (apInv.status_id = 1 OR apInv.status_id = 2)
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "vendorNumber" to vendorNumber
         )
      ) { rs, _ ->
         mapUnpaidAmounts(rs)
      }
   }

   @ReadOnly
   fun fetchInvoices(company: CompanyEntity, filterRequest: VendorStatisticsFilterRequest): RepositoryPage<AccountPayableInvoiceInquiryDTO, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "vendorId" to filterRequest.vendorId)
      val whereClause = StringBuilder("WHERE apInvoice.company_id = :comp_id AND vend.id = :vendorId ")

      return jdbc.queryPaged(
         """
            ${accountPayableInvoiceInquiryRepository.selectBaseQuery()}
            $whereClause
            ORDER BY naturalsort(apInvoice.invoice)
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {

            val apInvoiceId = rs.getUuid("apInvoice_id")
            val inquiryDTO = accountPayableInvoiceInquiryRepository.mapInvoice(rs, "apInvoice_")

            inquiryDTO.payments = accountPayableInvoiceInquiryRepository.fetchInquiryPayments(apInvoiceId, company)
            inquiryDTO.glDist = accountPayableInvoiceInquiryRepository.fetchInquiryDistributions(apInvoiceId, company)

            elements.add(inquiryDTO)

         } while (rs.next())
      }
   }

   private fun mapUnpaidAmounts(rs: ResultSet): Pair<BigDecimal, LocalDate> {
      return Pair(
         first = rs.getBigDecimal("unpaid_amount"),
         second = rs.getLocalDate("due_date")
      )
   }
}
