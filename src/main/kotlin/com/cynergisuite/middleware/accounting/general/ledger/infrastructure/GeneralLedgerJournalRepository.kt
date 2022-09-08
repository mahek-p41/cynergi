package com.cynergisuite.middleware.accounting.general.ledger.infrastructure

import com.cynergisuite.domain.GeneralLedgerJournalFilterRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class GeneralLedgerJournalRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val accountRepository: AccountRepository,
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val storeRepository: StoreRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerJournalRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            glJournal.id                                                    AS glJournal_id,
            glJournal.time_created                                          AS glJournal_time_created,
            glJournal.time_updated                                          AS glJournal_time_updated,
            glJournal.company_id                                            AS glJournal_company_id,
            glJournal.date                                                  AS glJournal_date,
            glJournal.amount                                                AS glJournal_amount,
            glJournal.message                                               AS glJournal_message,
            account.account_id                                              AS glJournal_account_id,
            account.account_number                                          AS glJournal_account_number,
            account.account_name                                            AS glJournal_account_name,
            account.account_form_1099_field                                 AS glJournal_account_form_1099_field,
            account.account_corporate_account_indicator                     AS glJournal_account_corporate_account_indicator,
            account.account_comp_id                                         AS glJournal_account_comp_id,
            account.account_deleted                                         AS glJournal_account_deleted,
            account.account_type_id                                         AS glJournal_account_type_id,
            account.account_type_value                                      AS glJournal_account_type_value,
            account.account_type_description                                AS glJournal_account_type_description,
            account.account_type_localization_code                          AS glJournal_account_type_localization_code,
            account.account_balance_type_id                                 AS glJournal_account_balance_type_id,
            account.account_balance_type_value                              AS glJournal_account_balance_type_value,
            account.account_balance_type_description                        AS glJournal_account_balance_type_description,
            account.account_balance_type_localization_code                  AS glJournal_account_balance_type_localization_code,
            account.account_status_id                                       AS glJournal_account_status_id,
            account.account_status_value                                    AS glJournal_account_status_value,
            account.account_status_description                              AS glJournal_account_status_description,
            account.account_status_localization_code                        AS glJournal_account_status_localization_code,
            account.account_vendor_1099_type_id                             AS glJournal_account_vendor_1099_type_id,
            account.account_vendor_1099_type_value                          AS glJournal_account_vendor_1099_type_value,
            account.account_vendor_1099_type_description                    AS glJournal_account_vendor_1099_type_description,
            account.account_vendor_1099_type_localization_code              AS glJournal_account_vendor_1099_type_localization_code,
            bank.id                                                         AS glJournal_account_bank_id,
            glJournal.profit_center_id_sfk                                  AS glJournal_profit_center_id_sfk,
            profitCenter.id                                                 AS glJournal_profitCenter_id,
            profitCenter.number                                             AS glJournal_profitCenter_number,
            profitCenter.name                                               AS glJournal_profitCenter_name,
            profitCenter.dataset                                            AS glJournal_profitCenter_dataset,
            source.id                                                       AS glJournal_source_id,
            source.company_id                                               AS glJournal_source_company_id,
            source.value                                                    AS glJournal_source_value,
            source.description                                              AS glJournal_source_description,
            source.company_id                                               AS glJournal_source_comp_id,
            count(*) OVER()                                                 AS total_elements
         FROM general_ledger_journal glJournal
            JOIN company comp ON glJournal.company_id = comp.id AND comp.deleted = FALSE
            JOIN account ON glJournal.account_id = account.account_id AND account.account_deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
               ON profitCenter.dataset = comp.dataset_code
                  AND profitCenter.id = glJournal.profit_center_id_sfk
            JOIN general_ledger_source_codes source ON glJournal.source_id = source.id AND source.deleted = FALSE
            LEFT OUTER JOIN bank ON bank.general_ledger_account_id = account.account_id AND bank.deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): GeneralLedgerJournalEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()}\nWHERE glJournal.id = :id AND glJournal.company_id = :comp_id AND glJournal.deleted = FALSE"

      logger.debug("Searching for GeneralLedgerJournal using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val generalLedgerJournal = mapRow(rs, company, "glJournal_")

         generalLedgerJournal
      }

      logger.trace("Searching for GeneralLedgerJournal: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, filterRequest: GeneralLedgerJournalFilterRequest) : RepositoryPage<GeneralLedgerJournalEntity, GeneralLedgerJournalFilterRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder("WHERE glJournal.company_id = :comp_id")

      if (filterRequest.profitCenter != null) {
         params["profitCenter"] = filterRequest.profitCenter
         whereClause.append(" AND profitCenter.id = :profitCenter")
      }

      if (filterRequest.beginSourceCode != null || filterRequest.endSourceCode != null) {
         params["beginSource"] = filterRequest.beginSourceCode
         params["endSource"] = filterRequest.endSourceCode
         whereClause.append(" AND source.value ")
            .append(buildFilterString(filterRequest.beginSourceCode != null, filterRequest.endSourceCode != null, "beginSource", "endSource"))
      }

      if (filterRequest.fromDate != null || filterRequest.thruDate != null) {
         params["fromDate"] = filterRequest.fromDate
         params["thruDate"] = filterRequest.thruDate
         whereClause.append(" AND glJournal.date ")
            .append(buildFilterString(filterRequest.fromDate != null, filterRequest.thruDate != null, "fromDate", "thruDate"))
      }

      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            $whereClause
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "glJournal_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: GeneralLedgerJournalEntity, company: CompanyEntity): GeneralLedgerJournalEntity {
      logger.debug("Inserting GeneralLedgerJournal {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_journal(
            company_id,
            account_id,
            profit_center_id_sfk,
            date,
            source_id,
            amount,
            message
         )
         VALUES (
            :company_id,
            :account_id,
            :profit_center_id_sfk,
            :date,
            :source_id,
            :amount,
            :message
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "account_id" to entity.account.id,
            "profit_center_id_sfk" to entity.profitCenter.myId(),
            "date" to entity.date,
            "source_id" to entity.source.id,
            "amount" to entity.amount,
            "message" to entity.message
         )
      ) { rs, _ -> mapRowUpsert(rs, entity.account, entity.profitCenter, entity.source) }
   }

   @Transactional
   fun update(entity: GeneralLedgerJournalEntity, company: CompanyEntity): GeneralLedgerJournalEntity {
      logger.debug("Updating GeneralLedgerJournal {}", entity)

      val updated = jdbc.updateReturning(
         """
         UPDATE general_ledger_journal
         SET
            company_id = :company_id,
            account_id = :account_id,
            profit_center_id_sfk = :profit_center_id_sfk,
            date = :date,
            source_id = :source_id,
            amount = :amount,
            message = :message
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.id,
            "account_id" to entity.account.id,
            "profit_center_id_sfk" to entity.profitCenter.myId(),
            "date" to entity.date,
            "source_id" to entity.source.id,
            "amount" to entity.amount,
            "message" to entity.message
         )
      ) { rs, _ -> mapRowUpsert(rs, entity.account, entity.profitCenter, entity.source) }

      logger.debug("Updated GeneralLedgerJournal {}", updated)

      return updated
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting GeneralLedgerJournal with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE general_ledger_journal
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "general_ledger_journal"
      )

      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   private fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): GeneralLedgerJournalEntity {
      return GeneralLedgerJournalEntity(
         id = rs.getUuid("${columnPrefix}id"),
         account = accountRepository.mapRow(rs, company, "${columnPrefix}account_"),
         profitCenter = storeRepository.mapRow(rs, company, "${columnPrefix}profitCenter_"),
         date = rs.getLocalDate("${columnPrefix}date"),
         source = generalLedgerSourceCodeRepository.mapRow(rs, "${columnPrefix}source_"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         message = rs.getString("${columnPrefix}message")
      )
   }

   private fun mapRowUpsert(
      rs: ResultSet,
      account: AccountEntity,
      profitCenter: Store,
      source: GeneralLedgerSourceCodeEntity,
      columnPrefix: String = EMPTY
   ): GeneralLedgerJournalEntity {
      return GeneralLedgerJournalEntity(
         id = rs.getUuid("${columnPrefix}id"),
         account = account,
         profitCenter = profitCenter,
         date = rs.getLocalDate("${columnPrefix}date"),
         source = source,
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         message = rs.getString("${columnPrefix}message")
      )
   }

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam "
      else if (begin) " > :$beginningParam "
      else " < :$endingParam "
   }
}
