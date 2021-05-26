package com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure

import com.cynergisuite.domain.PaymentReportFilterRequest
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentEntity
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AccountPayablePaymentRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val bankRepository: BankRepository,
   private val vendorRepository: VendorRepository,
   private val apPaymentStatusRepository: AccountPayablePaymentStatusTypeRepository,
   private val apPaymentTypeRepository: AccountPayablePaymentTypeTypeRepository,
   private val apPaymentDetailRepository: AccountPayablePaymentDetailRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayablePaymentRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH bnk AS (
            ${ bankRepository.selectBaseQuery() }
         ),
         vend AS (
            ${ vendorRepository.baseSelectQuery() }
         ),
         paymentDetail AS (
            ${ apPaymentDetailRepository.baseSelectQuery() }
         )
         SELECT
            apPayment.id                                              AS apPayment_id,
            apPayment.company_id                                      AS apPayment_company_id,
            bnk.bank_id                                               AS apPayment_bank_id,
            bnk.bank_name                                             AS apPayment_bank_name,
            bnk.bank_number                                           AS apPayment_bank_number,
            bnk.comp_id                                               AS apPayment_bank_comp_id,
            bnk.bank_account_id                                       AS apPayment_bank_account_id,
            bnk.bank_account_number                                   AS apPayment_bank_account_number,
            bnk.bank_account_name                                     AS apPayment_bank_account_name,
            bnk.bank_account_form_1099_field                          AS apPayment_bank_account_form_1099_field,
            bnk.bank_account_corporate_account_indicator              AS apPayment_bank_account_corporate_account_indicator,
            bnk.bank_account_comp_id                                  AS apPayment_bank_account_comp_id,
            bnk.bank_account_type_id                                  AS apPayment_bank_account_type_id,
            bnk.bank_account_type_value                               AS apPayment_bank_account_type_value,
            bnk.bank_account_type_description                         AS apPayment_bank_account_type_description,
            bnk.bank_account_type_localization_code                   AS apPayment_bank_account_type_localization_code,
            bnk.bank_account_balance_type_id                          AS apPayment_bank_account_balance_type_id,
            bnk.bank_account_balance_type_value                       AS apPayment_bank_account_balance_type_value,
            bnk.bank_account_balance_type_description                 AS apPayment_bank_account_balance_type_description,
            bnk.bank_account_balance_type_localization_code           AS apPayment_bank_account_balance_type_localization_code,
            bnk.bank_account_status_id                                AS apPayment_bank_account_status_id,
            bnk.bank_account_status_value                             AS apPayment_bank_account_status_value,
            bnk.bank_account_status_description                       AS apPayment_bank_account_status_description,
            bnk.bank_account_status_localization_code                 AS apPayment_bank_account_status_localization_code,
            bnk.bank_glProfitCenter_id                                AS apPayment_bank_glProfitCenter_id,
            bnk.bank_glProfitCenter_number                            AS apPayment_bank_glProfitCenter_number,
            bnk.bank_glProfitCenter_name                              AS apPayment_bank_glProfitCenter_name,
            bnk.bank_glProfitCenter_dataset                           AS apPayment_bank_glProfitCenter_dataset,
            vend.v_id                                                 AS apPayment_vendor_id,
            vend.v_company_id                                         AS apPayment_vendor_company_id,
            vend.v_number                                             AS apPayment_vendor_number,
            vend.v_name                                               AS apPayment_vendor_name,
            vend.v_address_id                                         AS apPayment_vendor_address_id,
            vend.v_account_number                                     AS apPayment_vendor_account_number,
            vend.v_pay_to_id                                          AS apPayment_vendor_pay_to_id,
            vend.v_freight_on_board_type_id                           AS apPayment_vendor_freight_on_board_type_id,
            vend.v_vendor_payment_term_id                            AS apPayment_vendor_vendor_payment_term_id,
            vend.v_normal_days                                        AS apPayment_vendor_normal_days,
            vend.v_return_policy                                      AS apPayment_vendor_return_policy,
            vend.v_ship_via_id                                        AS apPayment_vendor_ship_via_id,
            vend.v_vendor_group_id                                    AS apPayment_vendor_vendor_group_id,
            vend.v_minimum_quantity                                   AS apPayment_vendor_minimum_quantity,
            vend.v_minimum_amount                                     AS apPayment_vendor_minimum_amount,
            vend.v_free_ship_quantity                                 AS apPayment_vendor_free_ship_quantity,
            vend.v_free_ship_amount                                   AS apPayment_vendor_free_ship_amount,
            vend.v_vendor_1099                                        AS apPayment_vendor_vendor_1099,
            vend.v_federal_id_number                                  AS apPayment_vendor_federal_id_number,
            vend.v_sales_representative_name                          AS apPayment_vendor_sales_representative_name,
            vend.v_sales_representative_fax                           AS apPayment_vendor_sales_representative_fax,
            vend.v_separate_check                                     AS apPayment_vendor_separate_check,
            vend.v_bump_percent                                       AS apPayment_vendor_bump_percent,
            vend.v_freight_calc_method_type_id                        AS apPayment_vendor_freight_calc_method_type_id,
            vend.v_freight_percent                                    AS apPayment_vendor_freight_percent,
            vend.v_freight_amount                                     AS apPayment_vendor_freight_amount,
            vend.v_charge_inventory_tax_1                             AS apPayment_vendor_charge_inventory_tax_1,
            vend.v_charge_inventory_tax_2                             AS apPayment_vendor_charge_inventory_tax_2,
            vend.v_charge_inventory_tax_3                             AS apPayment_vendor_charge_inventory_tax_3,
            vend.v_charge_inventory_tax_4                             AS apPayment_vendor_charge_inventory_tax_4,
            vend.v_federal_id_number_verification                     AS apPayment_vendor_federal_id_number_verification,
            vend.v_email_address                                      AS apPayment_vendor_email_address,
            vend.v_purchase_order_submit_email                        AS apPayment_vendor_purchase_order_submit_email,
            vend.v_allow_drop_ship_to_customer                        AS apPayment_vendor_allow_drop_ship_to_customer,
            vend.v_auto_submit_purchase_order                         AS apPayment_vendor_auto_submit_purchase_order,
            vend.v_note                                               AS apPayment_vendor_note,
            vend.v_phone_number                                       AS apPayment_vendor_phone_number,
            vend.v_comp_id                                            AS apPayment_vendor_comp_id,
            vend.v_comp_time_created                                  AS apPayment_vendor_comp_time_created,
            vend.v_comp_time_updated                                  AS apPayment_vendor_comp_time_updated,
            vend.v_comp_name                                          AS apPayment_vendor_comp_name,
            vend.v_comp_doing_business_as                             AS apPayment_vendor_comp_doing_business_as,
            vend.v_comp_client_code                                   AS apPayment_vendor_comp_client_code,
            vend.v_comp_client_id                                     AS apPayment_vendor_comp_client_id,
            vend.v_comp_dataset_code                                  AS apPayment_vendor_comp_dataset_code,
            vend.v_comp_federal_id_number                             AS apPayment_vendor_comp_federal_id_number,
            vend.v_comp_address_id                                    AS apPayment_vendor_comp_address_id,
            vend.v_comp_address_name                                  AS apPayment_vendor_comp_address_name,
            vend.v_comp_address_address1                              AS apPayment_vendor_comp_address_address1,
            vend.v_comp_address_address2                              AS apPayment_vendor_comp_address_address2,
            vend.v_comp_address_city                                  AS apPayment_vendor_comp_address_city,
            vend.v_comp_address_state                                 AS apPayment_vendor_comp_address_state,
            vend.v_comp_address_postal_code                           AS apPayment_vendor_comp_address_postal_code,
            vend.v_comp_address_latitude                              AS apPayment_vendor_comp_address_latitude,
            vend.v_comp_address_longitude                             AS apPayment_vendor_comp_address_longitude,
            vend.v_comp_address_country                               AS apPayment_vendor_comp_address_country,
            vend.v_comp_address_county                                AS apPayment_vendor_comp_address_county,
            vend.v_comp_address_phone                                 AS apPayment_vendor_comp_address_phone,
            vend.v_comp_address_fax                                   AS apPayment_vendor_comp_address_fax,
            vend.v_onboard_id                                         AS apPayment_vendor_onboard_id,
            vend.v_onboard_value                                      AS apPayment_vendor_onboard_value,
            vend.v_onboard_description                                AS apPayment_vendor_onboard_description,
            vend.v_onboard_localization_code                          AS apPayment_vendor_onboard_localization_code,
            vend.v_method_id                                          AS apPayment_vendor_method_id,
            vend.v_method_value                                       AS apPayment_vendor_method_value,
            vend.v_method_description                                 AS apPayment_vendor_method_description,
            vend.v_method_localization_code                           AS apPayment_vendor_method_localization_code,
            vend.v_address_id                                         AS apPayment_vendor_address_id,
            vend.v_address_time_created                               AS apPayment_vendor_address_time_created,
            vend.v_address_time_updated                               AS apPayment_vendor_address_time_updated,
            vend.v_address_number                                     AS apPayment_vendor_address_number,
            vend.v_address_name                                       AS apPayment_vendor_address_name,
            vend.v_address_address1                                   AS apPayment_vendor_address_address1,
            vend.v_address_address2                                   AS apPayment_vendor_address_address2,
            vend.v_address_city                                       AS apPayment_vendor_address_city,
            vend.v_address_state                                      AS apPayment_vendor_address_state,
            vend.v_address_postal_code                                AS apPayment_vendor_address_postal_code,
            vend.v_address_latitude                                   AS apPayment_vendor_address_latitude,
            vend.v_address_longitude                                  AS apPayment_vendor_address_longitude,
            vend.v_address_country                                    AS apPayment_vendor_address_country,
            vend.v_address_county                                     AS apPayment_vendor_address_county,
            vend.v_address_phone                                      AS apPayment_vendor_address_phone,
            vend.v_address_fax                                        AS apPayment_vendor_address_fax,
            vend.v_vpt_id                                             AS apPayment_vendor_vpt_id,
            vend.v_vpt_time_created                                   AS apPayment_vendor_vpt_time_created,
            vend.v_vpt_time_updated                                   AS apPayment_vendor_vpt_time_updated,
            vend.v_vpt_company_id                                     AS apPayment_vendor_vpt_company_id,
            vend.v_vpt_description                                    AS apPayment_vendor_vpt_description,
            vend.v_vpt_number                                         AS apPayment_vendor_vpt_number,
            vend.v_vpt_number_of_payments                             AS apPayment_vendor_vpt_number_of_payments,
            vend.v_vpt_discount_month                                 AS apPayment_vendor_vpt_discount_month,
            vend.v_vpt_discount_days                                  AS apPayment_vendor_vpt_discount_days,
            vend.v_vpt_discount_percent                               AS apPayment_vendor_vpt_discount_percent,
            vend.v_shipVia_id                                         AS apPayment_vendor_shipVia_id,
            vend.v_shipVia_time_created                               AS apPayment_vendor_shipVia_time_created,
            vend.v_shipVia_time_updated                               AS apPayment_vendor_shipVia_time_updated,
            vend.v_shipVia_description                                AS apPayment_vendor_shipVia_description,
            vend.v_shipVia_number                                     AS apPayment_vendor_shipVia_number,
            vend.v_vgrp_id                                            AS apPayment_vendor_vgrp_id,
            vend.v_vgrp_time_created                                  AS apPayment_vendor_vgrp_time_created,
            vend.v_vgrp_time_updated                                  AS apPayment_vendor_vgrp_time_updated,
            vend.v_vgrp_company_id                                    AS apPayment_vendor_vgrp_company_id,
            vend.v_vgrp_value                                         AS apPayment_vendor_vgrp_value,
            vend.v_vgrp_description                                   AS apPayment_vendor_vgrp_description,
            status.id                                                 AS apPayment_status_id,
            status.value                                              AS apPayment_status_value,
            status.description                                        AS apPayment_status_description,
            status.localization_code                                  AS apPayment_status_localization_code,
            type.id                                                   AS apPayment_type_id,
            type.value                                                AS apPayment_type_value,
            type.description                                          AS apPayment_type_description,
            type.localization_code                                    AS apPayment_type_localization_code,
            apPayment.payment_number                                  AS apPayment_payment_number,
            apPayment.payment_date                                    AS apPayment_payment_date,
            apPayment.date_cleared                                    AS apPayment_date_cleared,
            apPayment.date_voided                                     AS apPayment_date_voided,
            apPayment.amount                                                                          AS apPayment_amount,
            paymentDetail.apPaymentDetail_id                                                          AS apPaymentDetail_id,
            paymentDetail.apPaymentDetail_company_id                                                  AS apPaymentDetail_company_id,
            paymentDetail.apPaymentDetail_apInvoice_id                                                AS apPaymentDetail_apInvoice_id,
            paymentDetail.apPaymentDetail_apInvoice_company_id                                        AS apPaymentDetail_apInvoice_company_id,
            paymentDetail.apPaymentDetail_apInvoice_invoice                                           AS apPaymentDetail_apInvoice_invoice,
            paymentDetail.apPaymentDetail_apInvoice_purchase_order_id                                 AS apPaymentDetail_apInvoice_purchase_order_id,
            paymentDetail.apPaymentDetail_apInvoice_invoice_date                                      AS apPaymentDetail_apInvoice_invoice_date,
            paymentDetail.apPaymentDetail_apInvoice_invoice_amount                                    AS apPaymentDetail_apInvoice_invoice_amount,
            paymentDetail.apPaymentDetail_apInvoice_discount_amount                                   AS apPaymentDetail_apInvoice_discount_amount,
            paymentDetail.apPaymentDetail_apInvoice_discount_percent                                  AS apPaymentDetail_apInvoice_discount_percent,
            paymentDetail.apPaymentDetail_apInvoice_auto_distribution_applied                         AS apPaymentDetail_apInvoice_auto_distribution_applied,
            paymentDetail.apPaymentDetail_apInvoice_discount_taken                                    AS apPaymentDetail_apInvoice_discount_taken,
            paymentDetail.apPaymentDetail_apInvoice_entry_date                                        AS apPaymentDetail_apInvoice_entry_date,
            paymentDetail.apPaymentDetail_apInvoice_expense_date                                      AS apPaymentDetail_apInvoice_expense_date,
            paymentDetail.apPaymentDetail_apInvoice_discount_date                                     AS apPaymentDetail_apInvoice_discount_date,
            paymentDetail.apPaymentDetail_apInvoice_employee_number_id_sfk                            AS apPaymentDetail_apInvoice_employee_number_id_sfk,
            paymentDetail.apPaymentDetail_apInvoice_original_invoice_amount                           AS apPaymentDetail_apInvoice_original_invoice_amount,
            paymentDetail.apPaymentDetail_apInvoice_message                                           AS apPaymentDetail_apInvoice_message,
            paymentDetail.apPaymentDetail_apInvoice_multiple_payment_indicator                        AS apPaymentDetail_apInvoice_multiple_payment_indicator,
            paymentDetail.apPaymentDetail_apInvoice_paid_amount                                       AS apPaymentDetail_apInvoice_paid_amount,
            paymentDetail.apPaymentDetail_apInvoice_selected_amount                                   AS apPaymentDetail_apInvoice_selected_amount,
            paymentDetail.apPaymentDetail_apInvoice_due_date                                          AS apPaymentDetail_apInvoice_due_date,
            paymentDetail.apPaymentDetail_apInvoice_pay_to_id                                         AS apPaymentDetail_apInvoice_pay_to_id,
            paymentDetail.apPaymentDetail_apInvoice_separate_check_indicator                          AS apPaymentDetail_apInvoice_separate_check_indicator,
            paymentDetail.apPaymentDetail_apInvoice_use_tax_indicator                                 AS apPaymentDetail_apInvoice_use_tax_indicator,
            paymentDetail.apPaymentDetail_apInvoice_receive_date                                      AS apPaymentDetail_apInvoice_receive_date,
            paymentDetail.apPaymentDetail_apInvoice_location_id_sfk                                   AS apPaymentDetail_apInvoice_location_id_sfk,
            paymentDetail.apPaymentDetail_apInvoice_vendor_id                                         AS apPaymentDetail_apInvoice_vendor_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_company_id                                 AS apPaymentDetail_apInvoice_vendor_company_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_number                                     AS apPaymentDetail_apInvoice_vendor_number,
            paymentDetail.apPaymentDetail_apInvoice_vendor_name                                       AS apPaymentDetail_apInvoice_vendor_name,
            paymentDetail.apPaymentDetail_apInvoice_vendor_account_number                             AS apPaymentDetail_apInvoice_vendor_account_number,
            paymentDetail.apPaymentDetail_apInvoice_vendor_pay_to_id                                  AS apPaymentDetail_apInvoice_vendor_pay_to_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_freight_on_board_type_id                   AS apPaymentDetail_apInvoice_vendor_freight_on_board_type_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vendor_payment_term_id                     AS apPaymentDetail_apInvoice_vendor_vendor_payment_term_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_normal_days                                AS apPaymentDetail_apInvoice_vendor_normal_days,
            paymentDetail.apPaymentDetail_apInvoice_vendor_return_policy                              AS apPaymentDetail_apInvoice_vendor_return_policy,
            paymentDetail.apPaymentDetail_apInvoice_vendor_ship_via_id                                AS apPaymentDetail_apInvoice_vendor_ship_via_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_group_id                                   AS apPaymentDetail_apInvoice_vendor_group_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_minimum_quantity                           AS apPaymentDetail_apInvoice_vendor_minimum_quantity,
            paymentDetail.apPaymentDetail_apInvoice_vendor_minimum_amount                             AS apPaymentDetail_apInvoice_vendor_minimum_amount,
            paymentDetail.apPaymentDetail_apInvoice_vendor_free_ship_quantity                         AS apPaymentDetail_apInvoice_vendor_free_ship_quantity,
            paymentDetail.apPaymentDetail_apInvoice_vendor_free_ship_amount                           AS apPaymentDetail_apInvoice_vendor_free_ship_amount,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vendor_1099                                AS apPaymentDetail_apInvoice_vendor_vendor_1099,
            paymentDetail.apPaymentDetail_apInvoice_vendor_federal_id_number                          AS apPaymentDetail_apInvoice_vendor_federal_id_number,
            paymentDetail.apPaymentDetail_apInvoice_vendor_sales_representative_name                  AS apPaymentDetail_apInvoice_vendor_sales_representative_name,
            paymentDetail.apPaymentDetail_apInvoice_vendor_sales_representative_fax                   AS apPaymentDetail_apInvoice_vendor_sales_representative_fax,
            paymentDetail.apPaymentDetail_apInvoice_vendor_separate_check                             AS apPaymentDetail_apInvoice_vendor_separate_check,
            paymentDetail.apPaymentDetail_apInvoice_vendor_bump_percent                               AS apPaymentDetail_apInvoice_vendor_bump_percent,
            paymentDetail.apPaymentDetail_apInvoice_vendor_freight_calc_method_type_id                AS apPaymentDetail_apInvoice_vendor_freight_calc_method_type_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_freight_percent                            AS apPaymentDetail_apInvoice_vendor_freight_percent,
            paymentDetail.apPaymentDetail_apInvoice_vendor_freight_amount                             AS apPaymentDetail_apInvoice_vendor_freight_amount,
            paymentDetail.apPaymentDetail_apInvoice_vendor_charge_inventory_tax_1                     AS apPaymentDetail_apInvoice_vendor_charge_inventory_tax_1,
            paymentDetail.apPaymentDetail_apInvoice_vendor_charge_inventory_tax_2                     AS apPaymentDetail_apInvoice_vendor_charge_inventory_tax_2,
            paymentDetail.apPaymentDetail_apInvoice_vendor_charge_inventory_tax_3                     AS apPaymentDetail_apInvoice_vendor_charge_inventory_tax_3,
            paymentDetail.apPaymentDetail_apInvoice_vendor_charge_inventory_tax_4                     AS apPaymentDetail_apInvoice_vendor_charge_inventory_tax_4,
            paymentDetail.apPaymentDetail_apInvoice_vendor_federal_id_number_verification             AS apPaymentDetail_apInvoice_vendor_federal_id_number_verification,
            paymentDetail.apPaymentDetail_apInvoice_vendor_email_address                              AS apPaymentDetail_apInvoice_vendor_email_address,
            paymentDetail.apPaymentDetail_apInvoice_vendor_purchase_order_submit_email                AS apPaymentDetail_apInvoice_vendor_purchase_order_submit_email,
            paymentDetail.apPaymentDetail_apInvoice_vendor_allow_drop_ship_to_customer                AS apPaymentDetail_apInvoice_vendor_allow_drop_ship_to_customer,
            paymentDetail.apPaymentDetail_apInvoice_vendor_auto_submit_purchase_order                 AS apPaymentDetail_apInvoice_vendor_auto_submit_purchase_order,
            paymentDetail.apPaymentDetail_apInvoice_vendor_note                                       AS apPaymentDetail_apInvoice_vendor_note,
            paymentDetail.apPaymentDetail_apInvoice_vendor_phone_number                               AS apPaymentDetail_apInvoice_vendor_phone_number,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_id                                    AS apPaymentDetail_apInvoice_vendor_comp_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_time_created                          AS apPaymentDetail_apInvoice_vendor_comp_time_created,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_time_updated                          AS apPaymentDetail_apInvoice_vendor_comp_time_updated,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_name                                  AS apPaymentDetail_apInvoice_vendor_comp_name,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_doing_business_as                     AS apPaymentDetail_apInvoice_vendor_comp_doing_business_as,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_client_code                           AS apPaymentDetail_apInvoice_vendor_comp_client_code,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_client_id                             AS apPaymentDetail_apInvoice_vendor_comp_client_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_dataset_code                          AS apPaymentDetail_apInvoice_vendor_comp_dataset_code,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_federal_id_number                     AS apPaymentDetail_apInvoice_vendor_comp_federal_id_number,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_id                            AS apPaymentDetail_apInvoice_vendor_comp_address_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_name                          AS apPaymentDetail_apInvoice_vendor_comp_address_name,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_address1                      AS apPaymentDetail_apInvoice_vendor_comp_address_address1,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_address2                      AS apPaymentDetail_apInvoice_vendor_comp_address_address2,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_city                          AS apPaymentDetail_apInvoice_vendor_comp_address_city,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_state                         AS apPaymentDetail_apInvoice_vendor_comp_address_state,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_postal_code                   AS apPaymentDetail_apInvoice_vendor_comp_address_postal_code,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_latitude                      AS apPaymentDetail_apInvoice_vendor_comp_address_latitude,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_longitude                     AS apPaymentDetail_apInvoice_vendor_comp_address_longitude,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_country                       AS apPaymentDetail_apInvoice_vendor_comp_address_country,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_county                        AS apPaymentDetail_apInvoice_vendor_comp_address_county,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_phone                         AS apPaymentDetail_apInvoice_vendor_comp_address_phone,
            paymentDetail.apPaymentDetail_apInvoice_vendor_comp_address_fax                           AS apPaymentDetail_apInvoice_vendor_comp_address_fax,
            paymentDetail.apPaymentDetail_apInvoice_vendor_onboard_id                                 AS apPaymentDetail_apInvoice_vendor_onboard_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_onboard_value                              AS apPaymentDetail_apInvoice_vendor_onboard_value,
            paymentDetail.apPaymentDetail_apInvoice_vendor_onboard_description                        AS apPaymentDetail_apInvoice_vendor_onboard_description,
            paymentDetail.apPaymentDetail_apInvoice_vendor_onboard_localization_code                  AS apPaymentDetail_apInvoice_vendor_onboard_localization_code,
            paymentDetail.apPaymentDetail_apInvoice_vendor_method_id                                  AS apPaymentDetail_apInvoice_vendor_method_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_method_value                               AS apPaymentDetail_apInvoice_vendor_method_value,
            paymentDetail.apPaymentDetail_apInvoice_vendor_method_description                         AS apPaymentDetail_apInvoice_vendor_method_description,
            paymentDetail.apPaymentDetail_apInvoice_vendor_method_localization_code                   AS apPaymentDetail_apInvoice_vendor_method_localization_code,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_id                                 AS apPaymentDetail_apInvoice_vendor_address_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_number                             AS apPaymentDetail_apInvoice_vendor_address_number,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_name                               AS apPaymentDetail_apInvoice_vendor_address_name,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_address1                           AS apPaymentDetail_apInvoice_vendor_address_address1,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_address2                           AS apPaymentDetail_apInvoice_vendor_address_address2,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_city                               AS apPaymentDetail_apInvoice_vendor_address_city,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_state                              AS apPaymentDetail_apInvoice_vendor_address_state,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_postal_code                        AS apPaymentDetail_apInvoice_vendor_address_postal_code,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_latitude                           AS apPaymentDetail_apInvoice_vendor_address_latitude,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_longitude                          AS apPaymentDetail_apInvoice_vendor_address_longitude,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_country                            AS apPaymentDetail_apInvoice_vendor_address_country,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_county                             AS apPaymentDetail_apInvoice_vendor_address_county,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_phone                              AS apPaymentDetail_apInvoice_vendor_address_phone,
            paymentDetail.apPaymentDetail_apInvoice_vendor_address_fax                                AS apPaymentDetail_apInvoice_vendor_address_fax,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_id                                     AS apPaymentDetail_apInvoice_vendor_vpt_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_time_created                           AS apPaymentDetail_apInvoice_vendor_vpt_time_created,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_time_updated                           AS apPaymentDetail_apInvoice_vendor_vpt_time_updated,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_company_id                             AS apPaymentDetail_apInvoice_vendor_vpt_company_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_description                            AS apPaymentDetail_apInvoice_vendor_vpt_description,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_number                                 AS apPaymentDetail_apInvoice_vendor_vpt_number,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_number_of_payments                     AS apPaymentDetail_apInvoice_vendor_vpt_number_of_payments,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_discount_month                         AS apPaymentDetail_apInvoice_vendor_vpt_discount_month,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_discount_days                          AS apPaymentDetail_apInvoice_vendor_vpt_discount_days,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vpt_discount_percent                       AS apPaymentDetail_apInvoice_vendor_vpt_discount_percent,
            paymentDetail.apPaymentDetail_apInvoice_vendor_shipVia_id                                 AS apPaymentDetail_apInvoice_vendor_shipVia_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_shipVia_time_created                       AS apPaymentDetail_apInvoice_vendor_shipVia_time_created,
            paymentDetail.apPaymentDetail_apInvoice_vendor_shipVia_time_updated                       AS apPaymentDetail_apInvoice_vendor_shipVia_time_updated,
            paymentDetail.apPaymentDetail_apInvoice_vendor_shipVia_description                        AS apPaymentDetail_apInvoice_vendor_shipVia_description,
            paymentDetail.apPaymentDetail_apInvoice_vendor_shipVia_number                             AS apPaymentDetail_apInvoice_vendor_shipVia_number,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vgrp_id                                    AS apPaymentDetail_apInvoice_vendor_vgrp_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vgrp_time_created                          AS apPaymentDetail_apInvoice_vendor_vgrp_time_created,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vgrp_time_updated                          AS apPaymentDetail_apInvoice_vendor_vgrp_time_updated,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vgrp_company_id                            AS apPaymentDetail_apInvoice_vendor_vgrp_company_id,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vgrp_value                                 AS apPaymentDetail_apInvoice_vendor_vgrp_value,
            paymentDetail.apPaymentDetail_apInvoice_vendor_vgrp_description                           AS apPaymentDetail_apInvoice_vendor_vgrp_description,
            paymentDetail.apPaymentDetail_apInvoice_employee_id                                       AS apPaymentDetail_apInvoice_employee_id,
            paymentDetail.apPaymentDetail_apInvoice_employee_number                                   AS apPaymentDetail_apInvoice_employee_number,
            paymentDetail.apPaymentDetail_apInvoice_employee_last_name                                AS apPaymentDetail_apInvoice_employee_last_name,
            paymentDetail.apPaymentDetail_apInvoice_employee_first_name_mi                            AS apPaymentDetail_apInvoice_employee_first_name_mi,
            paymentDetail.apPaymentDetail_apInvoice_employee_type                                     AS apPaymentDetail_apInvoice_employee_type,
            paymentDetail.apPaymentDetail_apInvoice_employee_pass_code                                AS apPaymentDetail_apInvoice_employee_pass_code,
            paymentDetail.apPaymentDetail_apInvoice_employee_active                                   AS apPaymentDetail_apInvoice_employee_active,
            paymentDetail.apPaymentDetail_apInvoice_employee_cynergi_system_admin                     AS apPaymentDetail_apInvoice_employee_cynergi_system_admin,
            paymentDetail.apPaymentDetail_apInvoice_employee_alternative_store_indicator              AS apPaymentDetail_apInvoice_employee_alternative_store_indicator,
            paymentDetail.apPaymentDetail_apInvoice_employee_alternative_area                         AS apPaymentDetail_apInvoice_employee_alternative_area,
            paymentDetail.apPaymentDetail_apInvoice_employee_store_id                                 AS apPaymentDetail_apInvoice_employee_store_id,
            paymentDetail.apPaymentDetail_apInvoice_employee_store_number                             AS apPaymentDetail_apInvoice_employee_store_number,
            paymentDetail.apPaymentDetail_apInvoice_employee_store_name                               AS apPaymentDetail_apInvoice_employee_store_name,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_id                                  AS apPaymentDetail_apInvoice_employee_comp_id,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_time_created                        AS apPaymentDetail_apInvoice_employee_comp_time_created,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_time_updated                        AS apPaymentDetail_apInvoice_employee_comp_time_updated,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_name                                AS apPaymentDetail_apInvoice_employee_comp_name,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_doing_business_as                   AS apPaymentDetail_apInvoice_employee_comp_doing_business_as,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_client_code                         AS apPaymentDetail_apInvoice_employee_comp_client_code,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_client_id                           AS apPaymentDetail_apInvoice_employee_comp_client_id,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_dataset_code                        AS apPaymentDetail_apInvoice_employee_comp_dataset_code,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_federal_id_number                   AS apPaymentDetail_apInvoice_employee_comp_federal_id_number,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_id                          AS apPaymentDetail_apInvoice_employee_comp_address_id,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_name                        AS apPaymentDetail_apInvoice_employee_comp_address_name,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_address1                    AS apPaymentDetail_apInvoice_employee_comp_address_address1,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_address2                    AS apPaymentDetail_apInvoice_employee_comp_address_address2,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_city                        AS apPaymentDetail_apInvoice_employee_comp_address_city,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_state                       AS apPaymentDetail_apInvoice_employee_comp_address_state,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_postal_code                 AS apPaymentDetail_apInvoice_employee_comp_address_postal_code,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_latitude                    AS apPaymentDetail_apInvoice_employee_comp_address_latitude,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_longitude                   AS apPaymentDetail_apInvoice_employee_comp_address_longitude,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_country                     AS apPaymentDetail_apInvoice_employee_comp_address_country,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_county                      AS apPaymentDetail_apInvoice_employee_comp_address_county,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_phone                       AS apPaymentDetail_apInvoice_employee_comp_address_phone,
            paymentDetail.apPaymentDetail_apInvoice_employee_comp_address_fax                         AS apPaymentDetail_apInvoice_employee_comp_address_fax,
            paymentDetail.apPaymentDetail_apInvoice_employee_dept_id                                  AS apPaymentDetail_apInvoice_employee_dept_id,
            paymentDetail.apPaymentDetail_apInvoice_employee_dept_code                                AS apPaymentDetail_apInvoice_employee_dept_code,
            paymentDetail.apPaymentDetail_apInvoice_employee_dept_description                         AS apPaymentDetail_apInvoice_employee_dept_description,
            paymentDetail.apPaymentDetail_apInvoice_selected_id                                       AS apPaymentDetail_apInvoice_selected_id,
            paymentDetail.apPaymentDetail_apInvoice_selected_value                                    AS apPaymentDetail_apInvoice_selected_value,
            paymentDetail.apPaymentDetail_apInvoice_selected_description                              AS apPaymentDetail_apInvoice_selected_description,
            paymentDetail.apPaymentDetail_apInvoice_selected_localization_code                        AS apPaymentDetail_apInvoice_selected_localization_code,
            paymentDetail.apPaymentDetail_apInvoice_type_id                                           AS apPaymentDetail_apInvoice_type_id,
            paymentDetail.apPaymentDetail_apInvoice_type_value                                        AS apPaymentDetail_apInvoice_type_value,
            paymentDetail.apPaymentDetail_apInvoice_type_description                                  AS apPaymentDetail_apInvoice_type_description,
            paymentDetail.apPaymentDetail_apInvoice_type_localization_code                            AS apPaymentDetail_apInvoice_type_localization_code,
            paymentDetail.apPaymentDetail_apInvoice_status_id                                         AS apPaymentDetail_apInvoice_status_id,
            paymentDetail.apPaymentDetail_apInvoice_status_value                                      AS apPaymentDetail_apInvoice_status_value,
            paymentDetail.apPaymentDetail_apInvoice_status_description                                AS apPaymentDetail_apInvoice_status_description,
            paymentDetail.apPaymentDetail_apInvoice_status_localization_code                          AS apPaymentDetail_apInvoice_status_localization_code,
            paymentDetail.apPaymentDetail_vendor_id                                                   AS apPaymentDetail_vendor_id,
            paymentDetail.apPaymentDetail_vendor_number                                               AS apPaymentDetail_vendor_number,
            paymentDetail.apPaymentDetail_vendor_company_id                                           AS apPaymentDetail_vendor_company_id,
            paymentDetail.apPaymentDetail_vendor_company_id                                           AS apPaymentDetail_vendor_company_id,
            paymentDetail.apPaymentDetail_vendor_name                                                 AS apPaymentDetail_vendor_name,
            paymentDetail.apPaymentDetail_vendor_address_id                                           AS apPaymentDetail_vendor_address_id,
            paymentDetail.apPaymentDetail_vendor_account_number                                       AS apPaymentDetail_vendor_account_number,
            paymentDetail.apPaymentDetail_vendor_pay_to_id                                            AS apPaymentDetail_vendor_pay_to_id,
            paymentDetail.apPaymentDetail_vendor_freight_on_board_type_id                             AS apPaymentDetail_vendor_freight_on_board_type_id,
            paymentDetail.apPaymentDetail_vendor_vendor_payment_term_id                               AS apPaymentDetail_vendor_vendor_payment_term_id,
            paymentDetail.apPaymentDetail_vendor_normal_days                                          AS apPaymentDetail_vendor_normal_days,
            paymentDetail.apPaymentDetail_vendor_return_policy                                        AS apPaymentDetail_vendor_return_policy,
            paymentDetail.apPaymentDetail_vendor_ship_via_id                                          AS apPaymentDetail_vendor_ship_via_id,
            paymentDetail.apPaymentDetail_vendor_vendor_group_id                                      AS apPaymentDetail_vendor_vendor_group_id,
            paymentDetail.apPaymentDetail_vendor_minimum_quantity                                     AS apPaymentDetail_vendor_minimum_quantity,
            paymentDetail.apPaymentDetail_vendor_minimum_amount                                       AS apPaymentDetail_vendor_minimum_amount,
            paymentDetail.apPaymentDetail_vendor_free_ship_quantity                                   AS apPaymentDetail_vendor_free_ship_quantity,
            paymentDetail.apPaymentDetail_vendor_free_ship_amount                                     AS apPaymentDetail_vendor_free_ship_amount,
            paymentDetail.apPaymentDetail_vendor_vendor_1099                                          AS apPaymentDetail_vendor_vendor_1099,
            paymentDetail.apPaymentDetail_vendor_federal_id_number                                    AS apPaymentDetail_vendor_federal_id_number,
            paymentDetail.apPaymentDetail_vendor_sales_representative_name                            AS apPaymentDetail_vendor_sales_representative_name,
            paymentDetail.apPaymentDetail_vendor_sales_representative_fax                             AS apPaymentDetail_vendor_sales_representative_fax,
            paymentDetail.apPaymentDetail_vendor_separate_check                                       AS apPaymentDetail_vendor_separate_check,
            paymentDetail.apPaymentDetail_vendor_bump_percent                                         AS apPaymentDetail_vendor_bump_percent,
            paymentDetail.apPaymentDetail_vendor_freight_calc_method_type_id                          AS apPaymentDetail_vendor_freight_calc_method_type_id,
            paymentDetail.apPaymentDetail_vendor_freight_percent                                      AS apPaymentDetail_vendor_freight_percent,
            paymentDetail.apPaymentDetail_vendor_freight_amount                                       AS apPaymentDetail_vendor_freight_amount,
            paymentDetail.apPaymentDetail_vendor_charge_inventory_tax_1                               AS apPaymentDetail_vendor_charge_inventory_tax_1,
            paymentDetail.apPaymentDetail_vendor_charge_inventory_tax_2                               AS apPaymentDetail_vendor_charge_inventory_tax_2,
            paymentDetail.apPaymentDetail_vendor_charge_inventory_tax_3                               AS apPaymentDetail_vendor_charge_inventory_tax_3,
            paymentDetail.apPaymentDetail_vendor_charge_inventory_tax_4                               AS apPaymentDetail_vendor_charge_inventory_tax_4,
            paymentDetail.apPaymentDetail_vendor_federal_id_number_verification                       AS apPaymentDetail_vendor_federal_id_number_verification,
            paymentDetail.apPaymentDetail_vendor_email_address                                        AS apPaymentDetail_vendor_email_address,
            paymentDetail.apPaymentDetail_vendor_purchase_order_submit_email                          AS apPaymentDetail_vendor_purchase_order_submit_email,
            paymentDetail.apPaymentDetail_vendor_allow_drop_ship_to_customer                          AS apPaymentDetail_vendor_allow_drop_ship_to_customer,
            paymentDetail.apPaymentDetail_vendor_auto_submit_purchase_order                           AS apPaymentDetail_vendor_auto_submit_purchase_order,
            paymentDetail.apPaymentDetail_vendor_note                                                 AS apPaymentDetail_vendor_note,
            paymentDetail.apPaymentDetail_vendor_phone_number                                         AS apPaymentDetail_vendor_phone_number,
            paymentDetail.apPaymentDetail_vendor_comp_id                                              AS apPaymentDetail_vendor_comp_id,
            paymentDetail.apPaymentDetail_vendor_comp_time_created                                    AS apPaymentDetail_vendor_comp_time_created,
            paymentDetail.apPaymentDetail_vendor_comp_time_updated                                    AS apPaymentDetail_vendor_comp_time_updated,
            paymentDetail.apPaymentDetail_vendor_comp_name                                            AS apPaymentDetail_vendor_comp_name,
            paymentDetail.apPaymentDetail_vendor_comp_doing_business_as                               AS apPaymentDetail_vendor_comp_doing_business_as,
            paymentDetail.apPaymentDetail_vendor_comp_client_code                                     AS apPaymentDetail_vendor_comp_client_code,
            paymentDetail.apPaymentDetail_vendor_comp_client_id                                       AS apPaymentDetail_vendor_comp_client_id,
            paymentDetail.apPaymentDetail_vendor_comp_dataset_code                                    AS apPaymentDetail_vendor_comp_dataset_code,
            paymentDetail.apPaymentDetail_vendor_comp_federal_id_number                               AS apPaymentDetail_vendor_comp_federal_id_number,
            paymentDetail.apPaymentDetail_vendor_comp_address_id                                      AS apPaymentDetail_vendor_comp_address_id,
            paymentDetail.apPaymentDetail_vendor_comp_address_name                                    AS apPaymentDetail_vendor_comp_address_name,
            paymentDetail.apPaymentDetail_vendor_comp_address_address1                                AS apPaymentDetail_vendor_comp_address_address1,
            paymentDetail.apPaymentDetail_vendor_comp_address_address2                                AS apPaymentDetail_vendor_comp_address_address2,
            paymentDetail.apPaymentDetail_vendor_comp_address_city                                    AS apPaymentDetail_vendor_comp_address_city,
            paymentDetail.apPaymentDetail_vendor_comp_address_state                                   AS apPaymentDetail_vendor_comp_address_state,
            paymentDetail.apPaymentDetail_vendor_comp_address_postal_code                             AS apPaymentDetail_vendor_comp_address_postal_code,
            paymentDetail.apPaymentDetail_vendor_comp_address_latitude                                AS apPaymentDetail_vendor_comp_address_latitude,
            paymentDetail.apPaymentDetail_vendor_comp_address_longitude                               AS apPaymentDetail_vendor_comp_address_longitude,
            paymentDetail.apPaymentDetail_vendor_comp_address_country                                 AS apPaymentDetail_vendor_comp_address_country,
            paymentDetail.apPaymentDetail_vendor_comp_address_county                                  AS apPaymentDetail_vendor_comp_address_county,
            paymentDetail.apPaymentDetail_vendor_comp_address_phone                                   AS apPaymentDetail_vendor_comp_address_phone,
            paymentDetail.apPaymentDetail_vendor_comp_address_fax                                     AS apPaymentDetail_vendor_comp_address_fax,
            paymentDetail.apPaymentDetail_vendor_onboard_id                                           AS apPaymentDetail_vendor_onboard_id,
            paymentDetail.apPaymentDetail_vendor_onboard_value                                        AS apPaymentDetail_vendor_onboard_value,
            paymentDetail.apPaymentDetail_vendor_onboard_description                                  AS apPaymentDetail_vendor_onboard_description,
            paymentDetail.apPaymentDetail_vendor_onboard_localization_code                            AS apPaymentDetail_vendor_onboard_localization_code,
            paymentDetail.apPaymentDetail_vendor_method_id                                            AS apPaymentDetail_vendor_method_id,
            paymentDetail.apPaymentDetail_vendor_method_value                                         AS apPaymentDetail_vendor_method_value,
            paymentDetail.apPaymentDetail_vendor_method_description                                   AS apPaymentDetail_vendor_method_description,
            paymentDetail.apPaymentDetail_vendor_method_localization_code                             AS apPaymentDetail_vendor_method_localization_code,
            paymentDetail.apPaymentDetail_vendor_address_number                                       AS apPaymentDetail_vendor_address_number,
            paymentDetail.apPaymentDetail_vendor_address_name                                         AS apPaymentDetail_vendor_address_name,
            paymentDetail.apPaymentDetail_vendor_address_address1                                     AS apPaymentDetail_vendor_address_address1,
            paymentDetail.apPaymentDetail_vendor_address_address2                                     AS apPaymentDetail_vendor_address_address2,
            paymentDetail.apPaymentDetail_vendor_address_city                                         AS apPaymentDetail_vendor_address_city,
            paymentDetail.apPaymentDetail_vendor_address_state                                        AS apPaymentDetail_vendor_address_state,
            paymentDetail.apPaymentDetail_vendor_address_postal_code                                  AS apPaymentDetail_vendor_address_postal_code,
            paymentDetail.apPaymentDetail_vendor_address_latitude                                     AS apPaymentDetail_vendor_address_latitude,
            paymentDetail.apPaymentDetail_vendor_address_longitude                                    AS apPaymentDetail_vendor_address_longitude,
            paymentDetail.apPaymentDetail_vendor_address_country                                      AS apPaymentDetail_vendor_address_country,
            paymentDetail.apPaymentDetail_vendor_address_county                                       AS apPaymentDetail_vendor_address_county,
            paymentDetail.apPaymentDetail_vendor_address_phone                                        AS apPaymentDetail_vendor_address_phone,
            paymentDetail.apPaymentDetail_vendor_address_fax                                          AS apPaymentDetail_vendor_address_fax,
            paymentDetail.apPaymentDetail_vendor_vpt_id                                               AS apPaymentDetail_vendor_vpt_id,
            paymentDetail.apPaymentDetail_vendor_vpt_time_created                                     AS apPaymentDetail_vendor_vpt_time_created,
            paymentDetail.apPaymentDetail_vendor_vpt_time_updated                                     AS apPaymentDetail_vendor_vpt_time_updated,
            paymentDetail.apPaymentDetail_vendor_vpt_company_id                                       AS apPaymentDetail_vendor_vpt_company_id,
            paymentDetail.apPaymentDetail_vendor_vpt_description                                      AS apPaymentDetail_vendor_vpt_description,
            paymentDetail.apPaymentDetail_vendor_vpt_number                                           AS apPaymentDetail_vendor_vpt_number,
            paymentDetail.apPaymentDetail_vendor_vpt_number_of_payments                               AS apPaymentDetail_vendor_vpt_number_of_payments,
            paymentDetail.apPaymentDetail_vendor_vpt_discount_month                                   AS apPaymentDetail_vendor_vpt_discount_month,
            paymentDetail.apPaymentDetail_vendor_vpt_discount_days                                    AS apPaymentDetail_vendor_vpt_discount_days,
            paymentDetail.apPaymentDetail_vendor_vpt_discount_percent                                 AS apPaymentDetail_vendor_vpt_discount_percent,
            paymentDetail.apPaymentDetail_vendor_shipVia_id                                           AS apPaymentDetail_vendor_shipVia_id,
            paymentDetail.apPaymentDetail_vendor_shipVia_time_created                                 AS apPaymentDetail_vendor_shipVia_time_created,
            paymentDetail.apPaymentDetail_vendor_shipVia_time_updated                                 AS apPaymentDetail_vendor_shipVia_time_updated,
            paymentDetail.apPaymentDetail_vendor_shipVia_description                                  AS apPaymentDetail_vendor_shipVia_description,
            paymentDetail.apPaymentDetail_vendor_shipVia_number                                       AS apPaymentDetail_vendor_shipVia_number,
            paymentDetail.apPaymentDetail_vendor_vgrp_id                                              AS apPaymentDetail_vendor_vgrp_id,
            paymentDetail.apPaymentDetail_vendor_vgrp_time_created                                    AS apPaymentDetail_vendor_vgrp_time_created,
            paymentDetail.apPaymentDetail_vendor_vgrp_time_updated                                    AS apPaymentDetail_vendor_vgrp_time_updated,
            paymentDetail.apPaymentDetail_vendor_vgrp_company_id                                      AS apPaymentDetail_vendor_vgrp_company_id,
            paymentDetail.apPaymentDetail_vendor_vgrp_value                                           AS apPaymentDetail_vendor_vgrp_value,
            paymentDetail.apPaymentDetail_vendor_vgrp_description                                     AS apPaymentDetail_vendor_vgrp_description,
            paymentDetail.apPaymentDetail_payment_number_id                                           AS apPaymentDetail_payment_number_id,
            paymentDetail.apPaymentDetail_amount                                                      AS apPaymentDetail_amount,
            paymentDetail.apPaymentDetail_discount                                                    AS apPaymentDetail_discount,
            count(*) OVER() AS total_elements
         FROM account_payable_payment apPayment
            JOIN bnk ON apPayment.bank_id = bnk.bank_id
            JOIN vend ON apPayment.vendor_id = vend.v_id
            JOIN account_payable_payment_status_type_domain status ON apPayment.account_payable_payment_status_id = status.id
            JOIN account_payable_payment_type_type_domain type ON apPayment.account_payable_payment_type_id = type.id
            LEFT JOIN paymentDetail ON apPayment.id = paymentDetail.apPaymentDetail_payment_number_id
      """
   }

   fun findOne(id: UUID, company: Company): AccountPayablePaymentEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = """
         ${selectBaseQuery()}
         WHERE apPayment.id = :id AND apPayment.company_id = :comp_id
         ORDER BY apPaymentDetail_id
         """.trimIndent()
      return try {
         jdbc.queryForObject(query, params) { rs, _ ->
              var found: AccountPayablePaymentEntity? = null
              do {
                 found = found ?: mapRow(rs, company, "apPayment_")
                 apPaymentDetailRepository.mapRowOrNull(rs, company, "apPaymentDetail_")?.let { found.paymentDetails?.add(it) }
              } while (rs.next())
              logger.trace("Searching for Account Payable Payment {} resulted in {}", id, found)
              found
         }
      } catch (e: EmptyResultDataAccessException) {
         null
      }
   }

   fun findAll(company: Company, filterRequest: PaymentReportFilterRequest): List<AccountPayablePaymentEntity> {
      val payments = mutableListOf<AccountPayablePaymentEntity>()
      var currentPayments: AccountPayablePaymentEntity? = null
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val whereClause = StringBuilder(" WHERE apPayment.company_id = :comp_id ")

      if (!filterRequest.pmtNums.isNullOrEmpty()) {
         params["pmtNums"] = filterRequest.pmtNums
         whereClause.append(" AND apPayment.payment_number IN (:pmtNums) ")
      }

      if (!filterRequest.banks.isNullOrEmpty()) {
         params["banks"] = filterRequest.banks
         whereClause.append(" AND bnk.bank_id IN (:banks) ")
      }

      if (!filterRequest.vendors.isNullOrEmpty()) {
         params["vendors"] = filterRequest.vendors
         whereClause.append(" AND vend.v_id IN (:vendors) ")
      }

      if (!filterRequest.vendorGroups.isNullOrEmpty()) {
         params["vendorGroups"] = filterRequest.vendorGroups
         whereClause.append(" AND vend.v_vgrp_id IN (:vendorGroups) ")
      }

      filterRequest.status?.let {
         params["status"] = filterRequest.status
         whereClause.append(" AND status.value = :status ")
      }

      filterRequest.type?.let {
         params["type"] = filterRequest.type
         whereClause.append(" AND type.value = :type ")
      }

      if (filterRequest.frmPmtDt != null || filterRequest.thruPmtDt != null) {
         params["frmPmtDt"] = filterRequest.frmPmtDt
         params["thruPmtDt"] = filterRequest.thruPmtDt
         whereClause.append(" AND apPayment.payment_date ")
               .append(buildDateFilterString(filterRequest.frmPmtDt, filterRequest.thruPmtDt, "frmPmtDt", "thruPmtDt"))
      }

      if (filterRequest.frmDtClr != null || filterRequest.thruDtClr != null) {
         params["frmDtClr"] = filterRequest.frmDtClr
         params["thruDtClr"] = filterRequest.thruDtClr
         whereClause.append(" AND apPayment.date_cleared ")
               .append(buildDateFilterString(filterRequest.frmDtClr, filterRequest.thruDtClr, "frmDtClr", "thruDtClr"))
      }

      if (filterRequest.frmDtVoid != null || filterRequest.thruDtVoid != null) {
         params["frmDtVoid"] = filterRequest.frmDtVoid
         params["thruDtVoid"] = filterRequest.thruDtVoid
         whereClause.append(" AND apPayment.date_voided ")
               .append(buildDateFilterString(filterRequest.frmDtVoid, filterRequest.thruDtVoid, "frmDtVoid", "thruDtVoid"))
      }

      jdbc.query(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY apPayment_id, apPaymentDetail_id
         """.trimIndent(),
         params
      ) { rs, _ ->
         do {
            val tempPayment = if (currentPayments?.id != rs.getUuid("apPayment_id")) {
               val localPayment = mapRow(rs, company, "apPayment_")
               payments.add(localPayment)
               currentPayments = localPayment

               localPayment
            } else {
               currentPayments!!
            }

            apPaymentDetailRepository.mapRowOrNull(rs, company, "apPaymentDetail_")?.also { tempPayment.paymentDetails?.add(it) }
         } while (rs.next())
      }

      return payments
   }

   @Transactional
   fun insert(entity: AccountPayablePaymentEntity, company: Company): AccountPayablePaymentEntity {
      logger.debug("Inserting Account Payable Payment {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_payment (
            company_id,
            bank_id,
            vendor_id,
            account_payable_payment_status_id,
            account_payable_payment_type_id,
            payment_number,
            payment_date,
            date_cleared,
            date_voided,
            amount
         )
         VALUES (
            :company_id,
            :bank_id,
            :vendor_id,
            :account_payable_payment_status_id,
            :account_payable_payment_type_id,
            :payment_number,
            :payment_date,
            :date_cleared,
            :date_voided,
            :amount
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "bank_id" to entity.bank.id,
            "vendor_id" to entity.vendor.id,
            "account_payable_payment_status_id" to entity.status.id,
            "account_payable_payment_type_id" to entity.type.id,
            "payment_number" to entity.paymentNumber,
            "payment_date" to entity.paymentDate,
            "date_cleared" to entity.dateCleared,
            "date_voided" to entity.dateVoided,
            "amount" to entity.amount
         )) { rs, _ ->
            mapRowUpsert(rs, entity)
         }
   }

   @Transactional
   fun update(entity: AccountPayablePaymentEntity, company: Company): AccountPayablePaymentEntity {
      logger.debug("Updating Account Payable Payment {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE account_payable_payment
         SET
            company_id = :company_id,
            bank_id = :bank_id,
            vendor_id = :vendor_id,
            account_payable_payment_status_id = :account_payable_payment_status_id,
            account_payable_payment_type_id = :account_payable_payment_type_id,
            payment_number = :payment_number,
            payment_date = :payment_date,
            date_cleared = :date_cleared,
            date_voided = :date_voided,
            amount = :amount
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "bank_id" to entity.bank.id,
            "vendor_id" to entity.vendor.id,
            "account_payable_payment_status_id" to entity.status.id,
            "account_payable_payment_type_id" to entity.type.id,
            "payment_number" to entity.paymentNumber,
            "payment_date" to entity.paymentDate,
            "date_cleared" to entity.dateCleared,
            "date_voided" to entity.dateVoided,
            "amount" to entity.amount
         )
      ) { rs, _ ->
         mapRowUpsert(rs, entity)
      }
   }

   @Transactional
   fun delete(id: UUID, company: Company) {
      logger.debug("Deleting Account Payable Payment with id={}", id)

      val rowsAffected = jdbc.update(
         """
         DELETE FROM account_payable_payment
         WHERE id = :id AND company_id = :company_id
         """,
         mapOf("id" to id, "company_id" to company.myId())
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   private fun mapRow(
      rs: ResultSet,
      company: Company,
      columnPrefix: String = EMPTY
   ): AccountPayablePaymentEntity {
      return AccountPayablePaymentEntity(
         id = rs.getUuid("${columnPrefix}id"),
         bank = bankRepository.mapRow(rs, company, "${columnPrefix}bank_"),
         vendor = vendorRepository.mapRow(rs, company, "${columnPrefix}vendor_"),
         status = apPaymentStatusRepository.mapRow(rs, "${columnPrefix}status_"),
         type = apPaymentTypeRepository.mapRow(rs, "${columnPrefix}type_"),
         paymentNumber = rs.getString("${columnPrefix}payment_number"),
         paymentDate = rs.getLocalDate("${columnPrefix}payment_date"),
         dateCleared = rs.getLocalDateOrNull("${columnPrefix}date_cleared"),
         dateVoided = rs.getLocalDateOrNull("${columnPrefix}date_voided"),
         amount = rs.getBigDecimal("${columnPrefix}amount")
      )
   }

   private fun mapRowUpsert(
      rs: ResultSet,
      entity: AccountPayablePaymentEntity,
      columnPrefix: String = EMPTY
   ): AccountPayablePaymentEntity {
      return AccountPayablePaymentEntity(
         id = rs.getUuid("${columnPrefix}id"),
         bank = entity.bank,
         vendor = entity.vendor,
         status = entity.status,
         type = entity.type,
         paymentNumber = rs.getString("${columnPrefix}payment_number"),
         paymentDate = rs.getLocalDate("${columnPrefix}payment_date"),
         dateCleared = rs.getLocalDateOrNull("${columnPrefix}date_cleared"),
         dateVoided = rs.getLocalDateOrNull("${columnPrefix}date_voided"),
         amount = rs.getBigDecimal("${columnPrefix}amount")
      )
   }

   private fun buildDateFilterString(from: OffsetDateTime?, thru: OffsetDateTime?, frmParam: String, thruParam: String): String {
      return if (from != null && thru != null) " BETWEEN :$frmParam AND :$thruParam "
            else if (from != null) " > :$frmParam "
            else " < :$thruParam "
   }
}
