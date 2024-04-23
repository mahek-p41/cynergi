package com.cynergisuite.middleware.accounting.account.payable.recurring.distribution.infrastructure

import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.sql.ResultSet
import java.util.UUID
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import javax.transaction.Transactional

@Singleton
class AccountPayableRecurringInvoiceDistributionRepository @Inject constructor(
   private val jdbc: Jdbi
){
   private val logger = LoggerFactory.getLogger(AccountPayableRecurringInvoiceDistributionRepository::class.java)
   fun selectBaseQuery(): String {
      return """
            SELECT
                apRecInvDist.id                                                             AS apRecInvDist_id,
                apRecInvDist.recurring_invoice_id                                           AS apRecInvDist_recurring_invoice_id,
                apRecInvDist.distribution_account_id                                        AS apRecInvDist_distribution_account_id,
                apRecInvDist.distribution_profit_center_id_sfk                              AS apRecInvDist_distribution_profit_center_id_sfk,
                apRecInvDist.distribution_amount                                            AS apRecInvDist_distribution_amount
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
      ) { rs, _ -> mapRow(rs, "apRecInvDist_") }
   }

   @Transactional
   fun insert(entity: AccountPayableRecurringInvoiceDistributionEntity, company: CompanyEntity): AccountPayableRecurringInvoiceDistributionEntity {
      logger.debug("Inserting account_payable_recurring_invoice_distribution {}", entity)
      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_recurring_invoice_distribution (
            recurring_invoice_id,
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
      ) { rs, _ -> mapRow(rs) }
   }

   @Transactional
   fun replace(invoiceId: UUID, entityList: List<AccountPayableRecurringInvoiceDistributionEntity>, company: CompanyEntity): List<AccountPayableRecurringInvoiceDistributionEntity> {
      val updatedList: MutableList<AccountPayableRecurringInvoiceDistributionEntity> = mutableListOf()
      jdbc.update(
         """
         DELETE FROM account_payable_recurring_invoice_distribution
         WHERE recurring_invoice_id = :recurring_invoice_id
      """.trimIndent(),
         mapOf(
            "recurring_invoice_id" to invoiceId,
         )
      )
      entityList.forEach {
         updatedList.add(insert(it, company))
      }
      return updatedList
   }



   @Transactional
   fun update(entity: AccountPayableRecurringInvoiceDistributionEntity, company: CompanyEntity): AccountPayableRecurringInvoiceDistributionEntity {
      logger.debug("Updating account_payable_recurring_invoice_distribution {}", entity)
      return jdbc.updateReturning(
         """
         UPDATE account_payable_recurring_invoice_distribution
         SET
            recurring_invoice_id = :invoiceId,
            distribution_account_id = :accountId,
            distribution_profit_center_id_sfk = :profitCenter,
            distribution_amount = :amount
         WHERE id = :id
         RETURNING *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "invoiceId" to entity.invoiceId,
            "accountId" to entity.accountId,
            "profitCenter" to entity.profitCenter,
            "amount" to entity.amount
         )
      ) { rs, _ -> mapRow(rs, entity) }
   }

   private fun mapRow(
      rs: ResultSet,
      columnPrefix: String = StringUtils.EMPTY
   ): AccountPayableRecurringInvoiceDistributionEntity {
      return AccountPayableRecurringInvoiceDistributionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         invoiceId = rs.getUuid("${columnPrefix}recurring_invoice_id"),
         accountId = rs.getUuid("${columnPrefix}distribution_account_id"),
         profitCenter = rs.getInt("${columnPrefix}distribution_profit_center_id_sfk"),
         amount = rs.getBigDecimal("${columnPrefix}distribution_amount")
      )
   }

   private fun mapRow(
      rs: ResultSet,
      entity: AccountPayableRecurringInvoiceDistributionEntity,
      columnPrefix: String = EMPTY
   ): AccountPayableRecurringInvoiceDistributionEntity {
      return AccountPayableRecurringInvoiceDistributionEntity(
         id = rs.getUuid("${columnPrefix}id"),
         invoiceId = entity.invoiceId,
         accountId = entity.accountId,
         profitCenter = entity.profitCenter,
         amount = entity.amount
         )
   }
}
