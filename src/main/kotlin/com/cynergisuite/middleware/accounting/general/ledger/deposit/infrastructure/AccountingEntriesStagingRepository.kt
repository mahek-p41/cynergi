package com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure

import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.general.ledger.deposit.AccountingEntriesStagingEntity
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.sql.ResultSet
import java.util.UUID
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class AccountingEntriesStagingRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountingEntriesStagingRepository::class.java)
   @ReadOnly
   fun findByVerifyId(company: CompanyEntity, stagingIds: List<UUID?>): List<AccountingEntriesStagingEntity> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "verifyId" to stagingIds)

      return jdbc.query(
         """
            SELECT
                aes.id,
                aes.company_id,
                aes.verify_id,
                aes.store_number_sfk,
                aes.business_date,
                aes.account_id,
                aes.profit_center_id_sfk,
                aes.source_id,
                aes.journal_entry_amount,
                aes.message
            FROM
                accounting_entries_staging aes
                JOIN verify_staging vs on aes.verify_id = vs.id
            WHERE vs.id IN (<verifyId>) AND vs.moved_to_pending_journal_entries = FALSE AND vs.verify_successful = TRUE
         """.trimIndent(),
         params
      ){ rs, _ ->
         mapRow(rs)
      }
   }
   fun mapRow(rs: ResultSet): AccountingEntriesStagingEntity =
      AccountingEntriesStagingEntity(
         id = rs.getUuid("id"),
         companyId = rs.getUuid("company_id"),
         accountId = rs.getUuid("account_id"),
         profitCenter = rs.getInt("profit_center_id_sfk"),
         date = rs.getLocalDate("business_date"),
         sourceId = rs.getUuid("source_id"),
         amount = rs.getBigDecimal("journal_entry_amount"),
         message = rs.getString("message")
      )
}
