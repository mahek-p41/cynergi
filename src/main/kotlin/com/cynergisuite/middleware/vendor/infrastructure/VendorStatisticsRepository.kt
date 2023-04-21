package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
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
         mapRow(rs)
      }
   }

   private fun mapRow(rs: ResultSet): Pair<BigDecimal, LocalDate> {
      return Pair(
         first = rs.getBigDecimal("unpaid_amount"),
         second = rs.getLocalDate("due_date")
      )
   }
}
