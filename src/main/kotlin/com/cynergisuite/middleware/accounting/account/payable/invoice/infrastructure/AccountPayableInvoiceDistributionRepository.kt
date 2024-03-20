package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDistributionEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.sql.ResultSet
import java.util.UUID
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import javax.transaction.Transactional


@Singleton
class AccountPayableInvoiceDistributionRepository @Inject constructor(
    private val jdbc: Jdbi
){
   private val logger = LoggerFactory.getLogger(AccountPayableInvoiceDistributionRepository::class.java)
    fun selectBaseQuery(): String {
        return """
            SELECT
                apInvDist.id                                                             AS apInvDist_id,
                apInvDist.invoice_id                                                     AS apInvDist_invoice_id,
                apInvDist.distribution_account_id                                        AS apInvDist_distribution_account_id,
                apInvDist.distribution_profit_center_id_sfk                              AS apInvDist_distribution_profit_center_number,
                apInvDist.distribution_amount                                            AS apInvDist_amount
            FROM account_payable_invoice_distribution apInvDist
        """
    }

   @Transactional
   fun insert(entity: AccountPayableInvoiceDistributionEntity): AccountPayableInvoiceDistributionEntity {
      logger.debug("Inserting account_payable_invoice_distribution {}", entity)
       return jdbc.insertReturning(
          """
          INSERT INTO account_payable_invoice_distribution (
             invoice_id,
             distribution_account_id,
             distribution_profit_center_id_sfk,
             distribution_amount
          ) VALUES (
             :invoiceId,
             :accountId,
             :profitCenter,
             :amount
          )
          RETURNING *
          """.trimIndent(),
          mapOf(
               "invoiceId" to entity.invoiceId,
               "accountId" to entity.accountId,
               "profitCenter" to entity.profitCenter,
               "amount" to entity.amount
            )
          ) { rs, _ ->
             mapRow(rs)
       }
   }

   @Transactional
   fun updateDistributions(invoiceId: UUID, entity: List<AccountPayableInvoiceDistributionEntity>): List<AccountPayableInvoiceDistributionEntity> {
      logger.debug("Updating account_payable_invoice_distribution {}", entity)
      jdbc.update(
         """
         DELETE FROM account_payable_invoice_distribution
         WHERE invoice_id = :invoiceId
      """.trimIndent(),
         mapOf(
            "invoiceId" to invoiceId,
         )
      )
         val updated = entity.map {
         jdbc.insertReturning(
            """
         INSERT INTO account_payable_invoice_distribution (
            invoice_id,
            distribution_account_id,
            distribution_profit_center_id_sfk,
            distribution_amount
         ) VALUES (
            :invoiceId,
            :accountId,
            :profitCenter,
            :amount
         )
         RETURNING *
      """.trimIndent(),
            mapOf(
               "invoiceId" to it.invoiceId,
               "accountId" to it.accountId,
               "profitCenter" to it.profitCenter,
               "amount" to it.amount
            )
         ) { rs, _ ->
            mapRow(rs)
         }
      }
      return updated
   }



   private fun mapRow(
      rs: ResultSet,
   ): AccountPayableInvoiceDistributionEntity {
      return AccountPayableInvoiceDistributionEntity(
         id = rs.getUuid("id"),
         invoiceId = rs.getUuid("invoice_id"),
         accountId = rs.getUuid("distribution_account_id"),
         profitCenter = rs.getInt("distribution_profit_center_id_sfk"),
         amount = rs.getBigDecimal("distribution_amount")
      )
   }
}
