package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.ExpenseReportFilterRequest
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableDistDetailReportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableExpenseReportAccountProfitCenterPair
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableExpenseReportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableExpenseReportExportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInventoryReportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayablePaymentDetailReportDTO
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.core.util.StringUtils
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.time.LocalDate

@Singleton
class AccountPayableExpenseReportRepository @Inject constructor(
   private val jdbc: Jdbi,
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableExpenseReportRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            apExpense.id                                                AS apExpense_id,
            apExpense.company_id                                        AS apExpense_company_id,
            apExpense.invoice                                           AS apExpense_invoice,

            apExpense.employee_number_id_sfk                            AS apExpense_operator,
            apExpense.use_tax_indicator                                 AS apExpense_use_tax_indicator,

            vendor.name                                                 AS apExpense_vendor_name,
            vendor.number                                               AS apExpense_vendor_number,
            vgrp.value                                                  AS apExpense_vendor_vgrp_value,
            invType.value                                               AS apExpense_type_value,
            poHeader.number                                             AS poHeader_number,
            apExpense.invoice_date                                      AS apExpense_invoice_date,
            apExpense.entry_date                                        AS apExpense_entry_date,
            invStatus.value                                             AS apExpense_status_value,
            apExpense.invoice_amount                                    AS apExpense_invoice_amount,
            apExpense.discount_taken                                    AS apExpense_discount_taken,
            apExpense.due_date                                          AS apExpense_due_date,
            apExpense.expense_date                                      AS apExpense_expense_date,
            apExpense.paid_amount                                       AS apExpense_paid_amount,

            bank.number                                                 AS bank_number,
            pmtType.value                                               AS apPayment_type_value,
            pmt.payment_number                                          AS apPayment_number,
            pmt.payment_date                                            AS apPayment_payment_date,
            pmtDetail.id                                                AS apPayment_detail_id,
            pmtDetail.amount                                            AS apPayment_detail_amount,

            apExpense.message                                           AS apExpense_message,
            account.number                                              AS apPayment_account_number,
            account.name                                                AS apPayment_account_name,
            invDist.distribution_profit_center_id_sfk                   AS apPayment_dist_center,
            invDist.distribution_amount                                 AS apPayment_dist_amount,
            CASE WHEN (apControl.id IS null) THEN false ELSE true END   AS apPayment_account_for_inventory,

            apExpense.receive_date                                      AS apInv_received_date,

            count(*) OVER()                                             AS total_elements
         FROM account_payable_invoice apExpense
            JOIN account_payable_invoice_type_domain invType            ON invType.id = apExpense.type_id
            JOIN account_payable_invoice_status_type_domain invStatus   ON invStatus.id = apExpense.status_id
            JOIN vendor                                                 ON apExpense.vendor_id = vendor.id AND vendor.deleted = FALSE
            LEFT JOIN vendor_group vgrp                                 ON vgrp.id = vendor.vendor_group_id AND vgrp.deleted = FALSE
            LEFT JOIN purchase_order_header poHeader                    ON poHeader.id = apExpense.purchase_order_id AND poHeader.deleted = FALSE
            LEFT JOIN account_payable_payment_detail pmtDetail          ON apExpense.id = pmtDetail.account_payable_invoice_id
            LEFT JOIN account_payable_payment pmt                       ON pmtDetail.payment_number_id = pmt.id
            LEFT JOIN account_payable_payment_type_type_domain pmtType  ON pmt.account_payable_payment_type_id = pmtType.id
            LEFT JOIN bank                                              ON pmt.bank_id = bank.id AND bank.deleted = FALSE
            JOIN account_payable_invoice_distribution invDist           ON apExpense.id = invDist.invoice_id
            JOIN account                                                ON invDist.distribution_account_id = account.id AND account.deleted = FALSE
            LEFT JOIN account_payable_control apControl                 ON invDist.distribution_account_id = apControl.general_ledger_inventory_account_id
            JOIN company comp                                           ON apExpense.company_id = comp.id AND comp.deleted = FALSE
      """
   }

   @ReadOnly
   fun fetchReport(company: CompanyEntity, filterRequest: ExpenseReportFilterRequest): List<AccountPayableExpenseReportAccountProfitCenterPair> {
      var reportData = mutableListOf<AccountPayableExpenseReportAccountProfitCenterPair>()
      var addExpense = true
      var currentPO: AccountPayableExpenseReportAccountProfitCenterPair? = null
      var currentExpense: AccountPayableExpenseReportDTO? = null
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE apExpense.company_id = :comp_id ")

      if (filterRequest.beginAcct != null && filterRequest.endAcct != null) {
         params["beginAcct"] = filterRequest.beginAcct
         params["endAcct"] = filterRequest.endAcct
         whereClause.append(" AND account.number ")
            .append(buildNumberFilterString("beginAcct", "endAcct"))
      }

      if (filterRequest.beginVen != null && filterRequest.endVen != null) {
         params["beginVen"] = filterRequest.beginVen
         params["endVen"] = filterRequest.endVen
         whereClause.append(" AND vendor.number ")
            .append(buildNumberFilterString("beginVen", "endVen"))
      }

      if (filterRequest.beginDate != null || filterRequest.endDate != null) {
         params["beginDate"] = filterRequest.beginDate
         params["endDate"] = filterRequest.endDate
         whereClause.append(" AND ((apExpense.expense_date ")
            .append(buildDateFilterString(filterRequest.beginDate, filterRequest.endDate, "beginDate", "endDate")).append(")")
            .append(" OR (apExpense.date_voided ")
            .append(buildDateFilterString(filterRequest.beginDate, filterRequest.endDate, "beginDate", "endDate")).append(")")
            .append(" OR (pmt.payment_date ")
            .append(buildDateFilterString(filterRequest.beginDate, filterRequest.endDate, "beginDate", "endDate")).append(")")
            .append(" OR (pmt.date_voided ")
            .append(buildDateFilterString(filterRequest.beginDate, filterRequest.endDate, "beginDate", "endDate")).append(")").append(")")
      }

      filterRequest.invStatus?.let {
         params["statuses"] = filterRequest.invStatus
         whereClause.append(" AND invStatus.value IN (<statuses>) ")
      }

      if (!filterRequest.iclHoldInv!!) {
         whereClause.append(" AND invStatus.value != 'H' ")
      }

      val ordering = " ORDER BY apExpense.id ${filterRequest.sortDirection()}, pmt.id, pmtDetail.id "

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            $ordering
         """.trimIndent(),
         params)
      { rs, elements ->
         do {

            val tempExpense = if (currentExpense?.id != rs.getUuid(("apExpense_id"))) {
               val localExpense  = mapExpense(rs, "apExpense_", "apPayment_")
               currentExpense = localExpense
               addExpense = true

               localExpense
            } else {
               addExpense = false

               currentExpense
            }

            mapExpenseDetailOrNull(rs)?.let { tempExpense?.invoiceDetails?.add(it) }
            tempExpense?.distDetails?.add(mapDistDetail(rs))

            val tempPO = if (currentPO?.poHeaderNumber != rs.getIntOrNull("poHeader_number")) {
               val localPO = mapPO(rs)
               reportData.add(localPO)
               currentPO = localPO

               localPO
            } else {
               currentPO
            }

            if (addExpense) {
               tempPO?.invoices?.add(tempExpense)
               addExpense = false
            }

         } while (rs.next())
      }

      if (filterRequest.sortBy() == "apExpense.invoice") {
         fun extractExpenseNumber(invoice: String) = invoice.substringBefore("-").substringAfter(":")

         val compareByExpense = compareBy<AccountPayableExpenseReportAccountProfitCenterPair> {
            extractExpenseNumber(it.invoices.first()!!.invoice!!).length
         }.thenBy {
            extractExpenseNumber(it.invoices.first()!!.invoice!!)
         }
         reportData.sortWith(compareByExpense)
      } else if (filterRequest.snakeSortBy() == "vendor.number") {
         val compareByVendorNumber = compareBy<AccountPayableExpenseReportAccountProfitCenterPair> {
            it.invoices.first()!!.vendorNumber
         }
         reportData.sortWith(compareByVendorNumber)
      } else if (filterRequest.snakeSortBy() == "vendor.name") {
         val compareByVendorName = compareBy<AccountPayableExpenseReportAccountProfitCenterPair> {
            it.invoices.first()!!.vendorName
         }
         reportData.sortWith(compareByVendorName)
      }

      if (filterRequest.sortDirection() == "DESC") reportData.reverse()

      return reportData
   }

   @ReadOnly
   fun export(company: CompanyEntity, filterRequest: ExpenseReportFilterRequest): List<AccountPayableExpenseReportExportDTO> {
      val invoices = mutableListOf<AccountPayableExpenseReportExportDTO>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE apExpense.company_id = :comp_id ")

      if (filterRequest.beginVen != null && filterRequest.endVen != null) {
         params["beginVen"] = filterRequest.beginVen
         params["endVen"] = filterRequest.endVen
         whereClause.append(" AND vendor.number ")
            .append(buildNumberFilterString("beginVen", "endVen"))
      }

      filterRequest.invStatus?.let {
         params["status"] = filterRequest.invStatus
         whereClause.append(" AND invStatus.value = :status ")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY apExpense.id, pmt.id, pmtDetail.id
         """.trimIndent(),
         params)
      { rs, elements ->
         do {
            invoices.add(mapExpenseExport(rs, "apExpense_", "apPayment_"))
         } while (rs.next())
      }

      return invoices
   }

   private fun mapPO(
      rs: ResultSet,
   ): AccountPayableExpenseReportAccountProfitCenterPair {
      return AccountPayableExpenseReportAccountProfitCenterPair(
         poHeaderNumber = rs.getIntOrNull("poHeader_number"),
      )
   }

   private fun mapExpense(
      rs: ResultSet,
      columnPrefix: String = EMPTY,
      paymentPrefix: String = EMPTY
   ): AccountPayableExpenseReportDTO {
      return AccountPayableExpenseReportDTO(
         id = rs.getUuid("${columnPrefix}id"),
         vendorNumber = rs.getInt("${columnPrefix}vendor_number"),
         vendorName = rs.getString("${columnPrefix}vendor_name"),
         vendorGroup = rs.getString("${columnPrefix}vendor_vgrp_value"),
         invoice = rs.getString("${columnPrefix}invoice"),
         operator = rs.getInt("${columnPrefix}operator"),
         useTax = rs.getBoolean("${columnPrefix}use_tax_indicator"),
         type = rs.getString("${columnPrefix}type_value"),
         invoiceDate = rs.getLocalDate("${columnPrefix}invoice_date"),
         entryDate = rs.getLocalDate("${columnPrefix}entry_date"),
         status = rs.getString("${columnPrefix}status_value"),
         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         discountTaken = rs.getBigDecimal("${columnPrefix}discount_taken"),
         dueDate = rs.getLocalDate("${columnPrefix}due_date"),
         expenseDate = rs.getLocalDate("${columnPrefix}expense_date"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paid_amount"),
         bankNumber = rs.getInt("bank_number"),
         pmtType = rs.getString("${paymentPrefix}type_value"),
         pmtNumber = rs.getString("${paymentPrefix}number"),
         notes = rs.getString("${columnPrefix}message"),
         acctNumber = rs.getInt("${paymentPrefix}account_number"),
         acctName = rs.getString("${paymentPrefix}account_name"),
         distCenter = rs.getString("${paymentPrefix}dist_center"),
         distAmount = rs.getBigDecimal("${paymentPrefix}dist_amount"),
      )
   }

   private fun mapExpenseExport(
      rs: ResultSet,
      columnPrefix: String = EMPTY,
      paymentPrefix: String = EMPTY
   ): AccountPayableExpenseReportExportDTO {
      return AccountPayableExpenseReportExportDTO(
         id = rs.getUuid("${columnPrefix}id"),
         vendorNumber = rs.getInt("${columnPrefix}vendor_number"),
         vendorName = rs.getString("${columnPrefix}vendor_name"),
         vendorGroup = rs.getString("${columnPrefix}vendor_vgrp_value"),
         invoice = rs.getString("${columnPrefix}invoice"),
         type = rs.getString("${columnPrefix}type_value"),
         invoiceDate = rs.getLocalDate("${columnPrefix}invoice_date"),
         entryDate = rs.getLocalDate("${columnPrefix}entry_date"),
         status = rs.getString("${columnPrefix}status_value"),
         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         discountTaken = rs.getBigDecimal("${columnPrefix}discount_taken"),
         poHeaderNumber = rs.getInt("poHeader_number"),
         dueDate = rs.getLocalDate("${columnPrefix}due_date"),
         expenseDate = rs.getLocalDate("${columnPrefix}expense_date"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paid_amount"),
         bankNumber = rs.getInt("bank_number"),
         pmtNumber = rs.getString("${paymentPrefix}number"),
         acctNumber = rs.getInt("${paymentPrefix}account_number"),
         acctName = rs.getString("${paymentPrefix}account_name"),
         distCenter = rs.getString("${paymentPrefix}dist_center"),
         distAmount = rs.getBigDecimal("${paymentPrefix}dist_amount"),
      )
   }

   private fun mapExpenseDetailOrNull(
      rs: ResultSet
   ): AccountPayablePaymentDetailReportDTO? =
      if (StringUtils.isNotEmpty(rs.getString("apPayment_detail_id"))) {
         mapExpenseDetail(rs)
      } else {
         null
      }

   private fun mapExpenseDetail(
      rs: ResultSet
   ): AccountPayablePaymentDetailReportDTO {
      return AccountPayablePaymentDetailReportDTO(
         bankNumber = rs.getInt("bank_number"),
         paymentType = rs.getString("apPayment_type_value"),
         paymentNumber = rs.getString("apPayment_number"),
         paymentDate = rs.getLocalDateOrNull("apPayment_payment_date"),
         paymentDetailId = rs.getString("apPayment_detail_id"),
         paymentDetailAmount = rs.getBigDecimalOrNull("apPayment_detail_amount"),
      )
   }

   private fun mapDistDetail(
      rs: ResultSet
   ): AccountPayableDistDetailReportDTO {
      return AccountPayableDistDetailReportDTO(
         accountNumber = rs.getInt("apPayment_account_number"),
         accountName = rs.getString("apPayment_account_name"),
         distProfitCenter = rs.getInt("apPayment_dist_center"),
         distAmount = rs.getBigDecimal("apPayment_dist_amount"),
         isAccountForInventory = rs.getBoolean("apPayment_account_for_inventory"),
      )
   }

   private fun mapInventory(
      rs: ResultSet
   ): AccountPayableInventoryReportDTO {
      return AccountPayableInventoryReportDTO(
         invoiceNumber = rs.getString("inv_invoice_number"),
         modelNumber = rs.getString("inv_model_number"),
         serialNumber = rs.getString("inv_serial_number"),
         description = rs.getString("inv_description"),
         cost = rs.getBigDecimal("inv_actual_cost"),
         received = rs.getString("inv_received_date"),
         status = rs.getString("inv_status"),
         receivedLocation = rs.getString("inv_received_location"),
         currentLocation = rs.getString("inv_current_location"),
      )
   }

   private fun buildDateFilterString(from: LocalDate?, thru: LocalDate?, frmParam: String, thruParam: String): String {
      return if (from != null && thru != null) " BETWEEN :$frmParam AND :$thruParam "
      else if (from != null) " > :$frmParam "
      else " < :$thruParam "
   }

   private fun buildNumberFilterString(beginningParam: String, endingParam: String): String {
      return " BETWEEN :$beginningParam AND :$endingParam "
   }
}
