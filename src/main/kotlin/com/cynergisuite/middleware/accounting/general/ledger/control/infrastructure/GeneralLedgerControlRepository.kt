package com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.control.GeneralLedgerControlEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerControlRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val storeRepository: StoreRepository,
   private val accountRepository: AccountRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerControlRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            glCtrl.id                                                      AS glCtrl_id,
            glCtrl.uu_row_id                                               AS glCtrl_uu_row_id,
            glCtrl.time_created                                            AS glCtrl_time_created,
            glCtrl.time_updated                                            AS glCtrl_time_updated,
            glCtrl.company_id                                              AS glCtrl_company_id,
            glCtrl.default_profit_center_sfk                               AS glCtrl_default_profit_center_sfk,
            profitCenter.id                                                AS profitCenter_id,
            profitCenter.number                                            AS profitCenter_number,
            profitCenter.name                                              AS profitCenter_name,
            profitCenter.dataset                                           AS profitCenter_dataset,
            defAPAcct.account_id                                           AS defAPAcct_id,
            defAPAcct.account_number                                       AS defAPAcct_number,
            defAPAcct.account_name                                         AS defAPAcct_name,
            defAPAcct.account_form_1099_field                              AS defAPAcct_form_1099_field,
            defAPAcct.account_corporate_account_indicator                  AS defAPAcct_corporate_account_indicator,
            defAPAcct.comp_id                                              AS defAPAcct_comp_id,
            defAPAcct.comp_uu_row_id                                       AS defAPAcct_comp_uu_row_id,
            defAPAcct.comp_time_created                                    AS defAPAcct_comp_time_created,
            defAPAcct.comp_time_updated                                    AS defAPAcct_comp_time_updated,
            defAPAcct.comp_name                                            AS defAPAcct_comp_name,
            defAPAcct.comp_doing_business_as                               AS defAPAcct_comp_doing_business_as,
            defAPAcct.comp_client_code                                     AS defAPAcct_comp_client_code,
            defAPAcct.comp_client_id                                       AS defAPAcct_comp_client_id,
            defAPAcct.comp_dataset_code                                    AS defAPAcct_comp_dataset_code,
            defAPAcct.comp_federal_id_number                               AS defAPAcct_comp_federal_id_number,
            defAPAcct.type_id                                              AS defAPAcct_type_id,
            defAPAcct.type_value                                           AS defAPAcct_type_value,
            defAPAcct.type_description                                     AS defAPAcct_type_description,
            defAPAcct.type_localization_code                               AS defAPAcct_type_localization_code,
            defAPAcct.balance_type_id                                      AS defAPAcct_balance_type_id,
            defAPAcct.balance_type_value                                   AS defAPAcct_balance_type_value,
            defAPAcct.balance_type_description                             AS defAPAcct_balance_type_description,
            defAPAcct.balance_type_localization_code                       AS defAPAcct_balance_type_localization_code,
            defAPAcct.status_id                                            AS defAPAcct_status_id,
            defAPAcct.status_value                                         AS defAPAcct_status_value,
            defAPAcct.status_description                                   AS defAPAcct_status_description,
            defAPAcct.status_localization_code                             AS defAPAcct_status_localization_code,
            defAPDiscAcct.account_id                                       AS defAPDiscAcct_id,
            defAPDiscAcct.account_number                                   AS defAPDiscAcct_number,
            defAPDiscAcct.account_name                                     AS defAPDiscAcct_name,
            defAPDiscAcct.account_form_1099_field                          AS defAPDiscAcct_form_1099_field,
            defAPDiscAcct.account_corporate_account_indicator              AS defAPDiscAcct_corporate_account_indicator,
            defAPDiscAcct.comp_id                                          AS defAPDiscAcct_comp_id,
            defAPDiscAcct.comp_uu_row_id                                   AS defAPDiscAcct_comp_uu_row_id,
            defAPDiscAcct.comp_time_created                                AS defAPDiscAcct_comp_time_created,
            defAPDiscAcct.comp_time_updated                                AS defAPDiscAcct_comp_time_updated,
            defAPDiscAcct.comp_name                                        AS defAPDiscAcct_comp_name,
            defAPDiscAcct.comp_doing_business_as                           AS defAPDiscAcct_comp_doing_business_as,
            defAPDiscAcct.comp_client_code                                 AS defAPDiscAcct_comp_client_code,
            defAPDiscAcct.comp_client_id                                   AS defAPDiscAcct_comp_client_id,
            defAPDiscAcct.comp_dataset_code                                AS defAPDiscAcct_comp_dataset_code,
            defAPDiscAcct.comp_federal_id_number                           AS defAPDiscAcct_comp_federal_id_number,
            defAPDiscAcct.type_id                                          AS defAPDiscAcct_type_id,
            defAPDiscAcct.type_value                                       AS defAPDiscAcct_type_value,
            defAPDiscAcct.type_description                                 AS defAPDiscAcct_type_description,
            defAPDiscAcct.type_localization_code                           AS defAPDiscAcct_type_localization_code,
            defAPDiscAcct.balance_type_id                                  AS defAPDiscAcct_balance_type_id,
            defAPDiscAcct.balance_type_value                               AS defAPDiscAcct_balance_type_value,
            defAPDiscAcct.balance_type_description                         AS defAPDiscAcct_balance_type_description,
            defAPDiscAcct.balance_type_localization_code                   AS defAPDiscAcct_balance_type_localization_code,
            defAPDiscAcct.status_id                                        AS defAPDiscAcct_status_id,
            defAPDiscAcct.status_value                                     AS defAPDiscAcct_status_value,
            defAPDiscAcct.status_description                               AS defAPDiscAcct_status_description,
            defAPDiscAcct.status_localization_code                         AS defAPDiscAcct_status_localization_code,
            defARAcct.account_id                                           AS defARAcct_id,
            defARAcct.account_number                                       AS defARAcct_number,
            defARAcct.account_name                                         AS defARAcct_name,
            defARAcct.account_form_1099_field                              AS defARAcct_form_1099_field,
            defARAcct.account_corporate_account_indicator                  AS defARAcct_corporate_account_indicator,
            defARAcct.comp_id                                              AS defARAcct_comp_id,
            defARAcct.comp_uu_row_id                                       AS defARAcct_comp_uu_row_id,
            defARAcct.comp_time_created                                    AS defARAcct_comp_time_created,
            defARAcct.comp_time_updated                                    AS defARAcct_comp_time_updated,
            defARAcct.comp_name                                            AS defARAcct_comp_name,
            defARAcct.comp_doing_business_as                               AS defARAcct_comp_doing_business_as,
            defARAcct.comp_client_code                                     AS defARAcct_comp_client_code,
            defARAcct.comp_client_id                                       AS defARAcct_comp_client_id,
            defARAcct.comp_dataset_code                                    AS defARAcct_comp_dataset_code,
            defARAcct.comp_federal_id_number                               AS defARAcct_comp_federal_id_number,
            defARAcct.type_id                                              AS defARAcct_type_id,
            defARAcct.type_value                                           AS defARAcct_type_value,
            defARAcct.type_description                                     AS defARAcct_type_description,
            defARAcct.type_localization_code                               AS defARAcct_type_localization_code,
            defARAcct.balance_type_id                                      AS defARAcct_balance_type_id,
            defARAcct.balance_type_value                                   AS defARAcct_balance_type_value,
            defARAcct.balance_type_description                             AS defARAcct_balance_type_description,
            defARAcct.balance_type_localization_code                       AS defARAcct_balance_type_localization_code,
            defARAcct.status_id                                            AS defARAcct_status_id,
            defARAcct.status_value                                         AS defARAcct_status_value,
            defARAcct.status_description                                   AS defARAcct_status_description,
            defARAcct.status_localization_code                             AS defARAcct_status_localization_code,
            defARDiscAcct.account_id                                       AS defARDiscAcct_id,
            defARDiscAcct.account_number                                   AS defARDiscAcct_number,
            defARDiscAcct.account_name                                     AS defARDiscAcct_name,
            defARDiscAcct.account_form_1099_field                          AS defARDiscAcct_form_1099_field,
            defARDiscAcct.account_corporate_account_indicator              AS defARDiscAcct_corporate_account_indicator,
            defARDiscAcct.comp_id                                          AS defARDiscAcct_comp_id,
            defARDiscAcct.comp_uu_row_id                                   AS defARDiscAcct_comp_uu_row_id,
            defARDiscAcct.comp_time_created                                AS defARDiscAcct_comp_time_created,
            defARDiscAcct.comp_time_updated                                AS defARDiscAcct_comp_time_updated,
            defARDiscAcct.comp_name                                        AS defARDiscAcct_comp_name,
            defARDiscAcct.comp_doing_business_as                           AS defARDiscAcct_comp_doing_business_as,
            defARDiscAcct.comp_client_code                                 AS defARDiscAcct_comp_client_code,
            defARDiscAcct.comp_client_id                                   AS defARDiscAcct_comp_client_id,
            defARDiscAcct.comp_dataset_code                                AS defARDiscAcct_comp_dataset_code,
            defARDiscAcct.comp_federal_id_number                           AS defARDiscAcct_comp_federal_id_number,
            defARDiscAcct.type_id                                          AS defARDiscAcct_type_id,
            defARDiscAcct.type_value                                       AS defARDiscAcct_type_value,
            defARDiscAcct.type_description                                 AS defARDiscAcct_type_description,
            defARDiscAcct.type_localization_code                           AS defARDiscAcct_type_localization_code,
            defARDiscAcct.balance_type_id                                  AS defARDiscAcct_balance_type_id,
            defARDiscAcct.balance_type_value                               AS defARDiscAcct_balance_type_value,
            defARDiscAcct.balance_type_description                         AS defARDiscAcct_balance_type_description,
            defARDiscAcct.balance_type_localization_code                   AS defARDiscAcct_balance_type_localization_code,
            defARDiscAcct.status_id                                        AS defARDiscAcct_status_id,
            defARDiscAcct.status_value                                     AS defARDiscAcct_status_value,
            defARDiscAcct.status_description                               AS defARDiscAcct_status_description,
            defARDiscAcct.status_localization_code                         AS defARDiscAcct_status_localization_code,
            defAcctMiscInvAcct.account_id                                  AS defAcctMiscInvAcct_id,
            defAcctMiscInvAcct.account_number                              AS defAcctMiscInvAcct_number,
            defAcctMiscInvAcct.account_name                                AS defAcctMiscInvAcct_name,
            defAcctMiscInvAcct.account_form_1099_field                     AS defAcctMiscInvAcct_form_1099_field,
            defAcctMiscInvAcct.account_corporate_account_indicator         AS defAcctMiscInvAcct_corporate_account_indicator,
            defAcctMiscInvAcct.comp_id                                     AS defAcctMiscInvAcct_comp_id,
            defAcctMiscInvAcct.comp_uu_row_id                              AS defAcctMiscInvAcct_comp_uu_row_id,
            defAcctMiscInvAcct.comp_time_created                           AS defAcctMiscInvAcct_comp_time_created,
            defAcctMiscInvAcct.comp_time_updated                           AS defAcctMiscInvAcct_comp_time_updated,
            defAcctMiscInvAcct.comp_name                                   AS defAcctMiscInvAcct_comp_name,
            defAcctMiscInvAcct.comp_doing_business_as                      AS defAcctMiscInvAcct_comp_doing_business_as,
            defAcctMiscInvAcct.comp_client_code                            AS defAcctMiscInvAcct_comp_client_code,
            defAcctMiscInvAcct.comp_client_id                              AS defAcctMiscInvAcct_comp_client_id,
            defAcctMiscInvAcct.comp_dataset_code                           AS defAcctMiscInvAcct_comp_dataset_code,
            defAcctMiscInvAcct.comp_federal_id_number                      AS defAcctMiscInvAcct_comp_federal_id_number,
            defAcctMiscInvAcct.type_id                                     AS defAcctMiscInvAcct_type_id,
            defAcctMiscInvAcct.type_value                                  AS defAcctMiscInvAcct_type_value,
            defAcctMiscInvAcct.type_description                            AS defAcctMiscInvAcct_type_description,
            defAcctMiscInvAcct.type_localization_code                      AS defAcctMiscInvAcct_type_localization_code,
            defAcctMiscInvAcct.balance_type_id                             AS defAcctMiscInvAcct_balance_type_id,
            defAcctMiscInvAcct.balance_type_value                          AS defAcctMiscInvAcct_balance_type_value,
            defAcctMiscInvAcct.balance_type_description                    AS defAcctMiscInvAcct_balance_type_description,
            defAcctMiscInvAcct.balance_type_localization_code              AS defAcctMiscInvAcct_balance_type_localization_code,
            defAcctMiscInvAcct.status_id                                   AS defAcctMiscInvAcct_status_id,
            defAcctMiscInvAcct.status_value                                AS defAcctMiscInvAcct_status_value,
            defAcctMiscInvAcct.status_description                          AS defAcctMiscInvAcct_status_description,
            defAcctMiscInvAcct.status_localization_code                    AS defAcctMiscInvAcct_status_localization_code,
            defAcctSerializedInvAcct.account_id                            AS defAcctSerializedInvAcct_id,
            defAcctSerializedInvAcct.account_number                        AS defAcctSerializedInvAcct_number,
            defAcctSerializedInvAcct.account_name                          AS defAcctSerializedInvAcct_name,
            defAcctSerializedInvAcct.account_form_1099_field               AS defAcctSerializedInvAcct_form_1099_field,
            defAcctSerializedInvAcct.account_corporate_account_indicator   AS defAcctSerializedInvAcct_corporate_account_indicator,
            defAcctSerializedInvAcct.comp_id                               AS defAcctSerializedInvAcct_comp_id,
            defAcctSerializedInvAcct.comp_uu_row_id                        AS defAcctSerializedInvAcct_comp_uu_row_id,
            defAcctSerializedInvAcct.comp_time_created                     AS defAcctSerializedInvAcct_comp_time_created,
            defAcctSerializedInvAcct.comp_time_updated                     AS defAcctSerializedInvAcct_comp_time_updated,
            defAcctSerializedInvAcct.comp_name                             AS defAcctSerializedInvAcct_comp_name,
            defAcctSerializedInvAcct.comp_doing_business_as                AS defAcctSerializedInvAcct_comp_doing_business_as,
            defAcctSerializedInvAcct.comp_client_code                      AS defAcctSerializedInvAcct_comp_client_code,
            defAcctSerializedInvAcct.comp_client_id                        AS defAcctSerializedInvAcct_comp_client_id,
            defAcctSerializedInvAcct.comp_dataset_code                     AS defAcctSerializedInvAcct_comp_dataset_code,
            defAcctSerializedInvAcct.comp_federal_id_number                AS defAcctSerializedInvAcct_comp_federal_id_number,
            defAcctSerializedInvAcct.type_id                               AS defAcctSerializedInvAcct_type_id,
            defAcctSerializedInvAcct.type_value                            AS defAcctSerializedInvAcct_type_value,
            defAcctSerializedInvAcct.type_description                      AS defAcctSerializedInvAcct_type_description,
            defAcctSerializedInvAcct.type_localization_code                AS defAcctSerializedInvAcct_type_localization_code,
            defAcctSerializedInvAcct.balance_type_id                       AS defAcctSerializedInvAcct_balance_type_id,
            defAcctSerializedInvAcct.balance_type_value                    AS defAcctSerializedInvAcct_balance_type_value,
            defAcctSerializedInvAcct.balance_type_description              AS defAcctSerializedInvAcct_balance_type_description,
            defAcctSerializedInvAcct.balance_type_localization_code        AS defAcctSerializedInvAcct_balance_type_localization_code,
            defAcctSerializedInvAcct.status_id                             AS defAcctSerializedInvAcct_status_id,
            defAcctSerializedInvAcct.status_value                          AS defAcctSerializedInvAcct_status_value,
            defAcctSerializedInvAcct.status_description                    AS defAcctSerializedInvAcct_status_description,
            defAcctSerializedInvAcct.status_localization_code              AS defAcctSerializedInvAcct_status_localization_code,
            defAcctUnbilledInvAcct.account_id                              AS defAcctUnbilledInvAcct_id,
            defAcctUnbilledInvAcct.account_number                          AS defAcctUnbilledInvAcct_number,
            defAcctUnbilledInvAcct.account_name                            AS defAcctUnbilledInvAcct_name,
            defAcctUnbilledInvAcct.account_form_1099_field                 AS defAcctUnbilledInvAcct_form_1099_field,
            defAcctUnbilledInvAcct.account_corporate_account_indicator     AS defAcctUnbilledInvAcct_corporate_account_indicator,
            defAcctUnbilledInvAcct.comp_id                                 AS defAcctUnbilledInvAcct_comp_id,
            defAcctUnbilledInvAcct.comp_uu_row_id                          AS defAcctUnbilledInvAcct_comp_uu_row_id,
            defAcctUnbilledInvAcct.comp_time_created                       AS defAcctUnbilledInvAcct_comp_time_created,
            defAcctUnbilledInvAcct.comp_time_updated                       AS defAcctUnbilledInvAcct_comp_time_updated,
            defAcctUnbilledInvAcct.comp_name                               AS defAcctUnbilledInvAcct_comp_name,
            defAcctUnbilledInvAcct.comp_doing_business_as                  AS defAcctUnbilledInvAcct_comp_doing_business_as,
            defAcctUnbilledInvAcct.comp_client_code                        AS defAcctUnbilledInvAcct_comp_client_code,
            defAcctUnbilledInvAcct.comp_client_id                          AS defAcctUnbilledInvAcct_comp_client_id,
            defAcctUnbilledInvAcct.comp_dataset_code                       AS defAcctUnbilledInvAcct_comp_dataset_code,
            defAcctUnbilledInvAcct.comp_federal_id_number                  AS defAcctUnbilledInvAcct_comp_federal_id_number,
            defAcctUnbilledInvAcct.type_id                                 AS defAcctUnbilledInvAcct_type_id,
            defAcctUnbilledInvAcct.type_value                              AS defAcctUnbilledInvAcct_type_value,
            defAcctUnbilledInvAcct.type_description                        AS defAcctUnbilledInvAcct_type_description,
            defAcctUnbilledInvAcct.type_localization_code                  AS defAcctUnbilledInvAcct_type_localization_code,
            defAcctUnbilledInvAcct.balance_type_id                         AS defAcctUnbilledInvAcct_balance_type_id,
            defAcctUnbilledInvAcct.balance_type_value                      AS defAcctUnbilledInvAcct_balance_type_value,
            defAcctUnbilledInvAcct.balance_type_description                AS defAcctUnbilledInvAcct_balance_type_description,
            defAcctUnbilledInvAcct.balance_type_localization_code          AS defAcctUnbilledInvAcct_balance_type_localization_code,
            defAcctUnbilledInvAcct.status_id                               AS defAcctUnbilledInvAcct_status_id,
            defAcctUnbilledInvAcct.status_value                            AS defAcctUnbilledInvAcct_status_value,
            defAcctUnbilledInvAcct.status_description                      AS defAcctUnbilledInvAcct_status_description,
            defAcctUnbilledInvAcct.status_localization_code                AS defAcctUnbilledInvAcct_status_localization_code,
            defAcctFreightAcct.account_id                                  AS defAcctFreightAcct_id,
            defAcctFreightAcct.account_number                              AS defAcctFreightAcct_number,
            defAcctFreightAcct.account_name                                AS defAcctFreightAcct_name,
            defAcctFreightAcct.account_form_1099_field                     AS defAcctFreightAcct_form_1099_field,
            defAcctFreightAcct.account_corporate_account_indicator         AS defAcctFreightAcct_corporate_account_indicator,
            defAcctFreightAcct.comp_id                                     AS defAcctFreightAcct_comp_id,
            defAcctFreightAcct.comp_uu_row_id                              AS defAcctFreightAcct_comp_uu_row_id,
            defAcctFreightAcct.comp_time_created                           AS defAcctFreightAcct_comp_time_created,
            defAcctFreightAcct.comp_time_updated                           AS defAcctFreightAcct_comp_time_updated,
            defAcctFreightAcct.comp_name                                   AS defAcctFreightAcct_comp_name,
            defAcctFreightAcct.comp_doing_business_as                      AS defAcctFreightAcct_comp_doing_business_as,
            defAcctFreightAcct.comp_client_code                            AS defAcctFreightAcct_comp_client_code,
            defAcctFreightAcct.comp_client_id                              AS defAcctFreightAcct_comp_client_id,
            defAcctFreightAcct.comp_dataset_code                           AS defAcctFreightAcct_comp_dataset_code,
            defAcctFreightAcct.comp_federal_id_number                      AS defAcctFreightAcct_comp_federal_id_number,
            defAcctFreightAcct.type_id                                     AS defAcctFreightAcct_type_id,
            defAcctFreightAcct.type_value                                  AS defAcctFreightAcct_type_value,
            defAcctFreightAcct.type_description                            AS defAcctFreightAcct_type_description,
            defAcctFreightAcct.type_localization_code                      AS defAcctFreightAcct_type_localization_code,
            defAcctFreightAcct.balance_type_id                             AS defAcctFreightAcct_balance_type_id,
            defAcctFreightAcct.balance_type_value                          AS defAcctFreightAcct_balance_type_value,
            defAcctFreightAcct.balance_type_description                    AS defAcctFreightAcct_balance_type_description,
            defAcctFreightAcct.balance_type_localization_code              AS defAcctFreightAcct_balance_type_localization_code,
            defAcctFreightAcct.status_id                                   AS defAcctFreightAcct_status_id,
            defAcctFreightAcct.status_value                                AS defAcctFreightAcct_status_value,
            defAcctFreightAcct.status_description                          AS defAcctFreightAcct_status_description,
            defAcctFreightAcct.status_localization_code                    AS defAcctFreightAcct_status_localization_code
         FROM general_ledger_control glCtrl
            JOIN company comp ON glCtrl.company_id = comp.id
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.number = glCtrl.default_profit_center_sfk
            LEFT JOIN account defAPAcct ON glCtrl.default_account_payable_account_id = defAPAcct.account_id
            LEFT JOIN account defAPDiscAcct ON glCtrl.default_account_payable_discount_account_id = defAPDiscAcct.account_id
            LEFT JOIN account defARAcct ON glCtrl.default_account_receivable_account_id = defARAcct.account_id
            LEFT JOIN account defARDiscAcct ON glCtrl.default_account_receivable_discount_account_id = defARDiscAcct.account_id
            LEFT JOIN account defAcctMiscInvAcct ON glCtrl.default_account_misc_inventory_account_id = defAcctMiscInvAcct.account_id
            LEFT JOIN account defAcctSerializedInvAcct ON glCtrl.default_account_serialized_inventory_account_id = defAcctSerializedInvAcct.account_id
            LEFT JOIN account defAcctUnbilledInvAcct ON glCtrl.default_account_unbilled_inventory_account_id = defAcctUnbilledInvAcct.account_id
            LEFT JOIN account defAcctFreightAcct ON glCtrl.default_account_freight_account_id = defAcctFreightAcct.account_id
      """
   }

   fun exists(company: Company): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT company_id FROM general_ledger_control WHERE company_id = :company_id)", mapOf("company_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if GeneralLedgerControl: {} exists resulted in {}", company, exists)

      return exists
   }

   fun findOne(company: Company): GeneralLedgerControlEntity? {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE glCtrl.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            val defaultProfitCenter = storeRepository.mapRow(rs, company, "profitCenter_")
            val defaultAccountPayableAccount = accountRepository.mapRowOrNull(rs, company, "defAPAcct_", "defAPAcct_")
            val defaultAccountPayableDiscountAccount = accountRepository.mapRowOrNull(rs, company, "defAPDiscAcct_", "defAPDiscAcct_")
            val defaultAccountReceivableAccount = accountRepository.mapRowOrNull(rs, company, "defARAcct_", "defARAcct_")
            val defaultAccountReceivableDiscountAccount = accountRepository.mapRowOrNull(rs, company, "defARDiscAcct_", "defARDiscAcct_")
            val defaultAccountMiscInventoryAccount = accountRepository.mapRowOrNull(rs, company, "defAcctMiscInvAcct_", "defAcctMiscInvAcct_")
            val defaultAccountSerializedInventoryAccount = accountRepository.mapRowOrNull(rs, company, "defAcctSerializedInvAcct_", "defAcctSerializedInvAcct_")
            val defaultAccountUnbilledInventoryAccount = accountRepository.mapRowOrNull(rs, company, "defAcctUnbilledInvAcct_", "defAcctUnbilledInvAcct_")
            val defaultAccountFreightAccount = accountRepository.mapRowOrNull(rs, company, "defAcctFreightAcct_", "defAcctFreightAcct_")

            mapRow(
               rs,
               defaultProfitCenter,
               defaultAccountPayableAccount,
               defaultAccountPayableDiscountAccount,
               defaultAccountReceivableAccount,
               defaultAccountReceivableDiscountAccount,
               defaultAccountMiscInventoryAccount,
               defaultAccountSerializedInventoryAccount,
               defaultAccountUnbilledInventoryAccount,
               defaultAccountFreightAccount,
               "glCtrl_"
            )
         }
      )

      logger.trace("Searching for GeneralLedgerControl: {} resulted in {}", company, found)

      return found
   }

   @Transactional
   fun insert(entity: GeneralLedgerControlEntity, company: Company): GeneralLedgerControlEntity {
      logger.debug("Inserting general_ledger_control {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO general_ledger_control(
            company_id,
            default_profit_center_sfk,
            default_account_payable_account_id,
            default_account_payable_discount_account_id,
            default_account_receivable_account_id,
            default_account_receivable_discount_account_id,
            default_account_misc_inventory_account_id,
            default_account_serialized_inventory_account_id,
            default_account_unbilled_inventory_account_id,
            default_account_freight_account_id
         )
         VALUES (
            :company_id,
            :default_profit_center_sfk,
            :default_account_payable_account_id,
            :default_account_payable_discount_account_id,
            :default_account_receivable_account_id,
            :default_account_receivable_discount_account_id,
            :default_account_misc_inventory_account_id,
            :default_account_serialized_inventory_account_id,
            :default_account_unbilled_inventory_account_id,
            :default_account_freight_account_id
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "default_profit_center_sfk" to entity.defaultProfitCenter.myNumber(),
            "default_account_payable_account_id" to entity.defaultAccountPayableAccount?.id,
            "default_account_payable_discount_account_id" to entity.defaultAccountPayableDiscountAccount?.id,
            "default_account_receivable_account_id" to entity.defaultAccountReceivableAccount?.id,
            "default_account_receivable_discount_account_id" to entity.defaultAccountReceivableDiscountAccount?.id,
            "default_account_misc_inventory_account_id" to entity.defaultAccountMiscInventoryAccount?.id,
            "default_account_serialized_inventory_account_id" to entity.defaultAccountSerializedInventoryAccount?.id,
            "default_account_unbilled_inventory_account_id" to entity.defaultAccountUnbilledInventoryAccount?.id,
            "default_account_freight_account_id" to entity.defaultAccountFreightAccount?.id
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.defaultProfitCenter,
               entity.defaultAccountPayableAccount,
               entity.defaultAccountPayableDiscountAccount,
               entity.defaultAccountReceivableAccount,
               entity.defaultAccountReceivableDiscountAccount,
               entity.defaultAccountMiscInventoryAccount,
               entity.defaultAccountSerializedInventoryAccount,
               entity.defaultAccountUnbilledInventoryAccount,
               entity.defaultAccountFreightAccount
            )
         }
      )
   }

   @Transactional
   fun update(entity: GeneralLedgerControlEntity, company: Company): GeneralLedgerControlEntity {
      logger.debug("Updating general_ledger_control {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE general_ledger_control
         SET
            company_id = :company_id,
            default_profit_center_sfk = :default_profit_center_sfk,
            default_account_payable_account_id = :default_account_payable_account_id,
            default_account_payable_discount_account_id = :default_account_payable_discount_account_id,
            default_account_receivable_account_id = :default_account_receivable_account_id,
            default_account_receivable_discount_account_id = :default_account_receivable_discount_account_id,
            default_account_misc_inventory_account_id = :default_account_misc_inventory_account_id,
            default_account_serialized_inventory_account_id = :default_account_serialized_inventory_account_id,
            default_account_unbilled_inventory_account_id = :default_account_unbilled_inventory_account_id,
            default_account_freight_account_id = :default_account_freight_account_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "default_profit_center_sfk" to entity.defaultProfitCenter.myNumber(),
            "default_account_payable_account_id" to entity.defaultAccountPayableAccount?.id,
            "default_account_payable_discount_account_id" to entity.defaultAccountPayableDiscountAccount?.id,
            "default_account_receivable_account_id" to entity.defaultAccountReceivableAccount?.id,
            "default_account_receivable_discount_account_id" to entity.defaultAccountReceivableDiscountAccount?.id,
            "default_account_misc_inventory_account_id" to entity.defaultAccountMiscInventoryAccount?.id,
            "default_account_serialized_inventory_account_id" to entity.defaultAccountSerializedInventoryAccount?.id,
            "default_account_unbilled_inventory_account_id" to entity.defaultAccountUnbilledInventoryAccount?.id,
            "default_account_freight_account_id" to entity.defaultAccountFreightAccount?.id
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.defaultProfitCenter,
               entity.defaultAccountPayableAccount,
               entity.defaultAccountPayableDiscountAccount,
               entity.defaultAccountReceivableAccount,
               entity.defaultAccountReceivableDiscountAccount,
               entity.defaultAccountMiscInventoryAccount,
               entity.defaultAccountSerializedInventoryAccount,
               entity.defaultAccountUnbilledInventoryAccount,
               entity.defaultAccountFreightAccount
            )
         }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      defaultProfitCenter: Store,
      defaultAccountPayableAccount: AccountEntity?,
      defaultAccountPayableDiscountAccount: AccountEntity?,
      defaultAccountReceivableAccount: AccountEntity?,
      defaultAccountReceivableDiscountAccount: AccountEntity?,
      defaultAccountMiscInventoryAccount: AccountEntity?,
      defaultAccountSerializedInventoryAccount: AccountEntity?,
      defaultAccountUnbilledInventoryAccount: AccountEntity?,
      defaultAccountFreightAccount: AccountEntity?,
      columnPrefix: String = EMPTY
   ): GeneralLedgerControlEntity {
      return GeneralLedgerControlEntity(
         id = rs.getLong("${columnPrefix}id"),
         defaultProfitCenter = defaultProfitCenter,
         defaultAccountPayableAccount = defaultAccountPayableAccount,
         defaultAccountPayableDiscountAccount = defaultAccountPayableDiscountAccount,
         defaultAccountReceivableAccount = defaultAccountReceivableAccount,
         defaultAccountReceivableDiscountAccount = defaultAccountReceivableDiscountAccount,
         defaultAccountMiscInventoryAccount = defaultAccountMiscInventoryAccount,
         defaultAccountSerializedInventoryAccount = defaultAccountSerializedInventoryAccount,
         defaultAccountUnbilledInventoryAccount = defaultAccountUnbilledInventoryAccount,
         defaultAccountFreightAccount = defaultAccountFreightAccount
      )
   }
}
