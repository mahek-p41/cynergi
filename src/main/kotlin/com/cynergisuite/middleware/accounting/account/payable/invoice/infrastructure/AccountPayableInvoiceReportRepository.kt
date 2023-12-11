package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.InvoiceReportFilterRequest
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableDistDetailReportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInventoryReportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceReportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceReportExportDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceReportPoWrapper
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
class AccountPayableInvoiceReportRepository @Inject constructor(
   private val jdbc: Jdbi,
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceReportRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            apInvoice.id                                                AS apInvoice_id,
            apInvoice.company_id                                        AS apInvoice_company_id,
            apInvoice.invoice                                           AS apInvoice_invoice,

            apInvoice.employee_number_id_sfk                            AS apInvoice_operator,
            apInvoice.use_tax_indicator                                 AS apInvoice_use_tax_indicator,

            vendor.name                                                 AS apInvoice_vendor_name,
            vendor.number                                               AS apInvoice_vendor_number,
            vgrp.value                                                  AS apInvoice_vendor_vgrp_value,
            invType.value                                               AS apInvoice_type_value,
            poHeader.number                                             AS poHeader_number,
            apInvoice.invoice_date                                      AS apInvoice_invoice_date,
            apInvoice.entry_date                                        AS apInvoice_entry_date,
            invStatus.value                                             AS apInvoice_status_value,
            apInvoice.invoice_amount                                    AS apInvoice_invoice_amount,
            apInvoice.discount_taken                                    AS apInvoice_discount_taken,
            apInvoice.due_date                                          AS apInvoice_due_date,
            apInvoice.expense_date                                      AS apInvoice_expense_date,
            apInvoice.paid_amount                                       AS apInvoice_paid_amount,

            bank.number                                                 AS bank_number,
            pmtType.value                                               AS apPayment_type_value,
            pmt.payment_number                                          AS apPayment_number,
            pmt.payment_date                                            AS apPayment_payment_date,
            pmtDetail.id                                                AS apPayment_detail_id,
            pmtDetail.amount                                            AS apPayment_detail_amount,

            apInvoice.message                                           AS apInvoice_message,
            account.number                                              AS apPayment_account_number,
            account.name                                                AS apPayment_account_name,
            invDist.distribution_profit_center_id_sfk                   AS apPayment_dist_center,
            invDist.distribution_amount                                 AS apPayment_dist_amount,
            CASE WHEN (apControl.id IS null) THEN false ELSE true END   AS apPayment_account_for_inventory,

            inv.invoice_number                                          AS inv_invoice_number,
            inv.model_number                                            AS inv_model_number,
            inv.serial_number                                           AS inv_serial_number,
            inv.description                                             AS inv_description,
            inv.actual_cost                                             AS inv_actual_cost,
            inv.received_date                                           AS inv_received_date,
            apInvoice.receive_date                                      AS apInv_received_date,
            inv.status                                                  AS inv_status,
            inv.primary_location                                        AS inv_received_location,
            inv.location                                                AS inv_current_location,

            count(*) OVER()                                             AS total_elements
         FROM account_payable_invoice apInvoice
            JOIN account_payable_invoice_type_domain invType            ON invType.id = apInvoice.type_id
            JOIN account_payable_invoice_status_type_domain invStatus   ON invStatus.id = apInvoice.status_id
            JOIN vendor                                                 ON apInvoice.vendor_id = vendor.id AND vendor.deleted = FALSE
            LEFT JOIN vendor_group vgrp                                 ON vgrp.id = vendor.vendor_group_id AND vgrp.deleted = FALSE
            LEFT JOIN purchase_order_header poHeader                    ON poHeader.id = apInvoice.purchase_order_id AND poHeader.deleted = FALSE
            LEFT JOIN account_payable_payment_detail pmtDetail          ON apInvoice.id = pmtDetail.account_payable_invoice_id
            LEFT JOIN account_payable_payment pmt                       ON pmtDetail.payment_number_id = pmt.id
            LEFT JOIN account_payable_payment_type_type_domain pmtType  ON pmt.account_payable_payment_type_id = pmtType.id
            LEFT JOIN bank                                              ON pmt.bank_id = bank.id AND bank.deleted = FALSE
            JOIN account_payable_invoice_distribution invDist           ON apInvoice.id = invDist.invoice_id
            JOIN account                                                ON invDist.distribution_account_id = account.id AND account.deleted = FALSE
            LEFT JOIN account_payable_control apControl                 ON invDist.distribution_account_id = apControl.general_ledger_inventory_account_id
            JOIN company comp                                           ON apInvoice.company_id = comp.id AND comp.deleted = FALSE
            LEFT JOIN fastinfo_prod_import.inventory_vw inv ON
                  comp.dataset_code = inv.dataset
                  AND inv.invoice_number = apInvoice.invoice
                  AND CASE
                        WHEN LEFT(apInvoice.invoice, 2) = 'P:' THEN inv.received_date = apInvoice.receive_date
                        ELSE true
                      END
      """
   }

   @ReadOnly
   fun fetchReport(company: CompanyEntity, filterRequest: InvoiceReportFilterRequest): List<AccountPayableInvoiceReportPoWrapper> {
      var purchaseOrders = mutableListOf<AccountPayableInvoiceReportPoWrapper>()
      var addInvoice = true
      var currentPO: AccountPayableInvoiceReportPoWrapper? = null
      var currentInvoice: AccountPayableInvoiceReportDTO? = null
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE apInvoice.company_id = :comp_id ")

      if (filterRequest.beginVen != null && filterRequest.endVen != null) {
         params["beginVen"] = filterRequest.beginVen
         params["endVen"] = filterRequest.endVen
         whereClause.append(" AND vendor.number ")
            .append(buildNumberFilterString("beginVen", "endVen"))
      }

      if (filterRequest.beginOpr != null && filterRequest.endOpr != null) {
         params["beginOpr"] = filterRequest.beginOpr
         params["endOpr"] = filterRequest.endOpr
         whereClause.append(" AND apInvoice.employee_number_id_sfk ")
            .append(buildNumberFilterString("beginOpr", "endOpr"))
      }

      if (filterRequest.beginPO != null && filterRequest.endPO != null) {
         params["beginPO"] = filterRequest.beginPO
         params["endPO"] = filterRequest.endPO
         whereClause.append(" AND poHeader.number ")
            .append(buildNumberFilterString("beginPO", "endPO"))
      }

      if (filterRequest.beginInvDate != null || filterRequest.endInvDate != null) {
         params["beginInvDate"] = filterRequest.beginInvDate
         params["endInvDate"] = filterRequest.endInvDate
         whereClause.append(" AND apInvoice.invoice_date ")
            .append(buildDateFilterString(filterRequest.beginInvDate, filterRequest.endInvDate, "beginInvDate", "endInvDate"))
      }

      if (filterRequest.beginExpDate != null || filterRequest.endExpDate != null) {
         params["beginExpDate"] = filterRequest.beginExpDate
         params["endExpDate"] = filterRequest.endExpDate
         whereClause.append(" AND apInvoice.expense_date ")
            .append(buildDateFilterString(filterRequest.beginExpDate, filterRequest.endExpDate, "beginExpDate", "endExpDate"))
      }

      if (filterRequest.beginEnDate != null || filterRequest.endEnDate != null) {
         params["beginEnDate"] = filterRequest.beginEnDate
         params["endEnDate"] = filterRequest.endEnDate
         whereClause.append(" AND apInvoice.entry_date ")
            .append(buildDateFilterString(filterRequest.beginEnDate, filterRequest.endEnDate, "beginEnDate", "endEnDate"))
      }

      if (filterRequest.beginPaidDate != null || filterRequest.endPaidDate != null) {
         params["beginPaidDate"] = filterRequest.beginPaidDate
         params["endPaidDate"] = filterRequest.endPaidDate
         whereClause.append(" AND pmt.payment_date ")
            .append(buildDateFilterString(filterRequest.beginPaidDate, filterRequest.endPaidDate, "beginPaidDate", "endPaidDate"))
      }

      if (filterRequest.beginDueDate != null || filterRequest.endDueDate != null) {
         params["beginDueDate"] = filterRequest.beginDueDate
         params["endDueDate"] = filterRequest.endDueDate
         whereClause.append(" AND apInvoice.due_date ")
            .append(buildDateFilterString(filterRequest.beginDueDate, filterRequest.endDueDate, "beginDueDate", "endDueDate"))
      }

      if (filterRequest.beginVenGr != null && filterRequest.endVenGr != null) {
         params["beginVenGr"] = filterRequest.beginVenGr
         params["endVenGr"] = filterRequest.endVenGr
         whereClause.append(" AND vgrp.value ")
            .append(buildNumberFilterString("beginVenGr", "endVenGr"))
      }

      filterRequest.invStatus?.let {
         params["status"] = filterRequest.invStatus
         whereClause.append(" AND invStatus.value = :status ")
      }

      filterRequest.useTax?.let {
         params["useTax"] = filterRequest.useTax
         whereClause.append(" AND apInvoice.use_tax_indicator = :useTax ")
      }
      val ordering = " ORDER BY poHeader.number ${filterRequest.sortDirection()}, apInvoice.id, pmt.id, pmtDetail.id "

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            $ordering
         """.trimIndent(),
         params)
      { rs, elements ->
         do {

            val tempInvoice = if (currentInvoice?.id != rs.getUuid(("apInvoice_id"))) {
               val localInvoice  = mapInvoice(rs, "apInvoice_", "apPayment_")
               currentInvoice = localInvoice
               addInvoice = true

               localInvoice
            } else {
               addInvoice = false

               currentInvoice
            }

            mapInvoiceDetailOrNull(rs)?.let { tempInvoice?.invoiceDetails?.add(it) }
            tempInvoice?.distDetails?.add(mapDistDetail(rs))
            tempInvoice?.inventories?.add(mapInventory(rs))

            val tempPO = if (currentPO?.poHeaderNumber != rs.getIntOrNull("poHeader_number")) {
               val localPO = mapPO(rs)
               purchaseOrders.add(localPO)
               currentPO = localPO

               localPO
            } else {
               currentPO
            }

            if (addInvoice) {
               tempPO?.invoices?.add(tempInvoice)
               addInvoice = false
            }

         } while (rs.next())
      }

      if (filterRequest.sortBy() == "apInvoice.invoice") {
         fun extractInvoiceNumber(invoice: String) = invoice.substringBefore("-").substringAfter(":")

         val compareByInvoice = compareBy<AccountPayableInvoiceReportPoWrapper> {
            extractInvoiceNumber(it.invoices.first()!!.invoice!!).length
         }.thenBy {
            extractInvoiceNumber(it.invoices.first()!!.invoice!!)
         }
         purchaseOrders.sortWith(compareByInvoice)
      } else if (filterRequest.snakeSortBy() == "vendor.number") {
         purchaseOrders.sortWith(compareBy { it.invoices.first()!!.vendorNumber })

         purchaseOrders.forEach { it ->
            it.invoices.sortWith(
               compareBy<AccountPayableInvoiceReportDTO?> { it?.vendorNumber }
                  .thenBy { it?.invoice }
            )
         }
      } else if (filterRequest.snakeSortBy() == "vendor.name") {
         purchaseOrders.sortWith(compareBy { it.invoices.first()!!.vendorName })

         purchaseOrders.forEach { it ->
            it.invoices.sortWith(
               compareBy<AccountPayableInvoiceReportDTO?> { it?.vendorName }
                  .thenBy { it?.invoice }
            )
         }
      }

      if (filterRequest.sortDirection() == "DESC") purchaseOrders.reverse()

      return purchaseOrders
   }

   @ReadOnly
   fun export(company: CompanyEntity, filterRequest: InvoiceReportFilterRequest): List<AccountPayableInvoiceReportExportDTO> {
      val invoices = mutableListOf<AccountPayableInvoiceReportExportDTO>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE apInvoice.company_id = :comp_id ")

      if (filterRequest.beginVen != null && filterRequest.endVen != null) {
         params["beginVen"] = filterRequest.beginVen
         params["endVen"] = filterRequest.endVen
         whereClause.append(" AND vendor.number ")
            .append(buildNumberFilterString("beginVen", "endVen"))
      }

      if (filterRequest.beginOpr != null && filterRequest.endOpr != null) {
         params["beginOpr"] = filterRequest.beginOpr
         params["endOpr"] = filterRequest.endOpr
         whereClause.append(" AND apInvoice.employee_number_id_sfk ")
            .append(buildNumberFilterString("beginOpr", "endOpr"))
      }

      if (filterRequest.beginPO != null && filterRequest.endPO != null) {
         params["beginPO"] = filterRequest.beginPO
         params["endPO"] = filterRequest.endPO
         whereClause.append(" AND poHeader.number ")
            .append(buildNumberFilterString("beginPO", "endPO"))
      }

      if (filterRequest.beginInvDate != null || filterRequest.endInvDate != null) {
         params["beginInvDate"] = filterRequest.beginInvDate
         params["endInvDate"] = filterRequest.endInvDate
         whereClause.append(" AND apInvoice.invoice_date ")
            .append(buildDateFilterString(filterRequest.beginInvDate, filterRequest.endInvDate, "beginInvDate", "endInvDate"))
      }

      if (filterRequest.beginExpDate != null || filterRequest.endExpDate != null) {
         params["beginExpDate"] = filterRequest.beginExpDate
         params["endExpDate"] = filterRequest.endExpDate
         whereClause.append(" AND apInvoice.expense_date ")
            .append(buildDateFilterString(filterRequest.beginExpDate, filterRequest.endExpDate, "beginExpDate", "endExpDate"))
      }

      if (filterRequest.beginEnDate != null || filterRequest.endEnDate != null) {
         params["beginEnDate"] = filterRequest.beginEnDate
         params["endEnDate"] = filterRequest.endEnDate
         whereClause.append(" AND apInvoice.entry_date ")
            .append(buildDateFilterString(filterRequest.beginEnDate, filterRequest.endEnDate, "beginEnDate", "endEnDate"))
      }

      if (filterRequest.beginPaidDate != null || filterRequest.endPaidDate != null) {
         params["beginPaidDate"] = filterRequest.beginPaidDate
         params["endPaidDate"] = filterRequest.endPaidDate
         whereClause.append(" AND pmt.payment_date ")
            .append(buildDateFilterString(filterRequest.beginPaidDate, filterRequest.endPaidDate, "beginPaidDate", "endPaidDate"))
      }

      if (filterRequest.beginDueDate != null || filterRequest.endDueDate != null) {
         params["beginDueDate"] = filterRequest.beginDueDate
         params["endDueDate"] = filterRequest.endDueDate
         whereClause.append(" AND apInvoice.due_date ")
            .append(buildDateFilterString(filterRequest.beginDueDate, filterRequest.endDueDate, "beginDueDate", "endDueDate"))
      }

      if (filterRequest.beginVenGr != null && filterRequest.endVenGr != null) {
         params["beginVenGr"] = filterRequest.beginVenGr
         params["endVenGr"] = filterRequest.endVenGr
         whereClause.append(" AND vgrp.value ")
            .append(buildNumberFilterString("beginVenGr", "endVenGr"))
      }

      filterRequest.invStatus?.let {
         params["status"] = filterRequest.invStatus
         whereClause.append(" AND invStatus.value = :status ")
      }

      filterRequest.useTax?.let {
         params["useTax"] = filterRequest.useTax
         whereClause.append(" AND apInvoice.use_tax_indicator = :useTax ")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY poHeader.number, apInvoice.id, pmt.id, pmtDetail.id
         """.trimIndent(),
         params)
      { rs, elements ->
         do {
            invoices.add(mapInvoiceExport(rs, "apInvoice_", "apPayment_"))
         } while (rs.next())
      }

      return invoices
   }

   private fun mapPO(
      rs: ResultSet,
   ): AccountPayableInvoiceReportPoWrapper {
      return AccountPayableInvoiceReportPoWrapper(
         poHeaderNumber = rs.getIntOrNull("poHeader_number"),
      )
   }

   private fun mapInvoice(
      rs: ResultSet,
      columnPrefix: String = EMPTY,
      paymentPrefix: String = EMPTY
   ): AccountPayableInvoiceReportDTO {
      return AccountPayableInvoiceReportDTO(
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

   private fun mapInvoiceExport(
      rs: ResultSet,
      columnPrefix: String = EMPTY,
      paymentPrefix: String = EMPTY
   ): AccountPayableInvoiceReportExportDTO {
      return AccountPayableInvoiceReportExportDTO(
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

   private fun mapInvoiceDetailOrNull(
      rs: ResultSet
   ): AccountPayablePaymentDetailReportDTO? =
      if (StringUtils.isNotEmpty(rs.getString("apPayment_detail_id"))) {
         mapInvoiceDetail(rs)
      } else {
         null
      }

   private fun mapInvoiceDetail(
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
