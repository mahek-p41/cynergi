package com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableRecurringInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.recurring.AccountPayableRecurringInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.recurring.ExpenseMonthCreationType
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
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
class AccountPayableRecurringInvoiceRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val vendorRepository: VendorRepository,
   private val statusTypeRepository: AccountPayableRecurringInvoiceStatusTypeRepository,
   private val expenseMonthCreationTypeRepository: ExpenseMonthCreationTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableRecurringInvoiceRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH vendor AS (
            ${vendorRepository.baseSelectQuery()}
         )
         SELECT
            apRecurringInvoice.id                                       AS apRecurringInvoice_id,
            apRecurringInvoice.company_id                               AS apRecurringInvoice_company_id,
            apRecurringInvoice.invoice                                  AS apRecurringInvoice_invoice,
            apRecurringInvoice.invoice_amount                           AS apRecurringInvoice_invoice_amount,
            apRecurringInvoice.fixed_amount_indicator                   AS apRecurringInvoice_fixed_amount_indicator,
            apRecurringInvoice.employee_number_id_sfk                   AS apRecurringInvoice_employee_number_id_sfk,
            apRecurringInvoice.message                                  AS apRecurringInvoice_message,
            apRecurringInvoice.code_indicator                           AS apRecurringInvoice_code_indicator,
            apRecurringInvoice.type                                     AS apRecurringInvoice_type,
            apRecurringInvoice.last_transfer_to_create_invoice_date     AS apRecurringInvoice_last_transfer_to_create_invoice_date,
            apRecurringInvoice.due_days                                 AS apRecurringInvoice_due_days,
            apRecurringInvoice.automated_indicator                      AS apRecurringInvoice_automated_indicator,
            apRecurringInvoice.separate_check_indicator                 AS apRecurringInvoice_separate_check_indicator,
            apRecurringInvoice.invoice_day                              AS apRecurringInvoice_invoice_day,
            apRecurringInvoice.expense_day                              AS apRecurringInvoice_expense_day,
            apRecurringInvoice.last_created_in_period                   AS apRecurringInvoice_last_created_in_period,
            apRecurringInvoice.next_creation_date                       AS apRecurringInvoice_next_creation_date,
            apRecurringInvoice.next_invoice_date                        AS apRecurringInvoice_next_invoice_date,
            apRecurringInvoice.next_expense_date                        AS apRecurringInvoice_next_expense_date,
            vendor.v_id                                                 AS vendor_id,
            vendor.v_uu_row_id                                          AS vendor_uu_row_id,
            vendor.v_time_created                                       AS vendor_time_created,
            vendor.v_time_updated                                       AS vendor_time_updated,
            vendor.v_company_id                                         AS vendor_company_id,
            vendor.v_number                                             AS vendor_number,
            vendor.v_name                                               AS vendor_name,
            vendor.v_address_id                                         AS vendor_address_id,
            vendor.v_account_number                                     AS vendor_account_number,
            vendor.v_pay_to_id                                          AS vendor_pay_to_id,
            vendor.v_freight_on_board_type_id                           AS vendor_freight_on_board_type_id,
            vendor.v_payment_terms_id                                   AS vendor_payment_terms_id,
            vendor.v_normal_days                                        AS vendor_normal_days,
            vendor.v_return_policy                                      AS vendor_return_policy,
            vendor.v_ship_via_id                                        AS vendor_ship_via_id,
            vendor.v_group_id                                           AS vendor_group_id,
            vendor.v_minimum_quantity                                   AS vendor_minimum_quantity,
            vendor.v_minimum_amount                                     AS vendor_minimum_amount,
            vendor.v_free_ship_quantity                                 AS vendor_free_ship_quantity,
            vendor.v_free_ship_amount                                   AS vendor_free_ship_amount,
            vendor.v_vendor_1099                                        AS vendor_vendor_1099,
            vendor.v_federal_id_number                                  AS vendor_federal_id_number,
            vendor.v_sales_representative_name                          AS vendor_sales_representative_name,
            vendor.v_sales_representative_fax                           AS vendor_sales_representative_fax,
            vendor.v_separate_check                                     AS vendor_separate_check,
            vendor.v_bump_percent                                       AS vendor_bump_percent,
            vendor.v_freight_calc_method_type_id                        AS vendor_freight_calc_method_type_id,
            vendor.v_freight_percent                                    AS vendor_freight_percent,
            vendor.v_freight_amount                                     AS vendor_freight_amount,
            vendor.v_charge_inventory_tax_1                             AS vendor_charge_inventory_tax_1,
            vendor.v_charge_inventory_tax_2                             AS vendor_charge_inventory_tax_2,
            vendor.v_charge_inventory_tax_3                             AS vendor_charge_inventory_tax_3,
            vendor.v_charge_inventory_tax_4                             AS vendor_charge_inventory_tax_4,
            vendor.v_federal_id_number_verification                     AS vendor_federal_id_number_verification,
            vendor.v_email_address                                      AS vendor_email_address,
            vendor.v_purchase_order_submit_email_address                AS vendor_purchase_order_submit_email_address,
            vendor.v_allow_drop_ship_to_customer                        AS vendor_allow_drop_ship_to_customer,
            vendor.v_auto_submit_purchase_order                         AS vendor_auto_submit_purchase_order,
            vendor.v_note                                               AS vendor_note,
            vendor.v_phone_number                                       AS vendor_phone_number,
            vendor.comp_id                                              AS comp_id,
            vendor.comp_uu_row_id                                       AS comp_uu_row_id,
            vendor.comp_time_created                                    AS comp_time_created,
            vendor.comp_time_updated                                    AS comp_time_updated,
            vendor.comp_name                                            AS comp_name,
            vendor.comp_doing_business_as                               AS comp_doing_business_as,
            vendor.comp_client_code                                     AS comp_client_code,
            vendor.comp_client_id                                       AS comp_client_id,
            vendor.comp_dataset_code                                    AS comp_dataset_code,
            vendor.comp_federal_id_number                               AS comp_federal_id_number,
            vendor.comp_address_id                                      AS comp_address_id,
            vendor.comp_address_name                                    AS comp_address_name,
            vendor.comp_address_address1                                AS comp_address_address1,
            vendor.comp_address_address2                                AS comp_address_address2,
            vendor.comp_address_city                                    AS comp_address_city,
            vendor.comp_address_state                                   AS comp_address_state,
            vendor.comp_address_postal_code                             AS comp_address_postal_code,
            vendor.comp_address_latitude                                AS comp_address_latitude,
            vendor.comp_address_longitude                               AS comp_address_longitude,
            vendor.comp_address_country                                 AS comp_address_country,
            vendor.comp_address_county                                  AS comp_address_county,
            vendor.comp_address_phone                                   AS comp_address_phone,
            vendor.comp_address_fax                                     AS comp_address_fax,
            vendor.onboard_id                                           AS onboard_id,
            vendor.onboard_value                                        AS onboard_value,
            vendor.onboard_description                                  AS onboard_description,
            vendor.onboard_localization_code                            AS onboard_localization_code,
            vendor.method_id                                            AS method_id,
            vendor.method_value                                         AS method_value,
            vendor.method_description                                   AS method_description,
            vendor.method_localization_code                             AS method_localization_code,
            vendor.address_id                                           AS address_id,
            vendor.address_uu_row_id                                    AS address_uu_row_id,
            vendor.address_time_created                                 AS address_time_created,
            vendor.address_time_updated                                 AS address_time_updated,
            vendor.address_number                                       AS address_number,
            vendor.address_name                                         AS address_name,
            vendor.address_address1                                     AS address_address1,
            vendor.address_address2                                     AS address_address2,
            vendor.address_city                                         AS address_city,
            vendor.address_state                                        AS address_state,
            vendor.address_postal_code                                  AS address_postal_code,
            vendor.address_latitude                                     AS address_latitude,
            vendor.address_longitude                                    AS address_longitude,
            vendor.address_country                                      AS address_country,
            vendor.address_county                                       AS address_county,
            vendor.address_phone                                        AS address_phone,
            vendor.address_fax                                          AS address_fax,
            vendor.vpt_id                                               AS vpt_id,
            vendor.vpt_uu_row_id                                        AS vpt_uu_row_id,
            vendor.vpt_time_created                                     AS vpt_time_created,
            vendor.vpt_time_updated                                     AS vpt_time_updated,
            vendor.vpt_company_id                                       AS vpt_company_id,
            vendor.vpt_description                                      AS vpt_description,
            vendor.vpt_number                                           AS vpt_number,
            vendor.vpt_number_of_payments                               AS vpt_number_of_payments,
            vendor.vpt_discount_month                                   AS vpt_discount_month,
            vendor.vpt_discount_days                                    AS vpt_discount_days,
            vendor.vpt_discount_percent                                 AS vpt_discount_percent,
            vendor.shipVia_id                                           AS shipVia_id,
            vendor.shipVia_uu_row_id                                    AS shipVia_uu_row_id,
            vendor.shipVia_time_created                                 AS shipVia_time_created,
            vendor.shipVia_time_updated                                 AS shipVia_time_updated,
            vendor.shipVia_description                                  AS shipVia_description,
            vendor.shipVia_number                                       AS shipVia_number,
            vendor.vgrp_id                                              AS vgrp_id,
            vendor.vgrp_uu_row_id                                       AS vgrp_uu_row_id,
            vendor.vgrp_time_created                                    AS vgrp_time_created,
            vendor.vgrp_time_updated                                    AS vgrp_time_updated,
            vendor.vgrp_company_id                                      AS vgrp_company_id,
            vendor.vgrp_value                                           AS vgrp_value,
            vendor.vgrp_description                                     AS vgrp_description,
            payTo.v_id                                                  AS payTo_id,
            payTo.v_uu_row_id                                           AS payTo_uu_row_id,
            payTo.v_time_created                                        AS payTo_time_created,
            payTo.v_time_updated                                        AS payTo_time_updated,
            payTo.v_company_id                                          AS payTo_company_id,
            payTo.v_number                                              AS payTo_number,
            payTo.v_name                                                AS payTo_name,
            payTo.v_address_id                                          AS payTo_address_id,
            payTo.v_account_number                                      AS payTo_account_number,
            payTo.v_pay_to_id                                           AS payTo_pay_to_id,
            payTo.v_freight_on_board_type_id                            AS payTo_freight_on_board_type_id,
            payTo.v_payment_terms_id                                    AS payTo_payment_terms_id,
            payTo.v_normal_days                                         AS payTo_normal_days,
            payTo.v_return_policy                                       AS payTo_return_policy,
            payTo.v_ship_via_id                                         AS payTo_ship_via_id,
            payTo.v_group_id                                            AS payTo_group_id,
            payTo.v_minimum_quantity                                    AS payTo_minimum_quantity,
            payTo.v_minimum_amount                                      AS payTo_minimum_amount,
            payTo.v_free_ship_quantity                                  AS payTo_free_ship_quantity,
            payTo.v_free_ship_amount                                    AS payTo_free_ship_amount,
            payTo.v_vendor_1099                                         AS payTo_vendor_1099,
            payTo.v_federal_id_number                                   AS payTo_federal_id_number,
            payTo.v_sales_representative_name                           AS payTo_sales_representative_name,
            payTo.v_sales_representative_fax                            AS payTo_sales_representative_fax,
            payTo.v_separate_check                                      AS payTo_separate_check,
            payTo.v_bump_percent                                        AS payTo_bump_percent,
            payTo.v_freight_calc_method_type_id                         AS payTo_freight_calc_method_type_id,
            payTo.v_freight_percent                                     AS payTo_freight_percent,
            payTo.v_freight_amount                                      AS payTo_freight_amount,
            payTo.v_charge_inventory_tax_1                              AS payTo_charge_inventory_tax_1,
            payTo.v_charge_inventory_tax_2                              AS payTo_charge_inventory_tax_2,
            payTo.v_charge_inventory_tax_3                              AS payTo_charge_inventory_tax_3,
            payTo.v_charge_inventory_tax_4                              AS payTo_charge_inventory_tax_4,
            payTo.v_federal_id_number_verification                      AS payTo_federal_id_number_verification,
            payTo.v_email_address                                       AS payTo_email_address,
            payTo.v_purchase_order_submit_email_address                 AS payTo_purchase_order_submit_email_address,
            payTo.v_allow_drop_ship_to_customer                         AS payTo_allow_drop_ship_to_customer,
            payTo.v_auto_submit_purchase_order                          AS payTo_auto_submit_purchase_order,
            payTo.v_note                                                AS payTo_note,
            payTo.v_phone_number                                        AS payTo_phone_number,
            payTo.comp_id                                               AS payTo_comp_id,
            payTo.comp_uu_row_id                                        AS payTo_comp_uu_row_id,
            payTo.comp_time_created                                     AS payTo_comp_time_created,
            payTo.comp_time_updated                                     AS payTo_comp_time_updated,
            payTo.comp_name                                             AS payTo_comp_name,
            payTo.comp_doing_business_as                                AS payTo_comp_doing_business_as,
            payTo.comp_client_code                                      AS payTo_comp_client_code,
            payTo.comp_client_id                                        AS payTo_comp_client_id,
            payTo.comp_dataset_code                                     AS payTo_comp_dataset_code,
            payTo.comp_federal_id_number                                AS payTo_comp_federal_id_number,
            payTo.comp_address_id                                       AS payTo_comp_address_id,
            payTo.comp_address_name                                     AS payTo_comp_address_name,
            payTo.comp_address_address1                                 AS payTo_comp_address_address1,
            payTo.comp_address_address2                                 AS payTo_comp_address_address2,
            payTo.comp_address_city                                     AS payTo_comp_address_city,
            payTo.comp_address_state                                    AS payTo_comp_address_state,
            payTo.comp_address_postal_code                              AS payTo_comp_address_postal_code,
            payTo.comp_address_latitude                                 AS payTo_comp_address_latitude,
            payTo.comp_address_longitude                                AS payTo_comp_address_longitude,
            payTo.comp_address_country                                  AS payTo_comp_address_country,
            payTo.comp_address_county                                   AS payTo_comp_address_county,
            payTo.comp_address_phone                                    AS payTo_comp_address_phone,
            payTo.comp_address_fax                                      AS payTo_comp_address_fax,
            payTo.onboard_id                                            AS payTo_onboard_id,
            payTo.onboard_value                                         AS payTo_onboard_value,
            payTo.onboard_description                                   AS payTo_onboard_description,
            payTo.onboard_localization_code                             AS payTo_onboard_localization_code,
            payTo.method_id                                             AS payTo_method_id,
            payTo.method_value                                          AS payTo_method_value,
            payTo.method_description                                    AS payTo_method_description,
            payTo.method_localization_code                              AS payTo_method_localization_code,
            payTo.address_id                                            AS payTo_address_id,
            payTo.address_uu_row_id                                     AS payTo_address_uu_row_id,
            payTo.address_time_created                                  AS payTo_address_time_created,
            payTo.address_time_updated                                  AS payTo_address_time_updated,
            payTo.address_number                                        AS payTo_address_number,
            payTo.address_name                                          AS payTo_address_name,
            payTo.address_address1                                      AS payTo_address_address1,
            payTo.address_address2                                      AS payTo_address_address2,
            payTo.address_city                                          AS payTo_address_city,
            payTo.address_state                                         AS payTo_address_state,
            payTo.address_postal_code                                   AS payTo_address_postal_code,
            payTo.address_latitude                                      AS payTo_address_latitude,
            payTo.address_longitude                                     AS payTo_address_longitude,
            payTo.address_country                                       AS payTo_address_country,
            payTo.address_county                                        AS payTo_address_county,
            payTo.address_phone                                         AS payTo_address_phone,
            payTo.address_fax                                           AS payTo_address_fax,
            payTo.vpt_id                                                AS payTo_vpt_id,
            payTo.vpt_uu_row_id                                         AS payTo_vpt_uu_row_id,
            payTo.vpt_time_created                                      AS payTo_vpt_time_created,
            payTo.vpt_time_updated                                      AS payTo_vpt_time_updated,
            payTo.vpt_company_id                                        AS payTo_vpt_company_id,
            payTo.vpt_description                                       AS payTo_vpt_description,
            payTo.vpt_number                                            AS payTo_vpt_number,
            payTo.vpt_number_of_payments                                AS payTo_vpt_number_of_payments,
            payTo.vpt_discount_month                                    AS payTo_vpt_discount_month,
            payTo.vpt_discount_days                                     AS payTo_vpt_discount_days,
            payTo.vpt_discount_percent                                  AS payTo_vpt_discount_percent,
            payTo.shipVia_id                                            AS payTo_shipVia_id,
            payTo.shipVia_uu_row_id                                     AS payTo_shipVia_uu_row_id,
            payTo.shipVia_time_created                                  AS payTo_shipVia_time_created,
            payTo.shipVia_time_updated                                  AS payTo_shipVia_time_updated,
            payTo.shipVia_description                                   AS payTo_shipVia_description,
            payTo.shipVia_number                                        AS payTo_shipVia_number,
            payTo.vgrp_id                                               AS payTo_vgrp_id,
            payTo.vgrp_uu_row_id                                        AS payTo_vgrp_uu_row_id,
            payTo.vgrp_time_created                                     AS payTo_vgrp_time_created,
            payTo.vgrp_time_updated                                     AS payTo_vgrp_time_updated,
            payTo.vgrp_company_id                                       AS payTo_vgrp_company_id,
            payTo.vgrp_value                                            AS payTo_vgrp_value,
            payTo.vgrp_description                                      AS payTo_vgrp_description,
            status.id                                                   AS status_id,
            status.value                                                AS status_value,
            status.description                                          AS status_description,
            status.localization_code                                    AS status_localization_code,
            creation_type.id                                            AS creation_type_id,
            creation_type.value                                         AS creation_type_value,
            creation_type.description                                   AS creation_type_description,
            creation_type.localization_code                             AS creation_type_localization_code,
            count(*) OVER() AS total_elements
         FROM account_payable_recurring_invoice apRecurringInvoice
            JOIN vendor ON apRecurringInvoice.vendor_id = vendor.v_id
            JOIN vendor payTo ON apRecurringInvoice.pay_to_id = payTo.v_id
            JOIN account_payable_recurring_invoice_status_type_domain status ON apRecurringInvoice.status_id = status.id
            JOIN expense_month_creation_type_domain creation_type ON apRecurringInvoice.expense_month_creation_indicator_id = creation_type.id
            LEFT JOIN schedule ON apRecurringInvoice.schedule_id = schedule.id
      """
   }

   fun findOne(id: Long, company: Company): AccountPayableRecurringInvoiceEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE apRecurringInvoice.id = :id AND apRecurringInvoice.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            val vendor = vendorRepository.mapRow(rs, company, "vendor_")
            val payTo = vendorRepository.mapRow(rs, company, "payTo_")
            val status = statusTypeRepository.mapRow(rs, "status_")
            val expenseMonthCreationType = expenseMonthCreationTypeRepository.mapRow(rs, "creation_type_")

            mapRow(
               rs,
               vendor,
               payTo,
               status,
               expenseMonthCreationType,
               null,
               "apRecurringInvoice_"
            )
         }
      )

      logger.trace("Searching for AccountPayableRecurringInvoice: {} resulted in {}", company, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<AccountPayableRecurringInvoiceEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE apRecurringInvoice.company_id = :comp_id
            ORDER BY apRecurringInvoice_${page.snakeSortBy()} ${page.sortDirection()}
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
            elements.add(mapRow(rs, company, "apRecurringInvoice_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: AccountPayableRecurringInvoiceEntity, company: Company): AccountPayableRecurringInvoiceEntity {
      logger.debug("Inserting account_payable_recurring_invoice {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_recurring_invoice (
            company_id,
            vendor_id,
            invoice,
            invoice_amount,
            fixed_amount_indicator,
            employee_number_id_sfk ,
            message,
            code_indicator,
            type,
            pay_to_id,
            last_transfer_to_create_invoice_date,
            status_id,
            due_days ,
            automated_indicator ,
            separate_check_indicator,
            expense_month_creation_indicator_id,
            invoice_day,
            expense_day,
            schedule_id,
            last_created_in_period,
            next_creation_date,
            next_invoice_date,
            next_expense_date
         )
         VALUES (
            :company_id,
            :vendor_id,
            :invoice,
            :invoice_amount,
            :fixed_amount_indicator,
            :employee_number_id_sfk,
            :message,
            :code_indicator,
            :type,
            :pay_to_id,
            :last_transfer_to_create_invoice_date,
            :status_id,
            :due_days,
            :automated_indicator,
            :separate_check_indicator,
            :expense_month_creation_indicator_id,
            :invoice_day,
            :expense_day,
            :schedule_id,
            :last_created_in_period,
            :next_creation_date,
            :next_invoice_date,
            :next_expense_date
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "vendor_id" to entity.vendor.id,
            "invoice" to entity.invoice,
            "invoice_amount" to entity.invoiceAmount,
            "fixed_amount_indicator" to entity.fixedAmountIndicator,
            "employee_number_id_sfk" to entity.employeeNumberId,
            "message" to entity.message,
            "code_indicator" to entity.codeIndicator,
            "type" to entity.type,
            "pay_to_id" to entity.payTo.id,
            "last_transfer_to_create_invoice_date" to entity.lastTransferToCreateInvoiceDate,
            "status_id" to entity.status.id,
            "due_days" to entity.dueDays,
            "automated_indicator" to entity.automatedIndicator,
            "separate_check_indicator" to entity.separateCheckIndicator,
            "expense_month_creation_indicator_id" to entity.expenseMonthCreationIndicator.id,
            "invoice_day" to entity.invoiceDay,
            "expense_day" to entity.expenseDay,
            "schedule_id" to entity.schedule?.myId(),
            "last_created_in_period" to entity.lastCreatedInPeriod,
            "next_creation_date" to entity.nextCreationDate,
            "next_invoice_date" to entity.nextInvoiceDate,
            "next_expense_date" to entity.nextExpenseDate
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.vendor,
               entity.payTo,
               entity.status,
               entity.expenseMonthCreationIndicator,
               entity.schedule
            )
         }
      )
   }

   @Transactional
   fun update(entity: AccountPayableRecurringInvoiceEntity, company: Company): AccountPayableRecurringInvoiceEntity {
      logger.debug("Updating account_payable_recurring_invoice {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE account_payable_recurring_invoice
         SET
            company_id = :company_id,
            vendor_id = :vendor_id,
            invoice = :invoice,
            invoice_amount = :invoice_amount,
            fixed_amount_indicator = :fixed_amount_indicator,
            employee_number_id_sfk  = :employee_number_id_sfk,
            message = :message,
            code_indicator = :code_indicator,
            type = :type,
            pay_to_id = :pay_to_id,
            last_transfer_to_create_invoice_date = :last_transfer_to_create_invoice_date,
            status_id = :status_id,
            due_days  = :due_days,
            automated_indicator  = :automated_indicator,
            separate_check_indicator = :separate_check_indicator,
            expense_month_creation_indicator_id = :expense_month_creation_indicator_id,
            invoice_day = :invoice_day,
            expense_day = :expense_day,
            schedule_id = :schedule_id,
            last_created_in_period = :last_created_in_period,
            next_creation_date = :next_creation_date,
            next_invoice_date = :next_invoice_date,
            next_expense_date = :next_expense_date
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "vendor_id" to entity.vendor.id,
            "invoice" to entity.invoice,
            "invoice_amount" to entity.invoiceAmount,
            "fixed_amount_indicator" to entity.fixedAmountIndicator,
            "employee_number_id_sfk" to entity.employeeNumberId,
            "message" to entity.message,
            "code_indicator" to entity.codeIndicator,
            "type" to entity.type,
            "pay_to_id" to entity.payTo.id,
            "last_transfer_to_create_invoice_date" to entity.lastTransferToCreateInvoiceDate,
            "status_id" to entity.status.id,
            "due_days" to entity.dueDays,
            "automated_indicator" to entity.automatedIndicator,
            "separate_check_indicator" to entity.separateCheckIndicator,
            "expense_month_creation_indicator_id" to entity.expenseMonthCreationIndicator.id,
            "invoice_day" to entity.invoiceDay,
            "expense_day" to entity.expenseDay,
            "schedule_id" to entity.schedule?.id,
            "last_created_in_period" to entity.lastCreatedInPeriod,
            "next_creation_date" to entity.nextCreationDate,
            "next_invoice_date" to entity.nextInvoiceDate,
            "next_expense_date" to entity.nextExpenseDate
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.vendor,
               entity.payTo,
               entity.status,
               entity.expenseMonthCreationIndicator,
               entity.schedule
            )
         }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      vendor: VendorEntity,
      payTo: VendorEntity,
      status: AccountPayableRecurringInvoiceStatusType,
      expenseMonthCreationIndicator: ExpenseMonthCreationType,
      schedule: ScheduleEntity?,
      columnPrefix: String = EMPTY
   ): AccountPayableRecurringInvoiceEntity {
      return AccountPayableRecurringInvoiceEntity(
         id = rs.getLong("${columnPrefix}id"),
         vendor = vendor,
         invoice = rs.getString("${columnPrefix}invoice"),
         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         fixedAmountIndicator = rs.getBoolean("${columnPrefix}fixed_amount_indicator"),
         employeeNumberId = rs.getInt("${columnPrefix}employee_number_id_sfk"),
         message = rs.getString("${columnPrefix}message"),
         codeIndicator = rs.getString("${columnPrefix}code_indicator"),
         type = rs.getString("${columnPrefix}type"),
         payTo = payTo,
         lastTransferToCreateInvoiceDate = rs.getLocalDateOrNull("${columnPrefix}last_transfer_to_create_invoice_date"),
         status = status,
         dueDays = rs.getInt("${columnPrefix}due_days"),
         automatedIndicator = rs.getBoolean("${columnPrefix}automated_indicator"),
         separateCheckIndicator = rs.getBoolean("${columnPrefix}separate_check_indicator"),
         expenseMonthCreationIndicator = expenseMonthCreationIndicator,
         invoiceDay = rs.getInt("${columnPrefix}invoice_day"),
         expenseDay = rs.getInt("${columnPrefix}expense_day"),
         schedule = schedule,
         lastCreatedInPeriod = rs.getLocalDateOrNull("${columnPrefix}last_created_in_period"),
         nextCreationDate = rs.getLocalDateOrNull("${columnPrefix}next_creation_date"),
         nextInvoiceDate = rs.getLocalDateOrNull("${columnPrefix}next_invoice_date"),
         nextExpenseDate = rs.getLocalDateOrNull("${columnPrefix}next_expense_date")
      )
   }

   private fun mapRow(
      rs: ResultSet,
      company: Company,
      columnPrefix: String = EMPTY
   ): AccountPayableRecurringInvoiceEntity {
      return AccountPayableRecurringInvoiceEntity(
         id = rs.getLong("${columnPrefix}id"),
         vendor = vendorRepository.mapRow(rs, company, "vendor_"),
         invoice = rs.getString("${columnPrefix}invoice"),
         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         fixedAmountIndicator = rs.getBoolean("${columnPrefix}fixed_amount_indicator"),
         employeeNumberId = rs.getInt("${columnPrefix}employee_number_id_sfk"),
         message = rs.getString("${columnPrefix}message"),
         codeIndicator = rs.getString("${columnPrefix}code_indicator"),
         type = rs.getString("${columnPrefix}type"),
         payTo = vendorRepository.mapRow(rs, company, "payTo_"),
         lastTransferToCreateInvoiceDate = rs.getLocalDateOrNull("${columnPrefix}last_transfer_to_create_invoice_date"),
         status = statusTypeRepository.mapRow(rs, "status_"),
         dueDays = rs.getInt("${columnPrefix}due_days"),
         automatedIndicator = rs.getBoolean("${columnPrefix}automated_indicator"),
         separateCheckIndicator = rs.getBoolean("${columnPrefix}separate_check_indicator"),
         expenseMonthCreationIndicator = expenseMonthCreationTypeRepository.mapRow(rs, "creation_type_"),
         invoiceDay = rs.getInt("${columnPrefix}invoice_day"),
         expenseDay = rs.getInt("${columnPrefix}expense_day"),
         schedule = null,
         lastCreatedInPeriod = rs.getLocalDateOrNull("${columnPrefix}last_created_in_period"),
         nextCreationDate = rs.getLocalDateOrNull("${columnPrefix}next_creation_date"),
         nextInvoiceDate = rs.getLocalDateOrNull("${columnPrefix}next_invoice_date"),
         nextExpenseDate = rs.getLocalDateOrNull("${columnPrefix}next_expense_date")
      )
   }
}
