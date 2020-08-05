package com.cynergisuite.middleware.accounting.account.payable.control.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.PrintCurrencyIndicatorType
import com.cynergisuite.middleware.accounting.account.payable.PurchaseOrderNumberRequiredIndicatorType
import com.cynergisuite.middleware.accounting.account.payable.control.AccountPayableControlEntity
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PrintCurrencyIndicatorTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PurchaseOrderNumberRequiredIndicatorTypeRepository
import com.cynergisuite.middleware.company.Company
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableControlRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val printCurrencyIndicatorTypeRepository: PrintCurrencyIndicatorTypeRepository,
   private val purchaseOrderNumberRequiredIndicatorTypeRepository: PurchaseOrderNumberRequiredIndicatorTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableControlRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
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
            printCurrencyIndType.id                                  AS printCurrencyIndType_id,
            printCurrencyIndType.value                               AS printCurrencyIndType_value,
            printCurrencyIndType.description                         AS printCurrencyIndType_description,
            printCurrencyIndType.localization_code                   AS printCurrencyIndType_localization_code,
            poNumReqIndType.id                                       AS poNumReqIndType_id,
            poNumReqIndType.value                                    AS poNumReqIndType_value,
            poNumReqIndType.description                              AS poNumReqIndType_description,
            poNumReqIndType.localization_code                        AS poNumReqIndType_localization_code
         FROM account_payable_control accountPayableControl
            JOIN print_currency_indicator_type_domain printCurrencyIndType ON accountPayableControl.print_currency_indicator_type_id = printCurrencyIndType.id
            JOIN purchase_order_number_required_indicator_type_domain poNumReqIndType ON accountPayableControl.purchase_order_number_required_indicator_type_id = poNumReqIndType.id
      """
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT id FROM account_payable_control WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if AccountPayableControl: {} exists resulted in {}", id, exists)

      return exists
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
            val printCurrencyIndicatorType = printCurrencyIndicatorTypeRepository.mapRow(rs, "printCurrencyIndType_")
            val purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorTypeRepository.mapRow(rs, "poNumReqIndType_")

            mapRow(
               rs, printCurrencyIndicatorType, purchaseOrderNumberRequiredIndicatorType, "accountPayableControl_"
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
            pay_after_discount_date,
            reset_expense,
            use_rebates_indicator,
            trade_company_indicator,
            print_currency_indicator_type_id,
            lock_inventory_indicator,
            purchase_order_number_required_indicator_type_id
         )
         VALUES (
            :company_id,
            :pay_after_discount_date,
            :reset_expense,
            :use_rebates_indicator,
            :trade_company_indicator,
            :print_currency_indicator_type_id,
            :lock_inventory_indicator,
            :purchase_order_number_required_indicator_type_id
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "pay_after_discount_date" to entity.payAfterDiscountDate,
            "reset_expense" to entity.resetExpense,
            "use_rebates_indicator" to entity.useRebatesIndicator,
            "trade_company_indicator" to entity.tradeCompanyIndicator,
            "print_currency_indicator_type_id" to entity.printCurrencyIndicatorType.id,
            "lock_inventory_indicator" to entity.lockInventoryIndicator,
            "purchase_order_number_required_indicator_type_id" to entity.purchaseOrderNumberRequiredIndicatorType.id
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs, entity.printCurrencyIndicatorType, entity.purchaseOrderNumberRequiredIndicatorType
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
            pay_after_discount_date = :pay_after_discount_date,
            reset_expense = :reset_expense,
            use_rebates_indicator = :use_rebates_indicator,
            trade_company_indicator = :trade_company_indicator,
            print_currency_indicator_type_id = :print_currency_indicator_type_id,
            lock_inventory_indicator = :lock_inventory_indicator,
            purchase_order_number_required_indicator_type_id = :purchase_order_number_required_indicator_type_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "pay_after_discount_date" to entity.payAfterDiscountDate,
            "reset_expense" to entity.resetExpense,
            "use_rebates_indicator" to entity.useRebatesIndicator,
            "trade_company_indicator" to entity.tradeCompanyIndicator,
            "print_currency_indicator_type_id" to entity.printCurrencyIndicatorType.id,
            "lock_inventory_indicator" to entity.lockInventoryIndicator,
            "purchase_order_number_required_indicator_type_id" to entity.purchaseOrderNumberRequiredIndicatorType.id
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.printCurrencyIndicatorType,
               entity.purchaseOrderNumberRequiredIndicatorType
            )
         }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      printCurrencyIndicatorType: PrintCurrencyIndicatorType,
      purchaseOrderNumberRequiredIndicatorType: PurchaseOrderNumberRequiredIndicatorType,
      columnPrefix: String = StringUtils.EMPTY
   ): AccountPayableControlEntity {
      return AccountPayableControlEntity(
         id = rs.getLong("${columnPrefix}id"),
         payAfterDiscountDate = rs.getBoolean("${columnPrefix}pay_after_discount_date"),
         resetExpense = rs.getBoolean("${columnPrefix}reset_expense"),
         useRebatesIndicator = rs.getBoolean("${columnPrefix}use_rebates_indicator"),
         tradeCompanyIndicator = rs.getBoolean("${columnPrefix}trade_company_indicator"),
         printCurrencyIndicatorType = printCurrencyIndicatorType,
         lockInventoryIndicator = rs.getBoolean("${columnPrefix}lock_inventory_indicator"),
         purchaseOrderNumberRequiredIndicatorType = purchaseOrderNumberRequiredIndicatorType
      )
   }
}
