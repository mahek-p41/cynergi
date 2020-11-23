package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLongOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountStatusType
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountStatusTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.rebate.RebateEntity
import com.cynergisuite.middleware.vendor.rebate.RebateType
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
class RebateRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val accountRepository: AccountRepository,
   private val accountStatusTypeRepository: AccountStatusTypeRepository,
   private val rebateTypeRepository: RebateTypeRepository,
   private val vendorRepository: VendorRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(RebateRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH vendor AS (
            ${vendorRepository.baseSelectQuery()}
         ),
         account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            r.id                                                AS r_id,
            r.uu_row_id                                         AS r_uu_row_id,
            r.time_created                                      AS r_time_created,
            r.time_updated                                      AS r_time_updated,
            r.company_id                                        AS r_comp_id,
            r.description                                       AS r_description,
            r.percent                                           AS r_percent,
            r.amount_per_unit                                   AS r_amount_per_unit,
            r.accrual_indicator                                 AS r_accrual_indicator,
            vendor.v_id                                         AS vendor_id,
            vendor.v_uu_row_id                                  AS vendor_uu_row_id,
            vendor.v_time_created                               AS vendor_time_created,
            vendor.v_time_updated                               AS vendor_time_updated,
            vendor.v_company_id                                 AS vendor_company_id,
            vendor.v_number                                     AS vendor_number,
            vendor.v_name                                       AS vendor_name,
            vendor.v_address_id                                 AS vendor_address_id,
            vendor.v_account_number                             AS vendor_account_number,
            vendor.v_pay_to_id                                  AS vendor_pay_to_id,
            vendor.v_freight_on_board_type_id                   AS vendor_freight_on_board_type_id,
            vendor.v_payment_terms_id                           AS vendor_payment_terms_id,
            vendor.v_normal_days                                AS vendor_normal_days,
            vendor.v_return_policy                              AS vendor_return_policy,
            vendor.v_ship_via_id                                AS vendor_ship_via_id,
            vendor.v_group_id                                   AS vendor_group_id,
            vendor.v_minimum_quantity                           AS vendor_minimum_quantity,
            vendor.v_minimum_amount                             AS vendor_minimum_amount,
            vendor.v_free_ship_quantity                         AS vendor_free_ship_quantity,
            vendor.v_free_ship_amount                           AS vendor_free_ship_amount,
            vendor.v_vendor_1099                                AS vendor_vendor_1099,
            vendor.v_federal_id_number                          AS vendor_federal_id_number,
            vendor.v_sales_representative_name                  AS vendor_sales_representative_name,
            vendor.v_sales_representative_fax                   AS vendor_sales_representative_fax,
            vendor.v_separate_check                             AS vendor_separate_check,
            vendor.v_bump_percent                               AS vendor_bump_percent,
            vendor.v_freight_calc_method_type_id                AS vendor_freight_calc_method_type_id,
            vendor.v_freight_percent                            AS vendor_freight_percent,
            vendor.v_freight_amount                             AS vendor_freight_amount,
            vendor.v_charge_inventory_tax_1                     AS vendor_charge_inventory_tax_1,
            vendor.v_charge_inventory_tax_2                     AS vendor_charge_inventory_tax_2,
            vendor.v_charge_inventory_tax_3                     AS vendor_charge_inventory_tax_3,
            vendor.v_charge_inventory_tax_4                     AS vendor_charge_inventory_tax_4,
            vendor.v_federal_id_number_verification             AS vendor_federal_id_number_verification,
            vendor.v_email_address                              AS vendor_email_address,
            vendor.v_purchase_order_submit_email_address        AS vendor_purchase_order_submit_email_address,
            vendor.v_allow_drop_ship_to_customer                AS vendor_allow_drop_ship_to_customer,
            vendor.v_auto_submit_purchase_order                 AS vendor_auto_submit_purchase_order,
            vendor.v_note                                       AS vendor_note,
            vendor.v_phone_number                               AS vendor_phone_number,
            vendor.comp_id                                      AS comp_id,
            vendor.comp_uu_row_id                               AS comp_uu_row_id,
            vendor.comp_time_created                            AS comp_time_created,
            vendor.comp_time_updated                            AS comp_time_updated,
            vendor.comp_name                                    AS comp_name,
            vendor.comp_doing_business_as                       AS comp_doing_business_as,
            vendor.comp_client_code                             AS comp_client_code,
            vendor.comp_client_id                               AS comp_client_id,
            vendor.comp_dataset_code                            AS comp_dataset_code,
            vendor.comp_federal_id_number                       AS comp_federal_id_number,
            vendor.comp_address_id                              AS comp_address_id,
            vendor.comp_address_name                            AS comp_address_name,
            vendor.comp_address_address1                        AS comp_address_address1,
            vendor.comp_address_address2                        AS comp_address_address2,
            vendor.comp_address_city                            AS comp_address_city,
            vendor.comp_address_state                           AS comp_address_state,
            vendor.comp_address_postal_code                     AS comp_address_postal_code,
            vendor.comp_address_latitude                        AS comp_address_latitude,
            vendor.comp_address_longitude                       AS comp_address_longitude,
            vendor.comp_address_country                         AS comp_address_country,
            vendor.comp_address_county                          AS comp_address_county,
            vendor.comp_address_phone                           AS comp_address_phone,
            vendor.comp_address_fax                             AS comp_address_fax,
            vendor.onboard_id                                   AS onboard_id,
            vendor.onboard_value                                AS onboard_value,
            vendor.onboard_description                          AS onboard_description,
            vendor.onboard_localization_code                    AS onboard_localization_code,
            vendor.method_id                                    AS method_id,
            vendor.method_value                                 AS method_value,
            vendor.method_description                           AS method_description,
            vendor.method_localization_code                     AS method_localization_code,
            vendor.address_id                                   AS address_id,
            vendor.address_uu_row_id                            AS address_uu_row_id,
            vendor.address_time_created                         AS address_time_created,
            vendor.address_time_updated                         AS address_time_updated,
            vendor.address_number                               AS address_number,
            vendor.address_name                                 AS address_name,
            vendor.address_address1                             AS address_address1,
            vendor.address_address2                             AS address_address2,
            vendor.address_city                                 AS address_city,
            vendor.address_state                                AS address_state,
            vendor.address_postal_code                          AS address_postal_code,
            vendor.address_latitude                             AS address_latitude,
            vendor.address_longitude                            AS address_longitude,
            vendor.address_country                              AS address_country,
            vendor.address_county                               AS address_county,
            vendor.address_phone                                AS address_phone,
            vendor.address_fax                                  AS address_fax,
            vendor.vpt_id                                       AS vpt_id,
            vendor.vpt_uu_row_id                                AS vpt_uu_row_id,
            vendor.vpt_time_created                             AS vpt_time_created,
            vendor.vpt_time_updated                             AS vpt_time_updated,
            vendor.vpt_company_id                               AS vpt_company_id,
            vendor.vpt_description                              AS vpt_description,
            vendor.vpt_number                                   AS vpt_number,
            vendor.vpt_number_of_payments                       AS vpt_number_of_payments,
            vendor.vpt_discount_month                           AS vpt_discount_month,
            vendor.vpt_discount_days                            AS vpt_discount_days,
            vendor.vpt_discount_percent                         AS vpt_discount_percent,
            vendor.shipVia_id                                   AS shipVia_id,
            vendor.shipVia_uu_row_id                            AS shipVia_uu_row_id,
            vendor.shipVia_time_created                         AS shipVia_time_created,
            vendor.shipVia_time_updated                         AS shipVia_time_updated,
            vendor.shipVia_description                          AS shipVia_description,
            vendor.shipVia_number                               AS shipVia_number,
            vendor.vgrp_id                                      AS vgrp_id,
            vendor.vgrp_uu_row_id                               AS vgrp_uu_row_id,
            vendor.vgrp_time_created                            AS vgrp_time_created,
            vendor.vgrp_time_updated                            AS vgrp_time_updated,
            vendor.vgrp_company_id                              AS vgrp_company_id,
            vendor.vgrp_value                                   AS vgrp_value,
            vendor.vgrp_description                             AS vgrp_description,
            count(*) OVER()                                     AS total_elements,
            status.id                                           AS status_id,
            status.value                                        AS status_value,
            status.description                                  AS status_description,
            status.localization_code                            AS status_localization_code,
            rebate.id                                           AS rebate_id,
            rebate.value                                        AS rebate_value,
            rebate.description                                  AS rebate_description,
            rebate.localization_code                            AS rebate_localization_code,
            glDebitAcct.account_id                              AS glDebitAcct_id,
            glDebitAcct.account_number                          AS glDebitAcct_number,
            glDebitAcct.account_name                            AS glDebitAcct_name,
            glDebitAcct.account_form_1099_field                 AS glDebitAcct_form_1099_field,
            glDebitAcct.account_corporate_account_indicator     AS glDebitAcct_corporate_account_indicator,
            glDebitAcct.comp_id                                 AS glDebitAcct_comp_id,
            glDebitAcct.comp_uu_row_id                          AS glDebitAcct_comp_uu_row_id,
            glDebitAcct.comp_time_created                       AS glDebitAcct_comp_time_created,
            glDebitAcct.comp_time_updated                       AS glDebitAcct_comp_time_updated,
            glDebitAcct.comp_name                               AS glDebitAcct_comp_name,
            glDebitAcct.comp_doing_business_as                  AS glDebitAcct_comp_doing_business_as,
            glDebitAcct.comp_client_code                        AS glDebitAcct_comp_client_code,
            glDebitAcct.comp_client_id                          AS glDebitAcct_comp_client_id,
            glDebitAcct.comp_dataset_code                       AS glDebitAcct_comp_dataset_code,
            glDebitAcct.comp_federal_id_number                  AS glDebitAcct_comp_federal_id_number,
            glDebitAcct.type_id                                 AS glDebitAcct_type_id,
            glDebitAcct.type_value                              AS glDebitAcct_type_value,
            glDebitAcct.type_description                        AS glDebitAcct_type_description,
            glDebitAcct.type_localization_code                  AS glDebitAcct_type_localization_code,
            glDebitAcct.balance_type_id                         AS glDebitAcct_balance_type_id,
            glDebitAcct.balance_type_value                      AS glDebitAcct_balance_type_value,
            glDebitAcct.balance_type_description                AS glDebitAcct_balance_type_description,
            glDebitAcct.balance_type_localization_code          AS glDebitAcct_balance_type_localization_code,
            glDebitAcct.status_id                               AS glDebitAcct_status_id,
            glDebitAcct.status_value                            AS glDebitAcct_status_value,
            glDebitAcct.status_description                      AS glDebitAcct_status_description,
            glDebitAcct.status_localization_code                AS glDebitAcct_status_localization_code,
            glCreditAcct.account_id                             AS glCreditAcct_id,
            glCreditAcct.account_number                         AS glCreditAcct_number,
            glCreditAcct.account_name                           AS glCreditAcct_name,
            glCreditAcct.account_form_1099_field                AS glCreditAcct_form_1099_field,
            glCreditAcct.account_corporate_account_indicator    AS glCreditAcct_corporate_account_indicator,
            glCreditAcct.comp_id                                AS glCreditAcct_comp_id,
            glCreditAcct.comp_uu_row_id                         AS glCreditAcct_comp_uu_row_id,
            glCreditAcct.comp_time_created                      AS glCreditAcct_comp_time_created,
            glCreditAcct.comp_time_updated                      AS glCreditAcct_comp_time_updated,
            glCreditAcct.comp_name                              AS glCreditAcct_comp_name,
            glCreditAcct.comp_doing_business_as                 AS glCreditAcct_comp_doing_business_as,
            glCreditAcct.comp_client_code                       AS glCreditAcct_comp_client_code,
            glCreditAcct.comp_client_id                         AS glCreditAcct_comp_client_id,
            glCreditAcct.comp_dataset_code                      AS glCreditAcct_comp_dataset_code,
            glCreditAcct.comp_federal_id_number                 AS glCreditAcct_comp_federal_id_number,
            glCreditAcct.type_id                                AS glCreditAcct_type_id,
            glCreditAcct.type_value                             AS glCreditAcct_type_value,
            glCreditAcct.type_description                       AS glCreditAcct_type_description,
            glCreditAcct.type_localization_code                 AS glCreditAcct_type_localization_code,
            glCreditAcct.balance_type_id                        AS glCreditAcct_balance_type_id,
            glCreditAcct.balance_type_value                     AS glCreditAcct_balance_type_value,
            glCreditAcct.balance_type_description               AS glCreditAcct_balance_type_description,
            glCreditAcct.balance_type_localization_code         AS glCreditAcct_balance_type_localization_code,
            glCreditAcct.status_id                              AS glCreditAcct_status_id,
            glCreditAcct.status_value                           AS glCreditAcct_status_value,
            glCreditAcct.status_description                     AS glCreditAcct_status_description,
            glCreditAcct.status_localization_code               AS glCreditAcct_status_localization_code
         FROM rebate r
            JOIN vendor                            ON r.vendor_id = vendor.v_id
            JOIN account_status_type_domain status ON r.status_type_id = status.id
            JOIN rebate_type_domain rebate         ON r.rebate_type_id = rebate.id
            JOIN account glDebitAcct               ON r.general_ledger_debit_account_id = glDebitAcct.account_id
            JOIN account glCreditAcct              ON r.general_ledger_credit_account_id = glCreditAcct.account_id
      """
   }

   fun findOne(id: Long, company: Company): RebateEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()}\nWHERE r.id = :id AND r.company_id = :comp_id"

      logger.debug("Searching for Rebate using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val rebate = mapRow(rs, company, "r_")

         rebate
      }

      logger.trace("Searching for Rebate: {} resulted in {}", id, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<RebateEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE r.company_id = :comp_id
            ORDER BY r_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId(),
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "r_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: RebateEntity, company: Company): RebateEntity {
      logger.debug("Inserting rebate {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO rebate(
            company_id,
            vendor_id,
            status_type_id,
            description,
            rebate_type_id,
            percent,
            amount_per_unit,
            accrual_indicator,
            general_ledger_debit_account_id,
            general_ledger_credit_account_id
         )
         VALUES (
            :company_id,
            :vendor_id,
            :status_type_id,
            :description,
            :rebate_type_id,
            :percent,
            :amount_per_unit,
            :accrual_indicator,
            :general_ledger_debit_account_id,
            :general_ledger_credit_account_id
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "vendor_id" to entity.vendor.myId(),
            "status_type_id" to entity.status.id,
            "description" to entity.description,
            "rebate_type_id" to entity.rebate.id,
            "percent" to entity.percent,
            "amount_per_unit" to entity.amountPerUnit,
            "accrual_indicator" to entity.accrualIndicator,
            "general_ledger_debit_account_id" to entity.generalLedgerDebitAccount?.id,
            "general_ledger_credit_account_id" to entity.generalLedgerCreditAccount.id
         ),
         RowMapper { rs, _ -> mapRowUpsert(rs, entity.vendor, entity.status, entity.rebate, entity.generalLedgerDebitAccount, entity.generalLedgerCreditAccount) }
      )
   }

   @Transactional
   fun update(entity: RebateEntity, company: Company): RebateEntity {
      logger.debug("Updating rebate {}", entity)

      return jdbc.updateReturning(
         """
            UPDATE rebate
            SET
               company_id = :company_id,
               vendor_id = :vendor_id,
               status_type_id = :status_type_id,
               description = :description,
               rebate_type_id = :rebate_type_id,
               percent = :percent,
               amount_per_unit = :amount_per_unit,
               accrual_indicator = :accrual_indicator,
               general_ledger_debit_account_id = :general_ledger_debit_account_id,
               general_ledger_credit_account_id = :general_ledger_credit_account_id
            WHERE id = :id
            RETURNING
               *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "vendor_id" to entity.vendor.myId(),
            "status_type_id" to entity.status.id,
            "description" to entity.description,
            "rebate_type_id" to entity.rebate.id,
            "percent" to entity.percent,
            "amount_per_unit" to entity.amountPerUnit,
            "accrual_indicator" to entity.accrualIndicator,
            "general_ledger_debit_account_id" to entity.generalLedgerDebitAccount?.id,
            "general_ledger_credit_account_id" to entity.generalLedgerCreditAccount.id
         ),
         RowMapper { rs, _ -> mapRowUpsert(rs, entity.vendor, entity.status, entity.rebate, entity.generalLedgerDebitAccount, entity.generalLedgerCreditAccount) }
      )
   }

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): RebateEntity {
      return RebateEntity(
         id = rs.getLong("${columnPrefix}id"),
         vendor = SimpleIdentifiableEntity(rs.getLongOrNull("vendor_id")!!),
         status = accountStatusTypeRepository.mapRow(rs, "status_"),
         description = rs.getString("${columnPrefix}description"),
         rebate = rebateTypeRepository.mapRow(rs, "rebate_"),
         percent = rs.getBigDecimal("${columnPrefix}percent"),
         amountPerUnit = rs.getBigDecimal("${columnPrefix}amount_per_unit"),
         accrualIndicator = rs.getBoolean("${columnPrefix}accrual_indicator"),
         generalLedgerDebitAccount = accountRepository.mapRowOrNull(rs, company, "glDebitAcct_", "glDebitAcct_"),
         generalLedgerCreditAccount = accountRepository.mapRow(rs, company, "glCreditAcct_", "glCreditAcct_")
      )
   }

   private fun mapRowUpsert(rs: ResultSet, vendor: Identifiable, status: AccountStatusType, rebate: RebateType, generalLedgerDebitAccount: AccountEntity?, generalLedgerCreditAccount: AccountEntity, columnPrefix: String = EMPTY): RebateEntity {
      return RebateEntity(
         id = rs.getLong("${columnPrefix}id"),
         vendor = vendor,
         status = status,
         description = rs.getString("${columnPrefix}description"),
         rebate = rebate,
         percent = rs.getBigDecimal("${columnPrefix}percent"),
         amountPerUnit = rs.getBigDecimal("${columnPrefix}amount_per_unit"),
         accrualIndicator = rs.getBoolean("${columnPrefix}accrual_indicator"),
         generalLedgerDebitAccount = generalLedgerDebitAccount,
         generalLedgerCreditAccount = generalLedgerCreditAccount
      )
   }
}
