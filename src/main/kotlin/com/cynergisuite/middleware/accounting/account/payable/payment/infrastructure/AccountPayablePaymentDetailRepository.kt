package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentDetailEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AccountPayablePaymentDetailRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val apInvoiceRepository: AccountPayableInvoiceRepository,
   private val vendorRepository: VendorRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayablePaymentDetailRepository::class.java)

   fun baseSelectQuery(): String {
      return """
         WITH vdr AS (
            ${vendorRepository.baseSelectQuery()}
         ),
         inv AS (
            ${apInvoiceRepository.selectBaseQuery()}
         )
         SELECT
            apPaymentDetail.id                                       AS apPaymentDetail_id,
            apPaymentDetail.company_id                               AS apPaymentDetail_company_id,
            inv.apInvoice_id                                         AS apPaymentDetail_apInvoice_id,
            inv.apInvoice_company_id                                 AS apPaymentDetail_apInvoice_company_id,
            inv.apInvoice_invoice                                    AS apPaymentDetail_apInvoice_invoice,
            inv.apInvoice_purchase_order_id                          AS apPaymentDetail_apInvoice_purchase_order_id,
            inv.apInvoice_invoice_date                               AS apPaymentDetail_apInvoice_invoice_date,
            inv.apInvoice_invoice_amount                             AS apPaymentDetail_apInvoice_invoice_amount,
            inv.apInvoice_discount_amount                            AS apPaymentDetail_apInvoice_discount_amount,
            inv.apInvoice_discount_percent                           AS apPaymentDetail_apInvoice_discount_percent,
            inv.apInvoice_auto_distribution_applied                  AS apPaymentDetail_apInvoice_auto_distribution_applied,
            inv.apInvoice_discount_taken                             AS apPaymentDetail_apInvoice_discount_taken,
            inv.apInvoice_entry_date                                 AS apPaymentDetail_apInvoice_entry_date,
            inv.apInvoice_expense_date                               AS apPaymentDetail_apInvoice_expense_date,
            inv.apInvoice_discount_date                              AS apPaymentDetail_apInvoice_discount_date,
            inv.apInvoice_employee_number_id_sfk                     AS apPaymentDetail_apInvoice_employee_number_id_sfk,
            inv.apInvoice_original_invoice_amount                    AS apPaymentDetail_apInvoice_original_invoice_amount,
            inv.apInvoice_message                                    AS apPaymentDetail_apInvoice_message,
            inv.apInvoice_multiple_payment_indicator                 AS apPaymentDetail_apInvoice_multiple_payment_indicator,
            inv.apInvoice_paid_amount                                AS apPaymentDetail_apInvoice_paid_amount,
            inv.apInvoice_selected_amount                            AS apPaymentDetail_apInvoice_selected_amount,
            inv.apInvoice_due_date                                   AS apPaymentDetail_apInvoice_due_date,
            inv.apInvoice_pay_to_id                                  AS apPaymentDetail_apInvoice_pay_to_id,
            inv.apInvoice_separate_check_indicator                   AS apPaymentDetail_apInvoice_separate_check_indicator,
            inv.apInvoice_use_tax_indicator                          AS apPaymentDetail_apInvoice_use_tax_indicator,
            inv.apInvoice_receive_date                               AS apPaymentDetail_apInvoice_receive_date,
            inv.apInvoice_location_id_sfk                            AS apPaymentDetail_apInvoice_location_id_sfk,
            inv.apInvoice_vendor_id                                  AS apPaymentDetail_apInvoice_vendor_id,
            inv.apInvoice_vendor_company_id                          AS apPaymentDetail_apInvoice_vendor_company_id,
            inv.apInvoice_vendor_number                              AS apPaymentDetail_apInvoice_vendor_number,
            inv.apInvoice_vendor_name                                AS apPaymentDetail_apInvoice_vendor_name,
            inv.apInvoice_vendor_account_number                      AS apPaymentDetail_apInvoice_vendor_account_number,
            inv.apInvoice_vendor_pay_to_id                           AS apPaymentDetail_apInvoice_vendor_pay_to_id,
            inv.apInvoice_vendor_freight_on_board_type_id            AS apPaymentDetail_apInvoice_vendor_freight_on_board_type_id,
            inv.apInvoice_vendor_vendor_payment_term_id              AS apPaymentDetail_apInvoice_vendor_vendor_payment_term_id,
            inv.apInvoice_vendor_normal_days                         AS apPaymentDetail_apInvoice_vendor_normal_days,
            inv.apInvoice_vendor_return_policy                       AS apPaymentDetail_apInvoice_vendor_return_policy,
            inv.apInvoice_vendor_ship_via_id                         AS apPaymentDetail_apInvoice_vendor_ship_via_id,
            inv.apInvoice_vendor_group_id                            AS apPaymentDetail_apInvoice_vendor_group_id,
            inv.apInvoice_vendor_minimum_quantity                    AS apPaymentDetail_apInvoice_vendor_minimum_quantity,
            inv.apInvoice_vendor_minimum_amount                      AS apPaymentDetail_apInvoice_vendor_minimum_amount,
            inv.apInvoice_vendor_free_ship_quantity                  AS apPaymentDetail_apInvoice_vendor_free_ship_quantity,
            inv.apInvoice_vendor_free_ship_amount                    AS apPaymentDetail_apInvoice_vendor_free_ship_amount,
            inv.apInvoice_vendor_vendor_1099                         AS apPaymentDetail_apInvoice_vendor_vendor_1099,
            inv.apInvoice_vendor_federal_id_number                   AS apPaymentDetail_apInvoice_vendor_federal_id_number,
            inv.apInvoice_vendor_sales_representative_name           AS apPaymentDetail_apInvoice_vendor_sales_representative_name,
            inv.apInvoice_vendor_sales_representative_fax            AS apPaymentDetail_apInvoice_vendor_sales_representative_fax,
            inv.apInvoice_vendor_separate_check                      AS apPaymentDetail_apInvoice_vendor_separate_check,
            inv.apInvoice_vendor_bump_percent                        AS apPaymentDetail_apInvoice_vendor_bump_percent,
            inv.apInvoice_vendor_freight_calc_method_type_id         AS apPaymentDetail_apInvoice_vendor_freight_calc_method_type_id,
            inv.apInvoice_vendor_freight_percent                     AS apPaymentDetail_apInvoice_vendor_freight_percent,
            inv.apInvoice_vendor_freight_amount                      AS apPaymentDetail_apInvoice_vendor_freight_amount,
            inv.apInvoice_vendor_charge_inventory_tax_1              AS apPaymentDetail_apInvoice_vendor_charge_inventory_tax_1,
            inv.apInvoice_vendor_charge_inventory_tax_2              AS apPaymentDetail_apInvoice_vendor_charge_inventory_tax_2,
            inv.apInvoice_vendor_charge_inventory_tax_3              AS apPaymentDetail_apInvoice_vendor_charge_inventory_tax_3,
            inv.apInvoice_vendor_charge_inventory_tax_4              AS apPaymentDetail_apInvoice_vendor_charge_inventory_tax_4,
            inv.apInvoice_vendor_federal_id_number_verification      AS apPaymentDetail_apInvoice_vendor_federal_id_number_verification,
            inv.apInvoice_vendor_email_address                       AS apPaymentDetail_apInvoice_vendor_email_address,
            inv.apInvoice_vendor_purchase_order_submit_email         AS apPaymentDetail_apInvoice_vendor_purchase_order_submit_email,
            inv.apInvoice_vendor_allow_drop_ship_to_customer         AS apPaymentDetail_apInvoice_vendor_allow_drop_ship_to_customer,
            inv.apInvoice_vendor_auto_submit_purchase_order          AS apPaymentDetail_apInvoice_vendor_auto_submit_purchase_order,
            inv.apInvoice_vendor_note                                AS apPaymentDetail_apInvoice_vendor_note,
            inv.apInvoice_vendor_phone_number                        AS apPaymentDetail_apInvoice_vendor_phone_number,
            inv.apInvoice_vendor_comp_id                             AS apPaymentDetail_apInvoice_vendor_comp_id,
            inv.apInvoice_vendor_comp_time_created                   AS apPaymentDetail_apInvoice_vendor_comp_time_created,
            inv.apInvoice_vendor_comp_time_updated                   AS apPaymentDetail_apInvoice_vendor_comp_time_updated,
            inv.apInvoice_vendor_comp_name                           AS apPaymentDetail_apInvoice_vendor_comp_name,
            inv.apInvoice_vendor_comp_doing_business_as              AS apPaymentDetail_apInvoice_vendor_comp_doing_business_as,
            inv.apInvoice_vendor_comp_client_code                    AS apPaymentDetail_apInvoice_vendor_comp_client_code,
            inv.apInvoice_vendor_comp_client_id                      AS apPaymentDetail_apInvoice_vendor_comp_client_id,
            inv.apInvoice_vendor_comp_dataset_code                   AS apPaymentDetail_apInvoice_vendor_comp_dataset_code,
            inv.apInvoice_vendor_comp_federal_id_number              AS apPaymentDetail_apInvoice_vendor_comp_federal_id_number,
            inv.apInvoice_vendor_comp_address_id                     AS apPaymentDetail_apInvoice_vendor_comp_address_id,
            inv.apInvoice_vendor_comp_address_name                   AS apPaymentDetail_apInvoice_vendor_comp_address_name,
            inv.apInvoice_vendor_comp_address_address1               AS apPaymentDetail_apInvoice_vendor_comp_address_address1,
            inv.apInvoice_vendor_comp_address_address2               AS apPaymentDetail_apInvoice_vendor_comp_address_address2,
            inv.apInvoice_vendor_comp_address_city                   AS apPaymentDetail_apInvoice_vendor_comp_address_city,
            inv.apInvoice_vendor_comp_address_state                  AS apPaymentDetail_apInvoice_vendor_comp_address_state,
            inv.apInvoice_vendor_comp_address_postal_code            AS apPaymentDetail_apInvoice_vendor_comp_address_postal_code,
            inv.apInvoice_vendor_comp_address_latitude               AS apPaymentDetail_apInvoice_vendor_comp_address_latitude,
            inv.apInvoice_vendor_comp_address_longitude              AS apPaymentDetail_apInvoice_vendor_comp_address_longitude,
            inv.apInvoice_vendor_comp_address_country                AS apPaymentDetail_apInvoice_vendor_comp_address_country,
            inv.apInvoice_vendor_comp_address_county                 AS apPaymentDetail_apInvoice_vendor_comp_address_county,
            inv.apInvoice_vendor_comp_address_phone                  AS apPaymentDetail_apInvoice_vendor_comp_address_phone,
            inv.apInvoice_vendor_comp_address_fax                    AS apPaymentDetail_apInvoice_vendor_comp_address_fax,
            inv.apInvoice_vendor_onboard_id                          AS apPaymentDetail_apInvoice_vendor_onboard_id,
            inv.apInvoice_vendor_onboard_value                       AS apPaymentDetail_apInvoice_vendor_onboard_value,
            inv.apInvoice_vendor_onboard_description                 AS apPaymentDetail_apInvoice_vendor_onboard_description,
            inv.apInvoice_vendor_onboard_localization_code           AS apPaymentDetail_apInvoice_vendor_onboard_localization_code,
            inv.apInvoice_vendor_method_id                           AS apPaymentDetail_apInvoice_vendor_method_id,
            inv.apInvoice_vendor_method_value                        AS apPaymentDetail_apInvoice_vendor_method_value,
            inv.apInvoice_vendor_method_description                  AS apPaymentDetail_apInvoice_vendor_method_description,
            inv.apInvoice_vendor_method_localization_code            AS apPaymentDetail_apInvoice_vendor_method_localization_code,
            inv.apInvoice_vendor_address_id                          AS apPaymentDetail_apInvoice_vendor_address_id,
            inv.apInvoice_vendor_address_number                      AS apPaymentDetail_apInvoice_vendor_address_number,
            inv.apInvoice_vendor_address_name                        AS apPaymentDetail_apInvoice_vendor_address_name,
            inv.apInvoice_vendor_address_address1                    AS apPaymentDetail_apInvoice_vendor_address_address1,
            inv.apInvoice_vendor_address_address2                    AS apPaymentDetail_apInvoice_vendor_address_address2,
            inv.apInvoice_vendor_address_city                        AS apPaymentDetail_apInvoice_vendor_address_city,
            inv.apInvoice_vendor_address_state                       AS apPaymentDetail_apInvoice_vendor_address_state,
            inv.apInvoice_vendor_address_postal_code                 AS apPaymentDetail_apInvoice_vendor_address_postal_code,
            inv.apInvoice_vendor_address_latitude                    AS apPaymentDetail_apInvoice_vendor_address_latitude,
            inv.apInvoice_vendor_address_longitude                   AS apPaymentDetail_apInvoice_vendor_address_longitude,
            inv.apInvoice_vendor_address_country                     AS apPaymentDetail_apInvoice_vendor_address_country,
            inv.apInvoice_vendor_address_county                      AS apPaymentDetail_apInvoice_vendor_address_county,
            inv.apInvoice_vendor_address_phone                       AS apPaymentDetail_apInvoice_vendor_address_phone,
            inv.apInvoice_vendor_address_fax                         AS apPaymentDetail_apInvoice_vendor_address_fax,
            inv.apInvoice_vendor_vpt_id                              AS apPaymentDetail_apInvoice_vendor_vpt_id,
            inv.apInvoice_vendor_vpt_time_created                    AS apPaymentDetail_apInvoice_vendor_vpt_time_created,
            inv.apInvoice_vendor_vpt_time_updated                    AS apPaymentDetail_apInvoice_vendor_vpt_time_updated,
            inv.apInvoice_vendor_vpt_company_id                      AS apPaymentDetail_apInvoice_vendor_vpt_company_id,
            inv.apInvoice_vendor_vpt_description                     AS apPaymentDetail_apInvoice_vendor_vpt_description,
            inv.apInvoice_vendor_vpt_number                          AS apPaymentDetail_apInvoice_vendor_vpt_number,
            inv.apInvoice_vendor_vpt_number_of_payments              AS apPaymentDetail_apInvoice_vendor_vpt_number_of_payments,
            inv.apInvoice_vendor_vpt_discount_month                  AS apPaymentDetail_apInvoice_vendor_vpt_discount_month,
            inv.apInvoice_vendor_vpt_discount_days                   AS apPaymentDetail_apInvoice_vendor_vpt_discount_days,
            inv.apInvoice_vendor_vpt_discount_percent                AS apPaymentDetail_apInvoice_vendor_vpt_discount_percent,
            inv.apInvoice_vendor_shipVia_id                          AS apPaymentDetail_apInvoice_vendor_shipVia_id,
            inv.apInvoice_vendor_shipVia_time_created                AS apPaymentDetail_apInvoice_vendor_shipVia_time_created,
            inv.apInvoice_vendor_shipVia_time_updated                AS apPaymentDetail_apInvoice_vendor_shipVia_time_updated,
            inv.apInvoice_vendor_shipVia_description                 AS apPaymentDetail_apInvoice_vendor_shipVia_description,
            inv.apInvoice_vendor_shipVia_number                      AS apPaymentDetail_apInvoice_vendor_shipVia_number,
            inv.apInvoice_vendor_vgrp_id                             AS apPaymentDetail_apInvoice_vendor_vgrp_id,
            inv.apInvoice_vendor_vgrp_time_created                   AS apPaymentDetail_apInvoice_vendor_vgrp_time_created,
            inv.apInvoice_vendor_vgrp_time_updated                   AS apPaymentDetail_apInvoice_vendor_vgrp_time_updated,
            inv.apInvoice_vendor_vgrp_company_id                     AS apPaymentDetail_apInvoice_vendor_vgrp_company_id,
            inv.apInvoice_vendor_vgrp_value                          AS apPaymentDetail_apInvoice_vendor_vgrp_value,
            inv.apInvoice_vendor_vgrp_description                    AS apPaymentDetail_apInvoice_vendor_vgrp_description,
            inv.apInvoice_employee_id                                AS apPaymentDetail_apInvoice_employee_id,
            inv.apInvoice_employee_number                            AS apPaymentDetail_apInvoice_employee_number,
            inv.apInvoice_employee_last_name                         AS apPaymentDetail_apInvoice_employee_last_name,
            inv.apInvoice_employee_first_name_mi                     AS apPaymentDetail_apInvoice_employee_first_name_mi,
            inv.apInvoice_employee_type                              AS apPaymentDetail_apInvoice_employee_type,
            inv.apInvoice_employee_pass_code                         AS apPaymentDetail_apInvoice_employee_pass_code,
            inv.apInvoice_employee_active                            AS apPaymentDetail_apInvoice_employee_active,
            inv.apInvoice_employee_cynergi_system_admin              AS apPaymentDetail_apInvoice_employee_cynergi_system_admin,
            inv.apInvoice_employee_alternative_store_indicator       AS apPaymentDetail_apInvoice_employee_alternative_store_indicator,
            inv.apInvoice_employee_alternative_area                  AS apPaymentDetail_apInvoice_employee_alternative_area,
            inv.apInvoice_employee_store_id                          AS apPaymentDetail_apInvoice_employee_store_id,
            inv.apInvoice_employee_store_number                      AS apPaymentDetail_apInvoice_employee_store_number,
            inv.apInvoice_employee_store_name                        AS apPaymentDetail_apInvoice_employee_store_name,
            inv.apInvoice_employee_comp_id                           AS apPaymentDetail_apInvoice_employee_comp_id,
            inv.apInvoice_employee_comp_time_created                 AS apPaymentDetail_apInvoice_employee_comp_time_created,
            inv.apInvoice_employee_comp_time_updated                 AS apPaymentDetail_apInvoice_employee_comp_time_updated,
            inv.apInvoice_employee_comp_name                         AS apPaymentDetail_apInvoice_employee_comp_name,
            inv.apInvoice_employee_comp_doing_business_as            AS apPaymentDetail_apInvoice_employee_comp_doing_business_as,
            inv.apInvoice_employee_comp_client_code                  AS apPaymentDetail_apInvoice_employee_comp_client_code,
            inv.apInvoice_employee_comp_client_id                    AS apPaymentDetail_apInvoice_employee_comp_client_id,
            inv.apInvoice_employee_comp_dataset_code                 AS apPaymentDetail_apInvoice_employee_comp_dataset_code,
            inv.apInvoice_employee_comp_federal_id_number            AS apPaymentDetail_apInvoice_employee_comp_federal_id_number,
            inv.apInvoice_employee_comp_address_id                   AS apPaymentDetail_apInvoice_employee_comp_address_id,
            inv.apInvoice_employee_comp_address_name                 AS apPaymentDetail_apInvoice_employee_comp_address_name,
            inv.apInvoice_employee_comp_address_address1             AS apPaymentDetail_apInvoice_employee_comp_address_address1,
            inv.apInvoice_employee_comp_address_address2             AS apPaymentDetail_apInvoice_employee_comp_address_address2,
            inv.apInvoice_employee_comp_address_city                 AS apPaymentDetail_apInvoice_employee_comp_address_city,
            inv.apInvoice_employee_comp_address_state                AS apPaymentDetail_apInvoice_employee_comp_address_state,
            inv.apInvoice_employee_comp_address_postal_code          AS apPaymentDetail_apInvoice_employee_comp_address_postal_code,
            inv.apInvoice_employee_comp_address_latitude             AS apPaymentDetail_apInvoice_employee_comp_address_latitude,
            inv.apInvoice_employee_comp_address_longitude            AS apPaymentDetail_apInvoice_employee_comp_address_longitude,
            inv.apInvoice_employee_comp_address_country              AS apPaymentDetail_apInvoice_employee_comp_address_country,
            inv.apInvoice_employee_comp_address_county               AS apPaymentDetail_apInvoice_employee_comp_address_county,
            inv.apInvoice_employee_comp_address_phone                AS apPaymentDetail_apInvoice_employee_comp_address_phone,
            inv.apInvoice_employee_comp_address_fax                  AS apPaymentDetail_apInvoice_employee_comp_address_fax,
            inv.apInvoice_employee_dept_id                           AS apPaymentDetail_apInvoice_employee_dept_id,
            inv.apInvoice_employee_dept_code                         AS apPaymentDetail_apInvoice_employee_dept_code,
            inv.apInvoice_employee_dept_description                  AS apPaymentDetail_apInvoice_employee_dept_description,
            inv.apInvoice_selected_id                                AS apPaymentDetail_apInvoice_selected_id,
            inv.apInvoice_selected_value                             AS apPaymentDetail_apInvoice_selected_value,
            inv.apInvoice_selected_description                       AS apPaymentDetail_apInvoice_selected_description,
            inv.apInvoice_selected_localization_code                 AS apPaymentDetail_apInvoice_selected_localization_code,
            inv.apInvoice_type_id                                    AS apPaymentDetail_apInvoice_type_id,
            inv.apInvoice_type_value                                 AS apPaymentDetail_apInvoice_type_value,
            inv.apInvoice_type_description                           AS apPaymentDetail_apInvoice_type_description,
            inv.apInvoice_type_localization_code                     AS apPaymentDetail_apInvoice_type_localization_code,
            inv.apInvoice_status_id                                  AS apPaymentDetail_apInvoice_status_id,
            inv.apInvoice_status_value                               AS apPaymentDetail_apInvoice_status_value,
            inv.apInvoice_status_description                         AS apPaymentDetail_apInvoice_status_description,
            inv.apInvoice_status_localization_code                   AS apPaymentDetail_apInvoice_status_localization_code,
            vdr.v_id                                                 AS apPaymentDetail_vendor_id,
            vdr.v_company_id                                         AS apPaymentDetail_vendor_company_id,
            vdr.v_number                                             AS apPaymentDetail_vendor_number,
            vdr.v_name                                               AS apPaymentDetail_vendor_name,
            vdr.v_address_id                                         AS apPaymentDetail_vendor_address_id,
            vdr.v_account_number                                     AS apPaymentDetail_vendor_account_number,
            vdr.v_pay_to_id                                          AS apPaymentDetail_vendor_pay_to_id,
            vdr.v_freight_on_board_type_id                           AS apPaymentDetail_vendor_freight_on_board_type_id,
            vdr.v_vendor_payment_term_id                             AS apPaymentDetail_vendor_vendor_payment_term_id,
            vdr.v_normal_days                                        AS apPaymentDetail_vendor_normal_days,
            vdr.v_return_policy                                      AS apPaymentDetail_vendor_return_policy,
            vdr.v_ship_via_id                                        AS apPaymentDetail_vendor_ship_via_id,
            vdr.v_vendor_group_id                                    AS apPaymentDetail_vendor_vendor_group_id,
            vdr.v_minimum_quantity                                   AS apPaymentDetail_vendor_minimum_quantity,
            vdr.v_minimum_amount                                     AS apPaymentDetail_vendor_minimum_amount,
            vdr.v_free_ship_quantity                                 AS apPaymentDetail_vendor_free_ship_quantity,
            vdr.v_free_ship_amount                                   AS apPaymentDetail_vendor_free_ship_amount,
            vdr.v_vendor_1099                                        AS apPaymentDetail_vendor_vendor_1099,
            vdr.v_federal_id_number                                  AS apPaymentDetail_vendor_federal_id_number,
            vdr.v_sales_representative_name                          AS apPaymentDetail_vendor_sales_representative_name,
            vdr.v_sales_representative_fax                           AS apPaymentDetail_vendor_sales_representative_fax,
            vdr.v_separate_check                                     AS apPaymentDetail_vendor_separate_check,
            vdr.v_bump_percent                                       AS apPaymentDetail_vendor_bump_percent,
            vdr.v_freight_calc_method_type_id                        AS apPaymentDetail_vendor_freight_calc_method_type_id,
            vdr.v_freight_percent                                    AS apPaymentDetail_vendor_freight_percent,
            vdr.v_freight_amount                                     AS apPaymentDetail_vendor_freight_amount,
            vdr.v_charge_inventory_tax_1                             AS apPaymentDetail_vendor_charge_inventory_tax_1,
            vdr.v_charge_inventory_tax_2                             AS apPaymentDetail_vendor_charge_inventory_tax_2,
            vdr.v_charge_inventory_tax_3                             AS apPaymentDetail_vendor_charge_inventory_tax_3,
            vdr.v_charge_inventory_tax_4                             AS apPaymentDetail_vendor_charge_inventory_tax_4,
            vdr.v_federal_id_number_verification                     AS apPaymentDetail_vendor_federal_id_number_verification,
            vdr.v_email_address                                      AS apPaymentDetail_vendor_email_address,
            vdr.v_purchase_order_submit_email                        AS apPaymentDetail_vendor_purchase_order_submit_email,
            vdr.v_allow_drop_ship_to_customer                        AS apPaymentDetail_vendor_allow_drop_ship_to_customer,
            vdr.v_auto_submit_purchase_order                         AS apPaymentDetail_vendor_auto_submit_purchase_order,
            vdr.v_note                                               AS apPaymentDetail_vendor_note,
            vdr.v_phone_number                                       AS apPaymentDetail_vendor_phone_number,
            vdr.v_comp_id                                            AS apPaymentDetail_vendor_comp_id,
            vdr.v_comp_time_created                                  AS apPaymentDetail_vendor_comp_time_created,
            vdr.v_comp_time_updated                                  AS apPaymentDetail_vendor_comp_time_updated,
            vdr.v_comp_name                                          AS apPaymentDetail_vendor_comp_name,
            vdr.v_comp_doing_business_as                             AS apPaymentDetail_vendor_comp_doing_business_as,
            vdr.v_comp_client_code                                   AS apPaymentDetail_vendor_comp_client_code,
            vdr.v_comp_client_id                                     AS apPaymentDetail_vendor_comp_client_id,
            vdr.v_comp_dataset_code                                  AS apPaymentDetail_vendor_comp_dataset_code,
            vdr.v_comp_federal_id_number                             AS apPaymentDetail_vendor_comp_federal_id_number,
            vdr.v_comp_address_id                                    AS apPaymentDetail_vendor_comp_address_id,
            vdr.v_comp_address_name                                  AS apPaymentDetail_vendor_comp_address_name,
            vdr.v_comp_address_address1                              AS apPaymentDetail_vendor_comp_address_address1,
            vdr.v_comp_address_address2                              AS apPaymentDetail_vendor_comp_address_address2,
            vdr.v_comp_address_city                                  AS apPaymentDetail_vendor_comp_address_city,
            vdr.v_comp_address_state                                 AS apPaymentDetail_vendor_comp_address_state,
            vdr.v_comp_address_postal_code                           AS apPaymentDetail_vendor_comp_address_postal_code,
            vdr.v_comp_address_latitude                              AS apPaymentDetail_vendor_comp_address_latitude,
            vdr.v_comp_address_longitude                             AS apPaymentDetail_vendor_comp_address_longitude,
            vdr.v_comp_address_country                               AS apPaymentDetail_vendor_comp_address_country,
            vdr.v_comp_address_county                                AS apPaymentDetail_vendor_comp_address_county,
            vdr.v_comp_address_phone                                 AS apPaymentDetail_vendor_comp_address_phone,
            vdr.v_comp_address_fax                                   AS apPaymentDetail_vendor_comp_address_fax,
            vdr.v_onboard_id                                         AS apPaymentDetail_vendor_onboard_id,
            vdr.v_onboard_value                                      AS apPaymentDetail_vendor_onboard_value,
            vdr.v_onboard_description                                AS apPaymentDetail_vendor_onboard_description,
            vdr.v_onboard_localization_code                          AS apPaymentDetail_vendor_onboard_localization_code,
            vdr.v_method_id                                          AS apPaymentDetail_vendor_method_id,
            vdr.v_method_value                                       AS apPaymentDetail_vendor_method_value,
            vdr.v_method_description                                 AS apPaymentDetail_vendor_method_description,
            vdr.v_method_localization_code                           AS apPaymentDetail_vendor_method_localization_code,
            vdr.v_address_number                                     AS apPaymentDetail_vendor_address_number,
            vdr.v_address_name                                       AS apPaymentDetail_vendor_address_name,
            vdr.v_address_address1                                   AS apPaymentDetail_vendor_address_address1,
            vdr.v_address_address2                                   AS apPaymentDetail_vendor_address_address2,
            vdr.v_address_city                                       AS apPaymentDetail_vendor_address_city,
            vdr.v_address_state                                      AS apPaymentDetail_vendor_address_state,
            vdr.v_address_postal_code                                AS apPaymentDetail_vendor_address_postal_code,
            vdr.v_address_latitude                                   AS apPaymentDetail_vendor_address_latitude,
            vdr.v_address_longitude                                  AS apPaymentDetail_vendor_address_longitude,
            vdr.v_address_country                                    AS apPaymentDetail_vendor_address_country,
            vdr.v_address_county                                     AS apPaymentDetail_vendor_address_county,
            vdr.v_address_phone                                      AS apPaymentDetail_vendor_address_phone,
            vdr.v_address_fax                                        AS apPaymentDetail_vendor_address_fax,
            vdr.v_vpt_id                                             AS apPaymentDetail_vendor_vpt_id,
            vdr.v_vpt_time_created                                   AS apPaymentDetail_vendor_vpt_time_created,
            vdr.v_vpt_time_updated                                   AS apPaymentDetail_vendor_vpt_time_updated,
            vdr.v_vpt_company_id                                     AS apPaymentDetail_vendor_vpt_company_id,
            vdr.v_vpt_description                                    AS apPaymentDetail_vendor_vpt_description,
            vdr.v_vpt_number                                         AS apPaymentDetail_vendor_vpt_number,
            vdr.v_vpt_number_of_payments                             AS apPaymentDetail_vendor_vpt_number_of_payments,
            vdr.v_vpt_discount_month                                 AS apPaymentDetail_vendor_vpt_discount_month,
            vdr.v_vpt_discount_days                                  AS apPaymentDetail_vendor_vpt_discount_days,
            vdr.v_vpt_discount_percent                               AS apPaymentDetail_vendor_vpt_discount_percent,
            vdr.v_shipVia_id                                         AS apPaymentDetail_vendor_shipVia_id,
            vdr.v_shipVia_time_created                               AS apPaymentDetail_vendor_shipVia_time_created,
            vdr.v_shipVia_time_updated                               AS apPaymentDetail_vendor_shipVia_time_updated,
            vdr.v_shipVia_description                                AS apPaymentDetail_vendor_shipVia_description,
            vdr.v_shipVia_number                                     AS apPaymentDetail_vendor_shipVia_number,
            vdr.v_vgrp_id                                            AS apPaymentDetail_vendor_vgrp_id,
            vdr.v_vgrp_time_created                                  AS apPaymentDetail_vendor_vgrp_time_created,
            vdr.v_vgrp_time_updated                                  AS apPaymentDetail_vendor_vgrp_time_updated,
            vdr.v_vgrp_company_id                                    AS apPaymentDetail_vendor_vgrp_company_id,
            vdr.v_vgrp_value                                         AS apPaymentDetail_vendor_vgrp_value,
            vdr.v_vgrp_description                                   AS apPaymentDetail_vendor_vgrp_description,
            apPaymentDetail.payment_number_id                        AS apPaymentDetail_payment_number_id,
            apPaymentDetail.amount                                   AS apPaymentDetail_amount,
            apPaymentDetail.discount                                 AS apPaymentDetail_discount,
            count(*) OVER() AS total_elements
         FROM account_payable_payment_detail apPaymentDetail
            JOIN inv ON apPaymentDetail.account_payable_invoice_id = inv.apInvoice_id
            JOIN vdr ON apPaymentDetail.vendor_id = vdr.v_id
      """
   }

   fun findOne(id: UUID, company: Company): AccountPayablePaymentDetailEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${baseSelectQuery()} WHERE apPaymentDetail.id = :id AND apPaymentDetail.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
            mapRow(rs, company, "apPaymentDetail_")
         }

      logger.trace("Searching for Account Payable Payment Detail: {} resulted in {}", company, found)

      return found
   }

   @Transactional
   fun insert(entity: AccountPayablePaymentDetailEntity, company: Company): AccountPayablePaymentDetailEntity {
      logger.debug("Inserting Account Payable Payment Detail {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_payment_detail (
            company_id,
            account_payable_invoice_id,
            payment_number_id,
            vendor_id,
            amount,
            discount
         )
         VALUES (
            :company_id,
            :account_payable_invoice_id,
            :payment_number_id,
            :vendor_id,
            :amount,
            :discount
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "account_payable_invoice_id" to entity.invoice.id,
            "payment_number_id" to entity.payment!!.id,
            "vendor_id" to entity.vendor?.id,
            "amount" to entity.amount,
            "discount" to entity.discount
         )) { rs, _ ->
            mapRowUpsert(rs, entity)
         }
   }

   @Transactional
   fun update(entity: AccountPayablePaymentDetailEntity, company: Company): AccountPayablePaymentDetailEntity {
      logger.debug("Updating Account Payable Payment Detail {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE account_payable_payment_detail
         SET
            company_id = :company_id,
            account_payable_invoice_id = :account_payable_invoice_id,
            payment_number_id = :payment_number_id,
            vendor_id = :vendor_id,
            amount = :amount,
            discount = :discount
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "account_payable_invoice_id" to entity.invoice.id,
            "payment_number_id" to entity.payment?.id,
            "vendor_id" to entity.vendor?.id,
            "amount" to entity.amount,
            "discount" to entity.discount
         )) { rs, _ ->
            mapRowUpsert(rs, entity)
         }

   }

   @Transactional
   fun delete(id: UUID, company: Company) {
      logger.debug("Deleting Account Payable Payment Detail with id={}", id)

      val rowsAffected = jdbc.update(
         """
         DELETE FROM account_payable_payment_detail
         WHERE id = :id AND company_id = :company_id
         """,
         mapOf("id" to id, "company_id" to company.myId())
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapRow(
      rs: ResultSet,
      company: Company,
      columnPrefix: String = EMPTY
   ): AccountPayablePaymentDetailEntity {
      return AccountPayablePaymentDetailEntity(
         id = rs.getUuid("${columnPrefix}id"),
         vendor = vendorRepository.mapRowOrNull(rs, company, "${columnPrefix}vendor_"),
         invoice = apInvoiceRepository.mapRow(rs, company, "${columnPrefix}apInvoice_"),
         payment = null,
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         discount = rs.getBigDecimal("${columnPrefix}discount")
      )
   }

   fun mapRowOrNull(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): AccountPayablePaymentDetailEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   private fun mapRowUpsert(
      rs: ResultSet,
      entity: AccountPayablePaymentDetailEntity,
      columnPrefix: String = EMPTY
   ): AccountPayablePaymentDetailEntity {
      return AccountPayablePaymentDetailEntity(
         id = rs.getUuid("${columnPrefix}id"),
         vendor = entity.vendor,
         invoice = entity.invoice,
         payment = null,
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         discount = rs.getBigDecimal("${columnPrefix}discount")
      )
   }
}
