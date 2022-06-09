package com.cynergisuite.middleware.accounting.account.payable.aging.infrastructure

import com.cynergisuite.domain.AgingReportFilterRequest
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.aging.AccountPayableAgingReportDTO
import com.cynergisuite.middleware.accounting.account.payable.aging.AccountPayableAgingReportEntity
import com.cynergisuite.middleware.accounting.account.payable.aging.AgingReportInvoiceDetailEntity
import com.cynergisuite.middleware.accounting.account.payable.aging.AgingReportVendorDetailEntity
import com.cynergisuite.middleware.accounting.account.payable.aging.BalanceDisplayEnum
import com.cynergisuite.middleware.accounting.account.payable.aging.BalanceDisplayTotalsEntity
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentDetailRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.temporal.ChronoUnit

@Singleton
class AccountPayableAgingReportRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val apPaymentDetailRepository: AccountPayablePaymentDetailRepository,
   private val statusRepository: AccountPayableInvoiceStatusTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableAgingReportRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH apPaymentDetail AS (
            ${apPaymentDetailRepository.baseSelectQuery()}
         )
         SELECT
            apInvoice.company_id                                           AS apInvoiceDetail_apInvoice_company_id,
            apInvoice.vendor_id                                            AS apInvoiceDetail_apInvoice_vendor_id,
            apInvoice.invoice                                              AS apInvoiceDetail_apInvoice_invoice,
            apInvoice.invoice_date                                         AS apInvoiceDetail_apInvoice_invoice_date,
            apInvoice.invoice_amount                                       AS apInvoiceDetail_apInvoice_invoice_amount,
            apInvoice.discount_amount                                      AS apInvoiceDetail_apInvoice_discount_amount,
            apInvoice.expense_date                                         AS apInvoiceDetail_apInvoice_expense_date,
            apInvoice.paid_amount                                          AS apInvoiceDetail_apInvoice_paid_amount,
            apInvoice.due_date                                             AS apInvoiceDetail_apInvoice_due_date,
            status.id                                                      AS apInvoiceDetail_apInvoice_status_id,
            status.value                                                   AS apInvoiceDetail_apInvoice_status_value,
            status.description                                             AS apInvoiceDetail_apInvoice_status_description,
            status.localization_code                                       AS apInvoiceDetail_apInvoice_status_localization_code,
            vend.company_id                                                AS apInvoiceDetail_vendor_company_id,
            vend.number                                                    AS apInvoiceDetail_vendor_number,
            vend.name                                                      AS apInvoiceDetail_vendor_name,
            vend.deleted                                                   AS apInvoiceDetail_vendor_deleted,
            vend.active                                                    AS apInvoiceDetail_vendor_active,
            apPayment.company_id                                           AS apInvoiceDetail_apPayment_company_id,
            apPayment.bank_id                                              AS apInvoiceDetail_apPayment_bank_id,
            apPayment.account_payable_payment_status_id                    AS apInvoiceDetail_apPayment_status_id,
            apPayment.payment_number                                       AS apInvoiceDetail_apPayment_payment_number,
            apPayment.payment_date                                         AS apInvoiceDetail_apPayment_payment_date,
            apPayment.date_voided                                          AS apInvoiceDetail_apPayment_date_voided,
            apPaymentDetail.company_id                                     AS apInvoiceDetail_apPaymentDetail_company_id,
            apPaymentDetail.payment_number_id                              AS apInvoiceDetail_apPaymentDetail_payment_number_id,
            apPaymentDetail.amount                                         AS apInvoiceDetail_apPaymentDetail_amount,
            count(*) OVER() AS total_elements
         FROM account_payable_invoice apInvoice
            JOIN vendor vend ON apInvoice.vendor_id = vend.id AND vend.deleted = FALSE
            JOIN account_payable_invoice_status_type_domain status ON apInvoice.status_id = status.id
            LEFT JOIN account_payable_payment_detail apPaymentDetail ON apInvoice.id = apPaymentDetail.account_payable_invoice_id
            LEFT JOIN account_payable_payment apPayment ON apPaymentDetail.payment_number_id = apPayment.id
      """
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, filterRequest: AgingReportFilterRequest): AccountPayableAgingReportDTO {
      val vendors = mutableListOf<AgingReportVendorDetailEntity>()
      var currentVendor: AgingReportVendorDetailEntity? = null
      val agedTotals = BalanceDisplayTotalsEntity()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE apInvoice.company_id = :comp_id")

      if (filterRequest.vendorStart != null) {
         params["vendorStart"] = filterRequest.vendorStart
         whereClause.append(" AND vend.number >= :vendorStart")
      }

      if (filterRequest.vendorEnd != null) {
         params["vendorEnd"] = filterRequest.vendorEnd
         whereClause.append(" AND vend.number <= :vendorEnd")
      }

      filterRequest.agingDate?.let {
         params["agingDate"] = filterRequest.agingDate
         whereClause.append(" AND apInvoice.expense_date <= :agingDate")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY apInvoiceDetail_vendor_${filterRequest.sortBy}, apInvoiceDetail_apInvoice_invoice_date
         """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            val tempVendor = if (currentVendor?.vendorNumber != rs.getIntOrNull("apInvoiceDetail_vendor_number")) {
               val localVendor = mapRowVendorDetail(rs, "apInvoiceDetail_")
               vendors.add(localVendor)
               currentVendor = localVendor

               localVendor
            } else {
               currentVendor!!
            }

            var invoiceFlag = false
            mapRowInvoiceDetail(rs, "apInvoiceDetail_").let {
               // determine if invoice should be included on report
               if (it.invoiceStatus.id == 2) {
                  tempVendor.invoices?.add(it) // include invoice if invoice status = 2 (Open)
                  invoiceFlag = true
               } else if (it.invoiceStatus.id == 3 && it.invoicePaidAmount > BigDecimal.ZERO) {
                  // perform back date inquiry
                  if (it.apPaymentPaymentDate!! > filterRequest.agingDate && it.apPaymentDateVoided == null) {
                     it.invoiceStatus = AccountPayableInvoiceStatusType(2, "O", "Open", "open")

                     it.invoicePaidAmount -= it.apPaymentDetailAmount!!
                     it.balance = it.invoiceAmount - it.invoicePaidAmount

                     tempVendor.invoices?.add(it)
                     invoiceFlag = true
                  }
               }

               if (invoiceFlag) {
                  // determine the proper balance display column for the invoice
                  var daysUntil = filterRequest.agingDate!!.until(it.invoiceDueDate, ChronoUnit.DAYS)

                  // Current - if the invoiceDate is the same or before the agingDate
                  if (daysUntil <= 0) {
                     it.balanceDisplay = BalanceDisplayEnum.CURRENT
                  }
                  // 1 to 30 - if the invoiceDate is 1 to 30 days after agingDate
                  else if (daysUntil >= 1 && daysUntil <= 30) {
                     it.balanceDisplay = BalanceDisplayEnum.ONETOTHIRTY
                  }
                  // 31 to 60 - if the invoiceDate is 31 to 60 days after agingDate
                  else if (daysUntil > 30 && daysUntil <= 60) {
                     it.balanceDisplay = BalanceDisplayEnum.THIRTYONETOSIXTY
                  }
                  // Over 60 - if the invoiceDate is 61+ days after agingDate
                  else if (daysUntil > 60) {
                     it.balanceDisplay = BalanceDisplayEnum.OVERSIXTY
                  }

                  // add invoice balance to running totals for balance and the proper balance display column
                  tempVendor.vendorTotals.balanceTotal = tempVendor.vendorTotals.balanceTotal.plus(it.balance)
                  when (it.balanceDisplay) {
                     BalanceDisplayEnum.CURRENT ->
                        tempVendor.vendorTotals.currentTotal =
                           tempVendor.vendorTotals.currentTotal.plus(it.balance)
                     BalanceDisplayEnum.ONETOTHIRTY ->
                        tempVendor.vendorTotals.oneToThirtyTotal =
                           tempVendor.vendorTotals.oneToThirtyTotal.plus(it.balance)
                     BalanceDisplayEnum.THIRTYONETOSIXTY ->
                        tempVendor.vendorTotals.thirtyOneToSixtyTotal =
                           tempVendor.vendorTotals.thirtyOneToSixtyTotal.plus(it.balance)
                     BalanceDisplayEnum.OVERSIXTY ->
                        tempVendor.vendorTotals.overSixtyTotal =
                           tempVendor.vendorTotals.overSixtyTotal.plus(it.balance)
                  }

                  // add invoice balance to running totals for balance and the proper balance display column
                  agedTotals.balanceTotal = agedTotals.balanceTotal.plus(it.balance)
                  when (it.balanceDisplay) {
                     BalanceDisplayEnum.CURRENT ->
                        agedTotals.currentTotal =
                           agedTotals.currentTotal.plus(it.balance)
                     BalanceDisplayEnum.ONETOTHIRTY ->
                        agedTotals.oneToThirtyTotal =
                           agedTotals.oneToThirtyTotal.plus(it.balance)
                     BalanceDisplayEnum.THIRTYONETOSIXTY ->
                        agedTotals.thirtyOneToSixtyTotal =
                           agedTotals.thirtyOneToSixtyTotal.plus(it.balance)
                     BalanceDisplayEnum.OVERSIXTY ->
                        agedTotals.overSixtyTotal =
                           agedTotals.overSixtyTotal.plus(it.balance)
                  }
               }
            }
         } while (rs.next())

         // remove vendors with no invoices
         vendors.removeIf { it.invoices?.size == 0 }
      }

      val entity = AccountPayableAgingReportEntity(vendors, agedTotals)

      return AccountPayableAgingReportDTO(entity)
   }

   private fun mapRowVendorDetail(rs: ResultSet, columnPrefix: String = EMPTY): AgingReportVendorDetailEntity {
      return AgingReportVendorDetailEntity(
         vendorCompanyId = rs.getUuid("${columnPrefix}vendor_company_id"),
         vendorNumber = rs.getInt("${columnPrefix}vendor_number"),
         vendorName = rs.getString("${columnPrefix}vendor_name")
      )
   }

   private fun mapRowInvoiceDetail(rs: ResultSet, columnPrefix: String = EMPTY): AgingReportInvoiceDetailEntity {
      val invoiceAmount = rs.getBigDecimal("${columnPrefix}apInvoice_invoice_amount")
      val invoicePaidAmount = rs.getBigDecimal("${columnPrefix}apInvoice_paid_amount")
      val balance = invoiceAmount - invoicePaidAmount

      return AgingReportInvoiceDetailEntity(
         invoiceCompanyId = rs.getUuid("${columnPrefix}apInvoice_company_id"),
         invoiceVendorId = rs.getUuid("${columnPrefix}apInvoice_vendor_id"),
         invoice = rs.getString("${columnPrefix}apInvoice_invoice"),
         invoiceDate = rs.getLocalDate("${columnPrefix}apInvoice_invoice_date"),
         invoiceAmount = invoiceAmount,
         invoiceDiscountAmount = rs.getBigDecimal("${columnPrefix}apInvoice_discount_amount"),
         invoiceExpenseDate = rs.getLocalDate("${columnPrefix}apInvoice_expense_date"),
         invoicePaidAmount = invoicePaidAmount,
         invoiceStatus = statusRepository.mapRow(rs, "${columnPrefix}apInvoice_status_"),
         invoiceDueDate = rs.getLocalDate("${columnPrefix}apInvoice_due_date"),
         apPaymentPaymentDate = rs.getLocalDateOrNull("${columnPrefix}apPayment_payment_date"),
         apPaymentDateVoided = rs.getLocalDateOrNull("${columnPrefix}apPayment_date_voided"),
         apPaymentDetailAmount = rs.getBigDecimalOrNull("${columnPrefix}apPaymentDetail_amount"),
         balance = balance,
         balanceDisplay = null
      )
   }
}
