package com.cynergisuite.middleware.accounting.account.payable.cashout.infrastructure

import com.cynergisuite.domain.CashRequirementFilterRequest
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.account.payable.cashout.AccountPayableCashRequirementDTO
import com.cynergisuite.middleware.accounting.account.payable.cashout.AccountPayableCashRequirementEntity
import com.cynergisuite.middleware.accounting.account.payable.cashout.CashRequirementBalanceEntity
import com.cynergisuite.middleware.accounting.account.payable.cashout.CashRequirementBalanceEnum
import com.cynergisuite.middleware.accounting.account.payable.cashout.CashRequirementReportInvoiceDetailEntity
import com.cynergisuite.middleware.accounting.account.payable.cashout.CashRequirementVendorEntity
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentDetailRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentStatusTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.time.LocalDate

@Singleton
class AccountPayableCashRequirementsRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val apPaymentDetailRepository: AccountPayablePaymentDetailRepository,
   private val statusRepository: AccountPayableInvoiceStatusTypeRepository,
   private val paymentStatus: AccountPayablePaymentStatusTypeRepository
){
      private val logger: Logger = LoggerFactory.getLogger(AccountPayableCashRequirementsRepository::class.java)

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
            apInvoice.discount_taken                                       AS apInvoiceDetail_apInvoice_discount_taken,
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
            apPayment.void_interfaced_indicator                            AS apInvoiceDetail_apPayment_void_interfaced_indicator,
            apPaymentDetail.company_id                                     AS apInvoiceDetail_apPaymentDetail_company_id,
            apPaymentDetail.payment_number_id                              AS apInvoiceDetail_apPaymentDetail_payment_number_id,
            apPaymentDetail.amount                                         AS apInvoiceDetail_apPaymentDetail_amount,
            apPaymentStatus.id                                             AS apInvoiceDetail_apPayment_status_id,
            apPaymentStatus.value                                          AS apInvoiceDetail_apPayment_status_value,
            apPaymentStatus.description                                    AS apInvoiceDetail_apPayment_status_description,
            count(*) OVER() AS total_elements
         FROM account_payable_invoice apInvoice
            JOIN vendor vend ON apInvoice.vendor_id = vend.id AND vend.deleted = FALSE
            JOIN account_payable_invoice_status_type_domain status ON apInvoice.status_id = status.id
            LEFT JOIN account_payable_payment_detail apPaymentDetail ON apInvoice.id = apPaymentDetail.account_payable_invoice_id
            LEFT JOIN account_payable_payment apPayment ON apPaymentDetail.payment_number_id = apPayment.id
            LEFT JOIN account_payable_payment_status_type_domain apPaymentStatus on apPayment.account_payable_payment_status_id = apPaymentStatus.id
      """
      }

   @ReadOnly
   fun findAll(company: CompanyEntity, filterRequest: CashRequirementFilterRequest): AccountPayableCashRequirementDTO {
      val vendors = mutableListOf<CashRequirementVendorEntity>()
      var currentVendor: CashRequirementVendorEntity? = null
      val cashoutTotals = CashRequirementBalanceEntity()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE apInvoice.company_id = :comp_id")

      if (filterRequest.beginVendor != null || filterRequest.endVendor!= null) {
         params["beginVendor"] = filterRequest.beginVendor
         params["endVendor"] = filterRequest.endVendor
         whereClause.append(" AND vend.number")
            .append(buildFilterString(filterRequest.beginVendor != null, filterRequest.endVendor != null, "beginVendor", "endVendor"))
      }

      if (filterRequest.fromDateOne != null || filterRequest.thruDateOne != null) {
         params["fromDateOne"] = filterRequest.fromDateOne
         params["thruDateOne"] = filterRequest.thruDateOne
         whereClause.append(" AND ( COALESCE(apPayment.payment_date, apInvoice.due_date) ")
            .append(buildFilterString(filterRequest.fromDateOne != null, filterRequest.thruDateOne != null, "fromDateOne", "thruDateOne"))
      }

      if (filterRequest.fromDateTwo != null || filterRequest.thruDateTwo != null) {
         params["fromDateTwo"] = filterRequest.fromDateTwo
         params["thruDateTwo"] = filterRequest.thruDateTwo
         whereClause.append(" OR COALESCE(apPayment.payment_date, apInvoice.due_date) ")
            .append(buildFilterString(filterRequest.fromDateTwo != null, filterRequest.thruDateTwo != null, "fromDateTwo", "thruDateTwo"))
      }

      if (filterRequest.fromDateThree != null || filterRequest.thruDateThree != null) {
         params["fromDateThree"] = filterRequest.fromDateThree
         params["thruDateThree"] = filterRequest.thruDateThree
         whereClause.append(" OR COALESCE(apPayment.payment_date, apInvoice.due_date) ")
            .append(buildFilterString(filterRequest.fromDateThree != null, filterRequest.thruDateThree != null, "fromDateThree", "thruDateThree"))
      }

      if (filterRequest.fromDateFour != null || filterRequest.thruDateFour != null) {
         params["fromDateFour"] = filterRequest.fromDateFour
         params["thruDateFour"] = filterRequest.thruDateFour
         whereClause.append(" OR COALESCE(apPayment.payment_date, apInvoice.due_date) ")
            .append(buildFilterString(filterRequest.fromDateFour != null, filterRequest.thruDateFour != null, "fromDateFour", "thruDateFour"))
      }

      if (filterRequest.fromDateFive != null || filterRequest.thruDateFive != null) {
         params["fromDateFive"] = filterRequest.fromDateFive
         params["thruDateFive"] = filterRequest.thruDateFive
         whereClause.append(" OR COALESCE(apPayment.payment_date, apInvoice.due_date) ")
            .append(buildFilterString(filterRequest.fromDateFive != null, filterRequest.thruDateFive != null, "fromDateFive", "thruDateFive"))
      }
      whereClause.append(")")
      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY name, invoice ${filterRequest.sortDirection()}
         """.trimIndent(),
         params,
      ) { rs, elements ->
         do {
            val tempVendor = if (currentVendor?.vendorNumber != rs.getIntOrNull("apInvoiceDetail_vendor_number")) {
               val localVendor = mapRow(rs, "apInvoiceDetail_")
               vendors.add(localVendor)
               currentVendor = localVendor

               localVendor
            } else {
               currentVendor!!
            }

            var invoiceFlag = false
            mapRowInvoiceDetail(rs, "apInvoiceDetail_").let {
               if(it.invoiceStatus.value != "V" && (it.apPaymentStatusValue == "P" || it.apPaymentStatusValue == null)){
                  tempVendor.invoices?.add(it)
                  invoiceFlag = true
               }

               if (invoiceFlag) {
                  val dateColumn: LocalDate = it.apPaymentPaymentDate ?: it.invoiceDueDate
                     if (filterRequest.fromDateOne != null && filterRequest.thruDateOne != null) {
                        if (filterRequest.fromDateOne!!.compareTo(dateColumn) * dateColumn.compareTo(
                              filterRequest.thruDateOne
                           ) >= 0
                        ) {
                           it.balanceDisplay = CashRequirementBalanceEnum.ONE
                        }
                     }
                     if (filterRequest.fromDateTwo != null && filterRequest.thruDateTwo != null) {
                        if (filterRequest.fromDateTwo!!.compareTo(dateColumn) * dateColumn.compareTo(
                              filterRequest.thruDateTwo
                           ) >= 0
                        ) {
                           it.balanceDisplay = CashRequirementBalanceEnum.TWO
                        }
                     }
                     if (filterRequest.fromDateThree != null && filterRequest.thruDateThree != null) {
                        if (filterRequest.fromDateThree!!.compareTo(dateColumn) * dateColumn.compareTo(
                              filterRequest.thruDateThree
                           ) >= 0
                        ) {
                           it.balanceDisplay = CashRequirementBalanceEnum.THREE
                        }
                     }
                     if (filterRequest.fromDateFour != null && filterRequest.thruDateFour != null) {
                        if (filterRequest.fromDateFour!!.compareTo(dateColumn) * dateColumn.compareTo(
                              filterRequest.thruDateFour
                           ) >= 0
                        ) {
                           it.balanceDisplay = CashRequirementBalanceEnum.FOUR
                        }
                     }
                     if (filterRequest.fromDateFive != null && filterRequest.thruDateFive != null) {
                        if (filterRequest.fromDateFive!!.compareTo(dateColumn) * dateColumn.compareTo(
                              filterRequest.thruDateFive
                           ) >= 0
                        ) {
                           it.balanceDisplay = CashRequirementBalanceEnum.FIVE
                        }
                     }

                  when (it.balanceDisplay) {
                     CashRequirementBalanceEnum.ONE -> {
                        tempVendor.vendorTotals.weekOneDue =
                           tempVendor.vendorTotals.weekOneDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        tempVendor.vendorTotals.weekOnePaid =
                           tempVendor.vendorTotals.weekOnePaid.plus(it.invoicePaidAmount)
                     }
                     CashRequirementBalanceEnum.TWO -> {
                        tempVendor.vendorTotals.weekTwoDue =
                           tempVendor.vendorTotals.weekTwoDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        tempVendor.vendorTotals.weekTwoPaid =
                           tempVendor.vendorTotals.weekTwoPaid.plus(it.invoicePaidAmount)
                     }
                     CashRequirementBalanceEnum.THREE -> {
                        tempVendor.vendorTotals.weekThreeDue =
                           tempVendor.vendorTotals.weekThreeDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        tempVendor.vendorTotals.weekThreePaid =
                           tempVendor.vendorTotals.weekThreePaid.plus(it.invoicePaidAmount)
                     }
                     CashRequirementBalanceEnum.FOUR -> {
                        tempVendor.vendorTotals.weekFourDue =
                           tempVendor.vendorTotals.weekFourDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        tempVendor.vendorTotals.weekFourPaid =
                           tempVendor.vendorTotals.weekFourPaid.plus(it.invoicePaidAmount)
                     }
                     CashRequirementBalanceEnum.FIVE -> {
                        tempVendor.vendorTotals.weekFiveDue =
                           tempVendor.vendorTotals.weekFiveDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        tempVendor.vendorTotals.weekFivePaid =
                           tempVendor.vendorTotals.weekFivePaid.plus(it.invoicePaidAmount)
                     }
                  }

                  when (it.balanceDisplay) {
                     CashRequirementBalanceEnum.ONE -> {
                        cashoutTotals.weekOneDue =
                           cashoutTotals.weekOneDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        cashoutTotals.weekOnePaid =
                           cashoutTotals.weekOnePaid.plus(it.invoicePaidAmount)
                     }
                     CashRequirementBalanceEnum.TWO -> {
                        cashoutTotals.weekTwoDue =
                           cashoutTotals.weekTwoDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        cashoutTotals.weekTwoPaid =
                           cashoutTotals.weekTwoPaid.plus(it.invoicePaidAmount)
                     }
                     CashRequirementBalanceEnum.THREE -> {
                        cashoutTotals.weekThreeDue =
                           cashoutTotals.weekThreeDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        cashoutTotals.weekThreePaid =
                           cashoutTotals.weekThreePaid.plus(it.invoicePaidAmount)
                     }
                     CashRequirementBalanceEnum.FOUR -> {
                        cashoutTotals.weekFourDue =
                           cashoutTotals.weekFourDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        cashoutTotals.weekFourPaid =
                           cashoutTotals.weekFourPaid.plus(it.invoicePaidAmount)
                     }
                     CashRequirementBalanceEnum.FIVE -> {
                        cashoutTotals.weekFiveDue =
                           cashoutTotals.weekFiveDue.plus(it.invoiceAmount - it.invoicePaidAmount - it.invoiceDiscountTaken)
                        cashoutTotals.weekFivePaid =
                           cashoutTotals.weekFivePaid.plus(it.invoicePaidAmount)
                     }
                  }
               }
            }
         } while (rs.next())

         vendors.removeIf { it.invoices?.size == 0 }
      }

      val entity = AccountPayableCashRequirementEntity(vendors, cashoutTotals)

      return AccountPayableCashRequirementDTO(entity)
   }

   private fun mapRow(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): CashRequirementVendorEntity {
      return CashRequirementVendorEntity(
         vendorCompanyId = rs.getUuid("${columnPrefix}vendor_company_id"),
         vendorNumber = rs.getInt("${columnPrefix}vendor_number"),
         vendorName = rs.getString("${columnPrefix}vendor_name"),
      )
   }

   private fun mapRowInvoiceDetail(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): CashRequirementReportInvoiceDetailEntity {
      val invoiceAmount = rs.getBigDecimal("${columnPrefix}apInvoice_invoice_amount")
      val invoicePaidAmount = rs.getBigDecimal("${columnPrefix}apInvoice_paid_amount")
      val invoiceDiscountTaken = rs.getBigDecimal("${columnPrefix}apInvoice_discount_taken")
      val balance = invoiceAmount - invoicePaidAmount

      return CashRequirementReportInvoiceDetailEntity(
         invoiceCompanyId = rs.getUuid("${columnPrefix}apInvoice_company_id"),
         invoiceVendorId = rs.getUuid("${columnPrefix}apInvoice_vendor_id"),
         invoice = rs.getString("${columnPrefix}apInvoice_invoice"),
         invoiceDate = rs.getLocalDate("${columnPrefix}apInvoice_invoice_date"),
         invoiceAmount = invoiceAmount,
         invoiceDiscountAmount = rs.getBigDecimal("${columnPrefix}apInvoice_discount_amount"),
         invoiceExpenseDate = rs.getLocalDate("${columnPrefix}apInvoice_expense_date"),
         invoicePaidAmount = invoicePaidAmount,
         invoiceDiscountTaken = invoiceDiscountTaken,
         invoiceStatus = statusRepository.mapRow(rs, "${columnPrefix}apInvoice_status_"),
         invoiceDueDate = rs.getLocalDate("${columnPrefix}apInvoice_due_date"),
         apPaymentPaymentDate = rs.getLocalDateOrNull("${columnPrefix}apPayment_payment_date"),
         apPaymentStatusId = rs.getIntOrNull( "${columnPrefix}apPayment_status_id"),
         apPaymentStatusValue = rs.getString("${columnPrefix}apPayment_status_value"),
         apPaymentDateVoided = rs.getLocalDateOrNull("${columnPrefix}apPayment_date_voided"),
         apPaymentIsVoided = rs.getBoolean("${columnPrefix}apPayment_void_interfaced_indicator"),
         apPaymentDetailAmount = rs.getBigDecimalOrNull("${columnPrefix}apPaymentDetail_amount"),
         balance = balance,
         balanceDisplay = null
      )
   }

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam"
      else if (begin) " >= :$beginningParam"
      else " <= :$endingParam"
   }
}
