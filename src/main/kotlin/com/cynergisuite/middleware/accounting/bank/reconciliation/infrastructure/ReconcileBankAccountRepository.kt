package com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure

import com.cynergisuite.domain.BankReconClearingFilterRequest
import com.cynergisuite.domain.BankReconFilterRequest
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryFullList
import com.cynergisuite.middleware.accounting.bank.BankDTO
import com.cynergisuite.middleware.accounting.bank.BankReconciliationReportDTO
import com.cynergisuite.middleware.accounting.bank.BankReconciliationReportEntity
import com.cynergisuite.middleware.accounting.bank.ReconcileBankAccountReportTemplate
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconSummaryEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDetailDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDetailEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationTypeEnum
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class ReconcileBankAccountRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val bankRepository: BankRepository,
   private val bankReconciliationTypeRepository: BankReconciliationTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(ReconcileBankAccountRepository::class.java)

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
            bank_vendor.vendor_name                           AS bankRecon_vendor_name
         FROM bank_reconciliation bankRecon
               JOIN bank ON bankRecon.bank_id = bank.bank_id AND bank.bank_deleted = FALSE
               JOIN bank_reconciliation_type_domain bankReconType ON bankRecon.type_id = bankReconType.id
               JOIN account ON account.id = bank.bank_account_id AND account.deleted = FALSE
               LEFT JOIN bank_recon_vendor_vw bank_vendor ON bank_vendor.bank_recon_id = bankRecon.id
      """
   }

   @ReadOnly
   fun findReport(filterRequest: BankReconFilterRequest, company: CompanyEntity): ReconcileBankAccountReportTemplate {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE bankRecon.company_id = :comp_id")

      return ReconcileBankAccountReportTemplate(
         jdbc.queryFullList<BankReconciliationReportDetailDTO>(
            """
               ${selectBaseQuery()}
               $whereClause
               ORDER BY bank_number, bankRecon_transaction_date, bankRecon_document, bankRecon_cleared_date
            """.trimIndent(),
            params,
         ) { rs, _, elements ->
            do {
               elements.add(mapReportRow(rs, company, "bankRecon_"))
            } while (rs.next())
         })
   }

   private fun mapReportRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY
   ): BankReconciliationReportDetailDTO {
      return BankReconciliationReportDetailDTO(
         id = rs.getUuid("${columnPrefix}id"),
         bank = BankDTO(bankRepository.mapRow(rs, company, "bank_")),
         type = BankReconciliationTypeDTO(bankReconciliationTypeRepository.mapRow(rs, "bankReconType_")),
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
