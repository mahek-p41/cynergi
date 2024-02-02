package com.cynergisuite.middleware.accounting.general.ledger.reconciliation.infrastructure

import com.cynergisuite.domain.GeneralLedgerReconciliationReportFilterRequest
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.query
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.reconciliation.GeneralLedgerReconciliationInventoryEntity
import com.cynergisuite.middleware.accounting.general.ledger.reconciliation.GeneralLedgerReconciliationReportEntity
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryEntity
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.inventory.InventoryEOMReportEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet

@Singleton
class GeneralLedgerReconciliationRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val generalLedgerSummary: GeneralLedgerSummaryRepository,
   private val financialCalendarRepository: FinancialCalendarRepository
   ) {
private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReconciliationRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
             i.store_number_sfk                                         AS store_number,
             'Asset'::text                                                    AS account_type,
             summary.glSummary_acct_number								      AS account_number,
             summary.glSummary_acct_name									      AS account_name,
             i.year                                                     AS year,
             i.month                                                    AS month,
             i.serial_number                                            AS serial_number,
             i.cost                                                     AS cost,
             i.net_book_value                                           AS net_book_value,
             i.book_depreciation                                        AS book_depreciation,
             i.asset_account_id                                         AS asset_account_id,
             i.contra_asset_account_id                                  AS contra_asset_account_id,
             i.model                                                    AS model,
             i.alternate_id                                             AS alternate_id,
             i.current_inv_indr                                         AS current_inv_indr,
             i.macrs_previous_fiscal_year_end_cost                      AS macrs_previous_fiscal_year_end_cost,
             i.macrs_previous_fiscal_year_end_depr                      AS macrs_previous_fiscal_year_end_depr,
             i.macrs_previous_fiscal_year_end_amt_depr                  AS macrs_previous_fiscal_year_end_depr,
             i.macrs_previous_fiscal_year_end_date                      AS macrs_previous_fiscal_year_end_date,
             i.macrs_latest_fiscal_year_end_cost                        AS macrs_latest_fiscal_year_end_cost ,
             i.macrs_latest_fiscal_year_end_depr                        AS macrs_latest_fiscal_year_end_depr,
             i.macrs_latest_fiscal_year_end_amt_depr                    AS macrs_latest_fiscal_year_end_depr ,
             i.macrs_previous_fiscal_year_bonus                         AS macrs_previous_fiscal_year_bonus,
             i.macrs_latest_fiscal_year_bonus                           AS macrs_latest_fiscal_year_bonus,
             i.deleted                                                  AS deleted,
             comp.id                                                    AS comp_id,
             comp.time_created                                          AS comp_time_created,
             comp.time_updated                                          AS comp_time_updated,
             comp.name                                                  AS comp_name,
             comp.doing_business_as                                     AS comp_doing_business_as,
             comp.client_code                                           AS comp_client_code,
             comp.client_id                                             AS comp_client_id,
             comp.dataset_code                                          AS comp_dataset_code,
             comp.federal_id_number                                     AS comp_federal_id_number,
             comp.include_demo_inventory                                AS comp_include_demo_inventory,
             summary.glSummary_id                                       AS glSummary_id,
             summary.glSummary_company_id                               AS glSummary_company_id,
             summary.glSummary_profit_center_id_sfk                     AS glSummary_profit_center_id_sfk,
             summary.glSummary_overallPeriod_id							      AS glSummary_overall_period_id,
             summary.glSummary_net_activity_period_1                    AS glSummary_net_activity_period_1,
             summary.glSummary_net_activity_period_2                    AS glSummary_net_activity_period_2,
             summary.glSummary_net_activity_period_3                    AS glSummary_net_activity_period_3,
             summary.glSummary_net_activity_period_4                    AS glSummary_net_activity_period_4,
             summary.glSummary_net_activity_period_5                    AS glSummary_net_activity_period_5,
             summary.glSummary_net_activity_period_6                    AS glSummary_net_activity_period_6,
             summary.glSummary_net_activity_period_7                    AS glSummary_net_activity_period_7,
             summary.glSummary_net_activity_period_8                    AS glSummary_net_activity_period_8,
             summary.glSummary_net_activity_period_9                    AS glSummary_net_activity_period_9,
             summary.glSummary_net_activity_period_10                   AS glSummary_net_activity_period_10,
             summary.glSummary_net_activity_period_11                   AS glSummary_net_activity_period_11,
             summary.glSummary_net_activity_period_12                   AS glSummary_net_activity_period_12,
             summary.glSummary_beginning_balance                        AS glSummary_beginning_balance,
             summary.glSummary_closing_balance                          AS glSummary_closing_balance,
             summary.glSummary_acct_id                                  AS glSummary_acct_id,
             summary.glSummary_acct_number                              AS glSummary_acct_number,
             summary.glSummary_acct_name                                AS glSummary_acct_name,
             summary.glSummary_acct_form_1099_field                     AS glSummary_acct_form_1099_field,
             summary.glSummary_acct_corporate_account_indicator         AS glSummary_acct_corporate_account_indicator,
             summary.glSummary_acct_comp_id                             AS glSummary_acct_comp_id,
             summary.glSummary_acct_deleted                             AS glSummary_acct_deleted,
             summary.glSummary_acct_type_id                             AS glSummary_acct_type_id,
             summary.glSummary_acct_type_value                          AS glSummary_acct_type_value,
             summary.glSummary_acct_type_description                    AS glSummary_acct_type_description,
             summary.glSummary_acct_type_localization_code              AS glSummary_acct_type_localization_code,
             summary.glSummary_acct_balance_type_id                     AS glSummary_acct_balance_type_id,
             summary.glSummary_acct_balance_type_value                  AS glSummary_acct_balance_type_value,
             summary.glSummary_acct_balance_type_description            AS glSummary_acct_balance_type_description,
             summary.glSummary_acct_balance_type_localization_code      AS glSummary_acct_balance_type_localization_code,
             summary.glSummary_acct_status_id                           AS glSummary_acct_status_id,
             summary.glSummary_acct_status_value                        AS glSummary_acct_status_value,
             summary.glSummary_acct_status_description                  AS glSummary_acct_status_description,
             summary.glSummary_acct_status_localization_code            AS glSummary_acct_status_localization_code,
             summary.glSummary_acct_vendor_1099_type_id                 AS glSummary_acct_vendor_1099_type_id,
             summary.glSummary_acct_vendor_1099_type_value              AS glSummary_acct_vendor_1099_type_value,
             summary.glSummary_acct_vendor_1099_type_description        AS glSummary_acct_vendor_1099_type_description,
             summary.glSummary_acct_vendor_1099_type_localization_code  AS glSummary_acct_vendor_1099_type_localization_code,
             profitCenter.id                                            AS glSummary_profitCenter_id,
             profitCenter.number                                        AS glSummary_profitCenter_number,
             profitCenter.name                                          AS glSummary_profitCenter_name,
             profitCenter.dataset                                       AS glSummary_profitCenter_dataset,
             overallPeriod.id                                           AS glSummary_overallPeriod_id,
             overallPeriod.value                                        AS glSummary_overallPeriod_value,
             overallPeriod.abbreviation                                 AS glSummary_overallPeriod_abbreviation,
             overallPeriod.description                                  AS glSummary_overallPeriod_description,
             overallPeriod.localization_code                            AS glSummary_overallPeriod_localization_code,
             bank.id                                                    AS glSummary_acct_bank_id,
             fc.r_period                                                   AS r_period,
             prodcmst.allow_depreciation_switch                            AS prodcmst_allow_depreciation_switch

          FROM inventory_end_of_month i
             JOIN company comp ON i.company_id = comp.id AND comp.deleted = FALSE
             JOIN financial_calendar fc ON comp.id = fc.r_company_id
             JOIN summary summary ON i.company_id = summary.glSummary_company_id AND i.store_number_sfk = summary.glSummary_profit_center_id_sfk
                AND i.asset_account_id = summary.glSummary_acct_id AND fc.r_overall_period_id = summary.glSummary_overallPeriod_id
             JOIN fastinfo_prod_import.store_vw profitCenter
                 ON profitCenter.dataset = comp.dataset_code
                    AND profitCenter.number = summary.glSummary_profit_center_id_sfk
             JOIN overall_period_type_domain overallPeriod ON summary.glSummary_overallPeriod_id = overallPeriod.id
             JOIN inventory_end_of_month_inventory_indr_type_domain invIndr ON i.current_inv_indr = invIndr.id
             JOIN fastinfo_prod_import.product_class_master_file_vw prodcmst on prodcmst.dataset = comp.dataset_code and invIndr.value = prodcmst.class_code
             LEFT OUTER JOIN bank ON bank.general_ledger_account_id = summary.glSummary_acct_id AND bank.deleted = FALSE
          """
   }

   @ReadOnly
   fun fetchReport(filterRequest: GeneralLedgerReconciliationReportFilterRequest, company: CompanyEntity): GeneralLedgerReconciliationReportEntity {
      var currentStore:GeneralLedgerReconciliationInventoryEntity ? = null
      var currentGLSummary:GeneralLedgerSummaryEntity ? = null
      val storeAccts = mutableListOf<GeneralLedgerReconciliationInventoryEntity>()
      var allowDepr: String ? = null
      val params = mutableMapOf<String, Any?>("date" to filterRequest.date, "comp_id" to company.id)
      val whereClause = StringBuilder(
         """
            WHERE comp.id = :comp_id
         """.trimIndent()
      )
      if (filterRequest.date != null) {
         params["year"] = filterRequest.date!!.year
         params["month"] = filterRequest.date!!.monthValue
         whereClause.append(" AND i.year = :year AND i.month = :month")
         whereClause.append(" AND fc.r_period_from <= :date AND fc.r_period_to >= :date")
      }
      val query =
         """WITH summary AS (
            ${generalLedgerSummary.selectBaseQuery()}
         ),
         financial_calendar AS (
            ${financialCalendarRepository.selectBaseQuery()}
         ),
            asset_inv AS (
            ${selectBaseQuery()}
            $whereClause
            ),
            contra_inv AS (
            SELECT
             i.store_number_sfk                                         AS store_number,
             'Contra'::text                                                   AS account_type,
             summary.glSummary_acct_number								      AS account_number,
             summary.glSummary_acct_name									      AS account_name,
             i.year                                                     AS year,
             i.month                                                    AS month,
             i.serial_number                                            AS serial_number,
             i.cost                                                     AS cost,
             i.net_book_value                                           AS net_book_value,
             i.book_depreciation                                        AS book_depreciation,
             i.asset_account_id                                         AS asset_account_id,
             i.contra_asset_account_id                                  AS contra_asset_account_id,
             i.model                                                    AS model,
             i.alternate_id                                             AS alternate_id,
             i.current_inv_indr                                         AS current_inv_indr,
             i.macrs_previous_fiscal_year_end_cost                      AS macrs_previous_fiscal_year_end_cost,
             i.macrs_previous_fiscal_year_end_depr                      AS macrs_previous_fiscal_year_end_depr,
             i.macrs_previous_fiscal_year_end_amt_depr                  AS macrs_previous_fiscal_year_end_depr,
             i.macrs_previous_fiscal_year_end_date                      AS macrs_previous_fiscal_year_end_date,
             i.macrs_latest_fiscal_year_end_cost                        AS macrs_latest_fiscal_year_end_cost ,
             i.macrs_latest_fiscal_year_end_depr                        AS macrs_latest_fiscal_year_end_depr,
             i.macrs_latest_fiscal_year_end_amt_depr                    AS macrs_latest_fiscal_year_end_depr ,
             i.macrs_previous_fiscal_year_bonus                         AS macrs_previous_fiscal_year_bonus,
             i.macrs_latest_fiscal_year_bonus                           AS macrs_latest_fiscal_year_bonus,
             i.deleted                                                  AS deleted,
             comp.id                                                    AS comp_id,
             comp.time_created                                          AS comp_time_created,
             comp.time_updated                                          AS comp_time_updated,
             comp.name                                                  AS comp_name,
             comp.doing_business_as                                     AS comp_doing_business_as,
             comp.client_code                                           AS comp_client_code,
             comp.client_id                                             AS comp_client_id,
             comp.dataset_code                                          AS comp_dataset_code,
             comp.federal_id_number                                     AS comp_federal_id_number,
             comp.include_demo_inventory                                AS comp_include_demo_inventory,
             summary.glSummary_id                                       AS glSummary_id,
             summary.glSummary_company_id                               AS glSummary_company_id,
             summary.glSummary_profit_center_id_sfk                     AS glSummary_profit_center_id_sfk,
             summary.glSummary_overallPeriod_id							      AS glSummary_overall_period_id,
             summary.glSummary_net_activity_period_1                    AS glSummary_net_activity_period_1,
             summary.glSummary_net_activity_period_2                    AS glSummary_net_activity_period_2,
             summary.glSummary_net_activity_period_3                    AS glSummary_net_activity_period_3,
             summary.glSummary_net_activity_period_4                    AS glSummary_net_activity_period_4,
             summary.glSummary_net_activity_period_5                    AS glSummary_net_activity_period_5,
             summary.glSummary_net_activity_period_6                    AS glSummary_net_activity_period_6,
             summary.glSummary_net_activity_period_7                    AS glSummary_net_activity_period_7,
             summary.glSummary_net_activity_period_8                    AS glSummary_net_activity_period_8,
             summary.glSummary_net_activity_period_9                    AS glSummary_net_activity_period_9,
             summary.glSummary_net_activity_period_10                   AS glSummary_net_activity_period_10,
             summary.glSummary_net_activity_period_11                   AS glSummary_net_activity_period_11,
             summary.glSummary_net_activity_period_12                   AS glSummary_net_activity_period_12,
             summary.glSummary_beginning_balance                        AS glSummary_beginning_balance,
             summary.glSummary_closing_balance                          AS glSummary_closing_balance,
             summary.glSummary_acct_id                                  AS glSummary_acct_id,
             summary.glSummary_acct_number                              AS glSummary_acct_number,
             summary.glSummary_acct_name                                AS glSummary_acct_name,
             summary.glSummary_acct_form_1099_field                     AS glSummary_acct_form_1099_field,
             summary.glSummary_acct_corporate_account_indicator         AS glSummary_acct_corporate_account_indicator,
             summary.glSummary_acct_comp_id                             AS glSummary_acct_comp_id,
             summary.glSummary_acct_deleted                             AS glSummary_acct_deleted,
             summary.glSummary_acct_type_id                             AS glSummary_acct_type_id,
             summary.glSummary_acct_type_value                          AS glSummary_acct_type_value,
             summary.glSummary_acct_type_description                    AS glSummary_acct_type_description,
             summary.glSummary_acct_type_localization_code              AS glSummary_acct_type_localization_code,
             summary.glSummary_acct_balance_type_id                     AS glSummary_acct_balance_type_id,
             summary.glSummary_acct_balance_type_value                  AS glSummary_acct_balance_type_value,
             summary.glSummary_acct_balance_type_description            AS glSummary_acct_balance_type_description,
             summary.glSummary_acct_balance_type_localization_code      AS glSummary_acct_balance_type_localization_code,
             summary.glSummary_acct_status_id                           AS glSummary_acct_status_id,
             summary.glSummary_acct_status_value                        AS glSummary_acct_status_value,
             summary.glSummary_acct_status_description                  AS glSummary_acct_status_description,
             summary.glSummary_acct_status_localization_code            AS glSummary_acct_status_localization_code,
             summary.glSummary_acct_vendor_1099_type_id                 AS glSummary_acct_vendor_1099_type_id,
             summary.glSummary_acct_vendor_1099_type_value              AS glSummary_acct_vendor_1099_type_value,
             summary.glSummary_acct_vendor_1099_type_description        AS glSummary_acct_vendor_1099_type_description,
             summary.glSummary_acct_vendor_1099_type_localization_code  AS glSummary_acct_vendor_1099_type_localization_code,
             profitCenter.id                                            AS glSummary_profitCenter_id,
             profitCenter.number                                        AS glSummary_profitCenter_number,
             profitCenter.name                                          AS glSummary_profitCenter_name,
             profitCenter.dataset                                       AS glSummary_profitCenter_dataset,
             overallPeriod.id                                           AS glSummary_overallPeriod_id,
             overallPeriod.value                                        AS glSummary_overallPeriod_value,
             overallPeriod.abbreviation                                 AS glSummary_overallPeriod_abbreviation,
             overallPeriod.description                                  AS glSummary_overallPeriod_description,
             overallPeriod.localization_code                            AS glSummary_overallPeriod_localization_code,
             bank.id                                                    AS glSummary_acct_bank_id,
             fc.r_period                                                   AS r_period,
             prodcmst.allow_depreciation_switch                            AS prodcmst_allow_depreciation_switch

          FROM inventory_end_of_month i
             JOIN company comp ON i.company_id = comp.id AND comp.deleted = FALSE
             JOIN financial_calendar fc ON comp.id = fc.r_company_id
             JOIN summary summary ON i.company_id = summary.glSummary_company_id AND i.store_number_sfk = summary.glSummary_profit_center_id_sfk
                AND i.contra_asset_account_id = summary.glSummary_acct_id AND fc.r_overall_period_id = summary.glSummary_overallPeriod_id
             JOIN fastinfo_prod_import.store_vw profitCenter
                 ON profitCenter.dataset = comp.dataset_code
                    AND profitCenter.number = summary.glSummary_profit_center_id_sfk
             JOIN overall_period_type_domain overallPeriod ON summary.glSummary_overallPeriod_id = overallPeriod.id
             JOIN inventory_end_of_month_inventory_indr_type_domain invIndr ON i.current_inv_indr = invIndr.id
             JOIN fastinfo_prod_import.product_class_master_file_vw prodcmst on prodcmst.dataset = comp.dataset_code and invIndr.value = prodcmst.class_code
             LEFT OUTER JOIN bank ON bank.general_ledger_account_id = summary.glSummary_acct_id AND bank.deleted = FALSE
            $whereClause
            )
            SELECT * from asset_inv
            UNION
            SELECT * from contra_inv
            ORDER by account_number, store_number
         """

      jdbc.query(query, params) { rs, elements ->
         do {
            val tempStoreAcct = if (currentStore?.storeNumber != (rs.getIntOrNull("store_number")) ||  currentStore?.accountNumber != rs.getInt("account_number")) {
               val localStoreAcct = mapCurrentStore(rs)
               currentGLSummary = mapGLSum(rs, company, "glSummary_")
               localStoreAcct.glBalance = calculateGLSumTotal(localStoreAcct, currentGLSummary!!)
               storeAccts.add(localStoreAcct)
               currentStore = localStoreAcct
               allowDepr = rs.getString("prodcmst_allow_depreciation_switch")


               localStoreAcct
            } else {
               currentStore!!
            }
            if(tempStoreAcct.accountType == "Asset") {
               mapAsset(rs).let {
                  if(allowDepr != "N") {
                     tempStoreAcct.deprUnits = tempStoreAcct.deprUnits!!.plus(it.deprUnits!!)
                  } else {
                     tempStoreAcct.nonDepr = tempStoreAcct.nonDepr!!.plus(it.nonDepr!!)
                  }
               }
            } else {
               mapContra(rs).let {
                  if(tempStoreAcct.currentInvInd != 4) {
                     tempStoreAcct.deprUnits = tempStoreAcct.deprUnits!!.plus(it.deprUnits!!)
                  } else {
                     tempStoreAcct.nonDepr = tempStoreAcct.nonDepr!!.plus(it.nonDepr!!)
                  }
               }
            }
         } while (rs.next())

         storeAccts.forEach {
            it.reportTotal = it.deprUnits!!.plus(it.nonDepr!!)
            it.difference = it.reportTotal!!.minus(it.glBalance ?: BigDecimal.ZERO)
         }
      }
      val inventoryTotals = storeAccts.groupBy {
         it.accountNumber }.map {
         (accountNumber, entities) ->
         GeneralLedgerReconciliationInventoryEntity(
            accountName = entities.first().accountName,
            accountNumber = accountNumber,
            accountType = entities.first().accountType,
            storeNumber = null,
            deprUnits = entities.sumOf { it.deprUnits!! },
            nonDepr = entities.sumOf { it.nonDepr!! },
            reportTotal = entities.sumOf { it.reportTotal!! },
            glBalance = entities.sumOf { it.glBalance!! },
            difference = entities.sumOf { it.difference!! },
            currentInvInd = null,
            period = null
         )
      }

      val entity = GeneralLedgerReconciliationReportEntity(storeAccts, inventoryTotals)

      return entity
   }

   private fun mapCurrentStore(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): GeneralLedgerReconciliationInventoryEntity {
      return GeneralLedgerReconciliationInventoryEntity(
         accountName = rs.getString("${columnPrefix}account_name"),
         accountNumber = rs.getInt("${columnPrefix}account_number"),
         accountType = rs.getString("${columnPrefix}account_type"),
         storeNumber = rs.getInt("${columnPrefix}store_number"),
         deprUnits = BigDecimal.ZERO,
         nonDepr = BigDecimal.ZERO,
         reportTotal = BigDecimal.ZERO,
         glBalance = BigDecimal.ZERO,
         difference = BigDecimal.ZERO,
         currentInvInd = rs.getInt("${columnPrefix}current_inv_indr"),
         period = rs.getInt("${columnPrefix}r_period")

      )
   }

   private fun mapAsset(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): InventoryEOMReportEntity {
      return InventoryEOMReportEntity(
         deprUnits = rs.getBigDecimal("${columnPrefix}cost"),
         nonDepr = rs.getBigDecimal("${columnPrefix}cost"),
      )
   }

   private fun mapContra(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): InventoryEOMReportEntity {
      return InventoryEOMReportEntity(
         deprUnits = rs.getBigDecimal("${columnPrefix}book_depreciation"),
         nonDepr = rs.getBigDecimal("${columnPrefix}book_depreciation")
      )
   }


   private fun mapGLSum(rs: ResultSet, company: CompanyEntity, columnPrefix: String = StringUtils.EMPTY): GeneralLedgerSummaryEntity {
      return generalLedgerSummary.mapRow(rs, company, "glSummary_")
   }

   private fun calculateGLSumTotal(storeAccount: GeneralLedgerReconciliationInventoryEntity, currentGLSummary: GeneralLedgerSummaryEntity ): BigDecimal? {
      val glSummaryTotal = currentGLSummary.run {
         val startingBalance = beginningBalance ?: BigDecimal.ZERO
         (1..storeAccount.period!!).sumOf { month ->
            getNetActivityForMonth(month) ?: BigDecimal.ZERO
         }.plus(startingBalance)
      }
      return glSummaryTotal
   }
}
