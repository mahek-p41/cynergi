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
import com.cynergisuite.middleware.purchase.order.PurchaseOrderEntity
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import com.cynergisuite.middleware.vendor.rebate.RebateEntity
import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateRepository
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
   private val purchaseOrderRepository: PurchaseOrderRepository,
   private val rebateRepository: RebateRepository,
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
   fun calculateUnpaidAmounts(vendorNumber: Int, company: CompanyEntity): List<Triple<BigDecimal, LocalDate, Int>> {
      return jdbc.query(
         """
            SELECT
               apInv.invoice_amount - apInv.discount_taken - apInv.paid_amount AS unpaid_amount,
               apInv.due_date AS due_date,
               apInv.status_id AS status_id
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
   fun fetchRebates(company: CompanyEntity, filterRequest: VendorStatisticsFilterRequest): RepositoryPage<RebateEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${rebateRepository.selectBaseQuery()}
               JOIN rebate_to_vendor r_to_v ON r.id = r_to_v.rebate_id
               JOIN vendor vend ON r_to_v.vendor_id = vend.id
            WHERE vend.company_id = :comp_id AND vend.id = :vendorId
            ORDER BY r.description
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "vendorId" to filterRequest.vendorId
         ),
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(rebateRepository.mapRow(rs, company, "r_"))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun fetchInvoices(company: CompanyEntity, filterRequest: VendorStatisticsFilterRequest): RepositoryPage<AccountPayableInvoiceInquiryDTO, PageRequest> {
      return jdbc.queryPaged(
         """
            ${accountPayableInvoiceInquiryRepository.selectBaseQuery()}
            WHERE vend.company_id = :comp_id AND vend.id = :vendorId
            ORDER BY naturalsort(apInvoice.invoice)
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "vendorId" to filterRequest.vendorId
         ),
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

   @ReadOnly
   fun fetchPurchaseOrders(company: CompanyEntity, filterRequest: VendorStatisticsFilterRequest): RepositoryPage<PurchaseOrderEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${purchaseOrderRepository.selectBaseQuery()}
            WHERE vendor.v_company_id = :comp_id AND vendor.v_id = :vendorId
            ORDER BY po.number
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "vendorId" to filterRequest.vendorId
         ),
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(purchaseOrderRepository.mapRow(rs, company, "po_"))
         } while (rs.next())
      }
   }

   private fun mapUnpaidAmounts(rs: ResultSet): Triple<BigDecimal, LocalDate, Int> {
      return Triple(
         first = rs.getBigDecimal("unpaid_amount"),
         second = rs.getLocalDate("due_date"),
         third = rs.getInt("status_id")
      )
   }
}
