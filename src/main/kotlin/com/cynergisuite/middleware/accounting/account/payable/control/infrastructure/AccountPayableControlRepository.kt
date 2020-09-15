package com.cynergisuite.middleware.accounting.account.payable.control.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableCheckFormType
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorType
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorType
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlEntity
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableCheckFormTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PrintCurrencyIndicatorTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PurchaseOrderNumberRequiredIndicatorTypeRepository
import com.cynergisuite.middleware.company.Company
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
class AccountPayableControlRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val accountRepository: AccountRepository,
   private val accountPayableCheckFormTypeRepository: AccountPayableCheckFormTypeRepository,
   private val printCurrencyIndicatorTypeRepository: PrintCurrencyIndicatorTypeRepository,
   private val purchaseOrderNumberRequiredIndicatorTypeRepository: PurchaseOrderNumberRequiredIndicatorTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableControlRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            accountPayableControl.id                                 AS accountPayableControl_id,
            accountPayableControl.uu_row_id                          AS accountPayableControl_uu_row_id,
            accountPayableControl.time_created                       AS accountPayableControl_time_created,
            accountPayableControl.time_updated                       AS accountPayableControl_time_updated,
            accountPayableControl.company_id                         AS accountPayableControl_company_id,
            accountPayableControl.pay_after_discount_date            AS accountPayableControl_pay_after_discount_date,
            accountPayableControl.reset_expense                      AS accountPayableControl_reset_expense,
            accountPayableControl.use_rebates_indicator              AS accountPayableControl_use_rebates_indicator,
            accountPayableControl.trade_company_indicator            AS accountPayableControl_trade_company_indicator,
            accountPayableControl.lock_inventory_indicator           AS accountPayableControl_lock_inventory_indicator,
            checkFormType.id                                         AS checkFormType_id,
            checkFormType.value                                      AS checkFormType_value,
            checkFormType.description                                AS checkFormType_description,
            checkFormType.localization_code                          AS checkFormType_localization_code,
            printCurrencyIndType.id                                  AS printCurrencyIndType_id,
            printCurrencyIndType.value                               AS printCurrencyIndType_value,
            printCurrencyIndType.description                         AS printCurrencyIndType_description,
            printCurrencyIndType.localization_code                   AS printCurrencyIndType_localization_code,
            poNumReqIndType.id                                       AS poNumReqIndType_id,
            poNumReqIndType.value                                    AS poNumReqIndType_value,
            poNumReqIndType.description                              AS poNumReqIndType_description,
            poNumReqIndType.localization_code                        AS poNumReqIndType_localization_code,
            glInvCleAcct.account_id                                  AS glInvCleAcct_id,
            glInvCleAcct.account_name                                AS glInvCleAcct_name,
            glInvCleAcct.account_form_1099_field                     AS glInvCleAcct_form_1099_field,
            glInvCleAcct.account_corporate_account_indicator         AS glInvCleAcct_corporate_account_indicator,
            glInvCleAcct.comp_id                                     AS glInvCleAcct_comp_id,
            glInvCleAcct.comp_uu_row_id                              AS glInvCleAcct_comp_uu_row_id,
            glInvCleAcct.comp_time_created                           AS glInvCleAcct_comp_time_created,
            glInvCleAcct.comp_time_updated                           AS glInvCleAcct_comp_time_updated,
            glInvCleAcct.comp_name                                   AS glInvCleAcct_comp_name,
            glInvCleAcct.comp_doing_business_as                      AS glInvCleAcct_comp_doing_business_as,
            glInvCleAcct.comp_client_code                            AS glInvCleAcct_comp_client_code,
            glInvCleAcct.comp_client_id                              AS glInvCleAcct_comp_client_id,
            glInvCleAcct.comp_dataset_code                           AS glInvCleAcct_comp_dataset_code,
            glInvCleAcct.comp_federal_id_number                      AS glInvCleAcct_comp_federal_id_number,
            glInvCleAcct.type_id                                     AS glInvCleAcct_type_id,
            glInvCleAcct.type_value                                  AS glInvCleAcct_type_value,
            glInvCleAcct.type_description                            AS glInvCleAcct_type_description,
            glInvCleAcct.type_localization_code                      AS glInvCleAcct_type_localization_code,
            glInvCleAcct.balance_type_id                             AS glInvCleAcct_balance_type_id,
            glInvCleAcct.balance_type_value                          AS glInvCleAcct_balance_type_value,
            glInvCleAcct.balance_type_description                    AS glInvCleAcct_balance_type_description,
            glInvCleAcct.balance_type_localization_code              AS glInvCleAcct_balance_type_localization_code,
            glInvCleAcct.status_id                                   AS glInvCleAcct_status_id,
            glInvCleAcct.status_value                                AS glInvCleAcct_status_value,
            glInvCleAcct.status_description                          AS glInvCleAcct_status_description,
            glInvCleAcct.status_localization_code                    AS glInvCleAcct_status_localization_code,
            glInvAcct.account_id                                     AS glInvAcct_id,
            glInvAcct.account_name                                   AS glInvAcct_name,
            glInvAcct.account_form_1099_field                        AS glInvAcct_form_1099_field,
            glInvAcct.account_corporate_account_indicator            AS glInvAcct_corporate_account_indicator,
            glInvAcct.comp_id                                        AS glInvAcct_comp_id,
            glInvAcct.comp_uu_row_id                                 AS glInvAcct_comp_uu_row_id,
            glInvAcct.comp_time_created                              AS glInvAcct_comp_time_created,
            glInvAcct.comp_time_updated                              AS glInvAcct_comp_time_updated,
            glInvAcct.comp_name                                      AS glInvAcct_comp_name,
            glInvAcct.comp_doing_business_as                         AS glInvAcct_comp_doing_business_as,
            glInvAcct.comp_client_code                               AS glInvAcct_comp_client_code,
            glInvAcct.comp_client_id                                 AS glInvAcct_comp_client_id,
            glInvAcct.comp_dataset_code                              AS glInvAcct_comp_dataset_code,
            glInvAcct.comp_federal_id_number                         AS glInvAcct_comp_federal_id_number,
            glInvAcct.type_id                                        AS glInvAcct_type_id,
            glInvAcct.type_value                                     AS glInvAcct_type_value,
            glInvAcct.type_description                               AS glInvAcct_type_description,
            glInvAcct.type_localization_code                         AS glInvAcct_type_localization_code,
            glInvAcct.balance_type_id                                AS glInvAcct_balance_type_id,
            glInvAcct.balance_type_value                             AS glInvAcct_balance_type_value,
            glInvAcct.balance_type_description                       AS glInvAcct_balance_type_description,
            glInvAcct.balance_type_localization_code                 AS glInvAcct_balance_type_localization_code,
            glInvAcct.status_id                                      AS glInvAcct_status_id,
            glInvAcct.status_value                                   AS glInvAcct_status_value,
            glInvAcct.status_description                             AS glInvAcct_status_description,
            glInvAcct.status_localization_code                       AS glInvAcct_status_localization_code
         FROM account_payable_control accountPayableControl
            JOIN account_payable_check_form_type_domain checkFormType ON accountPayableControl.check_form_type_id = checkFormType.id
            JOIN print_currency_indicator_type_domain printCurrencyIndType ON accountPayableControl.print_currency_indicator_type_id = printCurrencyIndType.id
            JOIN purchase_order_number_required_indicator_type_domain poNumReqIndType ON accountPayableControl.purchase_order_number_required_indicator_type_id = poNumReqIndType.id
            JOIN account glInvCleAcct ON accountPayableControl.general_ledger_inventory_clearing_account_id = glInvCleAcct.account_id
            JOIN account glInvAcct ON accountPayableControl.general_ledger_inventory_account_id = glInvAcct.account_id
      """
   }

   fun exists(company: Company): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT company_id FROM account_payable_control WHERE company_id = :company_id)", mapOf("company_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if AccountPayableControl: {} exists resulted in {}", company, exists)

      return exists
   }

   fun findOne(company: Company): AccountPayableControlEntity? {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE accountPayableControl.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            val checkFormType = accountPayableCheckFormTypeRepository.mapRow(rs, "checkFormType_")
            val printCurrencyIndicatorType = printCurrencyIndicatorTypeRepository.mapRow(rs, "printCurrencyIndType_")
            val purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorTypeRepository.mapRow(rs, "poNumReqIndType_")
            val generalLedgerInventoryClearingAccount = accountRepository.mapRow(rs, company, "glInvCleAcct_", "glInvCleAcct_")
            val generalLedgerInventoryAccount = accountRepository.mapRow(rs, company, "glInvAcct_", "glInvCleAcct_")

            mapRow(
               rs,
               checkFormType,
               printCurrencyIndicatorType,
               purchaseOrderNumberRequiredIndicatorType,
               generalLedgerInventoryClearingAccount,
               generalLedgerInventoryAccount,
               "accountPayableControl_"
            )
         }
      )

      logger.trace("Searching for AccountPayableControl: {} resulted in {}", company, found)

      return found
   }

   @Transactional
   fun insert(entity: AccountPayableControlEntity, company: Company): AccountPayableControlEntity {
      logger.debug("Inserting account_payable_control {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_control(
            company_id,
            check_form_type_id,
            pay_after_discount_date,
            reset_expense,
            use_rebates_indicator,
            trade_company_indicator,
            print_currency_indicator_type_id,
            lock_inventory_indicator,
            purchase_order_number_required_indicator_type_id,
            general_ledger_inventory_clearing_account_id,
            general_ledger_inventory_account_id
         )
         VALUES (
            :company_id,
            :check_form_type_id,
            :pay_after_discount_date,
            :reset_expense,
            :use_rebates_indicator,
            :trade_company_indicator,
            :print_currency_indicator_type_id,
            :lock_inventory_indicator,
            :purchase_order_number_required_indicator_type_id,
            :general_ledger_inventory_clearing_account_id,
            :general_ledger_inventory_account_id
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "check_form_type_id" to entity.checkFormType.id,
            "pay_after_discount_date" to entity.payAfterDiscountDate,
            "reset_expense" to entity.resetExpense,
            "use_rebates_indicator" to entity.useRebatesIndicator,
            "trade_company_indicator" to entity.tradeCompanyIndicator,
            "print_currency_indicator_type_id" to entity.printCurrencyIndicatorType.id,
            "lock_inventory_indicator" to entity.lockInventoryIndicator,
            "purchase_order_number_required_indicator_type_id" to entity.purchaseOrderNumberRequiredIndicatorType.id,
            "general_ledger_inventory_clearing_account_id" to entity.generalLedgerInventoryClearingAccount.id,
            "general_ledger_inventory_account_id" to entity.generalLedgerInventoryAccount.id
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.checkFormType,
               entity.printCurrencyIndicatorType,
               entity.purchaseOrderNumberRequiredIndicatorType,
               entity.generalLedgerInventoryClearingAccount,
               entity.generalLedgerInventoryAccount
            )
         }
      )
   }

   @Transactional
   fun update(entity: AccountPayableControlEntity, company: Company): AccountPayableControlEntity {
      logger.debug("Updating account_payable_control {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE account_payable_control
         SET
            company_id = :company_id,
            check_form_type_id = :check_form_type_id,
            pay_after_discount_date = :pay_after_discount_date,
            reset_expense = :reset_expense,
            use_rebates_indicator = :use_rebates_indicator,
            trade_company_indicator = :trade_company_indicator,
            print_currency_indicator_type_id = :print_currency_indicator_type_id,
            lock_inventory_indicator = :lock_inventory_indicator,
            purchase_order_number_required_indicator_type_id = :purchase_order_number_required_indicator_type_id,
            general_ledger_inventory_clearing_account_id = :general_ledger_inventory_clearing_account_id,
            general_ledger_inventory_account_id = :general_ledger_inventory_account_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "check_form_type_id" to entity.checkFormType.id,
            "pay_after_discount_date" to entity.payAfterDiscountDate,
            "reset_expense" to entity.resetExpense,
            "use_rebates_indicator" to entity.useRebatesIndicator,
            "trade_company_indicator" to entity.tradeCompanyIndicator,
            "print_currency_indicator_type_id" to entity.printCurrencyIndicatorType.id,
            "lock_inventory_indicator" to entity.lockInventoryIndicator,
            "purchase_order_number_required_indicator_type_id" to entity.purchaseOrderNumberRequiredIndicatorType.id,
            "general_ledger_inventory_clearing_account_id" to entity.generalLedgerInventoryClearingAccount.id,
            "general_ledger_inventory_account_id" to entity.generalLedgerInventoryAccount.id
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.checkFormType,
               entity.printCurrencyIndicatorType,
               entity.purchaseOrderNumberRequiredIndicatorType,
               entity.generalLedgerInventoryClearingAccount,
               entity.generalLedgerInventoryAccount
            )
         }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      checkFormType: AccountPayableCheckFormType,
      printCurrencyIndicatorType: PrintCurrencyIndicatorType,
      purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorType,
      generalLedgerInventoryClearingAccount: AccountEntity,
      generalLedgerInventoryAccount: AccountEntity,
      columnPrefix: String = EMPTY
   ): AccountPayableControlEntity {
      return AccountPayableControlEntity(
         id = rs.getLong("${columnPrefix}id"),
         checkFormType = checkFormType,
         payAfterDiscountDate = rs.getBoolean("${columnPrefix}pay_after_discount_date"),
         resetExpense = rs.getBoolean("${columnPrefix}reset_expense"),
         useRebatesIndicator = rs.getBoolean("${columnPrefix}use_rebates_indicator"),
         tradeCompanyIndicator = rs.getBoolean("${columnPrefix}trade_company_indicator"),
         printCurrencyIndicatorType = printCurrencyIndicatorType,
         lockInventoryIndicator = rs.getBoolean("${columnPrefix}lock_inventory_indicator"),
         purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorType,
         generalLedgerInventoryClearingAccount = generalLedgerInventoryClearingAccount,
         generalLedgerInventoryAccount = generalLedgerInventoryAccount
      )
   }
}
