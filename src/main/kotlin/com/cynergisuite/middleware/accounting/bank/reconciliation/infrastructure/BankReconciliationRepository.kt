package com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.company.Company
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class BankReconciliationRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val bankRepository: BankRepository,
   private val bankReconciliationTypeRepository: BankReconciliationTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(BankReconciliationRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH bank AS (
            ${bankRepository.selectBaseQuery()}
         )
         SELECT
            bankRecon.id                                      AS bankRecon_id,
            bankRecon.transaction_date                        AS bankRecon_transaction_date,
            bankRecon.cleared_date                            AS bankRecon_cleared_date,
            bankRecon.amount                                  AS bankRecon_amount,
            bankRecon.description                             AS bankRecon_description,
            bankRecon.document                                AS bankRecon_document,
            bankRecon.company_id                              AS bankRecon_comp_id,
            bank.bank_id                                      AS bank_id,
            bank.bank_name                                    AS bank_name,
            bank.bank_number                                  AS bank_number,
            bank.comp_id                                      AS bank_comp_id,
            bank.bank_account_id                              AS bank_account_id,
            bank.bank_account_number                          AS bank_account_number,
            bank.bank_account_name                            AS bank_account_name,
            bank.bank_account_form_1099_field                 AS bank_account_form_1099_field,
            bank.bank_account_corporate_account_indicator     AS bank_account_corporate_account_indicator,
            bank.bank_account_comp_id                         AS bank_account_comp_id,
            bank.bank_account_type_id                         AS bank_account_type_id,
            bank.bank_account_type_value                      AS bank_account_type_value,
            bank.bank_account_type_description                AS bank_account_type_description,
            bank.bank_account_type_localization_code          AS bank_account_type_localization_code,
            bank.bank_account_balance_type_id                 AS bank_account_balance_type_id,
            bank.bank_account_balance_type_value              AS bank_account_balance_type_value,
            bank.bank_account_balance_type_description        AS bank_account_balance_type_description,
            bank.bank_account_balance_type_localization_code  AS bank_account_balance_type_localization_code,
            bank.bank_account_status_id                       AS bank_account_status_id,
            bank.bank_account_status_value                    AS bank_account_status_value,
            bank.bank_account_status_description              AS bank_account_status_description,
            bank.bank_account_status_localization_code        AS bank_account_status_localization_code,
            bank.bank_glProfitCenter_id                       AS bank_glProfitCenter_id,
            bank.bank_glProfitCenter_number                   AS bank_glProfitCenter_number,
            bank.bank_glProfitCenter_name                     AS bank_glProfitCenter_name,
            bank.bank_glProfitCenter_dataset                  AS bank_glProfitCenter_dataset,
            bankReconType.id                                  AS bankReconType_id,
            bankReconType.value                               AS bankReconType_value,
            bankReconType.description                         AS bankReconType_description,
            bankReconType.localization_code                   AS bankReconType_localization_code
         FROM bank_reconciliation bankRecon
               JOIN bank ON bankRecon.bank_id = bank.bank_id
               JOIN bank_reconciliation_type_domain bankReconType ON bankRecon.type_id = bankReconType.id
      """
   }

   fun findOne(id: UUID, company: Company): BankReconciliationEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE bankRecon.id = :id AND bankRecon.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "bankRecon_")
      }

      logger.trace("Searching for BankReconciliation id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<BankReconciliationEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query =
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE bankRecon.company_id = :comp_id
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by bankRecon_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT ${page.size()} OFFSET ${page.offset()}
      """
      var totalElements: Long? = null
      val resultList: MutableList<BankReconciliationEntity> = mutableListOf()

      jdbc.query(query, params) { rs ->
         resultList.add(mapRow(rs, company, "bankRecon_"))
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         requested = page,
         elements = resultList,
         totalElements = totalElements ?: 0
      )
   }

   @Transactional
   fun insert(entity: BankReconciliationEntity, company: Company): BankReconciliationEntity {
      logger.debug("Inserting bank_reconciliation {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO bank_reconciliation(
            company_id,
            bank_id,
            type_id,
            transaction_date,
            cleared_date,
            amount,
            description,
            document
         )
	      VALUES (
            :company_id,
            :bank_id,
            :type_id,
            :transaction_date,
            :cleared_date,
            :amount,
            :description,
            :document
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "bank_id" to entity.bank.id,
            "type_id" to entity.type.id,
            "transaction_date" to entity.date,
            "cleared_date" to entity.clearedDate,
            "amount" to entity.amount,
            "description" to entity.description,
            "document" to entity.document
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun update(entity: BankReconciliationEntity, company: Company): BankReconciliationEntity {
      logger.debug("Updating bank reconciliation {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE bank_reconciliation
         SET
            company_id = :company_id,
            bank_id = :bank_id,
            type_id = :type_id,
            transaction_date = :transaction_date,
            cleared_date = :cleared_date,
            amount = :amount,
            description = :description,
            document = :document
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "bank_id" to entity.bank.id,
            "type_id" to entity.type.id,
            "transaction_date" to entity.date,
            "cleared_date" to entity.clearedDate,
            "amount" to entity.amount,
            "description" to entity.description,
            "document" to entity.document
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): BankReconciliationEntity {
      return BankReconciliationEntity(
         id = rs.getUuid("${columnPrefix}id"),
         bank = bankRepository.mapRow(rs, company, "bank_"),
         type = bankReconciliationTypeRepository.mapRow(rs, "bankReconType_"),
         date = rs.getLocalDate("${columnPrefix}transaction_date"),
         clearedDate = rs.getLocalDateOrNull("${columnPrefix}cleared_date"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         description = rs.getString("${columnPrefix}description"),
         document = rs.getIntOrNull("${columnPrefix}document")
      )
   }

   private fun mapRow(rs: ResultSet, entity: BankReconciliationEntity, columnPrefix: String = EMPTY): BankReconciliationEntity {
      return BankReconciliationEntity(
         id = rs.getUuid("${columnPrefix}id"),
         bank = entity.bank,
         type = entity.type,
         date = rs.getLocalDate("${columnPrefix}transaction_date"),
         clearedDate = rs.getLocalDateOrNull("${columnPrefix}cleared_date"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         description = rs.getString("${columnPrefix}description"),
         document = rs.getIntOrNull("${columnPrefix}document")
      )
   }
}
