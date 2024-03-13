package com.cynergisuite.middleware.accounting.account.payable.check.preview.infrastructure

import com.cynergisuite.domain.AccountPayableCheckPreviewFilterRequest
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.sumByBigDecimal
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewVendorsEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.ResultSet

@Singleton
class AccountPayableCheckPreviewRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val vendorRepository: VendorRepository,
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH vend AS (
            ${vendorRepository.baseSelectQuery()}
         )
         SELECT
            apInvoice.id                                                AS apInvoice_id,
            apInvoice.company_id                                        AS apInvoice_company_id,
            apInvoice.invoice                                           AS apInvoice_invoice,
            apInvoice.invoice_date                                      AS apInvoice_invoice_date,
            apInvoice.invoice_amount                                    AS apInvoice_invoice_amount,
            apInvoice.discount_amount                                   AS apInvoice_discount_amount,
            apInvoice.discount_percent                                  AS apInvoice_discount_percent,
            apInvoice.auto_distribution_applied                         AS apInvoice_auto_distribution_applied,
            apInvoice.discount_taken                                    AS apInvoice_discount_taken,
            apInvoice.entry_date                                        AS apInvoice_entry_date,
            apInvoice.expense_date                                      AS apInvoice_expense_date,
            apInvoice.discount_date                                     AS apInvoice_discount_date,
            apInvoice.employee_number_id_sfk                            AS apInvoice_employee_number_id_sfk,
            apInvoice.original_invoice_amount                           AS apInvoice_original_invoice_amount,
            apInvoice.message                                           AS apInvoice_message,
            apInvoice.multiple_payment_indicator                        AS apInvoice_multiple_payment_indicator,
            apInvoice.paid_amount                                       AS apInvoice_paid_amount,
            apInvoice.selected_amount                                   AS apInvoice_selected_amount,
            apInvoice.due_date                                          AS apInvoice_due_date,
            apInvoice.separate_check_indicator                          AS apInvoice_separate_check_indicator,
            apInvoice.use_tax_indicator                                 AS apInvoice_use_tax_indicator,
            apInvoice.receive_date                                      AS apInvoice_receive_date,
            apInvoice.location_id_sfk                                   AS apInvoice_location_id_sfk,
            vend.v_id                                                   AS apInvoice_vendor_id,
            vend.v_company_id                                           AS apInvoice_vendor_company_id,
            vend.v_number                                               AS apInvoice_vendor_number,
            vend.v_name                                                 AS apInvoice_vendor_name,
            vend.v_account_number                                       AS apInvoice_vendor_account_number,
            vend.v_pay_to_id                                            AS apInvoice_vendor_pay_to_id,
            vend.v_address_number                                       AS apInvoice_vendor_address_number,
            vend.v_address_name                                         AS apInvoice_vendor_address_name,
            vend.v_address_address1                                     AS apInvoice_vendor_address_address1,
            vend.v_address_address2                                     AS apInvoice_vendor_address_address2,
            vend.v_address_city                                         AS apInvoice_vendor_address_city,
            vend.v_address_state                                        AS apInvoice_vendor_address_state,
            vend.v_address_postal_code                                  AS apInvoice_vendor_address_postal_code,

            payTo.v_id                                                  AS apInvoice_payTo_id,
            payTo.v_company_id                                          AS apInvoice_payTo_company_id,
            payTo.v_number                                              AS apInvoice_payTo_number,
            payTo.v_name                                                AS apInvoice_payTo_name,
            payTo.v_account_number                                      AS apInvoice_payTo_account_number,
            payTo.v_pay_to_id                                           AS apInvoice_payTo_pay_to_id,
            payTo.v_vendor_group_id                                            AS apInvoice_payTo_group_id,

            pmt.payment_number                                          AS apPayment_number,
            pmt.payment_date                                            AS apPayment_payment_date,
            pmtDetail.id                                                AS apPayment_detail_id,
            pmtDetail.amount                                            AS apPayment_detail_amount,
            bank.id                                                     AS apInvoice_bank_id,
            bank.number                                                 AS apInvoice_bank_number,
            bank.name                                                   AS apInvoice_bank_name,
            apControl.pay_after_discount_date                           AS apPayment_pay_after_discount_date,
            selected.id                                                 AS apInvoice_selected_id,
            selected.value                                              AS apInvoice_selected_value,
            selected.description                                        AS apInvoice_selected_description,
            selected.localization_code                                  AS apInvoice_selected_localization_code,
            type.id                                                     AS apInvoice_type_id,
            type.value                                                  AS apInvoice_type_value,
            type.description                                            AS apInvoice_type_description,
            type.localization_code                                      AS apInvoice_type_localization_code,
            status.id                                                   AS apInvoice_status_id,
            status.value                                                AS apInvoice_status_value,
            status.description                                          AS apInvoice_status_description,
            status.localization_code                                    AS apInvoice_status_localization_code,
            poHeader.number                                             AS apInvoice_po_number
         FROM account_payable_invoice apInvoice
            JOIN company comp                                           ON apInvoice.company_id = comp.id AND comp.deleted = FALSE
            JOIN vend                                                   ON apInvoice.vendor_id = vend.v_id
            JOIN vend payTo                                             on apInvoice.pay_to_id = payTo.v_id
            JOIN account_payable_invoice_selected_type_domain selected  ON apInvoice.selected_id = selected.id
            JOIN account_payable_invoice_type_domain type               ON apInvoice.type_id = type.id
            JOIN account_payable_invoice_status_type_domain status      ON apInvoice.status_id = status.id
            LEFT JOIN account_payable_payment_detail pmtDetail          ON apInvoice.id = pmtDetail.account_payable_invoice_id
            LEFT JOIN account_payable_payment pmt                       ON pmtDetail.payment_number_id = pmt.id
            JOIN account_payable_invoice_distribution invDist           ON apInvoice.id = invDist.invoice_id
            JOIN account_payable_control apControl                      ON invDist.distribution_account_id = apControl.general_ledger_inventory_account_id
            LEFT JOIN bank                                              ON pmt.bank_id = bank.id AND bank.deleted = FALSE
            JOIN purchase_order_header poHeader                         ON apInvoice.purchase_order_id = poHeader.id
      """
   }

   @ReadOnly
   fun fetchCheckPreview(company: CompanyEntity, filterRequest: AccountPayableCheckPreviewFilterRequest): AccountPayableCheckPreviewEntity {
      var currentVendor: AccountPayableCheckPreviewVendorsEntity? = null
      var previousCheck = false
      var checkNumber = filterRequest.checkNumber.toBigInteger()
      val previewDetails = mutableListOf<AccountPayableCheckPreviewVendorsEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val sortBy = StringBuilder("ORDER BY ")
      val whereClause = StringBuilder(
         "WHERE apInvoice.company_id = :comp_id " +
            "AND apInvoice.status_id = 2 AND apInvoice.deleted = false "
      )

      if (filterRequest.checkDate != null) {
         params["checkDate"] = filterRequest.checkDate
      }

      if (filterRequest.vendorGroup != null) {
         params["vendorGroup"] = filterRequest.vendorGroup
         whereClause.append("AND payTo.v_vendor_group_id = :vendorGroup ")
      }

      if (filterRequest.dueDate != null) {
         params["dueDate"] = filterRequest.dueDate
         whereClause.append("AND apInvoice.due_date <= :dueDate ")
      }

      if (filterRequest.discountDate != null) {
         params["discountDate"] = filterRequest.discountDate
         whereClause.append("AND CASE " +
            "WHEN apControl.pay_after_discount_date = true THEN apInvoice.discount_date >= :discountDate " +
            "WHEN apControl.pay_after_discount_date = false THEN apInvoice.discount_date  <= :discountDate AND apInvoice.discount_date >= :checkDate END ")
      }
      if (filterRequest.sortBy == "V"){
         sortBy.append("apInvoice_vendor_name")
      }
      if (filterRequest.sortBy == "N") {
         sortBy.append("apInvoice_vendor_number")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            $sortBy
         """.trimIndent(),
         params)
      { rs, elements ->
         do {
            val separateCheck = rs.getBoolean("apInvoice_separate_check_indicator")
            val tempVendor = if (currentVendor?.vendorNumber != rs.getIntOrNull("apInvoice_vendor_number")) {
                  val localVendor = mapCheckPreview(rs, checkNumber)
                  checkNumber++
                  previewDetails.add(localVendor)
                  currentVendor = localVendor

                  localVendor
            } else {
               if(separateCheck || previousCheck ) {
                  val localVendor = mapCheckPreview(rs, checkNumber)
                  checkNumber++
                  previewDetails.add(localVendor)
                  currentVendor = localVendor

                  localVendor
               } else {
                  currentVendor
               }
            }
            mapRow(rs, company, "apInvoice_").let {
               tempVendor!!.invoiceList ?.add(it)
               tempVendor.gross = tempVendor.gross.plus(it.gross)
               tempVendor.discount = tempVendor.discount.plus(it.discount)
               tempVendor.deduction = tempVendor.deduction.plus(it.deduction ?: BigDecimal.ZERO)
               tempVendor.netPaid = tempVendor.netPaid.plus(it.netPaid ?: BigDecimal.ZERO)
            }
            previousCheck = separateCheck
         } while (rs.next())
      }

      val gross = previewDetails.sumByBigDecimal { it.gross }
      val discount = previewDetails.sumByBigDecimal { it.discount }
      val deduction = previewDetails.sumByBigDecimal { it.deduction }
      val netPaid =  previewDetails.sumByBigDecimal { it.netPaid }
      return AccountPayableCheckPreviewEntity(previewDetails, gross, discount, deduction, netPaid)
   }

   fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = StringUtils.EMPTY
   ): AccountPayableCheckPreviewInvoiceEntity {
      val gross = rs.getBigDecimal("${columnPrefix}invoice_amount")
      val discAmt = rs.getBigDecimal("${columnPrefix}discount_amount")
      val discPerc = rs.getBigDecimalOrNull("${columnPrefix}discount_percent")
      val discount = discAmt.times(discPerc ?: BigDecimal.ZERO)
      val netPaid = gross - discount
      return AccountPayableCheckPreviewInvoiceEntity(
         id = rs.getUuid("${columnPrefix}id"),
         invoiceNumber = rs.getString("${columnPrefix}invoice"),
         date = rs.getLocalDate("${columnPrefix}invoice_date"),
         dueDate = rs.getLocalDate("${columnPrefix}due_date"),
         poNumber = rs.getInt("${columnPrefix}po_number"),
         gross = gross,
         discount = discount,
         deduction = BigDecimal.ZERO,
         netPaid = netPaid,
         notes = rs.getString("${columnPrefix}message")
      )
   }

   private fun mapCheckPreview(
      rs: ResultSet,
      checkNumber: BigInteger
   ): AccountPayableCheckPreviewVendorsEntity {
      return AccountPayableCheckPreviewVendorsEntity(
         vendorNumber = rs.getInt("apInvoice_vendor_number"),
         vendorName = rs.getString("apInvoice_vendor_name"),
         address1 = rs.getString("apInvoice_vendor_address_address1"),
         address2 = rs.getString("apInvoice_vendor_address_address2"),
         city = rs.getString("apInvoice_vendor_address_city"),
         state = rs.getString("apInvoice_vendor_address_state"),
         postalCode = rs.getString("apInvoice_vendor_address_postal_code"),
         checkNumber = checkNumber.toString(),
         date = rs.getLocalDate("apInvoice_invoice_date")
      )
   }

   @ReadOnly
   fun validateCheckNums(
      checkNumber: BigInteger,
      bank: Long,
      vendorList: List<AccountPayableCheckPreviewVendorsEntity>
   ): Boolean {
      val numOfChecks = vendorList.size
      val list: ArrayList<BigInteger> = ArrayList()
      for (i in 0 until numOfChecks) {
         list.add(checkNumber + i.toBigInteger())
      }

      return jdbc.queryForObject(
         "SELECT EXISTS(SELECT payment_number FROM account_payable_payment " +
                 "JOIN bank on account_payable_payment.bank_id = bank.id AND bank.deleted = FALSE " +
                 "WHERE account_payable_payment.payment_number = " +
                 "any(array[<checkList>]::varchar[]) AND bank.number = :bank" +
                 ")",
         mapOf(
            "checkList" to list.map { it.toString()}, "bank" to bank
         ),
         Boolean::class.java
      )
   }
}
