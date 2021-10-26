package com.cynergisuite.middleware.accounting.account.payable.control.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryForObject
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
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AccountPayableControlRepository @Inject constructor(
   private val jdbc: Jdbi,
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
            glInvCleAcct.account_number                              AS glInvCleAcct_number,
            glInvCleAcct.account_form_1099_field                     AS glInvCleAcct_form_1099_field,
            glInvCleAcct.account_corporate_account_indicator         AS glInvCleAcct_corporate_account_indicator,
            glInvCleAcct.account_comp_id                             AS glInvCleAcct_comp_id,
            glInvCleAcct.account_type_id                             AS glInvCleAcct_type_id,
            glInvCleAcct.account_type_value                          AS glInvCleAcct_type_value,
            glInvCleAcct.account_type_description                    AS glInvCleAcct_type_description,
            glInvCleAcct.account_type_localization_code              AS glInvCleAcct_type_localization_code,
            glInvCleAcct.account_balance_type_id                     AS glInvCleAcct_balance_type_id,
            glInvCleAcct.account_balance_type_value                  AS glInvCleAcct_balance_type_value,
            glInvCleAcct.account_balance_type_description            AS glInvCleAcct_balance_type_description,
            glInvCleAcct.account_balance_type_localization_code      AS glInvCleAcct_balance_type_localization_code,
            glInvCleAcct.account_status_id                           AS glInvCleAcct_status_id,
            glInvCleAcct.account_status_value                        AS glInvCleAcct_status_value,
            glInvCleAcct.account_status_description                  AS glInvCleAcct_status_description,
            glInvCleAcct.account_status_localization_code            AS glInvCleAcct_status_localization_code,
            glInvAcct.account_id                                     AS glInvAcct_id,
            glInvAcct.account_number                                 AS glInvAcct_number,
            glInvAcct.account_name                                   AS glInvAcct_name,
            glInvAcct.account_form_1099_field                        AS glInvAcct_form_1099_field,
            glInvAcct.account_corporate_account_indicator            AS glInvAcct_corporate_account_indicator,
            glInvAcct.account_comp_id                                AS glInvAcct_comp_id,
            glInvAcct.account_type_id                                AS glInvAcct_type_id,
            glInvAcct.account_type_value                             AS glInvAcct_type_value,
            glInvAcct.account_type_description                       AS glInvAcct_type_description,
            glInvAcct.account_type_localization_code                 AS glInvAcct_type_localization_code,
            glInvAcct.account_balance_type_id                        AS glInvAcct_balance_type_id,
            glInvAcct.account_balance_type_value                     AS glInvAcct_balance_type_value,
            glInvAcct.account_balance_type_description               AS glInvAcct_balance_type_description,
            glInvAcct.account_balance_type_localization_code         AS glInvAcct_balance_type_localization_code,
            glInvAcct.account_status_id                              AS glInvAcct_status_id,
            glInvAcct.account_status_value                           AS glInvAcct_status_value,
            glInvAcct.account_status_description                     AS glInvAcct_status_description,
            glInvAcct.account_status_localization_code               AS glInvAcct_status_localization_code
         FROM account_payable_control accountPayableControl
            JOIN account_payable_check_form_type_domain checkFormType ON accountPayableControl.check_form_type_id = checkFormType.id
            JOIN print_currency_indicator_type_domain printCurrencyIndType ON accountPayableControl.print_currency_indicator_type_id = printCurrencyIndType.id
            JOIN purchase_order_number_required_indicator_type_domain poNumReqIndType ON accountPayableControl.purchase_order_number_required_indicator_type_id = poNumReqIndType.id
            JOIN account glInvCleAcct ON accountPayableControl.general_ledger_inventory_clearing_account_id = glInvCleAcct.account_id
            JOIN account glInvAcct ON accountPayableControl.general_ledger_inventory_account_id = glInvAcct.account_id
      """
   }

   @ReadOnly
   fun exists(company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT company_id FROM account_payable_control WHERE company_id = :company_id)", mapOf("company_id" to company.id), Boolean::class.java)

      logger.trace("Checking if AccountPayableControl: {} exists resulted in {}", company, exists)

      return exists
   }

   @ReadOnly
   fun findOne(company: CompanyEntity): AccountPayableControlEntity? {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE accountPayableControl.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         val checkFormType = accountPayableCheckFormTypeRepository.mapRow(rs, "checkFormType_")
         val printCurrencyIndicatorType = printCurrencyIndicatorTypeRepository.mapRow(rs, "printCurrencyIndType_")
         val purchaseOrderNumberRequiredIndicatorType =
            purchaseOrderNumberRequiredIndicatorTypeRepository.mapRow(rs, "poNumReqIndType_")
         val generalLedgerInventoryClearingAccount = accountRepository.mapRow(rs, company, "glInvCleAcct_")
         val generalLedgerInventoryAccount = accountRepository.mapRow(rs, company, "glInvAcct_")

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

      logger.trace("Searching for AccountPayableControl: {} resulted in {}", company, found)

      return found
   }

   @Transactional
   fun insert(entity: AccountPayableControlEntity, company: CompanyEntity): AccountPayableControlEntity {
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
            "company_id" to company.id,
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
         )
      ) { rs, _ ->
         mapRow(
            rs,
            entity.checkFormType,
            entity.printCurrencyIndicatorType,
            entity.purchaseOrderNumberRequiredIndicatorType,
            entity.generalLedgerInventoryClearingAccount,
            entity.generalLedgerInventoryAccount
         )
      }
   }

   @Transactional
   fun update(entity: AccountPayableControlEntity, company: CompanyEntity): AccountPayableControlEntity {
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
            "company_id" to company.id,
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
         )
      ) { rs, _ ->
         mapRow(
            rs,
            entity.checkFormType,
            entity.printCurrencyIndicatorType,
            entity.purchaseOrderNumberRequiredIndicatorType,
            entity.generalLedgerInventoryClearingAccount,
            entity.generalLedgerInventoryAccount
         )
      }
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
         id = rs.getUuid("${columnPrefix}id"),
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
