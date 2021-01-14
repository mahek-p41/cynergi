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
            vendor.v_comp_id                                            AS vendor_comp_id,
            vendor.v_comp_uu_row_id                                     AS vendor_comp_uu_row_id,
            vendor.v_comp_time_created                                  AS vendor_comp_time_created,
            vendor.v_comp_time_updated                                  AS vendor_comp_time_updated,
            vendor.v_comp_name                                          AS vendor_comp_name,
            vendor.v_comp_doing_business_as                             AS vendor_comp_doing_business_as,
            vendor.v_comp_client_code                                   AS vendor_comp_client_code,
            vendor.v_comp_client_id                                     AS vendor_comp_client_id,
            vendor.v_comp_dataset_code                                  AS vendor_comp_dataset_code,
            vendor.v_comp_federal_id_number                             AS vendor_comp_federal_id_number,
            vendor.v_comp_address_id                                    AS vendor_comp_address_id,
            vendor.v_comp_address_name                                  AS vendor_comp_address_name,
            vendor.v_comp_address_address1                              AS vendor_comp_address_address1,
            vendor.v_comp_address_address2                              AS vendor_comp_address_address2,
            vendor.v_comp_address_city                                  AS vendor_comp_address_city,
            vendor.v_comp_address_state                                 AS vendor_comp_address_state,
            vendor.v_comp_address_postal_code                           AS vendor_comp_address_postal_code,
            vendor.v_comp_address_latitude                              AS vendor_comp_address_latitude,
            vendor.v_comp_address_longitude                             AS vendor_comp_address_longitude,
            vendor.v_comp_address_country                               AS vendor_comp_address_country,
            vendor.v_comp_address_county                                AS vendor_comp_address_county,
            vendor.v_comp_address_phone                                 AS vendor_comp_address_phone,
            vendor.v_comp_address_fax                                   AS vendor_comp_address_fax,
            vendor.v_onboard_id                                         AS vendor_onboard_id,
            vendor.v_onboard_value                                      AS vendor_onboard_value,
            vendor.v_onboard_description                                AS vendor_onboard_description,
            vendor.v_onboard_localization_code                          AS vendor_onboard_localization_code,
            vendor.v_method_id                                          AS vendor_method_id,
            vendor.v_method_value                                       AS vendor_method_value,
            vendor.v_method_description                                 AS vendor_method_description,
            vendor.v_method_localization_code                           AS vendor_method_localization_code,
            vendor.v_address_id                                         AS vendor_address_id,
            vendor.v_address_uu_row_id                                  AS vendor_address_uu_row_id,
            vendor.v_address_time_created                               AS vendor_address_time_created,
            vendor.v_address_time_updated                               AS vendor_address_time_updated,
            vendor.v_address_number                                     AS vendor_address_number,
            vendor.v_address_name                                       AS vendor_address_name,
            vendor.v_address_address1                                   AS vendor_address_address1,
            vendor.v_address_address2                                   AS vendor_address_address2,
            vendor.v_address_city                                       AS vendor_address_city,
            vendor.v_address_state                                      AS vendor_address_state,
            vendor.v_address_postal_code                                AS vendor_address_postal_code,
            vendor.v_address_latitude                                   AS vendor_address_latitude,
            vendor.v_address_longitude                                  AS vendor_address_longitude,
            vendor.v_address_country                                    AS vendor_address_country,
            vendor.v_address_county                                     AS vendor_address_county,
            vendor.v_address_phone                                      AS vendor_address_phone,
            vendor.v_address_fax                                        AS vendor_address_fax,
            vendor.v_vpt_id                                             AS vendor_vpt_id,
            vendor.v_vpt_uu_row_id                                      AS vendor_vpt_uu_row_id,
            vendor.v_vpt_time_created                                   AS vendor_vpt_time_created,
            vendor.v_vpt_time_updated                                   AS vendor_vpt_time_updated,
            vendor.v_vpt_company_id                                     AS vendor_vpt_company_id,
            vendor.v_vpt_description                                    AS vendor_vpt_description,
            vendor.v_vpt_number                                         AS vendor_vpt_number,
            vendor.v_vpt_number_of_payments                             AS vendor_vpt_number_of_payments,
            vendor.v_vpt_discount_month                                 AS vendor_vpt_discount_month,
            vendor.v_vpt_discount_days                                  AS vendor_vpt_discount_days,
            vendor.v_vpt_discount_percent                               AS vendor_vpt_discount_percent,
            vendor.v_shipVia_id                                         AS vendor_shipVia_id,
            vendor.v_shipVia_uu_row_id                                  AS vendor_shipVia_uu_row_id,
            vendor.v_shipVia_time_created                               AS vendor_shipVia_time_created,
            vendor.v_shipVia_time_updated                               AS vendor_shipVia_time_updated,
            vendor.v_shipVia_description                                AS vendor_shipVia_description,
            vendor.v_shipVia_number                                     AS vendor_shipVia_number,
            vendor.v_vgrp_id                                            AS vendor_vgrp_id,
            vendor.v_vgrp_uu_row_id                                     AS vendor_vgrp_uu_row_id,
            vendor.v_vgrp_time_created                                  AS vendor_vgrp_time_created,
            vendor.v_vgrp_time_updated                                  AS vendor_vgrp_time_updated,
            vendor.v_vgrp_company_id                                    AS vendor_vgrp_company_id,
            vendor.v_vgrp_value                                         AS vendor_vgrp_value,
            vendor.v_vgrp_description                                   AS vendor_vgrp_description,
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
            payTo.v_comp_id                                             AS payTo_comp_id,
            payTo.v_comp_uu_row_id                                      AS payTo_comp_uu_row_id,
            payTo.v_comp_time_created                                   AS payTo_comp_time_created,
            payTo.v_comp_time_updated                                   AS payTo_comp_time_updated,
            payTo.v_comp_name                                           AS payTo_comp_name,
            payTo.v_comp_doing_business_as                              AS payTo_comp_doing_business_as,
            payTo.v_comp_client_code                                    AS payTo_comp_client_code,
            payTo.v_comp_client_id                                      AS payTo_comp_client_id,
            payTo.v_comp_dataset_code                                   AS payTo_comp_dataset_code,
            payTo.v_comp_federal_id_number                              AS payTo_comp_federal_id_number,
            payTo.v_comp_address_id                                     AS payTo_comp_address_id,
            payTo.v_comp_address_name                                   AS payTo_comp_address_name,
            payTo.v_comp_address_address1                               AS payTo_comp_address_address1,
            payTo.v_comp_address_address2                               AS payTo_comp_address_address2,
            payTo.v_comp_address_city                                   AS payTo_comp_address_city,
            payTo.v_comp_address_state                                  AS payTo_comp_address_state,
            payTo.v_comp_address_postal_code                            AS payTo_comp_address_postal_code,
            payTo.v_comp_address_latitude                               AS payTo_comp_address_latitude,
            payTo.v_comp_address_longitude                              AS payTo_comp_address_longitude,
            payTo.v_comp_address_country                                AS payTo_comp_address_country,
            payTo.v_comp_address_county                                 AS payTo_comp_address_county,
            payTo.v_comp_address_phone                                  AS payTo_comp_address_phone,
            payTo.v_comp_address_fax                                    AS payTo_comp_address_fax,
            payTo.v_onboard_id                                          AS payTo_onboard_id,
            payTo.v_onboard_value                                       AS payTo_onboard_value,
            payTo.v_onboard_description                                 AS payTo_onboard_description,
            payTo.v_onboard_localization_code                           AS payTo_onboard_localization_code,
            payTo.v_method_id                                           AS payTo_method_id,
            payTo.v_method_value                                        AS payTo_method_value,
            payTo.v_method_description                                  AS payTo_method_description,
            payTo.v_method_localization_code                            AS payTo_method_localization_code,
            payTo.v_address_id                                          AS payTo_address_id,
            payTo.v_address_uu_row_id                                   AS payTo_address_uu_row_id,
            payTo.v_address_time_created                                AS payTo_address_time_created,
            payTo.v_address_time_updated                                AS payTo_address_time_updated,
            payTo.v_address_number                                      AS payTo_address_number,
            payTo.v_address_name                                        AS payTo_address_name,
            payTo.v_address_address1                                    AS payTo_address_address1,
            payTo.v_address_address2                                    AS payTo_address_address2,
            payTo.v_address_city                                        AS payTo_address_city,
            payTo.v_address_state                                       AS payTo_address_state,
            payTo.v_address_postal_code                                 AS payTo_address_postal_code,
            payTo.v_address_latitude                                    AS payTo_address_latitude,
            payTo.v_address_longitude                                   AS payTo_address_longitude,
            payTo.v_address_country                                     AS payTo_address_country,
            payTo.v_address_county                                      AS payTo_address_county,
            payTo.v_address_phone                                       AS payTo_address_phone,
            payTo.v_address_fax                                         AS payTo_address_fax,
            payTo.v_vpt_id                                              AS payTo_vpt_id,
            payTo.v_vpt_uu_row_id                                       AS payTo_vpt_uu_row_id,
            payTo.v_vpt_time_created                                    AS payTo_vpt_time_created,
            payTo.v_vpt_time_updated                                    AS payTo_vpt_time_updated,
            payTo.v_vpt_company_id                                      AS payTo_vpt_company_id,
            payTo.v_vpt_description                                     AS payTo_vpt_description,
            payTo.v_vpt_number                                          AS payTo_vpt_number,
            payTo.v_vpt_number_of_payments                              AS payTo_vpt_number_of_payments,
            payTo.v_vpt_discount_month                                  AS payTo_vpt_discount_month,
            payTo.v_vpt_discount_days                                   AS payTo_vpt_discount_days,
            payTo.v_vpt_discount_percent                                AS payTo_vpt_discount_percent,
            payTo.v_shipVia_id                                          AS payTo_shipVia_id,
            payTo.v_shipVia_uu_row_id                                   AS payTo_shipVia_uu_row_id,
            payTo.v_shipVia_time_created                                AS payTo_shipVia_time_created,
            payTo.v_shipVia_time_updated                                AS payTo_shipVia_time_updated,
            payTo.v_shipVia_description                                 AS payTo_shipVia_description,
            payTo.v_shipVia_number                                      AS payTo_shipVia_number,
            payTo.v_vgrp_id                                             AS payTo_vgrp_id,
            payTo.v_vgrp_uu_row_id                                      AS payTo_vgrp_uu_row_id,
            payTo.v_vgrp_time_created                                   AS payTo_vgrp_time_created,
            payTo.v_vgrp_time_updated                                   AS payTo_vgrp_time_updated,
            payTo.v_vgrp_company_id                                     AS payTo_vgrp_company_id,
            payTo.v_vgrp_value                                          AS payTo_vgrp_value,
            payTo.v_vgrp_description                                    AS payTo_vgrp_description,
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
