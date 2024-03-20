package com.cynergisuite.middleware.accounting.account.payable.recurring.distribution.infrastructure

import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.sql.ResultSet
import java.util.UUID
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory

@Singleton
class AccountPayableRecurringInvoiceDistributionRepository @Inject constructor(
   private val jdbc: Jdbi
){
   private val logger = LoggerFactory.getLogger(AccountPayableRecurringInvoiceDistributionRepository::class.java)
   fun selectBaseQuery(): String {
      return """
            SELECT
                apRecInvDist.id                                                             AS apRecInvDist_id,
                apRecInvDist.recurring_invoice_id                                                     AS apRecInvDist_recurring_invoice_id,
                apRecInvDist.distribution_account_id                                                     AS apRecInvDist_account_id,
                apRecInvDist.distribution_profit_center_id_sfk                                                  AS apRecInvDist_profit_center,
                apRecInvDist.distribution_amount                                                         AS apRecInvDist_amount
            FROM account_payable_recurring_invoice_distribution apRecInvDist
        """
   }

   @ReadOnly
   fun findByRecurringInvoice(id: UUID, company: CompanyEntity): List<AccountPayableRecurringInvoiceDistributionEntity> {
      return jdbc.query(
         """
         ${selectBaseQuery()}
         WHERE apRecInvDist.recurring_invoice_id  = :id
         """.trimIndent(),
         mapOf("id" to id)
      ) { rs, _ -> mapRow(rs) }
   }

   private fun mapRow(
      rs: ResultSet,
   ): AccountPayableRecurringInvoiceDistributionEntity {
      return AccountPayableRecurringInvoiceDistributionEntity(
         id = rs.getUuid("id"),
         invoiceId = rs.getUuid("recurring_invoice_id"),
         accountId = rs.getUuid("distribution_account_id"),
         profitCenter = rs.getInt("distribution_profit_center_id_sfk"),
         amount = rs.getBigDecimal("distribution_amount")
      )
   }
}
