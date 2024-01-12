package com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure

import com.cynergisuite.domain.BankReconClearingFilterRequest
import com.cynergisuite.domain.BankReconFilterRequest
import com.cynergisuite.domain.BankReconciliationTransactionsFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.bank.BankReconciliationReportDTO
import com.cynergisuite.middleware.accounting.bank.BankReconciliationReportEntity
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconSummaryEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDetailEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationTypeEnum
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
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
class BankReconciliationRepository @Inject constructor(
   private val jdbc: Jdbi,
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
            bank.bank_comp_id                                 AS bank_comp_id,
            bank.bank_deleted                                 AS bank_deleted,
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
            bank.bank_account_vendor_1099_type_id                               AS bank_account_vendor_1099_type_id,
            bank.bank_account_vendor_1099_type_value                            AS bank_account_vendor_1099_type_value,
            bank.bank_account_vendor_1099_type_description                      AS bank_account_vendor_1099_type_description,
            bank.bank_account_vendor_1099_type_localization_code                AS bank_account_vendor_1099_type_localization_code,
            bank.bank_id                                      AS bank_account_bank_id,
            bank.bank_glProfitCenter_id                       AS bank_glProfitCenter_id,
            bank.bank_glProfitCenter_number                   AS bank_glProfitCenter_number,
            bank.bank_glProfitCenter_name                     AS bank_glProfitCenter_name,
            bank.bank_glProfitCenter_dataset                  AS bank_glProfitCenter_dataset,
            bankReconType.id                                  AS bankReconType_id,
            bankReconType.value                               AS bankReconType_value,
            bankReconType.description                         AS bankReconType_description,
            bankReconType.localization_code                   AS bankReconType_localization_code,
            bank_vendor.vendor_name                           AS bankRecon_vendor_name,
            count(*) OVER()                                   AS total_elements
         FROM bank_reconciliation bankRecon
               JOIN bank ON bankRecon.bank_id = bank.bank_id AND bank.bank_deleted = FALSE
               JOIN bank_reconciliation_type_domain bankReconType ON bankRecon.type_id = bankReconType.id
               JOIN account ON account.id = bank.bank_account_id AND account.deleted = FALSE
               LEFT JOIN bank_recon_vendor_vw bank_vendor ON bank_vendor.bank_recon_id = bankRecon.id
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): BankReconciliationEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE bankRecon.id = :id AND bankRecon.company_id = :comp_id and bankRecon.deleted = false"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "bankRecon_")
      }

      logger.trace("Searching for BankReconciliation id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<BankReconciliationEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val query =
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE bankRecon.company_id = :comp_id and bankRecon.deleted = false
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

      jdbc.query(query, params) { rs, _ ->
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
   fun insert(entity: BankReconciliationEntity, company: CompanyEntity): BankReconciliationEntity {
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
            "company_id" to company.id,
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
   fun bulkInsertByVerifyIds(verifyIDs: List<UUID>, company: CompanyEntity): Int {
      logger.debug("Creating bank reconciliation entries for Verify IDs {}", verifyIDs)
      val affectedRows = jdbc.update(
         """
         INSERT INTO bank_reconciliation (
             company_id,
             bank_id,
             type_id,
             transaction_date,
             cleared_date,
             amount,
             description,
             document
         )
         SELECT
             ds.company_id,
             b.id AS bank_id,
             brtd.id AS type_id,
             ds.business_date AS transaction_date,
             NULL AS cleared_date,
             ds.deposit_amount AS amount,
             'SUM ' || RIGHT(REPEAT('0', 4) || ds.store_number_sfk, 4) || ' ' || dep.value AS description,
             TO_CHAR(CURRENT_DATE, 'YYYYMMDD') AS document
         FROM
             deposits_staging ds
         JOIN
             deposits_staging_deposit_type_domain dep ON ds.deposit_type_id = dep.id
         JOIN
             area a ON ds.company_id = a.company_id
         JOIN
             area_type_domain atd ON a.area_type_id = atd.id AND atd.value = 'BR'
         JOIN
             bank b ON ds.deposit_account_id = b.general_ledger_account_id AND b.deleted = FALSE
         JOIN
             bank_reconciliation_type_domain brtd ON (
                 (ds.deposit_type_id BETWEEN 1 AND 7 AND brtd.value = 'D')
                 OR (ds.deposit_type_id BETWEEN 8 AND 9 AND brtd.value = 'M')
                 OR (ds.deposit_type_id BETWEEN 10 AND 11 AND brtd.value = 'R')
             )
         WHERE
             ds.verify_id IN (<verify_ids>)
             AND ds.deposit_amount > 0
             AND ds.deleted = FALSE
         """.trimIndent(),
         mapOf("verify_ids" to verifyIDs)
      )
      logger.info("Inserted {} bank_reconciliation rows.", affectedRows)
      return affectedRows
   }

   @Transactional
   fun update(entity: BankReconciliationEntity, company: CompanyEntity): BankReconciliationEntity {
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
            "company_id" to company.id,
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
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting BankReconciliation with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
            UPDATE bank_reconciliation
            SET deleted = TRUE
            WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "bank_reconciliation"
      )

      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   @ReadOnly
   fun findReport(filterRequest: BankReconFilterRequest, company: CompanyEntity): BankReconciliationReportDTO {
      val bankRecons = mutableListOf<BankReconciliationReportDetailEntity>()
      val reconSummary = BankReconSummaryEntity()
      var currentBank: BankReconciliationReportDetailEntity?
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE bankRecon.company_id = :comp_id and bankRecon.deleted = FALSE")

      if (filterRequest.beginBank != null || filterRequest.endBank != null) {
         params["beginBank"] = filterRequest.beginBank
         params["endBank"] = filterRequest.endBank
         whereClause.append(" AND bank.bank_number")
            .append(buildFilterString(filterRequest.beginBank != null, filterRequest.endBank != null, "beginBank", "endBank"))
      }

      if (filterRequest.fromDate != null || filterRequest.thruDate != null) {
         params["fromDate"] = filterRequest.fromDate
         params["thruDate"] = filterRequest.thruDate
         whereClause.append(" AND bankRecon.transaction_date")
            .append(buildFilterString(filterRequest.fromDate != null, filterRequest.thruDate != null, "fromDate", "thruDate"))
      }

      if (filterRequest.beginClearDate != null || filterRequest.endClearDate != null) {
         params["beginClearDate"] = filterRequest.beginClearDate
         params["endClearDate"] = filterRequest.endClearDate
         whereClause.append(" AND bankRecon.cleared_date")
            .append(buildFilterString(filterRequest.beginClearDate != null, filterRequest.endClearDate != null, "beginClearDate", "endClearDate"))
      }

      if (filterRequest.beginDocument != null || filterRequest.endDocument != null) {
         params["beginDocument"] = filterRequest.beginDocument
         params["endDocument"] = filterRequest.endDocument
         whereClause.append(" AND regexp_replace(bankRecon.document, '[^0-9]+', '', 'g')")
            .append(buildFilterString(filterRequest.beginDocument != null, filterRequest.endDocument != null, "beginDocument", "endDocument"))
      }

      if (filterRequest.bankReconciliationType != null) {
         params["type"] = filterRequest.bankReconciliationType
         whereClause.append(" AND bankReconType.value = :type")
      }

      if (filterRequest.status != null) {
         params["status"] = filterRequest.status
         if (filterRequest.status == "C") {
            whereClause.append(" AND bankRecon.cleared_date IS NOT NULL")
         }
         if (filterRequest.status == "O") {
            whereClause.append(" AND bankRecon.cleared_date IS NULL")
         }
      }

      if (filterRequest.description != null) {
         params["description"] = filterRequest.description
         whereClause.append(" AND (bank_vendor.vendor_name ILIKE \'%${filterRequest.description!!.trim()}%\' OR bankRecon.description ILIKE \'%${filterRequest.description!!.trim()}%\')")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY bank_number, bankRecon_transaction_date, bankRecon_document, bankRecon_cleared_date
         """.trimIndent(),
         params,
      ) {
         rs, elements ->
         do {
               val localBank = mapReportRow(rs, company, "bankRecon_")
               if(filterRequest.layout == "V"){
                  if(localBank.vendorName != null) {
                     localBank.description = localBank.vendorName!!
                  }
               }
               bankRecons.add(localBank)
               currentBank = localBank

               when(currentBank!!.type.value) {
                  BankReconciliationTypeEnum.ACH.codeValue -> {
                     reconSummary.ach = reconSummary.ach.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.CHECK.codeValue -> {
                     reconSummary.check = reconSummary.check.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.DEPOSIT.codeValue -> {
                     reconSummary.deposit = reconSummary.deposit.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.FEE.codeValue -> {
                     reconSummary.fee = reconSummary.fee.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.INTEREST.codeValue -> {
                     reconSummary.interest = reconSummary.interest.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.MISC.codeValue -> {
                     reconSummary.misc = reconSummary.misc.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.SERVICE_CHARGE.codeValue -> {
                     reconSummary.serviceCharge = reconSummary.serviceCharge.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.TRANSFER.codeValue -> {
                     reconSummary.transfer = reconSummary.transfer.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.RETURN_CHECK.codeValue -> {
                     reconSummary.returnCheck = reconSummary.returnCheck.plus(currentBank!!.amount)
                  }
                  BankReconciliationTypeEnum.VOID.codeValue -> {
                     reconSummary.void = reconSummary.void.plus(currentBank!!.amount)
                  }
               }

         } while (rs.next())
      }
      val entity = BankReconciliationReportEntity(bankRecons, reconSummary)
      return BankReconciliationReportDTO(entity)
   }

   @ReadOnly
   fun fetchClear(filterRequest: BankReconClearingFilterRequest, company: CompanyEntity): List<BankReconciliationEntity> {
      val bankRecons = mutableListOf<BankReconciliationEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE bankRecon.company_id = :comp_id and bankRecon.deleted = FALSE")

      if (filterRequest.bank != null) {
         params["bank"] = filterRequest.bank
         whereClause.append(" AND bank.bank_number = :bank")
      }

      if (filterRequest.fromTransactionDate != null || filterRequest.thruTransactionDate != null) {
         params["fromTransactionDate"] = filterRequest.fromTransactionDate
         params["thruTransactionDate"] = filterRequest.thruTransactionDate
         whereClause.append(" AND bankRecon.transaction_date")
            .append(
               buildFilterString(
                  filterRequest.fromTransactionDate != null,
                  filterRequest.thruTransactionDate != null,
                  "fromTransactionDate",
                  "thruTransactionDate"
               )
            )
      }

      if (filterRequest.beginDocNum != null || filterRequest.endDocNum != null) {
         params["beginDocNum"] = filterRequest.beginDocNum
         params["endDocNum"] = filterRequest.endDocNum
         whereClause.append(" AND bankRecon.document")
            .append(
               buildFilterString(
                  filterRequest.beginDocNum != null,
                  filterRequest.endDocNum != null,
                  "beginDocNum",
                  "endDocNum"
               )
            )
      }

      if (filterRequest.bankReconciliationType != null) {
         params["type"] = filterRequest.bankReconciliationType
         whereClause.append(" AND bankReconType.value = :type")
      }

      if (filterRequest.status != null) {
         params["status"] = filterRequest.status
         if (filterRequest.status == "C") {
            whereClause.append(" AND bankRecon.cleared_date IS NOT NULL")
         }
         if (filterRequest.status == "O") {
            whereClause.append(" AND bankRecon.cleared_date IS NULL")
         }
      }

      if (filterRequest.description != null) {
         params["description"] = filterRequest.description
         whereClause.append(" AND bankRecon.description ILIKE \'${filterRequest.description}%\'")
      }

      if (filterRequest.amount != null) {
         params["amount"] = filterRequest.amount
         whereClause.append(" AND bankRecon.amount = :amount")
      }

      if (filterRequest.statementDate != null) {
         params["statementDate"] = filterRequest.statementDate
         whereClause.append(" AND bankRecon")
      }


      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY bank_name, bankRecon_transaction_date, bankRecon_document, bankRecon_cleared_date
         """.trimIndent(),
         params,
      ) { rs, elements ->
         do {
            val localBank = mapRow(rs, company, "bankRecon_")

            bankRecons.add(localBank)
         } while (rs.next())
      }
      return bankRecons
   }

   @Transactional
   fun bulkUpdate(entities: List<BankReconciliationEntity>, company: CompanyEntity): List<BankReconciliationEntity> {
      logger.debug("Updating bank reconciliation {}", entities)
      val updated = mutableListOf<BankReconciliationEntity>()

      entities.map { update(it, company)}
         .forEach { updated.add(it) }

      return updated
   }

   @ReadOnly
   fun findTransactions(filterRequest: BankReconciliationTransactionsFilterRequest, company: CompanyEntity) : RepositoryPage<BankReconciliationEntity, PageRequest> {

      logger.trace("Searching for Reconciliation Transactions by Bank {} and Type {}", filterRequest.bank, filterRequest.bankReconciliationType)
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder(" WHERE bankRecon.company_id = :comp_id and bankRecon.deleted = FALSE")

      if (filterRequest.bank != null) {
         params["bank"] = filterRequest.bank
         whereClause.append(" AND bank.bank_number = :bank")
      }

      if (filterRequest.fromTransactionDate != null || filterRequest.thruTransactionDate != null) {
         params["fromTransactionDate"] = filterRequest.fromTransactionDate
         params["thruTransactionDate"] = filterRequest.thruTransactionDate
         whereClause.append(" AND bankRecon.transaction_date")
            .append(
               buildFilterString(
                  filterRequest.fromTransactionDate != null,
                  filterRequest.thruTransactionDate != null,
                  "fromTransactionDate",
                  "thruTransactionDate"
               )
            )
      }

      if (filterRequest.beginDocNum != null || filterRequest.endDocNum != null) {
         params["beginDocNum"] = filterRequest.beginDocNum
         params["endDocNum"] = filterRequest.endDocNum
         whereClause.append(" AND bankRecon.document")
            .append(
               buildFilterString(
                  filterRequest.beginDocNum != null,
                  filterRequest.endDocNum != null,
                  "beginDocNum",
                  "endDocNum"
               )
            )
      }

      if (filterRequest.description != null) {
         params["description"] = filterRequest.description
         whereClause.append(" AND bankRecon.description ILIKE \'%${filterRequest.description}%\'") //ILIKE is case-insensitive LIKE
      }

      if (filterRequest.bankReconciliationType != null) {
         params["type"] = filterRequest.bankReconciliationType
         whereClause.append(" AND bankReconType.value = :type") //bankReconType_value
      }

      if (filterRequest.status != null) {
         params["status"] = filterRequest.status
         if (filterRequest.status == "B") {
               if (filterRequest.fromClearedDate != null || filterRequest.thruClearedDate != null) {
                  params["fromClearedDate"] = filterRequest.fromClearedDate
                  params["thruClearedDate"] = filterRequest.thruClearedDate
                  whereClause.append(" AND (bankRecon.cleared_date")
                     .append(
                        buildFilterString(
                           filterRequest.fromClearedDate != null,
                           filterRequest.thruClearedDate != null,
                           "fromClearedDate",
                           "thruClearedDate"
                        )
                     ).append(" OR bankRecon.cleared_date IS NULL)")
               }
         }
         if (filterRequest.status == "C") {
            if (filterRequest.fromClearedDate != null || filterRequest.thruClearedDate != null) {
               params["fromClearedDate"] = filterRequest.fromClearedDate
               params["thruClearedDate"] = filterRequest.thruClearedDate
               whereClause.append(" AND bankRecon.cleared_date")
                  .append(
                     buildFilterString(
                        filterRequest.fromClearedDate != null,
                        filterRequest.thruClearedDate != null,
                        "fromClearedDate",
                        "thruClearedDate"
                     )
                  )
            }
         }
         if (filterRequest.status == "O") {
            whereClause.append(" AND bankRecon.cleared_date IS NULL")
         }
      }

      if (filterRequest.amount != null) {
         params["amount"] = filterRequest.amount
         whereClause.append(" AND bankRecon.amount = :amount")
      }

      val query =
         """
      WITH paged AS (
         ${selectBaseQuery()}
         $whereClause
      )
      SELECT
         p.*,
         count(*) OVER() as total_elements
      FROM paged AS p
      ORDER by bankRecon_${filterRequest.snakeSortBy()} ${filterRequest.sortDirection()}
      LIMIT ${filterRequest.size} OFFSET ${filterRequest.offset()}
   """
      var totalElements: Long? = null
      val resultList: MutableList<BankReconciliationEntity> = mutableListOf()

      jdbc.query(query, params) { rs, _ ->
         resultList.add(mapRow(rs, company, "bankRecon_"))
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         requested = filterRequest,
         elements = resultList,
         totalElements = totalElements ?: 0
      )

   }

   private fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): BankReconciliationEntity {
      return BankReconciliationEntity(
         id = rs.getUuid("${columnPrefix}id"),
         bank = bankRepository.mapRow(rs, company, "bank_"),
         type = bankReconciliationTypeRepository.mapRow(rs, "bankReconType_"),
         date = rs.getLocalDate("${columnPrefix}transaction_date"),
         clearedDate = rs.getLocalDateOrNull("${columnPrefix}cleared_date"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         description = rs.getString("${columnPrefix}description"),
         document = rs.getString("${columnPrefix}document")
      )
   }

   private fun mapRow(
      rs: ResultSet,
      entity: BankReconciliationEntity,
      columnPrefix: String = EMPTY
   ): BankReconciliationEntity {
      return BankReconciliationEntity(
         id = rs.getUuid("${columnPrefix}id"),
         bank = entity.bank,
         type = entity.type,
         date = rs.getLocalDate("${columnPrefix}transaction_date"),
         clearedDate = rs.getLocalDateOrNull("${columnPrefix}cleared_date"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         description = rs.getString("${columnPrefix}description"),
         document = rs.getString("${columnPrefix}document")
      )
   }

   private fun mapReportRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY
   ): BankReconciliationReportDetailEntity {
      return BankReconciliationReportDetailEntity(
         id = rs.getUuid("${columnPrefix}id"),
         bank = bankRepository.mapRow(rs, company, "bank_"),
         type = bankReconciliationTypeRepository.mapRow(rs, "bankReconType_"),
         date = rs.getLocalDate("${columnPrefix}transaction_date"),
         clearedDate = rs.getLocalDateOrNull("${columnPrefix}cleared_date"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         description = rs.getString("${columnPrefix}description"),
         document = rs.getString("${columnPrefix}document"),
         vendorName = rs.getString("${columnPrefix}vendor_name")
      )
   }

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam"
      else if (begin) " >= :$beginningParam"
      else " <= :$endingParam"
   }
}
