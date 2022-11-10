package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.PaymentReportFilterRequest
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceSelectedTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceReportDTO
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class AccountPayableInvoiceReportRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val vendorRepository: VendorRepository,
   private val employeeRepository: EmployeeRepository,
   private val selectedRepository: AccountPayableInvoiceSelectedTypeRepository,
   private val statusRepository: AccountPayableInvoiceStatusTypeRepository,
   private val typeRepository: AccountPayableInvoiceTypeRepository,
   private val apInvoiceRepository: AccountPayableInvoiceRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceReportRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            apInvoice.id                                                AS apInvoice_id,
            apInvoice.company_id                                        AS apInvoice_company_id,
            apInvoice.invoice                                           AS apInvoice_invoice,
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
            apInvoice.message                                           AS apInvoice_message,
            account.number                                              AS apPayment_account_number,
            account.name                                                AS apPayment_account_name,
            invDist.distribution_profit_center_id_sfk                   AS apPayment_dist_center,
            invDist.distribution_amount                                 AS apPayment_dist_amount,
            count(*) OVER()                                             AS total_elements
         FROM account_payable_invoice apInvoice
            JOIN account_payable_invoice_type_domain invType            ON invType.id = apInvoice.type_id
            JOIN account_payable_invoice_status_type_domain invStatus   ON invStatus.id = apInvoice.status_id
            JOIN vendor                                                 ON apInvoice.vendor_id = vendor.id
            LEFT OUTER JOIN vendor_group vgrp                           ON vgrp.id = vendor.vendor_group_id AND vgrp.deleted = FALSE
            JOIN purchase_order_header poHeader                         ON poHeader.id = apInvoice.purchase_order_id
            JOIN account_payable_payment_detail pmtDetail               ON apInvoice.id = pmtDetail.account_payable_invoice_id
            JOIN account_payable_payment pmt                            ON pmtDetail.payment_number_id = pmt.id
            JOIN account_payable_payment_type_type_domain pmtType       ON pmt.account_payable_payment_type_id = pmtType.id
            JOIN bank                                                   ON pmt.bank_id = bank.id
            JOIN account_payable_invoice_distribution invDist           ON apInvoice.id = invDist.invoice_id
            JOIN account                                                ON invDist.distribution_account_id = account.id
      """
   }

   @ReadOnly
   fun fetchReport(company: CompanyEntity, filterRequest: PaymentReportFilterRequest): List<AccountPayableInvoiceReportDTO> { //todo maybe return entity
      val invoices = mutableListOf<AccountPayableInvoiceReportDTO>()
      var currentInvoice: AccountPayableInvoiceReportDTO? = null
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      jdbc.query(
         """
            ${selectBaseQuery()}
            WHERE apInvoice.company_id = :comp_id
            LIMIT 4
         """.trimIndent(),
         params)
      { rs, elements ->
         do {
            val tempInvoice = if (currentInvoice?.id != rs.getUuid(("apInvoice_id"))) {
               val localInvoice  = mapInvoice(rs, company, "apInvoice_", "apPayment_")
               invoices.add(localInvoice)

               localInvoice
            } else {
               currentInvoice!!
            }


         } while (rs.next())
      }

      return invoices
   }

   private fun mapInvoice(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY,
      paymentPrefix: String = EMPTY
   ): AccountPayableInvoiceReportDTO {
      return AccountPayableInvoiceReportDTO(
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
         pmtType = rs.getString("${paymentPrefix}type_value"),
         pmtNumber = rs.getInt("${paymentPrefix}number"),
         notes = rs.getString("${columnPrefix}message"),
         acctNumber = rs.getInt("${paymentPrefix}account_number"),
         acctName = rs.getString("${paymentPrefix}account_name"),
         distCenter = rs.getString("${paymentPrefix}dist_center"),
         distAmount = rs.getBigDecimal("${paymentPrefix}dist_amount"),
      )
   }

//   private fun mapRow(
//      rs: ResultSet,
//      entity: AccountPayableInvoiceReportTemplate,
//      columnPrefix: String = EMPTY
//   ): AccountPayableInvoiceReportTemplate {
//      return AccountPayableInvoiceReportTemplate(
//         id = rs.getUuid("${columnPrefix}id"),
//         vendor = entity.vendor,
//         invoice = rs.getString("${columnPrefix}invoice"),
//         purchaseOrder = entity.purchaseOrder,
//         invoiceDate = rs.getLocalDate("${columnPrefix}invoice_date"),
//         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
//         discountAmount = rs.getBigDecimal("${columnPrefix}discount_amount"),
//         discountPercent = rs.getBigDecimal("${columnPrefix}discount_percent"),
//         autoDistributionApplied = rs.getBoolean("${columnPrefix}auto_distribution_applied"),
//         discountTaken = rs.getBigDecimal("${columnPrefix}discount_taken"),
//         entryDate = rs.getLocalDate("${columnPrefix}entry_date"),
//         expenseDate = rs.getLocalDate("${columnPrefix}expense_date"),
//         discountDate = rs.getLocalDateOrNull("${columnPrefix}discount_date"),
//         employee = entity.employee,
//         originalInvoiceAmount = rs.getBigDecimal("${columnPrefix}original_invoice_amount"),
//         message = rs.getString("${columnPrefix}message"),
//         selected = entity.selected,
//         multiplePaymentIndicator = rs.getBoolean("${columnPrefix}multiple_payment_indicator"),
//         paidAmount = rs.getBigDecimal("${columnPrefix}paid_amount"),
//         selectedAmount = rs.getBigDecimal("${columnPrefix}selected_amount"),
//         type = entity.type,
//         status = entity.status,
//         dueDate = rs.getLocalDate("${columnPrefix}due_date"),
//         payTo = entity.payTo,
//         separateCheckIndicator = rs.getBoolean("${columnPrefix}separate_check_indicator"),
//         useTaxIndicator = rs.getBoolean("${columnPrefix}use_tax_indicator"),
//         receiveDate = rs.getLocalDateOrNull("${columnPrefix}receive_date"),
//         location = entity.location
//      )
//   }
}
