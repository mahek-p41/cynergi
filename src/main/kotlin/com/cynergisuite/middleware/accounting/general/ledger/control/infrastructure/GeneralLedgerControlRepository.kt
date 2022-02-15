package com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.control.GeneralLedgerControlEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class GeneralLedgerControlRepository @Inject constructor(
   private val jdbc: Jdbi,
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
            glCtrl.id                                                         AS glCtrl_id,
            glCtrl.time_created                                               AS glCtrl_time_created,
            glCtrl.time_updated                                               AS glCtrl_time_updated,
            glCtrl.company_id                                                 AS glCtrl_company_id,
            glCtrl.default_profit_center_sfk                                  AS glCtrl_default_profit_center_sfk,
            profitCenter.id                                                   AS profitCenter_id,
            profitCenter.number                                               AS profitCenter_number,
            profitCenter.name                                                 AS profitCenter_name,
            profitCenter.dataset                                              AS profitCenter_dataset,
            defAPAcct.account_id                                              AS defAPAcct_id,
            defAPAcct.account_number                                          AS defAPAcct_number,
            defAPAcct.account_name                                            AS defAPAcct_name,
            defAPAcct.account_form_1099_field                                 AS defAPAcct_form_1099_field,
            defAPAcct.account_corporate_account_indicator                     AS defAPAcct_corporate_account_indicator,
            defAPAcct.account_comp_id                                         AS defAPAcct_comp_id,
            defAPAcct.account_deleted                                         AS defAPAcct_deleted,
            defAPAcct.account_type_id                                         AS defAPAcct_type_id,
            defAPAcct.account_type_value                                      AS defAPAcct_type_value,
            defAPAcct.account_type_description                                AS defAPAcct_type_description,
            defAPAcct.account_type_localization_code                          AS defAPAcct_type_localization_code,
            defAPAcct.account_balance_type_id                                 AS defAPAcct_balance_type_id,
            defAPAcct.account_balance_type_value                              AS defAPAcct_balance_type_value,
            defAPAcct.account_balance_type_description                        AS defAPAcct_balance_type_description,
            defAPAcct.account_balance_type_localization_code                  AS defAPAcct_balance_type_localization_code,
            defAPAcct.account_status_id                                       AS defAPAcct_status_id,
            defAPAcct.account_status_value                                    AS defAPAcct_status_value,
            defAPAcct.account_status_description                              AS defAPAcct_status_description,
            defAPAcct.account_status_localization_code                        AS defAPAcct_status_localization_code,
            defAPDiscAcct.account_id                                          AS defAPDiscAcct_id,
            defAPDiscAcct.account_number                                      AS defAPDiscAcct_number,
            defAPDiscAcct.account_name                                        AS defAPDiscAcct_name,
            defAPDiscAcct.account_form_1099_field                             AS defAPDiscAcct_form_1099_field,
            defAPDiscAcct.account_corporate_account_indicator                 AS defAPDiscAcct_corporate_account_indicator,
            defAPDiscAcct.account_comp_id                                     AS defAPDiscAcct_comp_id,
            defAPDiscAcct.account_deleted                                     AS defAPDiscAcct_deleted,
            defAPDiscAcct.account_type_id                                     AS defAPDiscAcct_type_id,
            defAPDiscAcct.account_type_value                                  AS defAPDiscAcct_type_value,
            defAPDiscAcct.account_type_description                            AS defAPDiscAcct_type_description,
            defAPDiscAcct.account_type_localization_code                      AS defAPDiscAcct_type_localization_code,
            defAPDiscAcct.account_balance_type_id                             AS defAPDiscAcct_balance_type_id,
            defAPDiscAcct.account_balance_type_value                          AS defAPDiscAcct_balance_type_value,
            defAPDiscAcct.account_balance_type_description                    AS defAPDiscAcct_balance_type_description,
            defAPDiscAcct.account_balance_type_localization_code              AS defAPDiscAcct_balance_type_localization_code,
            defAPDiscAcct.account_status_id                                   AS defAPDiscAcct_status_id,
            defAPDiscAcct.account_status_value                                AS defAPDiscAcct_status_value,
            defAPDiscAcct.account_status_description                          AS defAPDiscAcct_status_description,
            defAPDiscAcct.account_status_localization_code                    AS defAPDiscAcct_status_localization_code,
            defARAcct.account_id                                              AS defARAcct_id,
            defARAcct.account_number                                          AS defARAcct_number,
            defARAcct.account_name                                            AS defARAcct_name,
            defARAcct.account_form_1099_field                                 AS defARAcct_form_1099_field,
            defARAcct.account_corporate_account_indicator                     AS defARAcct_corporate_account_indicator,
            defARAcct.account_comp_id                                         AS defARAcct_comp_id,
            defARAcct.account_deleted                                         AS defARAcct_deleted,
            defARAcct.account_type_id                                         AS defARAcct_type_id,
            defARAcct.account_type_value                                      AS defARAcct_type_value,
            defARAcct.account_type_description                                AS defARAcct_type_description,
            defARAcct.account_type_localization_code                          AS defARAcct_type_localization_code,
            defARAcct.account_balance_type_id                                 AS defARAcct_balance_type_id,
            defARAcct.account_balance_type_value                              AS defARAcct_balance_type_value,
            defARAcct.account_balance_type_description                        AS defARAcct_balance_type_description,
            defARAcct.account_balance_type_localization_code                  AS defARAcct_balance_type_localization_code,
            defARAcct.account_status_id                                       AS defARAcct_status_id,
            defARAcct.account_status_value                                    AS defARAcct_status_value,
            defARAcct.account_status_description                              AS defARAcct_status_description,
            defARAcct.account_status_localization_code                        AS defARAcct_status_localization_code,
            defARDiscAcct.account_id                                          AS defARDiscAcct_id,
            defARDiscAcct.account_number                                      AS defARDiscAcct_number,
            defARDiscAcct.account_name                                        AS defARDiscAcct_name,
            defARDiscAcct.account_form_1099_field                             AS defARDiscAcct_form_1099_field,
            defARDiscAcct.account_corporate_account_indicator                 AS defARDiscAcct_corporate_account_indicator,
            defARDiscAcct.account_comp_id                                     AS defARDiscAcct_comp_id,
            defARDiscAcct.account_deleted                                     AS defARDiscAcct_deleted,
            defARDiscAcct.account_type_id                                     AS defARDiscAcct_type_id,
            defARDiscAcct.account_type_value                                  AS defARDiscAcct_type_value,
            defARDiscAcct.account_type_description                            AS defARDiscAcct_type_description,
            defARDiscAcct.account_type_localization_code                      AS defARDiscAcct_type_localization_code,
            defARDiscAcct.account_balance_type_id                             AS defARDiscAcct_balance_type_id,
            defARDiscAcct.account_balance_type_value                          AS defARDiscAcct_balance_type_value,
            defARDiscAcct.account_balance_type_description                    AS defARDiscAcct_balance_type_description,
            defARDiscAcct.account_balance_type_localization_code              AS defARDiscAcct_balance_type_localization_code,
            defARDiscAcct.account_status_id                                   AS defARDiscAcct_status_id,
            defARDiscAcct.account_status_value                                AS defARDiscAcct_status_value,
            defARDiscAcct.account_status_description                          AS defARDiscAcct_status_description,
            defARDiscAcct.account_status_localization_code                    AS defARDiscAcct_status_localization_code,
            defAcctMiscInvAcct.account_id                                     AS defAcctMiscInvAcct_id,
            defAcctMiscInvAcct.account_number                                 AS defAcctMiscInvAcct_number,
            defAcctMiscInvAcct.account_name                                   AS defAcctMiscInvAcct_name,
            defAcctMiscInvAcct.account_form_1099_field                        AS defAcctMiscInvAcct_form_1099_field,
            defAcctMiscInvAcct.account_corporate_account_indicator            AS defAcctMiscInvAcct_corporate_account_indicator,
            defAcctMiscInvAcct.account_comp_id                                AS defAcctMiscInvAcct_comp_id,
            defAcctMiscInvAcct.account_deleted                                AS defAcctMiscInvAcct_deleted,
            defAcctMiscInvAcct.account_type_id                                AS defAcctMiscInvAcct_type_id,
            defAcctMiscInvAcct.account_type_value                             AS defAcctMiscInvAcct_type_value,
            defAcctMiscInvAcct.account_type_description                       AS defAcctMiscInvAcct_type_description,
            defAcctMiscInvAcct.account_type_localization_code                 AS defAcctMiscInvAcct_type_localization_code,
            defAcctMiscInvAcct.account_balance_type_id                        AS defAcctMiscInvAcct_balance_type_id,
            defAcctMiscInvAcct.account_balance_type_value                     AS defAcctMiscInvAcct_balance_type_value,
            defAcctMiscInvAcct.account_balance_type_description               AS defAcctMiscInvAcct_balance_type_description,
            defAcctMiscInvAcct.account_balance_type_localization_code         AS defAcctMiscInvAcct_balance_type_localization_code,
            defAcctMiscInvAcct.account_status_id                              AS defAcctMiscInvAcct_status_id,
            defAcctMiscInvAcct.account_status_value                           AS defAcctMiscInvAcct_status_value,
            defAcctMiscInvAcct.account_status_description                     AS defAcctMiscInvAcct_status_description,
            defAcctMiscInvAcct.account_status_localization_code               AS defAcctMiscInvAcct_status_localization_code,
            defAcctSerializedInvAcct.account_id                               AS defAcctSerializedInvAcct_id,
            defAcctSerializedInvAcct.account_number                           AS defAcctSerializedInvAcct_number,
            defAcctSerializedInvAcct.account_name                             AS defAcctSerializedInvAcct_name,
            defAcctSerializedInvAcct.account_form_1099_field                  AS defAcctSerializedInvAcct_form_1099_field,
            defAcctSerializedInvAcct.account_corporate_account_indicator      AS defAcctSerializedInvAcct_corporate_account_indicator,
            defAcctSerializedInvAcct.account_comp_id                          AS defAcctSerializedInvAcct_comp_id,
            defAcctSerializedInvAcct.account_deleted                          AS defAcctSerializedInvAcct_deleted,
            defAcctSerializedInvAcct.account_type_id                          AS defAcctSerializedInvAcct_type_id,
            defAcctSerializedInvAcct.account_type_value                       AS defAcctSerializedInvAcct_type_value,
            defAcctSerializedInvAcct.account_type_description                 AS defAcctSerializedInvAcct_type_description,
            defAcctSerializedInvAcct.account_type_localization_code           AS defAcctSerializedInvAcct_type_localization_code,
            defAcctSerializedInvAcct.account_balance_type_id                  AS defAcctSerializedInvAcct_balance_type_id,
            defAcctSerializedInvAcct.account_balance_type_value               AS defAcctSerializedInvAcct_balance_type_value,
            defAcctSerializedInvAcct.account_balance_type_description         AS defAcctSerializedInvAcct_balance_type_description,
            defAcctSerializedInvAcct.account_balance_type_localization_code   AS defAcctSerializedInvAcct_balance_type_localization_code,
            defAcctSerializedInvAcct.account_status_id                        AS defAcctSerializedInvAcct_status_id,
            defAcctSerializedInvAcct.account_status_value                     AS defAcctSerializedInvAcct_status_value,
            defAcctSerializedInvAcct.account_status_description               AS defAcctSerializedInvAcct_status_description,
            defAcctSerializedInvAcct.account_status_localization_code         AS defAcctSerializedInvAcct_status_localization_code,
            defAcctUnbilledInvAcct.account_id                                 AS defAcctUnbilledInvAcct_id,
            defAcctUnbilledInvAcct.account_number                             AS defAcctUnbilledInvAcct_number,
            defAcctUnbilledInvAcct.account_name                               AS defAcctUnbilledInvAcct_name,
            defAcctUnbilledInvAcct.account_form_1099_field                    AS defAcctUnbilledInvAcct_form_1099_field,
            defAcctUnbilledInvAcct.account_corporate_account_indicator        AS defAcctUnbilledInvAcct_corporate_account_indicator,
            defAcctUnbilledInvAcct.account_comp_id                            AS defAcctUnbilledInvAcct_comp_id,
            defAcctUnbilledInvAcct.account_deleted                            AS defAcctUnbilledInvAcct_deleted,
            defAcctUnbilledInvAcct.account_type_id                            AS defAcctUnbilledInvAcct_type_id,
            defAcctUnbilledInvAcct.account_type_value                         AS defAcctUnbilledInvAcct_type_value,
            defAcctUnbilledInvAcct.account_type_description                   AS defAcctUnbilledInvAcct_type_description,
            defAcctUnbilledInvAcct.account_type_localization_code             AS defAcctUnbilledInvAcct_type_localization_code,
            defAcctUnbilledInvAcct.account_balance_type_id                    AS defAcctUnbilledInvAcct_balance_type_id,
            defAcctUnbilledInvAcct.account_balance_type_value                 AS defAcctUnbilledInvAcct_balance_type_value,
            defAcctUnbilledInvAcct.account_balance_type_description           AS defAcctUnbilledInvAcct_balance_type_description,
            defAcctUnbilledInvAcct.account_balance_type_localization_code     AS defAcctUnbilledInvAcct_balance_type_localization_code,
            defAcctUnbilledInvAcct.account_status_id                          AS defAcctUnbilledInvAcct_status_id,
            defAcctUnbilledInvAcct.account_status_value                       AS defAcctUnbilledInvAcct_status_value,
            defAcctUnbilledInvAcct.account_status_description                 AS defAcctUnbilledInvAcct_status_description,
            defAcctUnbilledInvAcct.account_status_localization_code           AS defAcctUnbilledInvAcct_status_localization_code,
            defAcctFreightAcct.account_id                                     AS defAcctFreightAcct_id,
            defAcctFreightAcct.account_number                                 AS defAcctFreightAcct_number,
            defAcctFreightAcct.account_name                                   AS defAcctFreightAcct_name,
            defAcctFreightAcct.account_form_1099_field                        AS defAcctFreightAcct_form_1099_field,
            defAcctFreightAcct.account_corporate_account_indicator            AS defAcctFreightAcct_corporate_account_indicator,
            defAcctFreightAcct.account_comp_id                                AS defAcctFreightAcct_comp_id,
            defAcctFreightAcct.account_deleted                                AS defAcctFreightAcct_deleted,
            defAcctFreightAcct.account_type_id                                AS defAcctFreightAcct_type_id,
            defAcctFreightAcct.account_type_value                             AS defAcctFreightAcct_type_value,
            defAcctFreightAcct.account_type_description                       AS defAcctFreightAcct_type_description,
            defAcctFreightAcct.account_type_localization_code                 AS defAcctFreightAcct_type_localization_code,
            defAcctFreightAcct.account_balance_type_id                        AS defAcctFreightAcct_balance_type_id,
            defAcctFreightAcct.account_balance_type_value                     AS defAcctFreightAcct_balance_type_value,
            defAcctFreightAcct.account_balance_type_description               AS defAcctFreightAcct_balance_type_description,
            defAcctFreightAcct.account_balance_type_localization_code         AS defAcctFreightAcct_balance_type_localization_code,
            defAcctFreightAcct.account_status_id                              AS defAcctFreightAcct_status_id,
            defAcctFreightAcct.account_status_value                           AS defAcctFreightAcct_status_value,
            defAcctFreightAcct.account_status_description                     AS defAcctFreightAcct_status_description,
            defAcctFreightAcct.account_status_localization_code               AS defAcctFreightAcct_status_localization_code
         FROM general_ledger_control glCtrl
            JOIN company comp ON glCtrl.company_id = comp.id AND comp.deleted = FALSE
            JOIN fastinfo_prod_import.store_vw profitCenter
                    ON profitCenter.dataset = comp.dataset_code
                       AND profitCenter.number = glCtrl.default_profit_center_sfk
            LEFT JOIN account defAPAcct ON glCtrl.default_account_payable_account_id = defAPAcct.account_id AND defAPAcct.account_deleted = FALSE
            LEFT JOIN account defAPDiscAcct ON glCtrl.default_account_payable_discount_account_id = defAPDiscAcct.account_id AND defAPDiscAcct.account_deleted = FALSE
            LEFT JOIN account defARAcct ON glCtrl.default_account_receivable_account_id = defARAcct.account_id AND defARAcct.account_deleted = FALSE
            LEFT JOIN account defARDiscAcct ON glCtrl.default_account_receivable_discount_account_id = defARDiscAcct.account_id AND defARDiscAcct.account_deleted = FALSE
            LEFT JOIN account defAcctMiscInvAcct ON glCtrl.default_account_misc_inventory_account_id = defAcctMiscInvAcct.account_id AND defAcctMiscInvAcct.account_deleted = FALSE
            LEFT JOIN account defAcctSerializedInvAcct ON glCtrl.default_account_serialized_inventory_account_id = defAcctSerializedInvAcct.account_id AND defAcctSerializedInvAcct.account_deleted = FALSE
            LEFT JOIN account defAcctUnbilledInvAcct ON glCtrl.default_account_unbilled_inventory_account_id = defAcctUnbilledInvAcct.account_id AND defAcctUnbilledInvAcct.account_deleted = FALSE
            LEFT JOIN account defAcctFreightAcct ON glCtrl.default_account_freight_account_id = defAcctFreightAcct.account_id AND defAcctFreightAcct.account_deleted = FALSE
      """
   }

   @ReadOnly
   fun exists(company: CompanyEntity): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS (SELECT company_id FROM general_ledger_control WHERE company_id = :company_id)",
         mapOf("company_id" to company.id),
         Boolean::class.java
      )

      logger.trace("Checking if GeneralLedgerControl: {} exists resulted in {}", company, exists)

      return exists
   }

   @ReadOnly
   fun findOne(company: CompanyEntity): GeneralLedgerControlEntity? {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE glCtrl.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         val defaultProfitCenter = storeRepository.mapRow(rs, company, "profitCenter_")
         val defaultAccountPayableAccount = accountRepository.mapRowOrNull(rs, company, "defAPAcct_")
         val defaultAccountPayableDiscountAccount = accountRepository.mapRowOrNull(rs, company, "defAPDiscAcct_")
         val defaultAccountReceivableAccount = accountRepository.mapRowOrNull(rs, company, "defARAcct_")
         val defaultAccountReceivableDiscountAccount = accountRepository.mapRowOrNull(rs, company, "defARDiscAcct_")
         val defaultAccountMiscInventoryAccount = accountRepository.mapRowOrNull(rs, company, "defAcctMiscInvAcct_")
         val defaultAccountSerializedInventoryAccount =
            accountRepository.mapRowOrNull(rs, company, "defAcctSerializedInvAcct_")
         val defaultAccountUnbilledInventoryAccount =
            accountRepository.mapRowOrNull(rs, company, "defAcctUnbilledInvAcct_")
         val defaultAccountFreightAccount = accountRepository.mapRowOrNull(rs, company, "defAcctFreightAcct_")

         mapRow(
            rs,
            company,
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

      logger.trace("Searching for GeneralLedgerControl: {} resulted in {}", company, found)

      return found
   }

   @Transactional
   fun insert(entity: GeneralLedgerControlEntity, company: CompanyEntity): GeneralLedgerControlEntity {
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
            "company_id" to company.id,
            "default_profit_center_sfk" to entity.defaultProfitCenter.myNumber(),
            "default_account_payable_account_id" to entity.defaultAccountPayableAccount?.id,
            "default_account_payable_discount_account_id" to entity.defaultAccountPayableDiscountAccount?.id,
            "default_account_receivable_account_id" to entity.defaultAccountReceivableAccount?.id,
            "default_account_receivable_discount_account_id" to entity.defaultAccountReceivableDiscountAccount?.id,
            "default_account_misc_inventory_account_id" to entity.defaultAccountMiscInventoryAccount?.id,
            "default_account_serialized_inventory_account_id" to entity.defaultAccountSerializedInventoryAccount?.id,
            "default_account_unbilled_inventory_account_id" to entity.defaultAccountUnbilledInventoryAccount?.id,
            "default_account_freight_account_id" to entity.defaultAccountFreightAccount?.id
         )
      ) { rs, _ ->
         mapRow(
            rs,
            company,
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
   }

   @Transactional
   fun update(entity: GeneralLedgerControlEntity, company: CompanyEntity): GeneralLedgerControlEntity {
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
            "company_id" to company.id,
            "default_profit_center_sfk" to entity.defaultProfitCenter.myNumber(),
            "default_account_payable_account_id" to entity.defaultAccountPayableAccount?.id,
            "default_account_payable_discount_account_id" to entity.defaultAccountPayableDiscountAccount?.id,
            "default_account_receivable_account_id" to entity.defaultAccountReceivableAccount?.id,
            "default_account_receivable_discount_account_id" to entity.defaultAccountReceivableDiscountAccount?.id,
            "default_account_misc_inventory_account_id" to entity.defaultAccountMiscInventoryAccount?.id,
            "default_account_serialized_inventory_account_id" to entity.defaultAccountSerializedInventoryAccount?.id,
            "default_account_unbilled_inventory_account_id" to entity.defaultAccountUnbilledInventoryAccount?.id,
            "default_account_freight_account_id" to entity.defaultAccountFreightAccount?.id
         )
      ) { rs, _ ->
         mapRow(
            rs,
            company,
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
   }

   private fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
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
         id = rs.getUuid("${columnPrefix}id"),
         company = company,
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
