package com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StagingStatusFilterRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryFullList
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.middleware.accounting.general.ledger.deposit.AccountingDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositEntity
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositStatusDTO
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class StagingDepositRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(StagingDepositRepository::class.java)

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      filterRequest: StagingDepositPageRequest
   ): RepositoryPage<StagingDepositEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "movedToJe" to filterRequest.movedToJe, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder(" WHERE vs.deleted = false AND vs.company_id = :comp_id AND dep.value IN ('DEP_1', 'DEP_2', 'DEP_3', 'DEP_4', 'DEP_5', 'DEP_6', 'DEP_7')  AND vs.moved_to_pending_journal_entries = :movedToJe ")

      if (filterRequest.verifiedSuccessful != null) {
         params["verifiedSuccessful"] = filterRequest.verifiedSuccessful
         whereClause.append(" AND vs.verify_successful = :verifiedSuccessful")
      }

      if (filterRequest.from != null || filterRequest.thru != null) {
         params["from"] = filterRequest.from
         params["thru"] = filterRequest.thru
         whereClause.append(" AND vs.business_date")
            .append(
               buildFilterString(
                  filterRequest.from != null,
                  filterRequest.thru != null,
                  "from",
                  "thru"
               )
            )
      }

      if (filterRequest.beginStore != null || filterRequest.endStore != null) {
         params["beginStore"] = filterRequest.beginStore
         params["endStore"] = filterRequest.endStore
         whereClause.append(" AND vs.store_number_sfk")
            .append(
               buildFilterString(
                  filterRequest.beginStore != null,
                  filterRequest.endStore != null,
                  "beginStore",
                  "endStore"
               )
            )
      }

      return jdbc.queryPaged(
      """
         SELECT
             vs.id,
             vs.verify_successful,
             vs.business_date,
             vs.moved_to_pending_journal_entries,
             vs.store_number_sfk,
             sv.name AS store_name,
             vs.error_amount,
             SUM(CASE WHEN dep.value = 'DEP_1' THEN ds.deposit_amount ELSE 0 END)   AS deposit_1,
             SUM(CASE WHEN dep.value = 'DEP_2' THEN ds.deposit_amount ELSE 0 END)   AS deposit_2,
             SUM(CASE WHEN dep.value = 'DEP_3' THEN ds.deposit_amount ELSE 0 END)   AS deposit_3,
             SUM(CASE WHEN dep.value = 'DEP_4' THEN ds.deposit_amount ELSE 0 END)   AS deposit_4,
             SUM(CASE WHEN dep.value = 'DEP_5' THEN ds.deposit_amount ELSE 0 END)   AS deposit_5,
             SUM(CASE WHEN dep.value = 'DEP_6' THEN ds.deposit_amount ELSE 0 END)   AS deposit_6,
             SUM(CASE WHEN dep.value = 'DEP_7' THEN ds.deposit_amount ELSE 0 END)   AS deposit_7,
             SUM(ds.deposit_amount)                                                 AS deposit_total,
             count(*) OVER()                                                        AS total_elements
         FROM
             verify_staging vs
             JOIN deposits_staging ds ON vs.company_id = ds.company_id AND vs.id = ds.verify_id
             JOIN company comp ON vs.company_id = comp.id AND comp.deleted = FALSE
             JOIN fastinfo_prod_import.store_vw sv
                    ON sv.dataset = comp.dataset_code
                       AND sv.number = vs.store_number_sfk
             JOIN deposits_staging_deposit_type_domain dep ON ds.deposit_type_id = dep.id
         $whereClause
         GROUP BY
             vs.id,
             vs.verify_successful,
             vs.business_date,
             vs.moved_to_pending_journal_entries,
             vs.store_number_sfk,
             sv.name,
             vs.error_amount
         ORDER BY vs.verify_successful, vs.business_date DESC, vs.store_number_sfk
         LIMIT :limit OFFSET :offset
               """.trimIndent()
      , params
      , filterRequest
      )
      { rs, elements ->
         do {
            elements.add(mapRow(rs))
         }  while (rs.next())
      }
   }

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      filterRequest: StagingStatusFilterRequest
   ): List<StagingDepositStatusDTO> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "year" to filterRequest.yearMonth.year, "month" to filterRequest.yearMonth.monthValue)
      val whereClause = StringBuilder(" WHERE vs.deleted = false AND vs.company_id = :comp_id ")

      whereClause.append(" AND EXTRACT(YEAR FROM vs.business_date) = :year AND EXTRACT(MONTH FROM vs.business_date) = :month ")

      return jdbc.queryFullList(
         """
         SELECT
             vs.id,
             vs.verify_successful,
             vs.business_date,
             vs.moved_to_pending_journal_entries,
             vs.store_number_sfk,
             sv.name AS store_name,
             vs.error_amount,
             count(*) OVER()                                                        AS total_elements
         FROM
             verify_staging vs
             JOIN company comp ON vs.company_id = comp.id AND comp.deleted = FALSE
             JOIN fastinfo_prod_import.store_vw sv
                    ON sv.dataset = comp.dataset_code
                       AND sv.number = vs.store_number_sfk
         $whereClause
         ORDER BY vs.business_date, vs.store_number_sfk
               """.trimIndent()
         , params
      )
      { rs, _, elements ->
         do {
            elements.add(mapStatusRow(rs))
         }  while (rs.next())
      }
   }

   @ReadOnly
   fun findByListId(company: CompanyEntity, idList: List<UUID>): List<StagingDepositEntity> {

      return jdbc.query(
         """
         SELECT
             vs.id,
             vs.verify_successful,
             vs.business_date,
             vs.moved_to_pending_journal_entries,
             vs.store_number_sfk,
             sv.name AS store_name,
             vs.error_amount,
             SUM(CASE WHEN dep.value = 'DEP_1' THEN ds.deposit_amount ELSE 0 END)   AS deposit_1,
             SUM(CASE WHEN dep.value = 'DEP_2' THEN ds.deposit_amount ELSE 0 END)   AS deposit_2,
             SUM(CASE WHEN dep.value = 'DEP_3' THEN ds.deposit_amount ELSE 0 END)   AS deposit_3,
             SUM(CASE WHEN dep.value = 'DEP_4' THEN ds.deposit_amount ELSE 0 END)   AS deposit_4,
             SUM(CASE WHEN dep.value = 'DEP_5' THEN ds.deposit_amount ELSE 0 END)   AS deposit_5,
             SUM(CASE WHEN dep.value = 'DEP_6' THEN ds.deposit_amount ELSE 0 END)   AS deposit_6,
             SUM(CASE WHEN dep.value = 'DEP_7' THEN ds.deposit_amount ELSE 0 END)   AS deposit_7,
             SUM(ds.deposit_amount)                                                 AS deposit_total,
             count(*) OVER()                                                        AS total_elements
         FROM
             verify_staging vs
             JOIN deposits_staging ds ON vs.company_id = ds.company_id AND vs.id = ds.verify_id
             JOIN company comp ON vs.company_id = comp.id AND comp.deleted = FALSE
             JOIN fastinfo_prod_import.store_vw sv
                    ON sv.dataset = comp.dataset_code
                       AND sv.number = vs.store_number_sfk
             JOIN deposits_staging_deposit_type_domain dep ON ds.deposit_type_id = dep.id
         WHERE vs.id IN (<idList>)
         GROUP BY
             vs.id,
             vs.verify_successful,
             vs.business_date,
             vs.moved_to_pending_journal_entries,
             vs.store_number_sfk,
             sv.name,
             vs.error_amount
         ORDER BY vs.verify_successful, vs.business_date DESC, vs.store_number_sfk
               """.trimIndent(),
         mapOf("idList" to idList),
      ) { rs, _ ->
        mapRow(rs)
      }
   }

   @ReadOnly
   fun fetchAccountingDetails(
      company: CompanyEntity,
      verifyId: UUID
   ): List<AccountingDetailDTO> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "verify_id" to verifyId)
      return jdbc.queryFullList(
         """
         SELECT aes.company_id,
                verify_id,
                business_date,
                account_id,
                acct.number AS account_number,
                acct.name AS account_name,
                aes.profit_center_id_sfk AS profit_center_number,
                source_id,
                source.value AS source_value,
                CASE WHEN journal_entry_amount >= 0 THEN journal_entry_amount ELSE 0 END AS debit,
                CASE WHEN journal_entry_amount < 0 THEN journal_entry_amount ELSE 0 END AS credit,
                aes.deleted,
                message
         FROM accounting_entries_staging aes
            JOIN company comp ON aes.company_id = comp.id AND comp.deleted = FALSE
            JOIN account acct ON aes.account_id = acct.id AND acct.deleted = FALSE
            JOIN general_ledger_source_codes source ON aes.source_id = source.id AND source.deleted = FALSE
         WHERE aes.company_id = :comp_id AND verify_id = :verify_id AND aes.deleted = false
         ORDER BY acct.number, deposit_type_id
               """.trimIndent(), params
      ) { rs, _, elements ->
         do {
            elements.add(mapAccountingDetail(rs))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findByStagingIds(company: CompanyEntity, stagingIds: List<UUID?>, isAdmin: Boolean): List<AccountingDetailDTO> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "verifyId" to stagingIds)
      val whereClause = StringBuilder("WHERE vs.deleted = false AND vs.company_id = :comp_id AND vs.id IN (<verifyId>) AND vs.verify_successful = TRUE")
      if (!isAdmin) {
         whereClause.append(" AND vs.moved_to_pending_journal_entries = FALSE")
      }

      return jdbc.query(
         """
            SELECT
                aes.id,
                aes.company_id,
                aes.verify_id,
                aes.store_number_sfk,
                aes.business_date,
                aes.account_id,
                acct.number AS account_number,
                acct.name AS account_name,
                aes.profit_center_id_sfk AS profit_center_number,
                aes.source_id,
                source.value AS source_value,
                CASE WHEN journal_entry_amount >= 0 THEN journal_entry_amount ELSE 0 END AS debit,
                CASE WHEN journal_entry_amount < 0 THEN journal_entry_amount ELSE 0 END AS credit,
                aes.deleted,
                message
            FROM
                accounting_entries_staging aes
                JOIN company comp ON aes.company_id = comp.id AND comp.deleted = FALSE
                JOIN account acct ON aes.account_id = acct.id AND acct.deleted = FALSE
                JOIN general_ledger_source_codes source ON aes.source_id = source.id AND source.deleted = FALSE
                JOIN verify_staging vs on aes.verify_id = vs.id
            $whereClause
         """.trimIndent(),
         params
      ){ rs, _ ->
         mapAccountingDetail(rs)
      }
   }

   private fun mapAccountingDetail(rs: ResultSet): AccountingDetailDTO {
      return AccountingDetailDTO(
         verifyId = rs.getUuid("verify_id"),
         accountId = rs.getUuid("account_id"),
         accountNumber = rs.getInt("account_number"),
         accountName = rs.getString("account_name"),
         profitCenterNumber = rs.getInt("profit_center_number"),
         sourceId = rs.getUuid("source_id"),
         sourceValue = rs.getString("source_value"),
         debit = rs.getBigDecimal("debit"),
         credit = rs.getBigDecimal("credit"),
         message = rs.getString("message"),
         date = rs.getLocalDate("business_date")
      )
   }


   @Transactional
   fun updateMovedPendingJE(company: CompanyEntity, dto: List<UUID?>) {
      jdbc.update(
         """
            UPDATE verify_staging
            SET
               moved_to_pending_journal_entries = true
            WHERE id IN (<ids>)
         """.trimIndent(),
         mapOf("ids" to dto)
      )
   }

   fun mapRow(rs: ResultSet): StagingDepositEntity =
      StagingDepositEntity(
         id = rs.getUuid("id"),
         verifySuccessful = rs.getBoolean("verify_successful"),
         businessDate = rs.getLocalDate("business_date"),
         movedToPendingJournalEntries = rs.getBoolean("moved_to_pending_journal_entries"),
         store = rs.getInt("store_number_sfk"),
         storeName = rs.getString("store_name"),
         errorAmount = rs.getBigDecimal("error_amount"),
         deposit1Cash = rs.getBigDecimal("deposit_1"),
         deposit2PmtForOtherStores = rs.getBigDecimal("deposit_2"),
         deposit3PmtFromOtherStores = rs.getBigDecimal("deposit_3"),
         deposit4CCInStr = rs.getBigDecimal("deposit_4"),
         deposit5ACHOLP = rs.getBigDecimal("deposit_5"),
         deposit6CCOLP = rs.getBigDecimal("deposit_6"),
         deposit7DebitCard = rs.getBigDecimal("deposit_7"),
         depositTotal = rs.getBigDecimal("deposit_total")
      )

   fun mapStatusRow(rs: ResultSet): StagingDepositStatusDTO =
      StagingDepositStatusDTO(
         id = rs.getUuid("id"),
         verifySuccessful = rs.getBoolean("verify_successful"),
         businessDate = rs.getLocalDate("business_date"),
         movedToPendingJournalEntries = rs.getBoolean("moved_to_pending_journal_entries"),
         store = rs.getInt("store_number_sfk"),
         storeName = rs.getString("store_name"),
      )

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam "
      else if (begin) " >= :$beginningParam "
      else " <= :$endingParam "
   }
}
