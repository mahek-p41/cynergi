package com.cynergisuite.middleware.accounting.account.payable.check.preview.infrastructure

import com.cynergisuite.domain.AccountPayableCheckPreviewFilterRequest
import com.cynergisuite.extensions.getBigDecimalOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.sumByBigDecimal
import com.cynergisuite.middleware.accounting.account.payable.check.AccountPayableVoidCheckDTO
import com.cynergisuite.middleware.accounting.account.payable.check.infrastructure.AccountPayableVoidCheckFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableCheckPreviewVendorsEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.ResultSet
import javax.transaction.Transactional

@Singleton
class AccountPayableCheckPreviewRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val vendorRepository: VendorRepository,
   private val accountPayableInvoiceRepository: AccountPayableInvoiceRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH vend AS (
            ${vendorRepository.baseSelectQuery()}
         )
         SELECT
            apInvoice.id                                                AS apInvoice_id,
            apInvoice.company_id                                        AS apInvoice_company_id,
            apInvoice.invoice                                           AS apInvoice_invoice,
            apInvoice.purchase_order_id                                 AS apInvoice_purchase_order_id,
            apInvoice.invoice_date                                      AS apInvoice_invoice_date,
            apInvoice.invoice_amount                                    AS apInvoice_invoice_amount,
            apInvoice.discount_amount                                   AS apInvoice_discount_amount,
            apInvoice.discount_percent                                  AS apInvoice_discount_percent,
            apInvoice.auto_distribution_applied                         AS apInvoice_auto_distribution_applied,
            apInvoice.discount_taken                                    AS apInvoice_discount_taken,
            apInvoice.entry_date                                        AS apInvoice_entry_date,
            apInvoice.expense_date                                      AS apInvoice_expense_date,
            apInvoice.discount_date                                     AS apInvoice_discount_date,
            apInvoice.employee_number_id_sfk                            AS apInvoice_employee_number_id_sfk,
            apInvoice.original_invoice_amount                           AS apInvoice_original_invoice_amount,
            apInvoice.message                                           AS apInvoice_message,
            apInvoice.multiple_payment_indicator                        AS apInvoice_multiple_payment_indicator,
            apInvoice.paid_amount                                       AS apInvoice_paid_amount,
            apInvoice.selected_amount                                   AS apInvoice_selected_amount,
            apInvoice.due_date                                          AS apInvoice_due_date,
            apInvoice.separate_check_indicator                          AS apInvoice_separate_check_indicator,
            apInvoice.use_tax_indicator                                 AS apInvoice_use_tax_indicator,
            apInvoice.receive_date                                      AS apInvoice_receive_date,
            apInvoice.location_id_sfk                                   AS apInvoice_location_id_sfk,
                        vend.v_id                                                   AS apInvoice_vendor_id,
            vend.v_company_id                                           AS apInvoice_vendor_company_id,
            vend.v_number                                               AS apInvoice_vendor_number,
            vend.v_name                                                 AS apInvoice_vendor_name,
            vend.v_account_number                                       AS apInvoice_vendor_account_number,
            vend.v_pay_to_id                                            AS apInvoice_vendor_pay_to_id,
            vend.v_freight_on_board_type_id                             AS apInvoice_vendor_freight_on_board_type_id,
            vend.v_vendor_payment_term_id                               AS apInvoice_vendor_vendor_payment_term_id,
            vend.v_normal_days                                          AS apInvoice_vendor_normal_days,
            vend.v_return_policy                                        AS apInvoice_vendor_return_policy,
            vend.v_ship_via_id                                          AS apInvoice_vendor_ship_via_id,
            vend.v_vendor_group_id                                      AS apInvoice_vendor_group_id,
            vend.v_minimum_quantity                                     AS apInvoice_vendor_minimum_quantity,
            vend.v_minimum_amount                                       AS apInvoice_vendor_minimum_amount,
            vend.v_free_ship_quantity                                   AS apInvoice_vendor_free_ship_quantity,
            vend.v_free_ship_amount                                     AS apInvoice_vendor_free_ship_amount,
            vend.v_vendor_1099                                          AS apInvoice_vendor_vendor_1099,
            vend.v_federal_id_number                                    AS apInvoice_vendor_federal_id_number,
            vend.v_sales_representative_name                            AS apInvoice_vendor_sales_representative_name,
            vend.v_sales_representative_fax                             AS apInvoice_vendor_sales_representative_fax,
            vend.v_separate_check                                       AS apInvoice_vendor_separate_check,
            vend.v_bump_percent                                         AS apInvoice_vendor_bump_percent,
            vend.v_freight_calc_method_type_id                          AS apInvoice_vendor_freight_calc_method_type_id,
            vend.v_freight_percent                                      AS apInvoice_vendor_freight_percent,
            vend.v_freight_amount                                       AS apInvoice_vendor_freight_amount,
            vend.v_charge_inventory_tax_1                               AS apInvoice_vendor_charge_inventory_tax_1,
            vend.v_charge_inventory_tax_2                               AS apInvoice_vendor_charge_inventory_tax_2,
            vend.v_charge_inventory_tax_3                               AS apInvoice_vendor_charge_inventory_tax_3,
            vend.v_charge_inventory_tax_4                               AS apInvoice_vendor_charge_inventory_tax_4,
            vend.v_federal_id_number_verification                       AS apInvoice_vendor_federal_id_number_verification,
            vend.v_email_address                                        AS apInvoice_vendor_email_address,
            vend.v_purchase_order_submit_email                          AS apInvoice_vendor_purchase_order_submit_email,
            vend.v_allow_drop_ship_to_customer                          AS apInvoice_vendor_allow_drop_ship_to_customer,
            vend.v_auto_submit_purchase_order                           AS apInvoice_vendor_auto_submit_purchase_order,
            vend.v_note                                                 AS apInvoice_vendor_note,
            vend.v_phone_number                                         AS apInvoice_vendor_phone_number,
            vend.v_active                                               AS apInvoice_vendor_active,
            vend.v_comp_id                                              AS apInvoice_vendor_comp_id,
            vend.v_comp_time_created                                    AS apInvoice_vendor_comp_time_created,
            vend.v_comp_time_updated                                    AS apInvoice_vendor_comp_time_updated,
            vend.v_comp_name                                            AS apInvoice_vendor_comp_name,
            vend.v_comp_doing_business_as                               AS apInvoice_vendor_comp_doing_business_as,
            vend.v_comp_client_code                                     AS apInvoice_vendor_comp_client_code,
            vend.v_comp_client_id                                       AS apInvoice_vendor_comp_client_id,
            vend.v_comp_dataset_code                                    AS apInvoice_vendor_comp_dataset_code,
            vend.v_comp_federal_id_number                               AS apInvoice_vendor_comp_federal_id_number,
            vend.v_comp_include_demo_inventory                          AS apInvoice_vendor_comp_include_demo_inventory,
            vend.v_comp_address_id                                      AS apInvoice_vendor_comp_address_id,
            vend.v_comp_address_name                                    AS apInvoice_vendor_comp_address_name,
            vend.v_comp_address_address1                                AS apInvoice_vendor_comp_address_address1,
            vend.v_comp_address_address2                                AS apInvoice_vendor_comp_address_address2,
            vend.v_comp_address_city                                    AS apInvoice_vendor_comp_address_city,
            vend.v_comp_address_state                                   AS apInvoice_vendor_comp_address_state,
            vend.v_comp_address_postal_code                             AS apInvoice_vendor_comp_address_postal_code,
            vend.v_comp_address_latitude                                AS apInvoice_vendor_comp_address_latitude,
            vend.v_comp_address_longitude                               AS apInvoice_vendor_comp_address_longitude,
            vend.v_comp_address_country                                 AS apInvoice_vendor_comp_address_country,
            vend.v_comp_address_county                                  AS apInvoice_vendor_comp_address_county,
            vend.v_comp_address_phone                                   AS apInvoice_vendor_comp_address_phone,
            vend.v_comp_address_fax                                     AS apInvoice_vendor_comp_address_fax,
            vend.v_onboard_id                                           AS apInvoice_vendor_onboard_id,
            vend.v_onboard_value                                        AS apInvoice_vendor_onboard_value,
            vend.v_onboard_description                                  AS apInvoice_vendor_onboard_description,
            vend.v_onboard_localization_code                            AS apInvoice_vendor_onboard_localization_code,
            vend.v_method_id                                            AS apInvoice_vendor_method_id,
            vend.v_method_value                                         AS apInvoice_vendor_method_value,
            vend.v_method_description                                   AS apInvoice_vendor_method_description,
            vend.v_method_localization_code                             AS apInvoice_vendor_method_localization_code,
            vend.v_address_id                                           AS apInvoice_vendor_address_id,
            vend.v_address_time_created                                 AS apInvoice_vendor_address_time_created,
            vend.v_address_time_updated                                 AS apInvoice_vendor_address_time_updated,
            vend.v_address_number                                       AS apInvoice_vendor_address_number,
            vend.v_address_name                                         AS apInvoice_vendor_address_name,
            vend.v_address_address1                                     AS apInvoice_vendor_address_address1,
            vend.v_address_address2                                     AS apInvoice_vendor_address_address2,
            vend.v_address_city                                         AS apInvoice_vendor_address_city,
            vend.v_address_state                                        AS apInvoice_vendor_address_state,
            vend.v_address_postal_code                                  AS apInvoice_vendor_address_postal_code,
            vend.v_address_latitude                                     AS apInvoice_vendor_address_latitude,
            vend.v_address_longitude                                    AS apInvoice_vendor_address_longitude,
            vend.v_address_country                                      AS apInvoice_vendor_address_country,
            vend.v_address_county                                       AS apInvoice_vendor_address_county,
            vend.v_address_phone                                        AS apInvoice_vendor_address_phone,
            vend.v_address_fax                                          AS apInvoice_vendor_address_fax,
            vend.v_vpt_id                                               AS apInvoice_vendor_vpt_id,
            vend.v_vpt_time_created                                     AS apInvoice_vendor_vpt_time_created,
            vend.v_vpt_time_updated                                     AS apInvoice_vendor_vpt_time_updated,
            vend.v_vpt_company_id                                       AS apInvoice_vendor_vpt_company_id,
            vend.v_vpt_description                                      AS apInvoice_vendor_vpt_description,
            vend.v_vpt_number                                           AS apInvoice_vendor_vpt_number,
            vend.v_vpt_number_of_payments                               AS apInvoice_vendor_vpt_number_of_payments,
            vend.v_vpt_discount_month                                   AS apInvoice_vendor_vpt_discount_month,
            vend.v_vpt_discount_days                                    AS apInvoice_vendor_vpt_discount_days,
            vend.v_vpt_discount_percent                                 AS apInvoice_vendor_vpt_discount_percent,
            vend.v_shipVia_id                                           AS apInvoice_vendor_shipVia_id,
            vend.v_shipVia_time_created                                 AS apInvoice_vendor_shipVia_time_created,
            vend.v_shipVia_time_updated                                 AS apInvoice_vendor_shipVia_time_updated,
            vend.v_shipVia_description                                  AS apInvoice_vendor_shipVia_description,
            vend.v_shipVia_number                                       AS apInvoice_vendor_shipVia_number,
            vend.v_vgrp_id                                              AS apInvoice_vendor_vgrp_id,
            vend.v_vgrp_time_created                                    AS apInvoice_vendor_vgrp_time_created,
            vend.v_vgrp_time_updated                                    AS apInvoice_vendor_vgrp_time_updated,
            vend.v_vgrp_company_id                                      AS apInvoice_vendor_vgrp_company_id,
            vend.v_vgrp_value                                           AS apInvoice_vendor_vgrp_value,
            vend.v_vgrp_description                                     AS apInvoice_vendor_vgrp_description,
            vend.v_has_rebate                                           AS apInvoice_vendor_has_rebate,

            payTo.v_id                                                  AS apInvoice_payTo_id,
            payTo.v_company_id                                          AS apInvoice_payTo_company_id,
            payTo.v_number                                              AS apInvoice_payTo_number,
            payTo.v_name                                                AS apInvoice_payTo_name,
            payTo.v_account_number                                      AS apInvoice_payTo_account_number,
            payTo.v_pay_to_id                                           AS apInvoice_payTo_pay_to_id,
            payTo.v_freight_on_board_type_id                            AS apInvoice_payTo_freight_on_board_type_id,
            payTo.v_vendor_payment_term_id                              AS apInvoice_payTo_vendor_payment_term_id,
            payTo.v_normal_days                                         AS apInvoice_payTo_normal_days,
            payTo.v_return_policy                                       AS apInvoice_payTo_return_policy,
            payTo.v_ship_via_id                                         AS apInvoice_payTo_ship_via_id,
            payTo.v_vendor_group_id                                     AS apInvoice_payTo_group_id,
            payTo.v_minimum_quantity                                    AS apInvoice_payTo_minimum_quantity,
            payTo.v_minimum_amount                                      AS apInvoice_payTo_minimum_amount,
            payTo.v_free_ship_quantity                                  AS apInvoice_payTo_free_ship_quantity,
            payTo.v_free_ship_amount                                    AS apInvoice_payTo_free_ship_amount,
            payTo.v_vendor_1099                                         AS apInvoice_payTo_vendor_1099,
            payTo.v_federal_id_number                                   AS apInvoice_payTo_federal_id_number,
            payTo.v_sales_representative_name                           AS apInvoice_payTo_sales_representative_name,
            payTo.v_sales_representative_fax                            AS apInvoice_payTo_sales_representative_fax,
            payTo.v_separate_check                                      AS apInvoice_payTo_separate_check,
            payTo.v_bump_percent                                        AS apInvoice_payTo_bump_percent,
            payTo.v_freight_calc_method_type_id                         AS apInvoice_payTo_freight_calc_method_type_id,
            payTo.v_freight_percent                                     AS apInvoice_payTo_freight_percent,
            payTo.v_freight_amount                                      AS apInvoice_payTo_freight_amount,
            payTo.v_charge_inventory_tax_1                              AS apInvoice_payTo_charge_inventory_tax_1,
            payTo.v_charge_inventory_tax_2                              AS apInvoice_payTo_charge_inventory_tax_2,
            payTo.v_charge_inventory_tax_3                              AS apInvoice_payTo_charge_inventory_tax_3,
            payTo.v_charge_inventory_tax_4                              AS apInvoice_payTo_charge_inventory_tax_4,
            payTo.v_federal_id_number_verification                      AS apInvoice_payTo_federal_id_number_verification,
            payTo.v_email_address                                       AS apInvoice_payTo_email_address,
            payTo.v_purchase_order_submit_email                         AS apInvoice_payTo_purchase_order_submit_email,
            payTo.v_allow_drop_ship_to_customer                         AS apInvoice_payTo_allow_drop_ship_to_customer,
            payTo.v_auto_submit_purchase_order                          AS apInvoice_payTo_auto_submit_purchase_order,
            payTo.v_note                                                AS apInvoice_payTo_note,
            payTo.v_phone_number                                        AS apInvoice_payTo_phone_number,
            payTo.v_active                                              AS apInvoice_payTo_active,
            payTo.v_comp_id                                             AS apInvoice_payTo_comp_id,
            payTo.v_comp_time_created                                   AS apInvoice_payTo_comp_time_created,
            payTo.v_comp_time_updated                                   AS apInvoice_payTo_comp_time_updated,
            payTo.v_comp_name                                           AS apInvoice_payTo_comp_name,
            payTo.v_comp_doing_business_as                              AS apInvoice_payTo_comp_doing_business_as,
            payTo.v_comp_client_code                                    AS apInvoice_payTo_comp_client_code,
            payTo.v_comp_client_id                                      AS apInvoice_payTo_comp_client_id,
            payTo.v_comp_dataset_code                                   AS apInvoice_payTo_comp_dataset_code,
            payTo.v_comp_federal_id_number                              AS apInvoice_payTo_comp_federal_id_number,
            payTo.v_comp_include_demo_inventory                         AS apInvoice_payTo_comp_include_demo_inventory,
            payTo.v_comp_address_id                                     AS apInvoice_payTo_comp_address_id,
            payTo.v_comp_address_name                                   AS apInvoice_payTo_comp_address_name,
            payTo.v_comp_address_address1                               AS apInvoice_payTo_comp_address_address1,
            payTo.v_comp_address_address2                               AS apInvoice_payTo_comp_address_address2,
            payTo.v_comp_address_city                                   AS apInvoice_payTo_comp_address_city,
            payTo.v_comp_address_state                                  AS apInvoice_payTo_comp_address_state,
            payTo.v_comp_address_postal_code                            AS apInvoice_payTo_comp_address_postal_code,
            payTo.v_comp_address_latitude                               AS apInvoice_payTo_comp_address_latitude,
            payTo.v_comp_address_longitude                              AS apInvoice_payTo_comp_address_longitude,
            payTo.v_comp_address_country                                AS apInvoice_payTo_comp_address_country,
            payTo.v_comp_address_county                                 AS apInvoice_payTo_comp_address_county,
            payTo.v_comp_address_phone                                  AS apInvoice_payTo_comp_address_phone,
            payTo.v_comp_address_fax                                    AS apInvoice_payTo_comp_address_fax,
            payTo.v_onboard_id                                          AS apInvoice_payTo_onboard_id,
            payTo.v_onboard_value                                       AS apInvoice_payTo_onboard_value,
            payTo.v_onboard_description                                 AS apInvoice_payTo_onboard_description,
            payTo.v_onboard_localization_code                           AS apInvoice_payTo_onboard_localization_code,
            payTo.v_method_id                                           AS apInvoice_payTo_method_id,
            payTo.v_method_value                                        AS apInvoice_payTo_method_value,
            payTo.v_method_description                                  AS apInvoice_payTo_method_description,
            payTo.v_method_localization_code                            AS apInvoice_payTo_method_localization_code,
            payTo.v_address_id                                          AS apInvoice_payTo_address_id,
            payTo.v_address_time_created                                AS apInvoice_payTo_address_time_created,
            payTo.v_address_time_updated                                AS apInvoice_payTo_address_time_updated,
            payTo.v_address_number                                      AS apInvoice_payTo_address_number,
            payTo.v_address_name                                        AS apInvoice_payTo_address_name,
            payTo.v_address_address1                                    AS apInvoice_payTo_address_address1,
            payTo.v_address_address2                                    AS apInvoice_payTo_address_address2,
            payTo.v_address_city                                        AS apInvoice_payTo_address_city,
            payTo.v_address_state                                       AS apInvoice_payTo_address_state,
            payTo.v_address_postal_code                                 AS apInvoice_payTo_address_postal_code,
            payTo.v_address_latitude                                    AS apInvoice_payTo_address_latitude,
            payTo.v_address_longitude                                   AS apInvoice_payTo_address_longitude,
            payTo.v_address_country                                     AS apInvoice_payTo_address_country,
            payTo.v_address_county                                      AS apInvoice_payTo_address_county,
            payTo.v_address_phone                                       AS apInvoice_payTo_address_phone,
            payTo.v_address_fax                                         AS apInvoice_payTo_address_fax,
            payTo.v_vpt_id                                              AS apInvoice_payTo_vpt_id,
            payTo.v_vpt_time_created                                    AS apInvoice_payTo_vpt_time_created,
            payTo.v_vpt_time_updated                                    AS apInvoice_payTo_vpt_time_updated,
            payTo.v_vpt_company_id                                      AS apInvoice_payTo_vpt_company_id,
            payTo.v_vpt_description                                     AS apInvoice_payTo_vpt_description,
            payTo.v_vpt_number                                          AS apInvoice_payTo_vpt_number,
            payTo.v_vpt_number_of_payments                              AS apInvoice_payTo_vpt_number_of_payments,
            payTo.v_vpt_discount_month                                  AS apInvoice_payTo_vpt_discount_month,
            payTo.v_vpt_discount_days                                   AS apInvoice_payTo_vpt_discount_days,
            payTo.v_vpt_discount_percent                                AS apInvoice_payTo_vpt_discount_percent,
            payTo.v_shipVia_id                                          AS apInvoice_payTo_shipVia_id,
            payTo.v_shipVia_time_created                                AS apInvoice_payTo_shipVia_time_created,
            payTo.v_shipVia_time_updated                                AS apInvoice_payTo_shipVia_time_updated,
            payTo.v_shipVia_description                                 AS apInvoice_payTo_shipVia_description,
            payTo.v_shipVia_number                                      AS apInvoice_payTo_shipVia_number,
            payTo.v_vgrp_id                                             AS apInvoice_payTo_vgrp_id,
            payTo.v_vgrp_time_created                                   AS apInvoice_payTo_vgrp_time_created,
            payTo.v_vgrp_time_updated                                   AS apInvoice_payTo_vgrp_time_updated,
            payTo.v_vgrp_company_id                                     AS apInvoice_payTo_vgrp_company_id,
            payTo.v_vgrp_value                                          AS apInvoice_payTo_vgrp_value,
            payTo.v_vgrp_description                                    AS apInvoice_payTo_vgrp_description,
            payTo.v_has_rebate                                          AS apInvoice_payTo_has_rebate,
            employee.emp_id                                             AS apInvoice_employee_id,
            employee.emp_number                                         AS apInvoice_employee_number,
            employee.emp_last_name                                      AS apInvoice_employee_last_name,
            employee.emp_first_name_mi                                  AS apInvoice_employee_first_name_mi,
            employee.emp_type                                           AS apInvoice_employee_type,
            employee.emp_pass_code                                      AS apInvoice_employee_pass_code,
            employee.emp_active                                         AS apInvoice_employee_active,
            employee.emp_cynergi_system_admin                           AS apInvoice_employee_cynergi_system_admin,
            employee.emp_alternative_store_indicator                    AS apInvoice_employee_alternative_store_indicator,
            employee.emp_alternative_area                               AS apInvoice_employee_alternative_area,
            employee.store_id                                           AS apInvoice_employee_store_id,
            employee.store_number                                       AS apInvoice_employee_store_number,
            employee.store_name                                         AS apInvoice_employee_store_name,
            employee.comp_id                                            AS apInvoice_employee_comp_id,
            employee.comp_time_created                                  AS apInvoice_employee_comp_time_created,
            employee.comp_time_updated                                  AS apInvoice_employee_comp_time_updated,
            employee.comp_name                                          AS apInvoice_employee_comp_name,
            employee.comp_doing_business_as                             AS apInvoice_employee_comp_doing_business_as,
            employee.comp_client_code                                   AS apInvoice_employee_comp_client_code,
            employee.comp_client_id                                     AS apInvoice_employee_comp_client_id,
            employee.comp_dataset_code                                  AS apInvoice_employee_comp_dataset_code,
            employee.comp_federal_id_number                             AS apInvoice_employee_comp_federal_id_number,
            employee.comp_include_demo_inventory                        AS apInvoice_employee_comp_include_demo_inventory,
            employee.address_id                                         AS apInvoice_employee_comp_address_id,
            employee.address_name                                       AS apInvoice_employee_comp_address_name,
            employee.address_address1                                   AS apInvoice_employee_comp_address_address1,
            employee.address_address2                                   AS apInvoice_employee_comp_address_address2,
            employee.address_city                                       AS apInvoice_employee_comp_address_city,
            employee.address_state                                      AS apInvoice_employee_comp_address_state,
            employee.address_postal_code                                AS apInvoice_employee_comp_address_postal_code,
            employee.address_latitude                                   AS apInvoice_employee_comp_address_latitude,
            employee.address_longitude                                  AS apInvoice_employee_comp_address_longitude,
            employee.address_country                                    AS apInvoice_employee_comp_address_country,
            employee.address_county                                     AS apInvoice_employee_comp_address_county,
            employee.address_phone                                      AS apInvoice_employee_comp_address_phone,
            employee.address_fax                                        AS apInvoice_employee_comp_address_fax,
            employee.dept_id                                            AS apInvoice_employee_dept_id,
            employee.dept_code                                          AS apInvoice_employee_dept_code,
            employee.dept_description                                   AS apInvoice_employee_dept_description,

            pmt.payment_number                                          AS apPayment_number,
            pmt.payment_date                                            AS apPayment_payment_date,
            pmt.account_payable_payment_status_id                       AS apPayment_status_id,
            pmt.date_cleared                                            AS apPayment_date_cleared,
            pmtStatus.value                                             AS apPayment_status_value,
            pmtDetail.id                                                AS apPayment_detail_id,
            pmtDetail.amount                                            AS apPayment_detail_amount,
            bank.id                                                     AS apInvoice_bank_id,
            bank.number                                                 AS apInvoice_bank_number,
            bank.name                                                   AS apInvoice_bank_name,
            apControl.pay_after_discount_date                           AS apPayment_pay_after_discount_date,
            selected.id                                                 AS apInvoice_selected_id,
            selected.value                                              AS apInvoice_selected_value,
            selected.description                                        AS apInvoice_selected_description,
            selected.localization_code                                  AS apInvoice_selected_localization_code,
            type.id                                                     AS apInvoice_type_id,
            type.value                                                  AS apInvoice_type_value,
            type.description                                            AS apInvoice_type_description,
            type.localization_code                                      AS apInvoice_type_localization_code,
            status.id                                                   AS apInvoice_status_id,
            status.value                                                AS apInvoice_status_value,
            status.description                                          AS apInvoice_status_description,
            status.localization_code                                    AS apInvoice_status_localization_code,
            poHeader.number                                             AS apInvoice_purchase_order_number
         FROM account_payable_invoice apInvoice
            JOIN company comp                                           ON apInvoice.company_id = comp.id AND comp.deleted = FALSE
            JOIN vend                                                   ON apInvoice.vendor_id = vend.v_id
            JOIN vend payTo                                             on apInvoice.pay_to_id = payTo.v_id
            JOIN account_payable_invoice_selected_type_domain selected  ON apInvoice.selected_id = selected.id
            JOIN account_payable_invoice_type_domain type               ON apInvoice.type_id = type.id
            JOIN account_payable_invoice_status_type_domain status      ON apInvoice.status_id = status.id
            LEFT JOIN account_payable_payment_detail pmtDetail          ON apInvoice.id = pmtDetail.account_payable_invoice_id
            LEFT JOIN account_payable_payment pmt                       ON pmtDetail.payment_number_id = pmt.id
            LEFT JOIN account_payable_payment_status_type_domain pmtStatus   ON pmt.account_payable_payment_status_id = pmtStatus.id
            JOIN account_payable_invoice_distribution invDist           ON apInvoice.id = invDist.invoice_id
            JOIN account_payable_control apControl                      ON invDist.distribution_account_id = apControl.general_ledger_inventory_account_id
            LEFT JOIN bank                                              ON pmt.bank_id = bank.id AND bank.deleted = FALSE
            JOIN purchase_order_header poHeader                         ON apInvoice.purchase_order_id = poHeader.id
            LEFT JOIN system_employees_fimvw employee                   ON apInvoice.employee_number_id_sfk = employee.emp_number AND employee.comp_id = comp.id
      """
   }

   @ReadOnly
   fun fetchCheckPreview(company: CompanyEntity, filterRequest: AccountPayableCheckPreviewFilterRequest): AccountPayableCheckPreviewEntity {
      var currentVendor: AccountPayableCheckPreviewVendorsEntity? = null
      var previousCheck = false
      var checkNumber = filterRequest.checkNumber.toBigInteger()
      val previewDetails = mutableListOf<AccountPayableCheckPreviewVendorsEntity>()
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val sortBy = StringBuilder("ORDER BY ")
      val whereClause = StringBuilder(
         "WHERE apInvoice.company_id = :comp_id " +
            "AND apInvoice.status_id = 2 AND apInvoice.deleted = false "
      )

      if (filterRequest.checkDate != null) {
         params["checkDate"] = filterRequest.checkDate
      }

      if (filterRequest.vendorGroup != null) {
         params["vendorGroup"] = filterRequest.vendorGroup
         whereClause.append("AND payTo.v_vendor_group_id = :vendorGroup ")
      }

      if (filterRequest.dueDate != null) {
         params["dueDate"] = filterRequest.dueDate
         whereClause.append("AND apInvoice.due_date <= :dueDate ")
      }

      if (filterRequest.discountDate != null) {
         params["discountDate"] = filterRequest.discountDate
         whereClause.append("AND CASE " +
            "WHEN apControl.pay_after_discount_date = true THEN apInvoice.discount_date >= :discountDate " +
            "WHEN apControl.pay_after_discount_date = false THEN apInvoice.discount_date  <= :discountDate AND apInvoice.discount_date >= :checkDate END ")
      }
      if (filterRequest.sortBy == "V"){
         sortBy.append("apInvoice_vendor_name")
      }
      if (filterRequest.sortBy == "N") {
         sortBy.append("apInvoice_vendor_number")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            $sortBy
         """.trimIndent(),
         params)
      { rs, elements ->
         do {
            val separateCheck = rs.getBoolean("apInvoice_separate_check_indicator")
            val tempVendor = if (currentVendor?.vendorNumber != rs.getIntOrNull("apInvoice_vendor_number")) {
                  val localVendor = mapCheckPreview(rs, checkNumber)
                  checkNumber++
                  previewDetails.add(localVendor)
                  currentVendor = localVendor

                  localVendor
            } else {
               if(separateCheck || previousCheck ) {
                  val localVendor = mapCheckPreview(rs, checkNumber)
                  checkNumber++
                  previewDetails.add(localVendor)
                  currentVendor = localVendor

                  localVendor
               } else {
                  currentVendor
               }
            }
            mapRow(rs, company, "apInvoice_").let {
               tempVendor!!.invoiceList ?.add(it)
               tempVendor.gross = tempVendor.gross.plus(it.gross)
               tempVendor.discount = tempVendor.discount.plus(it.discount)
               tempVendor.deduction = tempVendor.deduction.plus(it.deduction ?: BigDecimal.ZERO)
               tempVendor.netPaid = tempVendor.netPaid.plus(it.netPaid ?: BigDecimal.ZERO)
            }
            previousCheck = separateCheck
         } while (rs.next())
      }

      val gross = previewDetails.sumByBigDecimal { it.gross }
      val discount = previewDetails.sumByBigDecimal { it.discount }
      val deduction = previewDetails.sumByBigDecimal { it.deduction }
      val netPaid =  previewDetails.sumByBigDecimal { it.netPaid }
      return AccountPayableCheckPreviewEntity(previewDetails, gross, discount, deduction, netPaid)
   }

   @Transactional
   fun voidCheck(company: CompanyEntity, filterRequest: AccountPayableVoidCheckFilterRequest): AccountPayableVoidCheckDTO {
      var currentVendor: AccountPayableVoidCheckDTO? = null
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val sortBy = StringBuilder("ORDER BY ")
      val whereClause = StringBuilder(
         "WHERE apInvoice.company_id = :comp_id "
      )

      if (filterRequest.bank != null) {
         params["bank"] = filterRequest.bank
         whereClause.append("AND bank.number = :bank ")
      }

      if (filterRequest.checkNumber != null) {
         params["checkNumber"] = filterRequest.checkNumber
         whereClause.append("AND pmt.payment_number = :checkNumber ")
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
         """.trimIndent(),
         params
      )
      { rs, elements ->
         do {
            val tempVendor = if (currentVendor?.vendorNumber != rs.getIntOrNull("apInvoice_payTo_number")) {
               val localVendor = mapVoidCheck(rs)
               currentVendor = localVendor

               localVendor
            } else {
               currentVendor
            }
            accountPayableInvoiceRepository.mapRow(rs, company, "apInvoice_").let {
               tempVendor!!.invoices ?.add(AccountPayableInvoiceDTO(it))
            }
         } while (rs.next())
      }
      return currentVendor!!
   }

   fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = StringUtils.EMPTY
   ): AccountPayableCheckPreviewInvoiceEntity {
      val gross = rs.getBigDecimal("${columnPrefix}invoice_amount")
      val discAmt = rs.getBigDecimal("${columnPrefix}discount_amount")
      val discPerc = rs.getBigDecimalOrNull("${columnPrefix}discount_percent")
      val discount = discAmt.times(discPerc ?: BigDecimal.ZERO)
      val netPaid = gross - discount
      return AccountPayableCheckPreviewInvoiceEntity(
         id = rs.getUuid("${columnPrefix}id"),
         invoiceNumber = rs.getString("${columnPrefix}invoice"),
         date = rs.getLocalDate("${columnPrefix}invoice_date"),
         dueDate = rs.getLocalDate("${columnPrefix}due_date"),
         poNumber = rs.getInt("${columnPrefix}purchase_order_number"),
         gross = gross,
         discount = discount,
         deduction = BigDecimal.ZERO,
         netPaid = netPaid,
         notes = rs.getString("${columnPrefix}message")
      )
   }

   private fun mapCheckPreview(
      rs: ResultSet,
      checkNumber: BigInteger
   ): AccountPayableCheckPreviewVendorsEntity {
      return AccountPayableCheckPreviewVendorsEntity(
         vendorNumber = rs.getInt("apInvoice_vendor_number"),
         vendorName = rs.getString("apInvoice_vendor_name"),
         address1 = rs.getString("apInvoice_vendor_address_address1"),
         address2 = rs.getString("apInvoice_vendor_address_address2"),
         city = rs.getString("apInvoice_vendor_address_city"),
         state = rs.getString("apInvoice_vendor_address_state"),
         postalCode = rs.getString("apInvoice_vendor_address_postal_code"),
         checkNumber = checkNumber.toString(),
         date = rs.getLocalDate("apInvoice_invoice_date")
      )
   }

   @ReadOnly
   fun validateCheckNums(
      checkNumber: BigInteger,
      bank: Long,
      vendorList: List<AccountPayableCheckPreviewVendorsEntity>
   ): Boolean {
      val numOfChecks = vendorList.size
      val list: ArrayList<BigInteger> = ArrayList()
      for (i in 0 until numOfChecks) {
         list.add(checkNumber + i.toBigInteger())
      }

      return jdbc.queryForObject(
         "SELECT EXISTS(SELECT payment_number FROM account_payable_payment " +
                 "JOIN bank on account_payable_payment.bank_id = bank.id AND bank.deleted = FALSE " +
                 "WHERE account_payable_payment.payment_number = " +
                 "any(array[<checkList>]::varchar[]) AND bank.number = :bank" +
                 ")",
         mapOf(
            "checkList" to list.map { it.toString()}, "bank" to bank
         ),
         Boolean::class.java
      )
   }

   private fun mapVoidCheck(rs: ResultSet,): AccountPayableVoidCheckDTO {

      return AccountPayableVoidCheckDTO(
         vendorNumber = rs.getInt("apInvoice_payTo_number"),
         vendorName = rs.getString("apInvoice_payTo_name"),
         bankId = rs.getUuid("apInvoice_bank_id"),
         checkNumber = rs.getString("apPayment_number"),
         amount = rs.getBigDecimal("apPayment_detail_amount"),
         paymentStatus = rs.getString("apPayment_status_value"),
         dateCleared = rs.getLocalDateOrNull("apPayment_date_cleared"),
         date = rs.getLocalDate("apPayment_payment_date"),
         effectiveDate = null,
      )
   }

}
