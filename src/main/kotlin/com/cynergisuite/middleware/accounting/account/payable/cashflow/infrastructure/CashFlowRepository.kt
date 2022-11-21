package com.cynergisuite.middleware.accounting.account.payable.cashflow.infrastructure

import com.cynergisuite.domain.CashFlowFilterRequest
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.account.payable.cashflow.AccountPayableCashFlowDTO
import com.cynergisuite.middleware.accounting.account.payable.cashflow.AccountPayableCashFlowEntity
import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowBalanceEntity
import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowReportInvoiceDetailEntity
import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowVendorEntity
import com.cynergisuite.middleware.accounting.account.payable.cashout.CashRequirementBalanceEnum
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentDetailRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDate
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

@Singleton
class CashFlowRepository @Inject constructor(
   private val jdbc:Jdbi,
   private val apPaymentDetailRepository:AccountPayablePaymentDetailRepository,
   private val statusRepository:AccountPayableInvoiceStatusTypeRepository
){
   private val logger: Logger = LoggerFactory.getLogger(CashFlowRepository:: class.java)

   private fun selectBaseQuery ():String {
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
               apInvoice.discount_date                                        AS apInvoiceDetail_apInvoice_discount_date,
               apInvoice.discount_percent                                     AS apInvoiceDetail_apInvoice_discount_percent,
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
               count(*) OVER() AS total_elements
            FROM account_payable_invoice apInvoice
               JOIN vendor vend ON apInvoice.vendor_id = vend.id AND vend.deleted = FALSE
               JOIN account_payable_invoice_status_type_domain status ON apInvoice.status_id = status.id
         """
   }

   @ReadOnly
   fun findAll (company: CompanyEntity, filterRequest: CashFlowFilterRequest): AccountPayableCashFlowDTO {
      val vendors = mutableListOf <CashFlowVendorEntity> ()
      var currentVendor:CashFlowVendorEntity ? = null
      val cashflowTotals = CashFlowBalanceEntity()
      val params = mutableMapOf < String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE apInvoice.company_id = :comp_id AND apInvoice.status_id = 2")
      val dates = mutableListOf <LocalDate> ()

      //This is finding oldest and newest dates from a filter request in the case a user doesn't provide all date filters
      CashFlowFilterRequest:: class.memberProperties.forEach {
         if (it.returnType.jvmErasure.java == LocalDate:: class.java){
            val date = if (it.getter.call(filterRequest) != null) {
               it.getter.call(filterRequest) as LocalDate
            } else null
            if (date != null) {
               dates.add(date)
            }
         }
      }
      val oldestDate = dates.stream().min(LocalDate::compareTo).get()
      val newestDate = dates.stream().max(LocalDate::compareTo).get()

      if (oldestDate != null || newestDate != null) {
         params["fromDate"] = oldestDate
         params["thruDate"] = newestDate
         whereClause.append(" AND apInvoice.due_date ")
            .append(buildFilterString(oldestDate != null, newestDate != null, "fromDate", "thruDate"))
      }

      jdbc.query(
         """
               ${selectBaseQuery()}
               $whereClause
               ORDER BY ${filterRequest.snakeSortBy()} ${filterRequest.sortDirection()}
            """.trimIndent(),
         params,
         ) {
         rs, elements ->
         do {
            val tempVendor = if (currentVendor ?.vendorNumber != rs.getIntOrNull("apInvoiceDetail_vendor_number")){
               val localVendor = mapRow(rs, "apInvoiceDetail_")
               vendors.add(localVendor)
               currentVendor = localVendor

               localVendor
            } else{
               currentVendor !!
            }

            var invoiceFlag = false
            mapRowInvoiceDetail(rs, "apInvoiceDetail_").let {
               if (it.invoiceStatus.id == 2) {
                  tempVendor.invoices ?.add(it)
                  invoiceFlag = true
               }

               if (invoiceFlag) {
                  if (filterRequest.fromDateOne != null && filterRequest.thruDateOne != null) {
                     if (filterRequest.fromDateOne !!.
                     compareTo(it.invoiceDueDate) * it.invoiceDueDate.compareTo(filterRequest.thruDateOne) >= 0){
                        it.balanceDisplay = CashRequirementBalanceEnum.ONE
                     }
                  }
                  if (filterRequest.fromDateTwo != null && filterRequest.thruDateTwo != null) {
                     if (filterRequest.fromDateTwo !!.
                     compareTo(it.invoiceDueDate) * it.invoiceDueDate.compareTo(filterRequest.thruDateTwo) >= 0){
                        it.balanceDisplay = CashRequirementBalanceEnum.TWO
                     }
                  }
                  if (filterRequest.fromDateThree != null && filterRequest.thruDateThree != null) {
                     if (filterRequest.fromDateThree !!.
                     compareTo(it.invoiceDueDate) * it.invoiceDueDate.compareTo(filterRequest.thruDateThree) >= 0){
                        it.balanceDisplay = CashRequirementBalanceEnum.THREE
                     }
                  }
                  if (filterRequest.fromDateFour != null && filterRequest.thruDateFour != null) {
                     if (filterRequest.fromDateFour !!.
                     compareTo(it.invoiceDueDate) * it.invoiceDueDate.compareTo(filterRequest.thruDateFour) >= 0){
                        it.balanceDisplay = CashRequirementBalanceEnum.FOUR
                     }
                  }
                  if (filterRequest.fromDateFive != null && filterRequest.thruDateFive != null) {
                     if (filterRequest.fromDateFive !!.
                     compareTo(it.invoiceDueDate) * it.invoiceDueDate.compareTo(filterRequest.thruDateFive) >= 0){
                        it.balanceDisplay = CashRequirementBalanceEnum.FIVE
                     }
                  }

                  //sorts out discount taken / discount lost
                  if (it.invoiceDiscountAmount!! > BigDecimal.ZERO) {
                     if (it.invoiceDiscountDate!! > LocalDate.now()) {
                        it.discountAmount = it.invoiceDiscountAmount!!.times(it.invoiceDiscountPercent!!)
                        it.balance = it.invoiceAmount - it.invoicePaidAmount - it.discountAmount!!
                        tempVendor.vendorTotals.discountTaken = tempVendor.vendorTotals.discountTaken.plus(it.discountAmount!!)
                        cashflowTotals.discountTaken = cashflowTotals.discountTaken.plus(it.discountAmount!!)
                     } else {
                        it.lostAmount = it.invoiceDiscountAmount!!.times(it.invoiceDiscountPercent!!)
                        it.balance = it.invoiceAmount - it.invoicePaidAmount
                        tempVendor.vendorTotals.discountLost = tempVendor.vendorTotals.discountLost.plus(it.lostAmount!!)
                        cashflowTotals.discountLost = cashflowTotals.discountLost.plus(it.lostAmount!!)
                     }
                  }


                  when(it.balanceDisplay) {
                     CashRequirementBalanceEnum.ONE ->{
                        tempVendor.vendorTotals.dateOneAmount =
                           tempVendor.vendorTotals.dateOneAmount.plus(it.balance)
                        cashflowTotals.dateOneAmount =
                           cashflowTotals.dateOneAmount.plus(it.balance)

                     }
                     CashRequirementBalanceEnum.TWO ->{
                        tempVendor.vendorTotals.dateTwoAmount =
                           tempVendor.vendorTotals.dateTwoAmount.plus(it.balance)
                        cashflowTotals.dateTwoAmount =
                           cashflowTotals.dateTwoAmount.plus(it.balance)
                     }
                     CashRequirementBalanceEnum.THREE ->{
                        tempVendor.vendorTotals.dateThreeAmount =
                           tempVendor.vendorTotals.dateThreeAmount.plus(it.balance)
                        cashflowTotals.dateThreeAmount =
                           cashflowTotals.dateThreeAmount.plus(it.balance)
                     }
                     CashRequirementBalanceEnum.FOUR ->{
                        tempVendor.vendorTotals.dateFourAmount =
                           tempVendor.vendorTotals.dateFourAmount.plus(it.balance)
                        cashflowTotals.dateFourAmount =
                           cashflowTotals.dateFourAmount.plus(it.balance)
                     }
                     CashRequirementBalanceEnum.FIVE ->{
                        tempVendor.vendorTotals.dateFiveAmount =
                           tempVendor.vendorTotals.dateFiveAmount.plus(it.balance)
                        cashflowTotals.dateFiveAmount =
                           cashflowTotals.dateFiveAmount.plus(it.balance)

                     }
                  }
               }
            }
         } while (rs.next())

         vendors.removeIf {
            it.invoices ?.size == 0
         }
      }

      val entity = AccountPayableCashFlowEntity(vendors, cashflowTotals)

      return AccountPayableCashFlowDTO(entity)
   }

   private fun mapRow(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): CashFlowVendorEntity {
      return CashFlowVendorEntity(
         vendorCompanyId = rs.getUuid("${columnPrefix}vendor_company_id"),
         vendorNumber = rs.getInt("${columnPrefix}vendor_number"),
         vendorName = rs.getString("${columnPrefix}vendor_name"),
      )
   }

   private fun mapRowInvoiceDetail(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): CashFlowReportInvoiceDetailEntity {
      val invoiceAmount = rs.getBigDecimal("${columnPrefix}apInvoice_invoice_amount")
      val invoicePaidAmount = rs.getBigDecimal("${columnPrefix}apInvoice_paid_amount")
      val invoiceDiscountTaken = rs.getBigDecimal("${columnPrefix}apInvoice_discount_taken")
      val invoiceDiscountDate = rs.getLocalDateOrNull("${columnPrefix}apInvoice_discount_date")
      val invoiceDiscountPercent = rs.getBigDecimal("${columnPrefix}apInvoice_discount_percent")
      val balance = invoiceAmount - invoicePaidAmount

      return CashFlowReportInvoiceDetailEntity(
         invoiceCompanyId = rs.getUuid("${columnPrefix}apInvoice_company_id"),
         invoiceVendorId = rs.getUuid("${columnPrefix}apInvoice_vendor_id"),
         invoice = rs.getString("${columnPrefix}apInvoice_invoice"),
         invoiceDate = rs.getLocalDate("${columnPrefix}apInvoice_invoice_date"),
         invoiceAmount = invoiceAmount,
         invoiceDiscountAmount = rs.getBigDecimal("${columnPrefix}apInvoice_discount_amount"),
         invoiceExpenseDate = rs.getLocalDate("${columnPrefix}apInvoice_expense_date"),
         invoicePaidAmount = invoicePaidAmount,
         invoiceDiscountTaken = invoiceDiscountTaken,
         invoiceDiscountPercent = invoiceDiscountPercent,
         invoiceDiscountDate = invoiceDiscountDate,
         invoiceStatus = statusRepository.mapRow(rs, "${columnPrefix}apInvoice_status_"),
         invoiceDueDate = rs.getLocalDate("${columnPrefix}apInvoice_due_date"),
         discountAmount = null,
         lostAmount = null,
         balance = balance,
         balanceDisplay = null
      )
   }

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam "
      else if (begin) " > :$beginningParam "
      else " < :$endingParam "
   }
}

