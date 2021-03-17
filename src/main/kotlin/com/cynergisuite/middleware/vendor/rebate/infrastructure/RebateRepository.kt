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
            vendor.v_comp_id                                    AS vendor_comp_id,
            vendor.v_comp_uu_row_id                             AS vendor_comp_uu_row_id,
            vendor.v_comp_time_created                          AS vendor_comp_time_created,
            vendor.v_comp_time_updated                          AS vendor_comp_time_updated,
            vendor.v_comp_name                                  AS vendor_comp_name,
            vendor.v_comp_doing_business_as                     AS vendor_comp_doing_business_as,
            vendor.v_comp_client_code                           AS vendor_comp_client_code,
            vendor.v_comp_client_id                             AS vendor_comp_client_id,
            vendor.v_comp_dataset_code                          AS vendor_comp_dataset_code,
            vendor.v_comp_federal_id_number                     AS vendor_comp_federal_id_number,
            vendor.v_comp_address_id                            AS vendor_comp_address_id,
            vendor.v_comp_address_name                          AS vendor_comp_address_name,
            vendor.v_comp_address_address1                      AS vendor_comp_address_address1,
            vendor.v_comp_address_address2                      AS vendor_comp_address_address2,
            vendor.v_comp_address_city                          AS vendor_comp_address_city,
            vendor.v_comp_address_state                         AS vendor_comp_address_state,
            vendor.v_comp_address_postal_code                   AS vendor_comp_address_postal_code,
            vendor.v_comp_address_latitude                      AS vendor_comp_address_latitude,
            vendor.v_comp_address_longitude                     AS vendor_comp_address_longitude,
            vendor.v_comp_address_country                       AS vendor_comp_address_country,
            vendor.v_comp_address_county                        AS vendor_comp_address_county,
            vendor.v_comp_address_phone                         AS vendor_comp_address_phone,
            vendor.v_comp_address_fax                           AS vendor_comp_address_fax,
            vendor.v_onboard_id                                 AS vendor_onboard_id,
            vendor.v_onboard_value                              AS vendor_onboard_value,
            vendor.v_onboard_description                        AS vendor_onboard_description,
            vendor.v_onboard_localization_code                  AS vendor_onboard_localization_code,
            vendor.v_method_id                                  AS vendor_method_id,
            vendor.v_method_value                               AS vendor_method_value,
            vendor.v_method_description                         AS vendor_method_description,
            vendor.v_method_localization_code                   AS vendor_method_localization_code,
            vendor.v_address_id                                 AS vendor_address_id,
            vendor.v_address_uu_row_id                          AS vendor_address_uu_row_id,
            vendor.v_address_time_created                       AS vendor_address_time_created,
            vendor.v_address_time_updated                       AS vendor_address_time_updated,
            vendor.v_address_number                             AS vendor_address_number,
            vendor.v_address_name                               AS vendor_address_name,
            vendor.v_address_address1                           AS vendor_address_address1,
            vendor.v_address_address2                           AS vendor_address_address2,
            vendor.v_address_city                               AS vendor_address_city,
            vendor.v_address_state                              AS vendor_address_state,
            vendor.v_address_postal_code                        AS vendor_address_postal_code,
            vendor.v_address_latitude                           AS vendor_address_latitude,
            vendor.v_address_longitude                          AS vendor_address_longitude,
            vendor.v_address_country                            AS vendor_address_country,
            vendor.v_address_county                             AS vendor_address_county,
            vendor.v_address_phone                              AS vendor_address_phone,
            vendor.v_address_fax                                AS vendor_address_fax,
            vendor.v_vpt_id                                     AS vendor_vpt_id,
            vendor.v_vpt_uu_row_id                              AS vendor_vpt_uu_row_id,
            vendor.v_vpt_time_created                           AS vendor_vpt_time_created,
            vendor.v_vpt_time_updated                           AS vendor_vpt_time_updated,
            vendor.v_vpt_company_id                             AS vendor_vpt_company_id,
            vendor.v_vpt_description                            AS vendor_vpt_description,
            vendor.v_vpt_number                                 AS vendor_vpt_number,
            vendor.v_vpt_number_of_payments                     AS vendor_vpt_number_of_payments,
            vendor.v_vpt_discount_month                         AS vendor_vpt_discount_month,
            vendor.v_vpt_discount_days                          AS vendor_vpt_discount_days,
            vendor.v_vpt_discount_percent                       AS vendor_vpt_discount_percent,
            vendor.v_shipVia_id                                 AS vendor_shipVia_id,
            vendor.v_shipVia_uu_row_id                          AS vendor_shipVia_uu_row_id,
            vendor.v_shipVia_time_created                       AS vendor_shipVia_time_created,
            vendor.v_shipVia_time_updated                       AS vendor_shipVia_time_updated,
            vendor.v_shipVia_description                        AS vendor_shipVia_description,
            vendor.v_shipVia_number                             AS vendor_shipVia_number,
            vendor.v_vgrp_id                                    AS vendor_vgrp_id,
            vendor.v_vgrp_uu_row_id                             AS vendor_vgrp_uu_row_id,
            vendor.v_vgrp_time_created                          AS vendor_vgrp_time_created,
            vendor.v_vgrp_time_updated                          AS vendor_vgrp_time_updated,
            vendor.v_vgrp_company_id                            AS vendor_vgrp_company_id,
            vendor.v_vgrp_value                                 AS vendor_vgrp_value,
            vendor.v_vgrp_description                           AS vendor_vgrp_description,
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
            glDebitAcct.account_comp_id                         AS glDebitAcct_comp_id,
            glDebitAcct.account_type_id                         AS glDebitAcct_type_id,
            glDebitAcct.account_type_value                      AS glDebitAcct_type_value,
            glDebitAcct.account_type_description                AS glDebitAcct_type_description,
            glDebitAcct.account_type_localization_code          AS glDebitAcct_type_localization_code,
            glDebitAcct.account_balance_type_id                 AS glDebitAcct_balance_type_id,
            glDebitAcct.account_balance_type_value              AS glDebitAcct_balance_type_value,
            glDebitAcct.account_balance_type_description        AS glDebitAcct_balance_type_description,
            glDebitAcct.account_balance_type_localization_code  AS glDebitAcct_balance_type_localization_code,
            glDebitAcct.account_status_id                       AS glDebitAcct_status_id,
            glDebitAcct.account_status_value                    AS glDebitAcct_status_value,
            glDebitAcct.account_status_description              AS glDebitAcct_status_description,
            glDebitAcct.account_status_localization_code        AS glDebitAcct_status_localization_code,
            glCreditAcct.account_id                             AS glCreditAcct_id,
            glCreditAcct.account_number                         AS glCreditAcct_number,
            glCreditAcct.account_name                           AS glCreditAcct_name,
            glCreditAcct.account_form_1099_field                AS glCreditAcct_form_1099_field,
            glCreditAcct.account_corporate_account_indicator    AS glCreditAcct_corporate_account_indicator,
            glCreditAcct.account_comp_id                        AS glCreditAcct_comp_id,
            glCreditAcct.account_type_id                        AS glCreditAcct_type_id,
            glCreditAcct.account_type_value                     AS glCreditAcct_type_value,
            glCreditAcct.account_type_description               AS glCreditAcct_type_description,
            glCreditAcct.account_type_localization_code         AS glCreditAcct_type_localization_code,
            glCreditAcct.account_balance_type_id                AS glCreditAcct_balance_type_id,
            glCreditAcct.account_balance_type_value             AS glCreditAcct_balance_type_value,
            glCreditAcct.account_balance_type_description       AS glCreditAcct_balance_type_description,
            glCreditAcct.account_balance_type_localization_code AS glCreditAcct_balance_type_localization_code,
            glCreditAcct.account_status_id                      AS glCreditAcct_status_id,
            glCreditAcct.account_status_value                   AS glCreditAcct_status_value,
            glCreditAcct.account_status_description             AS glCreditAcct_status_description,
            glCreditAcct.account_status_localization_code       AS glCreditAcct_status_localization_code
         FROM rebate r
            JOIN vendor                            ON r.vendor_id = vendor.v_id
            JOIN account_status_type_domain status ON r.status_type_id = status.id
            JOIN rebate_type_domain rebate         ON r.rebate_type_id = rebate.id
            LEFT JOIN account glDebitAcct          ON r.general_ledger_debit_account_id = glDebitAcct.account_id
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
         generalLedgerDebitAccount = accountRepository.mapRowOrNull(rs, company, "glDebitAcct_"),
         generalLedgerCreditAccount = accountRepository.mapRow(rs, company, "glCreditAcct_")
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
