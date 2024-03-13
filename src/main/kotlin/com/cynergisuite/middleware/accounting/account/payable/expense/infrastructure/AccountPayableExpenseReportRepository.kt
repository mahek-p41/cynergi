package com.cynergisuite.middleware.accounting.account.payable.expense.infrastructure

import com.cynergisuite.domain.ExpenseReportFilterRequest
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.account.payable.expense.AccountPayableExpenseReportDTO
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.util.GroupingType
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

   fun selectBaseQuery(beginDate: LocalDate, endDate: LocalDate): String {
      return """
         SELECT
            apExpense.id                                                AS apExpense_id,
            apExpense.company_id                                        AS apExpense_company_id,
            apExpense.invoice                                           AS apExpense_invoice,

            vendor.name                                                 AS apExpense_vendor_name,
            vendor.number                                               AS apExpense_vendor_number,
            vgrp.value                                                  AS apExpense_vendor_vgrp_value,
            invType.value                                               AS apExpense_type_value,
            poHeader.number                                             AS poHeader_number,
            apExpense.invoice_date                                      AS apExpense_invoice_date,
            invStatus.value                                             AS apExpense_status_value,
            apExpense.invoice_amount                                    AS apExpense_invoice_amount,
            apExpense.expense_date                                      AS apExpense_expense_date,
            apExpense.paid_amount                                       AS apExpense_paid_amount,

            apExpense.message                                           AS apExpense_message,
            CASE WHEN (apControl.id IS null) THEN false ELSE true END   AS apPayment_account_for_inventory,

            InvoiceAndPayment.dist_center                               AS apExpense_dist_center,
            InvoiceAndPayment.account_number                            AS apExpense_account_number,
            InvoiceAndPayment.account_name                              AS apExpense_account_name,
            InvoiceAndPayment.inv_dist_amount                           AS apExpense_inv_dist_amount,
            InvoiceAndPayment.pmt_dist_amount                           AS apExpense_pmt_dist_amount,
            InvoiceAndPayment.gl_amount                                 AS apExpense_gl_amount,
            InvoiceAndPayment.payment_number                            AS apPayment_number,
            InvoiceAndPayment.payment_date                              AS apPayment_payment_date,
            InvoiceAndPayment.date_voided                               AS apPayment_date_voided,
            InvoiceAndPayment.bank_number                               AS bank_number,

            count(*) OVER()                                             AS total_elements

         FROM
             account_payable_invoice apExpense
         JOIN account_payable_invoice_type_domain invType ON invType.id = apExpense.type_id
         JOIN account_payable_invoice_status_type_domain invStatus ON invStatus.id = apExpense.status_id
         JOIN vendor ON apExpense.vendor_id = vendor.id AND vendor.deleted = FALSE
         LEFT JOIN vendor_group vgrp ON vgrp.id = vendor.vendor_group_id AND vgrp.deleted = FALSE
         LEFT JOIN purchase_order_header poHeader ON poHeader.id = apExpense.purchase_order_id AND poHeader.deleted = FALSE
         LEFT JOIN (
             SELECT
                 invoice_id,
                 account_id,
                 account_number AS account_number,
                 account_name AS account_name,
                 dist_center,
                 MAX(inv_dist_amount) AS inv_dist_amount,
                 MAX(pmt_dist_amount) AS pmt_dist_amount,
                 SUM(
                     COALESCE(inv_dist_amount, 0) + COALESCE(pmt_dist_amount, 0)
                 ) AS gl_amount,
                 MAX(payment_number) AS payment_number,
                 MAX(payment_date) AS payment_date,
                 MAX(date_voided) AS date_voided,
                 MAX(bank_number) AS bank_number
             FROM
                 (
                     SELECT
                         invDist.invoice_id AS invoice_id,
                         invDist.distribution_account_id AS account_id,
                         invDist.distribution_profit_center_id_sfk AS dist_center,
                         invDist.distribution_amount AS inv_dist_amount,
                         NULL AS pmt_dist_amount,
                         account.number AS account_number,
                         account.name AS account_name,
                         pmt.payment_number AS payment_number,
                         pmt.payment_date AS payment_date,
                         pmt.date_voided AS date_voided,
                         bank.number AS bank_number
                     FROM
                         account_payable_invoice_distribution invDist
                     JOIN account ON invDist.distribution_account_id = account.id AND account.deleted = FALSE
                     LEFT JOIN account_payable_payment_detail pmtDetail ON pmtDetail.account_payable_invoice_id = invDist.invoice_id
                     LEFT JOIN account_payable_payment pmt ON (
                             pmtDetail.payment_number_id = pmt.id
                             AND pmt.payment_date BETWEEN '${beginDate}' AND '${endDate}'
                         )
                     LEFT JOIN account_payable_payment_status_type_domain pmtStatus ON pmtStatus.id = pmt.account_payable_payment_status_id
                     LEFT JOIN bank ON pmt.bank_id = bank.id AND bank.deleted = FALSE
                     WHERE (pmtStatus.value = 'P' OR pmt.id IS NULL)
                     UNION
                     SELECT
                         pmtDetail.account_payable_invoice_id AS invoice_id,
                         pmtDist.distribution_account AS account_id,
                         pmtDist.distribution_profit_center_sfk AS dist_center,
                         NULL AS inv_dist_amount,
                         -- pmtDist.distribution_amount AS pmt_dist_amount, -- This gives wrong results for account 1030, 2000
                         -1 * pmtDetail.amount AS pmt_dist_amount,  -- This gives wrong results for account 2000
                         account.number AS account_number,
                         account.name AS account_name,
                         pmt.payment_number AS payment_number,
                         pmt.payment_date AS payment_date,
                         pmt.date_voided AS date_voided,
                         bank.number AS bank_number
                     FROM
                         account_payable_payment_detail pmtDetail
                     JOIN account_payable_payment pmt ON (
                             pmtDetail.payment_number_id = pmt.id
                             AND pmt.payment_date BETWEEN '${beginDate}' AND '${endDate}'
                         )
                     JOIN account_payable_payment_distribution pmtDist ON (pmt.id = pmtDist.payment_id)
                     JOIN account_payable_payment_status_type_domain pmtStatus ON pmtStatus.id = pmt.account_payable_payment_status_id
                         AND pmtStatus.value = 'P'
                     JOIN account_payable_payment_type_type_domain pmtType ON pmt.account_payable_payment_type_id = pmtType.id
                     LEFT JOIN bank ON pmt.bank_id = bank.id AND bank.deleted = FALSE
                     JOIN account ON pmtDist.distribution_account = account.id AND account.deleted = FALSE
                 ) AS subquery
             GROUP BY invoice_id, account_id, dist_center, account_number, account_name
         ) AS InvoiceAndPayment ON apExpense.id = InvoiceAndPayment.invoice_id
         LEFT JOIN account_payable_control apControl ON InvoiceAndPayment.account_id = apControl.general_ledger_inventory_account_id
         JOIN company comp ON apExpense.company_id = comp.id AND comp.deleted = FALSE
      """
   }

   @ReadOnly
   fun fetchReport(company: CompanyEntity, filterRequest: ExpenseReportFilterRequest): List<AccountPayableExpenseReportDTO> {
      var reportData = mutableListOf<AccountPayableExpenseReportDTO>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE apExpense.company_id = :comp_id ")

      if (filterRequest.beginAcct != null && filterRequest.endAcct != null) {
         params["beginAcct"] = filterRequest.beginAcct
         params["endAcct"] = filterRequest.endAcct
         whereClause.append(" AND InvoiceAndPayment.account_number ")
            .append(buildNumberFilterString("beginAcct", "endAcct"))
      }

      if (filterRequest.beginVen != null && filterRequest.endVen != null) {
         params["beginVen"] = filterRequest.beginVen
         params["endVen"] = filterRequest.endVen
         whereClause.append(" AND vendor.number ")
            .append(buildNumberFilterString("beginVen", "endVen"))
      }

      if (filterRequest.beginVenGr != null && filterRequest.endVenGr != null) {
         params["beginVenGr"] = filterRequest.beginVenGr
         params["endVenGr"] = filterRequest.endVenGr
         whereClause.append(" AND vgrp.value ")
            .append(buildNumberFilterString("beginVenGr", "endVenGr"))
      }

      if (filterRequest.beginDate != null || filterRequest.endDate != null) {
         params["beginDate"] = filterRequest.beginDate
         params["endDate"] = filterRequest.endDate
         whereClause.append(" AND ((apExpense.expense_date ")
            .append(buildDateFilterString(filterRequest.beginDate, filterRequest.endDate, "beginDate", "endDate")).append(")")
            .append(" OR (apExpense.date_voided ")
            .append(buildDateFilterString(filterRequest.beginDate, filterRequest.endDate, "beginDate", "endDate")).append(")")
            .append(" OR (InvoiceAndPayment.payment_date ")
            .append(buildDateFilterString(filterRequest.beginDate, filterRequest.endDate, "beginDate", "endDate")).append(" AND InvoiceAndPayment.pmt_dist_amount != 0)")
            .append(" OR (InvoiceAndPayment.date_voided ")
            .append(buildDateFilterString(filterRequest.beginDate, filterRequest.endDate, "beginDate", "endDate")).append(" AND invStatus.value = 'V')").append(")")
      }

      filterRequest.invStatus?.let {
         params["statuses"] = filterRequest.invStatus
         whereClause.append(" AND invStatus.value IN (<statuses>) ")
      }

      if (!filterRequest.iclHoldInv!!) {
         whereClause.append(" AND invStatus.value != 'H' ")
      }

      val ordering =
         if (GroupingType.fromString(filterRequest.snakeSortBy()) == GroupingType.ACCOUNT) {
            " ORDER BY InvoiceAndPayment.account_number, InvoiceAndPayment.dist_center, apExpense.expense_date, vendor.number "
         } else {
            " ORDER BY vendor.number, InvoiceAndPayment.account_number, InvoiceAndPayment.payment_number "
         }

      jdbc.query(
         """
            ${selectBaseQuery(filterRequest.beginDate!!, filterRequest.endDate!!)}
            $whereClause
            $ordering
         """.trimIndent(),
         params)
      { rs, elements ->
         do {
            reportData.add(mapExpense(rs, "apExpense_", "apPayment_"))
         } while (rs.next())
      }

      return reportData
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
         type = rs.getString("${columnPrefix}type_value"),
         invoiceDate = rs.getLocalDate("${columnPrefix}invoice_date"),
         status = rs.getString("${columnPrefix}status_value"),
         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         expenseDate = rs.getLocalDate("${columnPrefix}expense_date"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paid_amount"),
         bankNumber = rs.getInt("bank_number"),
         pmtNumber = rs.getString("${paymentPrefix}number"),
         pmtDate = rs.getLocalDateOrNull("${paymentPrefix}payment_date"),
         dateVoided = rs.getLocalDateOrNull("${paymentPrefix}date_voided"),
         notes = rs.getString("${columnPrefix}message"),
         acctNumber = rs.getInt("${columnPrefix}account_number"),
         acctName = rs.getString("${columnPrefix}account_name"),
         distCenter = rs.getInt("${columnPrefix}dist_center"),
         glAmount = rs.getBigDecimal("${columnPrefix}gl_amount"),
         poHeaderNumber = rs.getInt("poHeader_number"),
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
