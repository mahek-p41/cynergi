package com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure

import com.cynergisuite.domain.AccountPayableInvoiceListByVendorFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableRecurringInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableRecurringInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.recurring.AccountPayableRecurringInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.recurring.ExpenseMonthCreationType
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.schedule.ScheduleEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class AccountPayableRecurringInvoiceRepository @Inject constructor(
   private val jdbc: Jdbi,
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
            apRecurringInvoice.start_date                               AS apRecurringInvoice_start_date,
            apRecurringInvoice.end_date                                 AS apRecurringInvoice_end_date,
            vendor.v_id                                                 AS apRecurringInvoice_vendor_id,
            vendor.v_time_created                                       AS apRecurringInvoice_vendor_time_created,
            vendor.v_time_updated                                       AS apRecurringInvoice_vendor_time_updated,
            vendor.v_company_id                                         AS apRecurringInvoice_vendor_company_id,
            vendor.v_number                                             AS apRecurringInvoice_vendor_number,
            vendor.v_name                                               AS apRecurringInvoice_vendor_name,
            vendor.v_address_id                                         AS apRecurringInvoice_vendor_address_id,
            vendor.v_account_number                                     AS apRecurringInvoice_vendor_account_number,
            vendor.v_pay_to_id                                          AS apRecurringInvoice_vendor_pay_to_id,
            vendor.v_freight_on_board_type_id                           AS apRecurringInvoice_vendor_freight_on_board_type_id,
            vendor.v_vendor_payment_term_id                             AS apRecurringInvoice_vendor_vendor_payment_term_id,
            vendor.v_normal_days                                        AS apRecurringInvoice_vendor_normal_days,
            vendor.v_return_policy                                      AS apRecurringInvoice_vendor_return_policy,
            vendor.v_ship_via_id                                        AS apRecurringInvoice_vendor_ship_via_id,
            vendor.v_vendor_group_id                                    AS apRecurringInvoice_vendor_group_id,
            vendor.v_minimum_quantity                                   AS apRecurringInvoice_vendor_minimum_quantity,
            vendor.v_minimum_amount                                     AS apRecurringInvoice_vendor_minimum_amount,
            vendor.v_free_ship_quantity                                 AS apRecurringInvoice_vendor_free_ship_quantity,
            vendor.v_free_ship_amount                                   AS apRecurringInvoice_vendor_free_ship_amount,
            vendor.v_vendor_1099                                        AS apRecurringInvoice_vendor_vendor_1099,
            vendor.v_federal_id_number                                  AS apRecurringInvoice_vendor_federal_id_number,
            vendor.v_sales_representative_name                          AS apRecurringInvoice_vendor_sales_representative_name,
            vendor.v_sales_representative_fax                           AS apRecurringInvoice_vendor_sales_representative_fax,
            vendor.v_separate_check                                     AS apRecurringInvoice_vendor_separate_check,
            vendor.v_bump_percent                                       AS apRecurringInvoice_vendor_bump_percent,
            vendor.v_freight_calc_method_type_id                        AS apRecurringInvoice_vendor_freight_calc_method_type_id,
            vendor.v_freight_percent                                    AS apRecurringInvoice_vendor_freight_percent,
            vendor.v_freight_amount                                     AS apRecurringInvoice_vendor_freight_amount,
            vendor.v_charge_inventory_tax_1                             AS apRecurringInvoice_vendor_charge_inventory_tax_1,
            vendor.v_charge_inventory_tax_2                             AS apRecurringInvoice_vendor_charge_inventory_tax_2,
            vendor.v_charge_inventory_tax_3                             AS apRecurringInvoice_vendor_charge_inventory_tax_3,
            vendor.v_charge_inventory_tax_4                             AS apRecurringInvoice_vendor_charge_inventory_tax_4,
            vendor.v_federal_id_number_verification                     AS apRecurringInvoice_vendor_federal_id_number_verification,
            vendor.v_email_address                                      AS apRecurringInvoice_vendor_email_address,
            vendor.v_purchase_order_submit_email                        AS apRecurringInvoice_vendor_purchase_order_submit_email,
            vendor.v_allow_drop_ship_to_customer                        AS apRecurringInvoice_vendor_allow_drop_ship_to_customer,
            vendor.v_auto_submit_purchase_order                         AS apRecurringInvoice_vendor_auto_submit_purchase_order,
            vendor.v_note                                               AS apRecurringInvoice_vendor_note,
            vendor.v_phone_number                                       AS apRecurringInvoice_vendor_phone_number,
            vendor.v_active                                             AS apRecurringInvoice_vendor_active,
            vendor.v_comp_id                                            AS apRecurringInvoice_vendor_comp_id,
            vendor.v_comp_time_created                                  AS apRecurringInvoice_vendor_comp_time_created,
            vendor.v_comp_time_updated                                  AS apRecurringInvoice_vendor_comp_time_updated,
            vendor.v_comp_name                                          AS apRecurringInvoice_vendor_comp_name,
            vendor.v_comp_doing_business_as                             AS apRecurringInvoice_vendor_comp_doing_business_as,
            vendor.v_comp_client_code                                   AS apRecurringInvoice_vendor_comp_client_code,
            vendor.v_comp_client_id                                     AS apRecurringInvoice_vendor_comp_client_id,
            vendor.v_comp_dataset_code                                  AS apRecurringInvoice_vendor_comp_dataset_code,
            vendor.v_comp_federal_id_number                             AS apRecurringInvoice_vendor_comp_federal_id_number,
            vendor.v_comp_include_demo_inventory                        AS apRecurringInvoice_vendor_comp_include_demo_inventory,
            vendor.v_comp_address_id                                    AS apRecurringInvoice_vendor_comp_address_id,
            vendor.v_comp_address_name                                  AS apRecurringInvoice_vendor_comp_address_name,
            vendor.v_comp_address_address1                              AS apRecurringInvoice_vendor_comp_address_address1,
            vendor.v_comp_address_address2                              AS apRecurringInvoice_vendor_comp_address_address2,
            vendor.v_comp_address_city                                  AS apRecurringInvoice_vendor_comp_address_city,
            vendor.v_comp_address_state                                 AS apRecurringInvoice_vendor_comp_address_state,
            vendor.v_comp_address_postal_code                           AS apRecurringInvoice_vendor_comp_address_postal_code,
            vendor.v_comp_address_latitude                              AS apRecurringInvoice_vendor_comp_address_latitude,
            vendor.v_comp_address_longitude                             AS apRecurringInvoice_vendor_comp_address_longitude,
            vendor.v_comp_address_country                               AS apRecurringInvoice_vendor_comp_address_country,
            vendor.v_comp_address_county                                AS apRecurringInvoice_vendor_comp_address_county,
            vendor.v_comp_address_phone                                 AS apRecurringInvoice_vendor_comp_address_phone,
            vendor.v_comp_address_fax                                   AS apRecurringInvoice_vendor_comp_address_fax,
            vendor.v_onboard_id                                         AS apRecurringInvoice_vendor_onboard_id,
            vendor.v_onboard_value                                      AS apRecurringInvoice_vendor_onboard_value,
            vendor.v_onboard_description                                AS apRecurringInvoice_vendor_onboard_description,
            vendor.v_onboard_localization_code                          AS apRecurringInvoice_vendor_onboard_localization_code,
            vendor.v_method_id                                          AS apRecurringInvoice_vendor_method_id,
            vendor.v_method_value                                       AS apRecurringInvoice_vendor_method_value,
            vendor.v_method_description                                 AS apRecurringInvoice_vendor_method_description,
            vendor.v_method_localization_code                           AS apRecurringInvoice_vendor_method_localization_code,
            vendor.v_address_id                                         AS apRecurringInvoice_vendor_address_id,
            vendor.v_address_time_created                               AS apRecurringInvoice_vendor_address_time_created,
            vendor.v_address_time_updated                               AS apRecurringInvoice_vendor_address_time_updated,
            vendor.v_address_number                                     AS apRecurringInvoice_vendor_address_number,
            vendor.v_address_name                                       AS apRecurringInvoice_vendor_address_name,
            vendor.v_address_address1                                   AS apRecurringInvoice_vendor_address_address1,
            vendor.v_address_address2                                   AS apRecurringInvoice_vendor_address_address2,
            vendor.v_address_city                                       AS apRecurringInvoice_vendor_address_city,
            vendor.v_address_state                                      AS apRecurringInvoice_vendor_address_state,
            vendor.v_address_postal_code                                AS apRecurringInvoice_vendor_address_postal_code,
            vendor.v_address_latitude                                   AS apRecurringInvoice_vendor_address_latitude,
            vendor.v_address_longitude                                  AS apRecurringInvoice_vendor_address_longitude,
            vendor.v_address_country                                    AS apRecurringInvoice_vendor_address_country,
            vendor.v_address_county                                     AS apRecurringInvoice_vendor_address_county,
            vendor.v_address_phone                                      AS apRecurringInvoice_vendor_address_phone,
            vendor.v_address_fax                                        AS apRecurringInvoice_vendor_address_fax,
            vendor.v_vpt_id                                             AS apRecurringInvoice_vendor_vpt_id,
            vendor.v_vpt_time_created                                   AS apRecurringInvoice_vendor_vpt_time_created,
            vendor.v_vpt_time_updated                                   AS apRecurringInvoice_vendor_vpt_time_updated,
            vendor.v_vpt_company_id                                     AS apRecurringInvoice_vendor_vpt_company_id,
            vendor.v_vpt_description                                    AS apRecurringInvoice_vendor_vpt_description,
            vendor.v_vpt_number                                         AS apRecurringInvoice_vendor_vpt_number,
            vendor.v_vpt_number_of_payments                             AS apRecurringInvoice_vendor_vpt_number_of_payments,
            vendor.v_vpt_discount_month                                 AS apRecurringInvoice_vendor_vpt_discount_month,
            vendor.v_vpt_discount_days                                  AS apRecurringInvoice_vendor_vpt_discount_days,
            vendor.v_vpt_discount_percent                               AS apRecurringInvoice_vendor_vpt_discount_percent,
            vendor.v_shipVia_id                                         AS apRecurringInvoice_vendor_shipVia_id,
            vendor.v_shipVia_time_created                               AS apRecurringInvoice_vendor_shipVia_time_created,
            vendor.v_shipVia_time_updated                               AS apRecurringInvoice_vendor_shipVia_time_updated,
            vendor.v_shipVia_description                                AS apRecurringInvoice_vendor_shipVia_description,
            vendor.v_shipVia_number                                     AS apRecurringInvoice_vendor_shipVia_number,
            vendor.v_vgrp_id                                            AS apRecurringInvoice_vendor_vgrp_id,
            vendor.v_vgrp_time_created                                  AS apRecurringInvoice_vendor_vgrp_time_created,
            vendor.v_vgrp_time_updated                                  AS apRecurringInvoice_vendor_vgrp_time_updated,
            vendor.v_vgrp_company_id                                    AS apRecurringInvoice_vendor_vgrp_company_id,
            vendor.v_vgrp_value                                         AS apRecurringInvoice_vendor_vgrp_value,
            vendor.v_vgrp_description                                   AS apRecurringInvoice_vendor_vgrp_description,
            vendor.v_has_rebate                                         AS apRecurringInvoice_vendor_has_rebate,
            payTo.v_id                                                  AS apRecurringInvoice_payTo_id,
            payTo.v_time_created                                        AS apRecurringInvoice_payTo_time_created,
            payTo.v_time_updated                                        AS apRecurringInvoice_payTo_time_updated,
            payTo.v_company_id                                          AS apRecurringInvoice_payTo_company_id,
            payTo.v_number                                              AS apRecurringInvoice_payTo_number,
            payTo.v_name                                                AS apRecurringInvoice_payTo_name,
            payTo.v_address_id                                          AS apRecurringInvoice_payTo_address_id,
            payTo.v_account_number                                      AS apRecurringInvoice_payTo_account_number,
            payTo.v_pay_to_id                                           AS apRecurringInvoice_payTo_pay_to_id,
            payTo.v_freight_on_board_type_id                            AS apRecurringInvoice_payTo_freight_on_board_type_id,
            payTo.v_vendor_payment_term_id                              AS apRecurringInvoice_payTo_vendor_payment_term_id,
            payTo.v_normal_days                                         AS apRecurringInvoice_payTo_normal_days,
            payTo.v_return_policy                                       AS apRecurringInvoice_payTo_return_policy,
            payTo.v_ship_via_id                                         AS apRecurringInvoice_payTo_ship_via_id,
            payTo.v_vendor_group_id                                     AS apRecurringInvoice_payTo_group_id,
            payTo.v_minimum_quantity                                    AS apRecurringInvoice_payTo_minimum_quantity,
            payTo.v_minimum_amount                                      AS apRecurringInvoice_payTo_minimum_amount,
            payTo.v_free_ship_quantity                                  AS apRecurringInvoice_payTo_free_ship_quantity,
            payTo.v_free_ship_amount                                    AS apRecurringInvoice_payTo_free_ship_amount,
            payTo.v_vendor_1099                                         AS apRecurringInvoice_payTo_vendor_1099,
            payTo.v_federal_id_number                                   AS apRecurringInvoice_payTo_federal_id_number,
            payTo.v_sales_representative_name                           AS apRecurringInvoice_payTo_sales_representative_name,
            payTo.v_sales_representative_fax                            AS apRecurringInvoice_payTo_sales_representative_fax,
            payTo.v_separate_check                                      AS apRecurringInvoice_payTo_separate_check,
            payTo.v_bump_percent                                        AS apRecurringInvoice_payTo_bump_percent,
            payTo.v_freight_calc_method_type_id                         AS apRecurringInvoice_payTo_freight_calc_method_type_id,
            payTo.v_freight_percent                                     AS apRecurringInvoice_payTo_freight_percent,
            payTo.v_freight_amount                                      AS apRecurringInvoice_payTo_freight_amount,
            payTo.v_charge_inventory_tax_1                              AS apRecurringInvoice_payTo_charge_inventory_tax_1,
            payTo.v_charge_inventory_tax_2                              AS apRecurringInvoice_payTo_charge_inventory_tax_2,
            payTo.v_charge_inventory_tax_3                              AS apRecurringInvoice_payTo_charge_inventory_tax_3,
            payTo.v_charge_inventory_tax_4                              AS apRecurringInvoice_payTo_charge_inventory_tax_4,
            payTo.v_federal_id_number_verification                      AS apRecurringInvoice_payTo_federal_id_number_verification,
            payTo.v_email_address                                       AS apRecurringInvoice_payTo_email_address,
            payTo.v_purchase_order_submit_email                         AS apRecurringInvoice_payTo_purchase_order_submit_email,
            payTo.v_allow_drop_ship_to_customer                         AS apRecurringInvoice_payTo_allow_drop_ship_to_customer,
            payTo.v_auto_submit_purchase_order                          AS apRecurringInvoice_payTo_auto_submit_purchase_order,
            payTo.v_note                                                AS apRecurringInvoice_payTo_note,
            payTo.v_phone_number                                        AS apRecurringInvoice_payTo_phone_number,
            payTo.v_active                                              AS apRecurringInvoice_payTo_active,
            payTo.v_comp_id                                             AS apRecurringInvoice_payTo_comp_id,
            payTo.v_comp_time_created                                   AS apRecurringInvoice_payTo_comp_time_created,
            payTo.v_comp_time_updated                                   AS apRecurringInvoice_payTo_comp_time_updated,
            payTo.v_comp_name                                           AS apRecurringInvoice_payTo_comp_name,
            payTo.v_comp_doing_business_as                              AS apRecurringInvoice_payTo_comp_doing_business_as,
            payTo.v_comp_client_code                                    AS apRecurringInvoice_payTo_comp_client_code,
            payTo.v_comp_client_id                                      AS apRecurringInvoice_payTo_comp_client_id,
            payTo.v_comp_dataset_code                                   AS apRecurringInvoice_payTo_comp_dataset_code,
            payTo.v_comp_federal_id_number                              AS apRecurringInvoice_payTo_comp_federal_id_number,
            payTo.v_comp_include_demo_inventory                         AS apRecurringInvoice_payTo_comp_include_demo_inventory,
            payTo.v_comp_address_id                                     AS apRecurringInvoice_payTo_comp_address_id,
            payTo.v_comp_address_name                                   AS apRecurringInvoice_payTo_comp_address_name,
            payTo.v_comp_address_address1                               AS apRecurringInvoice_payTo_comp_address_address1,
            payTo.v_comp_address_address2                               AS apRecurringInvoice_payTo_comp_address_address2,
            payTo.v_comp_address_city                                   AS apRecurringInvoice_payTo_comp_address_city,
            payTo.v_comp_address_state                                  AS apRecurringInvoice_payTo_comp_address_state,
            payTo.v_comp_address_postal_code                            AS apRecurringInvoice_payTo_comp_address_postal_code,
            payTo.v_comp_address_latitude                               AS apRecurringInvoice_payTo_comp_address_latitude,
            payTo.v_comp_address_longitude                              AS apRecurringInvoice_payTo_comp_address_longitude,
            payTo.v_comp_address_country                                AS apRecurringInvoice_payTo_comp_address_country,
            payTo.v_comp_address_county                                 AS apRecurringInvoice_payTo_comp_address_county,
            payTo.v_comp_address_phone                                  AS apRecurringInvoice_payTo_comp_address_phone,
            payTo.v_comp_address_fax                                    AS apRecurringInvoice_payTo_comp_address_fax,
            payTo.v_onboard_id                                          AS apRecurringInvoice_payTo_onboard_id,
            payTo.v_onboard_value                                       AS apRecurringInvoice_payTo_onboard_value,
            payTo.v_onboard_description                                 AS apRecurringInvoice_payTo_onboard_description,
            payTo.v_onboard_localization_code                           AS apRecurringInvoice_payTo_onboard_localization_code,
            payTo.v_method_id                                           AS apRecurringInvoice_payTo_method_id,
            payTo.v_method_value                                        AS apRecurringInvoice_payTo_method_value,
            payTo.v_method_description                                  AS apRecurringInvoice_payTo_method_description,
            payTo.v_method_localization_code                            AS apRecurringInvoice_payTo_method_localization_code,
            payTo.v_address_id                                          AS apRecurringInvoice_payTo_address_id,
            payTo.v_address_time_created                                AS apRecurringInvoice_payTo_address_time_created,
            payTo.v_address_time_updated                                AS apRecurringInvoice_payTo_address_time_updated,
            payTo.v_address_number                                      AS apRecurringInvoice_payTo_address_number,
            payTo.v_address_name                                        AS apRecurringInvoice_payTo_address_name,
            payTo.v_address_address1                                    AS apRecurringInvoice_payTo_address_address1,
            payTo.v_address_address2                                    AS apRecurringInvoice_payTo_address_address2,
            payTo.v_address_city                                        AS apRecurringInvoice_payTo_address_city,
            payTo.v_address_state                                       AS apRecurringInvoice_payTo_address_state,
            payTo.v_address_postal_code                                 AS apRecurringInvoice_payTo_address_postal_code,
            payTo.v_address_latitude                                    AS apRecurringInvoice_payTo_address_latitude,
            payTo.v_address_longitude                                   AS apRecurringInvoice_payTo_address_longitude,
            payTo.v_address_country                                     AS apRecurringInvoice_payTo_address_country,
            payTo.v_address_county                                      AS apRecurringInvoice_payTo_address_county,
            payTo.v_address_phone                                       AS apRecurringInvoice_payTo_address_phone,
            payTo.v_address_fax                                         AS apRecurringInvoice_payTo_address_fax,
            payTo.v_vpt_id                                              AS apRecurringInvoice_payTo_vpt_id,
            payTo.v_vpt_time_created                                    AS apRecurringInvoice_payTo_vpt_time_created,
            payTo.v_vpt_time_updated                                    AS apRecurringInvoice_payTo_vpt_time_updated,
            payTo.v_vpt_company_id                                      AS apRecurringInvoice_payTo_vpt_company_id,
            payTo.v_vpt_description                                     AS apRecurringInvoice_payTo_vpt_description,
            payTo.v_vpt_number                                          AS apRecurringInvoice_payTo_vpt_number,
            payTo.v_vpt_number_of_payments                              AS apRecurringInvoice_payTo_vpt_number_of_payments,
            payTo.v_vpt_discount_month                                  AS apRecurringInvoice_payTo_vpt_discount_month,
            payTo.v_vpt_discount_days                                   AS apRecurringInvoice_payTo_vpt_discount_days,
            payTo.v_vpt_discount_percent                                AS apRecurringInvoice_payTo_vpt_discount_percent,
            payTo.v_shipVia_id                                          AS apRecurringInvoice_payTo_shipVia_id,
            payTo.v_shipVia_time_created                                AS apRecurringInvoice_payTo_shipVia_time_created,
            payTo.v_shipVia_time_updated                                AS apRecurringInvoice_payTo_shipVia_time_updated,
            payTo.v_shipVia_description                                 AS apRecurringInvoice_payTo_shipVia_description,
            payTo.v_shipVia_number                                      AS apRecurringInvoice_payTo_shipVia_number,
            payTo.v_vgrp_id                                             AS apRecurringInvoice_payTo_vgrp_id,
            payTo.v_vgrp_time_created                                   AS apRecurringInvoice_payTo_vgrp_time_created,
            payTo.v_vgrp_time_updated                                   AS apRecurringInvoice_payTo_vgrp_time_updated,
            payTo.v_vgrp_company_id                                     AS apRecurringInvoice_payTo_vgrp_company_id,
            payTo.v_vgrp_value                                          AS apRecurringInvoice_payTo_vgrp_value,
            payTo.v_vgrp_description                                    AS apRecurringInvoice_payTo_vgrp_description,
            payTo.v_has_rebate                                          AS apRecurringInvoice_payTo_has_rebate,
            status.id                                                   AS apRecurringInvoice_status_id,
            status.value                                                AS apRecurringInvoice_status_value,
            status.description                                          AS apRecurringInvoice_status_description,
            status.localization_code                                    AS apRecurringInvoice_status_localization_code,
            creation_type.id                                            AS apRecurringInvoice_creation_type_id,
            creation_type.value                                         AS apRecurringInvoice_creation_type_value,
            creation_type.description                                   AS apRecurringInvoice_creation_type_description,
            creation_type.localization_code                             AS apRecurringInvoice_creation_type_localization_code,
            count(*) OVER()                                             AS total_elements
         FROM account_payable_recurring_invoice apRecurringInvoice
            JOIN vendor ON apRecurringInvoice.vendor_id = vendor.v_id
            JOIN vendor payTo ON apRecurringInvoice.pay_to_id = payTo.v_id
            JOIN account_payable_recurring_invoice_status_type_domain status ON apRecurringInvoice.status_id = status.id
            JOIN expense_month_creation_type_domain creation_type ON apRecurringInvoice.expense_month_creation_indicator_id = creation_type.id
            LEFT JOIN schedule ON apRecurringInvoice.schedule_id = schedule.id
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AccountPayableRecurringInvoiceEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE apRecurringInvoice.id = :id AND apRecurringInvoice.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         val vendor = vendorRepository.mapRow(rs, company, "apRecurringInvoice_vendor_")
         val payTo = vendorRepository.mapRow(rs, company, "apRecurringInvoice_payTo_")
         val status = statusTypeRepository.mapRow(rs, "apRecurringInvoice_status_")
         val expenseMonthCreationType =
            expenseMonthCreationTypeRepository.mapRow(rs, "apRecurringInvoice_creation_type_")

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

      logger.trace("Searching for AccountPayableRecurringInvoice: {} resulted in {}", company, found)

      return found
   }

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      filterRequest: AccountPayableInvoiceListByVendorFilterRequest
   ): RepositoryPage<AccountPayableRecurringInvoiceEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder(" WHERE apRecurringInvoice.company_id = :comp_id")

      if (filterRequest.vendor != null) {
         params["vendor"] = filterRequest.vendor
         whereClause.append(" AND vendor.v_number >= :vendor ")
      }

      if (filterRequest.invoice != null) {
         params["invoice"] = filterRequest.invoice
         whereClause.append(" AND apRecurringInvoice.invoice = :invoice ")
      }
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY apRecurringInvoice_${filterRequest.snakeSortBy()} ${filterRequest.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "apRecurringInvoice_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(
      entity: AccountPayableRecurringInvoiceEntity,
      company: CompanyEntity
   ): AccountPayableRecurringInvoiceEntity {
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
            next_expense_date,
            start_date,
            end_date
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
            :next_expense_date,
            :start_date,
            :end_date
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
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
            "next_expense_date" to entity.nextExpenseDate,
            "start_date" to entity.startDate,
            "end_date" to entity.endDate
         )
      ) { rs, _ ->
         mapRow(
            rs,
            entity.vendor,
            entity.payTo,
            entity.status,
            entity.expenseMonthCreationIndicator,
            entity.schedule
         )
      }
   }

   @Transactional
   fun update(
      entity: AccountPayableRecurringInvoiceEntity,
      company: CompanyEntity
   ): AccountPayableRecurringInvoiceEntity {
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
            next_expense_date = :next_expense_date,
            start_date = :start_date,
            end_date = :end_date
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.id,
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
            "next_expense_date" to entity.nextExpenseDate,
            "start_date" to entity.startDate,
            "end_date" to entity.endDate
         )
      ) { rs, _ ->
         mapRow(
            rs,
            entity.vendor,
            entity.payTo,
            entity.status,
            entity.expenseMonthCreationIndicator,
            entity.schedule
         )
      }
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
         id = rs.getUuid("${columnPrefix}id"),
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
         nextExpenseDate = rs.getLocalDateOrNull("${columnPrefix}next_expense_date"),
         startDate = rs.getLocalDate("${columnPrefix}start_date"),
         endDate = rs.getLocalDate("${columnPrefix}end_date")
      )
   }

   private fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY
   ): AccountPayableRecurringInvoiceEntity {
      return AccountPayableRecurringInvoiceEntity(
         id = rs.getUuid("${columnPrefix}id"),
         vendor = vendorRepository.mapRow(rs, company, "${columnPrefix}vendor_"),
         invoice = rs.getString("${columnPrefix}invoice"),
         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         fixedAmountIndicator = rs.getBoolean("${columnPrefix}fixed_amount_indicator"),
         employeeNumberId = rs.getInt("${columnPrefix}employee_number_id_sfk"),
         message = rs.getString("${columnPrefix}message"),
         codeIndicator = rs.getString("${columnPrefix}code_indicator"),
         type = rs.getString("${columnPrefix}type"),
         payTo = vendorRepository.mapRow(rs, company, "${columnPrefix}payTo_"),
         lastTransferToCreateInvoiceDate = rs.getLocalDateOrNull("${columnPrefix}last_transfer_to_create_invoice_date"),
         status = statusTypeRepository.mapRow(rs, "${columnPrefix}status_"),
         dueDays = rs.getInt("${columnPrefix}due_days"),
         automatedIndicator = rs.getBoolean("${columnPrefix}automated_indicator"),
         separateCheckIndicator = rs.getBoolean("${columnPrefix}separate_check_indicator"),
         expenseMonthCreationIndicator = expenseMonthCreationTypeRepository.mapRow(rs, "${columnPrefix}creation_type_"),
         invoiceDay = rs.getInt("${columnPrefix}invoice_day"),
         expenseDay = rs.getInt("${columnPrefix}expense_day"),
         schedule = null,
         lastCreatedInPeriod = rs.getLocalDateOrNull("${columnPrefix}last_created_in_period"),
         nextCreationDate = rs.getLocalDateOrNull("${columnPrefix}next_creation_date"),
         nextInvoiceDate = rs.getLocalDateOrNull("${columnPrefix}next_invoice_date"),
         nextExpenseDate = rs.getLocalDateOrNull("${columnPrefix}next_expense_date"),
         startDate = rs.getLocalDate("${columnPrefix}start_date"),
         endDate = rs.getLocalDate("${columnPrefix}end_date")
      )
   }
}
