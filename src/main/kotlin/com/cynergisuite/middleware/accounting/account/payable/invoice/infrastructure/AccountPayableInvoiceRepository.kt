package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.AccountPayableInvoiceFilterRequest
import com.cynergisuite.domain.AccountPayableInvoiceListByVendorFilterRequest
import com.cynergisuite.domain.AccountPayableVendorBalanceReportFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getLongOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.getUuidOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.VendorBalanceDTO
import com.cynergisuite.middleware.accounting.account.VendorBalanceEntity
import com.cynergisuite.middleware.accounting.account.VendorBalanceInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceSelectedTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceEntity
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceListByVendorDTO
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.purchase.order.infrastructure.PurchaseOrderRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class AccountPayableInvoiceRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val vendorRepository: VendorRepository,
   private val employeeRepository: EmployeeRepository,
   private val selectedRepository: AccountPayableInvoiceSelectedTypeRepository,
   private val statusRepository: AccountPayableInvoiceStatusTypeRepository,
   private val typeRepository: AccountPayableInvoiceTypeRepository,
   private val purchaseOrderRepository: PurchaseOrderRepository
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
			   poHeader.number												         AS apInvoice_purchase_order_number,
            count(*) OVER()                                             AS total_elements,

            invoice_schedule.id                                         AS apInvoice_schedule_id,
            invoice_schedule.time_created                               AS apInvoice_schedule_time_created,
            invoice_schedule.time_updated                               AS apInvoice_schedule_time_updated,
            invoice_schedule.company_id                                 AS apInvoice_schedule_company_id,
            invoice_schedule.schedule_date                              AS apInvoice_schedule_date,
            invoice_schedule.payment_sequence_number                    AS apInvoice_schedule_payment_sequence_number,
            invoice_schedule.amount_to_pay                              AS apInvoice_schedule_amount_to_pay,
            invoice_schedule.bank_id                                    AS apInvoice_schedule_bank_id,
            invoice_schedule.external_payment_type_id                   AS apInvoice_schedule_external_payment_type_id,
            invoice_schedule.external_payment_number                    AS apInvoice_schedule_external_payment_number,
            invoice_schedule.external_payment_date                      AS apInvoice_schedule_external_payment_date,
            invoice_schedule.selected_for_processing                    AS apInvoice_schedule_selected_for_processing,
            invoice_schedule.payment_processed                          AS apInvoice_schedule_payment_processed,
            invoice_schedule.deleted                                    AS apInvoice_schedule_deleted

         FROM account_payable_invoice apInvoice
            JOIN company comp                                           ON apInvoice.company_id = comp.id AND comp.deleted = FALSE
            JOIN vend                                                   ON apInvoice.vendor_id = vend.v_id
            JOIN vend payTo                                             ON apInvoice.pay_to_id = payTo.v_id
            LEFT JOIN system_employees_fimvw employee                   ON apInvoice.employee_number_id_sfk = employee.emp_number AND employee.comp_id = comp.id
            JOIN account_payable_invoice_selected_type_domain selected  ON apInvoice.selected_id = selected.id
            JOIN account_payable_invoice_type_domain type               ON apInvoice.type_id = type.id
            JOIN account_payable_invoice_status_type_domain status      ON apInvoice.status_id = status.id
            LEFT JOIN purchase_order_header poHeader                    ON apInvoice.purchase_order_id = poHeader.id AND poHeader.deleted = FALSE
            LEFT JOIN account_payable_invoice_schedule invoice_schedule      ON apInvoice.id = invoice_schedule.invoice_id AND invoice_schedule.deleted = FALSE
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AccountPayableInvoiceEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE apInvoice.id = :id AND apInvoice.company_id = :comp_id AND apInvoice.deleted = false"

      logger.trace("Querying for a single account payable invoice {}/{}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         mapRow(rs, company, "apInvoice_")
      }

      logger.trace("Searching for AccountPayableInvoice: {} resulted in {}", company, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, filterRequest: AccountPayableInvoiceFilterRequest): RepositoryPage<AccountPayableInvoiceEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder("WHERE apInvoice.company_id = :comp_id AND apInvoice.deleted = false ")

      if (filterRequest.vendor != null) {
         params["vendor"] = filterRequest.vendor
         whereClause.append(" AND vend.v_number = :vendor ")
      }

      if (filterRequest.payTo != null) {
         params["payTo"] = filterRequest.payTo
         whereClause.append(" AND payTo.v_number = :payTo ")
      }

      if (filterRequest.invStatus != null) {
         params["status"] = filterRequest.invStatus
         whereClause.append(" AND status.value = :status ")
      }

      if (filterRequest.poNbr != null) {
         params["poNumber"] = filterRequest.poNbr
         whereClause.append(" AND poHeader.number = :poNumber ")
      }

      if (filterRequest.invNbr != null) {
         params["invoiceNumber"] = filterRequest.invNbr
         whereClause.append(" AND apInvoice.invoice = :invoiceNumber ")
      }

      if (filterRequest.invDate != null) {
         params["invoiceDate"] = filterRequest.invDate
         whereClause.append(" AND apInvoice.invoice_date = :invoiceDate ")
      }

      if (filterRequest.dueDate != null) {
         params["dueDate"] = filterRequest.dueDate
         whereClause.append(" AND apInvoice.due_date = :dueDate ")
      }

      if (filterRequest.schedDate != null) {
         logger.debug("Scheduling date is {}", filterRequest.schedDate)
         params["schedDate"] = filterRequest.schedDate
         whereClause.append(" AND invoice_schedule.schedule_date = :schedDate ")
      }

      if (filterRequest.invAmount != null) {
         params["invoiceAmount"] = filterRequest.invAmount
         whereClause.append(" AND apInvoice.invoice_amount = :invoiceAmount ")
      }

      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            $whereClause
            ORDER BY apInvoice_${filterRequest.snakeSortBy()} ${filterRequest.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company, "apInvoice_"))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findAllByVendor(company: CompanyEntity, filterRequest: AccountPayableInvoiceListByVendorFilterRequest): RepositoryPage<AccountPayableInvoiceListByVendorDTO, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder("WHERE apInvoice.company_id = :comp_id AND apInvoice.deleted = false ")

      val query = """
         SELECT
            vend.number                         AS vendor_number,
            vend.name                           AS vendor_name,
            apInvoice.invoice                   AS invoice,
            apInvoice.invoice_date              AS invoice_date,
            apInvoice.invoice_amount            AS invoice_amount,
            poHeader.number                     AS purchase_order_number,
            status.id                           AS apInvoice_status_id,
            status.value                        AS apInvoice_status_value,
            status.description                  AS apInvoice_status_description,
            status.localization_code            AS apInvoice_status_localization_code,
            count(*) OVER() AS total_elements
         FROM account_payable_invoice apInvoice
            JOIN vendor vend                                         ON apInvoice.vendor_id = vend.id AND vend.deleted = FALSE
            LEFT JOIN purchase_order_header poHeader                 ON poHeader.id = apInvoice.purchase_order_id AND poHeader.deleted = FALSE
            JOIN account_payable_invoice_status_type_domain status   ON status.id = apInvoice.status_id
      """.trimIndent()

      if (filterRequest.vendor != null) {
         params["vendor"] = filterRequest.vendor
         whereClause.append(" AND vend.number >= :vendor ")
      }

      if (filterRequest.vendor != null && filterRequest.invoice != null) {
         params["invoice"] = filterRequest.invoice
         whereClause.append(" AND apInvoice.invoice >= :invoice ")
      }

      return jdbc.queryPaged(
         """
            $query
            $whereClause
            ORDER BY vend.number, apInvoice.invoice
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(mapRowVendor(rs))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findOpenByVendor(company: CompanyEntity, filterRequest: AccountPayableInvoiceListByVendorFilterRequest): RepositoryPage<AccountPayableInvoiceListByVendorDTO, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder("WHERE apInvoice.company_id = :comp_id AND status.id = 2 AND apInvoice.deleted = false")

      val query = """
         SELECT
            vend.number                         AS vendor_number,
            vend.name                           AS vendor_name,
            apInvoice.invoice                   AS invoice,
            apInvoice.invoice_date              AS invoice_date,
            apInvoice.invoice_amount            AS invoice_amount,
            poHeader.number                     AS purchase_order_number,
            status.id                           AS apInvoice_status_id,
            status.value                        AS apInvoice_status_value,
            status.description                  AS apInvoice_status_description,
            status.localization_code            AS apInvoice_status_localization_code,
            count(*) OVER() AS total_elements
         FROM account_payable_invoice apInvoice
            JOIN vendor vend                                         ON apInvoice.vendor_id = vend.id AND vend.deleted = FALSE
            LEFT JOIN purchase_order_header poHeader                 ON poHeader.id = apInvoice.purchase_order_id AND poHeader.deleted = FALSE
            JOIN account_payable_invoice_status_type_domain status   ON status.id = apInvoice.status_id
      """.trimIndent()

      if (filterRequest.vendor != null) {
         params["vendor"] = filterRequest.vendor
         whereClause.append(" AND vend.number = :vendor ")
      }

      return jdbc.queryPaged(
         """
            $query
            $whereClause
            ORDER BY apInvoice.invoice
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         params,
         filterRequest
      ) { rs, elements ->
         do {
            elements.add(mapRowVendor(rs))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: AccountPayableInvoiceEntity, company: CompanyEntity): AccountPayableInvoiceEntity {
      logger.debug("Inserting account_payable_invoice {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_invoice (
            company_id,
            vendor_id,
            invoice,
            purchase_order_id,
            invoice_date,
            invoice_amount,
            discount_amount,
            discount_percent,
            auto_distribution_applied,
            discount_taken,
            entry_date,
            expense_date,
            discount_date,
            employee_number_id_sfk,
            original_invoice_amount,
            message,
            selected_id,
            multiple_payment_indicator,
            paid_amount,
            selected_amount,
            type_id,
            status_id,
            due_date,
            pay_to_id,
            separate_check_indicator,
            use_tax_indicator,
            receive_date,
            location_id_sfk
         )
         VALUES (
            :company_id,
            :vendor_id,
            :invoice,
            :purchase_order_id,
            :invoice_date,
            :invoice_amount,
            :discount_amount,
            :discount_percent,
            :auto_distribution_applied,
            :discount_taken,
            :entry_date,
            :expense_date,
            :discount_date,
            :employee_number_id_sfk,
            :original_invoice_amount,
            :message,
            :selected_id,
            :multiple_payment_indicator,
            :paid_amount,
            :selected_amount,
            :type_id,
            :status_id,
            :due_date,
            :pay_to_id,
            :separate_check_indicator,
            :use_tax_indicator,
            :receive_date,
            :location_id_sfk
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.id,
            "vendor_id" to entity.vendor.myId(),
            "invoice" to entity.invoice,
            "purchase_order_id" to entity.purchaseOrder?.myId(),
            "invoice_date" to entity.invoiceDate,
            "invoice_amount" to entity.invoiceAmount,
            "discount_amount" to entity.discountAmount,
            "discount_percent" to entity.discountPercent,
            "auto_distribution_applied" to entity.autoDistributionApplied,
            "discount_taken" to entity.discountTaken,
            "entry_date" to entity.entryDate,
            "expense_date" to entity.expenseDate,
            "discount_date" to entity.discountDate,
            "employee_number_id_sfk" to entity.employee?.number,
            "original_invoice_amount" to entity.originalInvoiceAmount,
            "message" to entity.message,
            "selected_id" to entity.selected.id,
            "multiple_payment_indicator" to entity.multiplePaymentIndicator,
            "paid_amount" to entity.paidAmount,
            "selected_amount" to entity.selectedAmount,
            "type_id" to entity.type.id,
            "status_id" to entity.status.id,
            "due_date" to entity.dueDate,
            "pay_to_id" to entity.payTo.myId(),
            "separate_check_indicator" to entity.separateCheckIndicator,
            "use_tax_indicator" to entity.useTaxIndicator,
            "receive_date" to entity.receiveDate,
            "location_id_sfk" to entity.location?.myId()
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun update(entity: AccountPayableInvoiceEntity, company: CompanyEntity): AccountPayableInvoiceEntity {
      logger.debug("Updating account_payable_invoice {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE account_payable_invoice
         SET
            company_id = :company_id,
            vendor_id = :vendor_id,
            invoice = :invoice,
            purchase_order_id = :purchase_order_id,

            invoice_date = :invoice_date,
            invoice_amount = :invoice_amount,
            discount_amount = :discount_amount,
            discount_percent = :discount_percent,
            auto_distribution_applied = :auto_distribution_applied,
            discount_taken = :discount_taken,
            entry_date = :entry_date,
            expense_date = :expense_date,
            discount_date = :discount_date,
            employee_number_id_sfk = :employee_number_id_sfk,
            original_invoice_amount = :original_invoice_amount,
            message = :message,
            selected_id = :selected_id,
            multiple_payment_indicator = :multiple_payment_indicator,
            paid_amount = :paid_amount,
            selected_amount = :selected_amount,
            type_id = :type_id,
            status_id = :status_id,
            due_date = :due_date,
            pay_to_id = :pay_to_id,
            separate_check_indicator = :separate_check_indicator,
            use_tax_indicator = :use_tax_indicator,
            receive_date = :receive_date,
            location_id_sfk = :location_id_sfk
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.id,
            "vendor_id" to entity.vendor.myId(),
            "invoice" to entity.invoice,
            "purchase_order_id" to entity.purchaseOrder?.myId(),
            "invoice_date" to entity.invoiceDate,
            "invoice_amount" to entity.invoiceAmount,
            "discount_amount" to entity.discountAmount,
            "discount_percent" to entity.discountPercent,
            "auto_distribution_applied" to entity.autoDistributionApplied,
            "discount_taken" to entity.discountTaken,
            "entry_date" to entity.entryDate,
            "expense_date" to entity.expenseDate,
            "discount_date" to entity.discountDate,
            "employee_number_id_sfk" to entity.employee?.number,
            "original_invoice_amount" to entity.originalInvoiceAmount,
            "message" to entity.message,
            "selected_id" to entity.selected.id,
            "multiple_payment_indicator" to entity.multiplePaymentIndicator,
            "paid_amount" to entity.paidAmount,
            "selected_amount" to entity.selectedAmount,
            "type_id" to entity.type.id,
            "status_id" to entity.status.id,
            "due_date" to entity.dueDate,
            "pay_to_id" to entity.payTo.myId(),
            "separate_check_indicator" to entity.separateCheckIndicator,
            "use_tax_indicator" to entity.useTaxIndicator,
            "receive_date" to entity.receiveDate,
            "location_id_sfk" to entity.location?.myId()
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @ReadOnly
   fun vendorBalance(company: CompanyEntity, filterRequest: AccountPayableVendorBalanceReportFilterRequest): List<VendorBalanceDTO> {
      val vendors = mutableListOf<VendorBalanceEntity>()
      var currentVendor: VendorBalanceEntity? = null
      var runningBalance = BigDecimal.ZERO
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val sortBy = StringBuilder("ORDER BY ")
      val topWhere = StringBuilder("WHERE inv.apInvoice_company_id = :comp_id AND inv.apInvoice_status_id IN (2,3) AND inv.apInvoice_deleted = false")
      val bottomWhere = StringBuilder("WHERE payment.apPayment_company_id = :comp_id AND payment.apPayment_status_id = 1")
      val paymentWhere = StringBuilder("WHERE payment.apPayment_company_id = :comp_id and payment.apPayment_status_id = 1")
      val invoiceWhere = StringBuilder("WHERE inv.apInvoice_company_id = :comp_id AND inv.apInvoice_status_id IN (2,3) AND inv.apInvoice_deleted = false")

      if (filterRequest.beginVendor != null || filterRequest.endVendor != null) {
         params["beginVendor"] = filterRequest.beginVendor
         params["endVendor"] = filterRequest.endVendor
         topWhere.append(" AND inv.apInvoice_payTo_number")
            .append(buildFilterString(filterRequest.beginVendor != null, filterRequest.endVendor  != null, "beginVendor", "endVendor"))
         bottomWhere.append(" AND apPaymentDetail_apInvoice_pay_to_number")
            .append(buildFilterString(filterRequest.beginVendor != null, filterRequest.endVendor  != null, "beginVendor", "endVendor"))
         paymentWhere.append(" AND payment.apPaymentDetail_apInvoice_pay_to_number")
            .append(buildFilterString(filterRequest.beginVendor != null, filterRequest.endVendor  != null, "beginVendor", "endVendor"))
         invoiceWhere.append(" AND inv.apInvoice_payto_number")
            .append(buildFilterString(filterRequest.beginVendor != null, filterRequest.endVendor  != null, "beginVendor", "endVendor"))
      }
      if (filterRequest.fromDate != null && filterRequest.thruDate != null) {
        params["fromDate"] = filterRequest.fromDate
        params["thruDate"] = filterRequest.thruDate
        topWhere.append(" AND inv.apInvoice_expense_date")
           .append(buildFilterString(filterRequest.fromDate != null, filterRequest.thruDate != null, "fromDate", "thruDate"))
        bottomWhere.append(" AND payment.apPayment_payment_date")
          .append(buildFilterString(filterRequest.fromDate != null, filterRequest.thruDate != null, "fromDate", "thruDate"))
        paymentWhere.append(" AND payment.apPayment_payment_date < :fromDate")
        invoiceWhere.append(" AND inv.apInvoice_expense_date < :fromDate")
     }

      if (filterRequest.sortOption == "V"){
         sortBy.append("apInvoice_payTo_name")
      }
      if (filterRequest.sortOption == "N") {
         sortBy.append("apInvoice_payTo_number")
      }
      sortBy.append(", apInvoice_vendor_number, apInvoice_invoice, action")

      jdbc.query(
         """
         with company as (
            SELECT
               comp.id                                  AS id,
               comp.deleted                             AS deleted
            FROM company comp
            ),
            vend as (
               SELECT
                  v.id                                  AS v_id,
                  v.company_id                          AS v_company_id,
                  v.number                              AS v_number,
                  v.name                                AS v_name,
                  v.account_number                      AS v_account_number,
                  v.pay_to_id                           AS v_pay_to_id,
                  v.deleted                             AS v_deleted,
                  comp.id                               AS v_comp_id
               FROM vendor v
               JOIN company comp                        ON v.company_id = comp.id AND comp.deleted = FALSE
            ),
            inv as (
               SELECT
                  apInvoice.id                                                AS apInvoice_id,
                  apInvoice.company_id                                        AS apInvoice_company_id,
                  apInvoice.invoice                                           AS apInvoice_invoice,
                  apInvoice.purchase_order_id                                 AS apInvoice_purchase_order_id,
                  apInvoice.invoice_date                                      AS apInvoice_invoice_date,
                  apInvoice.invoice_amount                                    AS apInvoice_invoice_amount,
                  apInvoice.discount_amount                                   AS apInvoice_discount_amount,
                  apInvoice.discount_percent                                  AS apInvoice_discount_percent,
                  apInvoice.discount_taken                                    AS apInvoice_discount_taken,
                  apInvoice.entry_date                                        AS apInvoice_entry_date,
                  apInvoice.expense_date                                      AS apInvoice_expense_date,
                  apInvoice.discount_date                                     AS apInvoice_discount_date,
                  apInvoice.paid_amount                                       AS apInvoice_paid_amount,
                  apInvoice.selected_amount                                   AS apInvoice_selected_amount,
                  apInvoice.due_date                                          AS apInvoice_due_date,
                  apInvoice.receive_date                                      AS apInvoice_receive_date,
                  apInvoice.deleted                                           AS apInvoice_deleted,
                  vend.v_id                                                   AS apInvoice_vendor_id,
                  vend.v_company_id                                           AS apInvoice_vendor_company_id,
                  vend.v_number                                               AS apInvoice_vendor_number,
                  vend.v_name                                                 AS apInvoice_vendor_name,
                  vend.v_account_number                                       AS apInvoice_vendor_account_number,
                  payTo.v_id                                                  AS apInvoice_payTo_id,
                  payTo.v_company_id                                          AS apInvoice_payTo_company_id,
                  payTo.v_number                                              AS apInvoice_payTo_number,
                  payTo.v_name                                                AS apInvoice_payTo_name,
                  payTo.v_account_number                                      AS apInvoice_payTo_account_number,
                  status.id                                                   AS apInvoice_status_id,
                  poHeader.number												         AS apInvoice_purchase_order_number
               FROM account_payable_invoice apInvoice
               JOIN company comp                                           ON apInvoice.company_id = comp.id AND comp.deleted = FALSE
               JOIN vend                                                   ON apInvoice.vendor_id = vend.v_id
               JOIN vend payTo                                             ON apInvoice.pay_to_id = payTo.v_id
               JOIN account_payable_invoice_status_type_domain status      ON apInvoice.status_id = status.id
               LEFT JOIN purchase_order_header poHeader                    ON apInvoice.purchase_order_id = poHeader.id AND poHeader.deleted = FALSE
         ),
            paymentDetail as (
               SELECT
                  apPaymentDetail.id                                       AS apPaymentDetail_id,
                  apPaymentDetail.company_id                               AS apPaymentDetail_company_id,
                  inv.apInvoice_id                                         AS apPaymentDetail_apInvoice_id,
                  vend.v_id                                                AS apPaymentDetail_vendor_id,
                  apPaymentDetail.payment_number_id                        AS apPaymentDetail_payment_number_id,
                  apPaymentDetail.amount                                   AS apPaymentDetail_amount,
                  apPaymentDetail.discount                                 AS apPaymentDetail_discount,
                  inv.apInvoice_vendor_number							         AS apPaymentDetail_apInvoice_vendor_name,
                  inv.apInvoice_invoice                                    AS apPaymentDetail_apInvoice_invoice,
                  inv.apInvoice_invoice_date                               AS apPaymentDetail_apInvoice_invoice_date,
                  inv.apInvoice_payTo_number						               AS apPaymentDetail_apInvoice_vendor_pay_to_number,
                  inv.apInvoice_payTo_name								         AS appaymentdetail_apInvoice_vendor_pay_to_name,
                  inv.apInvoice_purchase_order_id                          AS apPaymentDetail_apInvoice_purchase_order_id,
                  inv.apInvoice_purchase_order_number					         AS apPaymentDetail_apInvoice_purchase_order_number,
                  vend.v_number                                            AS apPaymentDetail_vendor_number,
                  vend.v_name                                              AS apPaymentDetail_vendor_name
               FROM account_payable_payment_detail apPaymentDetail
               JOIN inv ON apPaymentDetail.account_payable_invoice_id = inv.apInvoice_id
               JOIN vend ON apPaymentDetail.vendor_id = vend.v_id
            ),
            payment as (
               SELECT
                  apPayment.id                                              AS apPayment_id,
                  apPayment.company_id                                      AS apPayment_company_id,
                  vend.v_id                                                 AS apPayment_vendor_id,
                  vend.v_company_id                                         AS apPayment_vendor_company_id,
                  vend.v_number                                             AS apPayment_vendor_number,
                  vend.v_name                                               AS apPayment_vendor_name,
                  vend.v_account_number                                     AS apPayment_vendor_account_number,
                  vend.v_pay_to_id                                          AS apPayment_vendor_pay_to_id,
                  vend.v_comp_id                                            AS apPayment_vendor_comp_id,
                  vend.v_deleted                                            AS apPayment_vendor_deleted,
                  status.id                                                 AS apPayment_status_id,
                  status.value                                              AS apPayment_status_value,
                  status.description                                        AS apPayment_status_description,
                  status.localization_code                                  AS apPayment_status_localization_code,
                  apPayment.payment_number                                  AS apPayment_payment_number,
                  apPayment.payment_date                                    AS apPayment_payment_date,
                  apPayment.date_cleared                                    AS apPayment_date_cleared,
                  apPayment.date_voided                                     AS apPayment_date_voided,
                  paymentDetail.apPaymentDetail_apInvoice_invoice                                           AS apPaymentDetail_apInvoice_invoice,
                  paymentDetail.apPaymentDetail_apInvoice_invoice_date                                      AS apPaymentDetail_apInvoice_invoice_date,
                  apPayment.amount                                                                          AS apPayment_amount,
                  paymentDetail.apPaymentDetail_apInvoice_vendor_pay_to_number						            AS apPaymentDetail_apInvoice_pay_to_number,
                  paymentDetail.apPaymentDetail_apInvoice_vendor_pay_to_name  							         AS apPaymentDetail_apInvoice_pay_to_name,
                  paymentDetail.apPaymentDetail_apInvoice_purchase_order_number                             AS apPaymentDetail_apInvoice_purchase_order_number,
                  paymentDetail.apPaymentDetail_vendor_id                                                   AS apPaymentDetail_vendor_id,
                  paymentDetail.apPaymentDetail_vendor_number                                               AS apPaymentDetail_vendor_number,
                  paymentDetail.apPaymentDetail_vendor_name                                                 AS apPaymentDetail_vendor_name,
                  paymentDetail.apPaymentDetail_payment_number_id                                           AS apPaymentDetail_payment_number_id,
                  paymentDetail.apPaymentDetail_amount                                                      AS apPaymentDetail_amount,
                  paymentDetail.apPaymentDetail_discount                                                    AS apPaymentDetail_discount,
                  paymentDetail.apPaymentDetail_apInvoice_vendor_name									            AS apPaymentDetail_apInvoice_vendor_name
               FROM account_payable_payment apPayment
               JOIN vend ON apPayment.vendor_id = vend.v_id AND vend.v_deleted = FALSE
               JOIN account_payable_payment_status_type_domain status ON apPayment.account_payable_payment_status_id = status.id
               LEFT JOIN paymentDetail ON apPayment.id = paymentDetail.apPaymentDetail_payment_number_id
            ),
            paymentSum as (
               SELECT
                  SUM(apPaymentDetail_amount) as total_payment_detail,
                  SUM(apPaymentDetail_discount) as total_discount,
                  apPaymentDetail_apInvoice_pay_to_number,
                  apPaymentDetail_apInvoice_vendor_name
               FROM payment
               $paymentWhere
               GROUP BY apPaymentDetail_apInvoice_pay_to_number, apPaymentDetail_apInvoice_vendor_name
            ),
            invoiceSum as (
               SELECT
                 SUM(apInvoice_invoice_amount) as invoice_amount,
                 apInvoice_payTo_number,
                 apInvoice_vendor_name
               FROM inv
               $invoiceWhere
               GROUP BY inv.apInvoice_payto_number, inv.apInvoice_vendor_name
            ),
            beginningBalance as (
               SELECT
                  apInvoice_payTo_number,
                  coalesce(sum(ps.total_payment_detail), 0) as total_payment_amount,
                  coalesce(sum(ps.total_discount), 0) as total_payment_discount,
                  coalesce(sum(invSum.invoice_amount), 0) as total_invoice_amount,
                  coalesce(sum(invSum.invoice_amount), 0) - coalesce(sum(ps.total_payment_detail),0) + coalesce(sum(ps.total_discount), 0)as balance
               FROM paymentSum ps
               FULL OUTER JOIN invoiceSum invSum on ps.apPaymentDetail_apInvoice_pay_to_number = invSum.apInvoice_payTo_number
               GROUP BY apInvoice_payTo_number
            ),
            combinedData as (
               SELECT
                  apInvoice_vendor_name,
                  apInvoice_vendor_number,
                  apInvoice_payTo_number,
                  apInvoice_payTo_name,
                  apInvoice_expense_date,
                  'invoice'::text as action,
                  apInvoice_invoice,
                  apInvoice_invoice_date,
                  apInvoice_purchase_order_number,
                  apInvoice_invoice_amount
               FROM inv
               $topWhere
               UNION
               SELECT
                  apPaymentDetail_vendor_name,
                  apPaymentDetail_vendor_number,
                  apPaymentDetail_apInvoice_pay_to_number,
                  apPaymentDetail_apInvoice_pay_to_name,
                  apPayment_payment_date,
                  'payment'::text as action,
                  apPaymentDetail_apInvoice_invoice,
                  apPaymentDetail_apInvoice_invoice_date,
                  apPaymentDetail_apInvoice_purchase_order_number,
                  apPaymentDetail_amount
               FROM payment
               $bottomWhere
               ORDER BY apInvoice_vendor_name, apInvoice_expense_date
            )
            SELECT
              combinedData.*,
              bb.total_payment_amount,
              bb.total_payment_discount,
              bb.total_invoice_amount,
              bb.balance
            FROM combinedData
            LEFT JOIN beginningBalance bb ON combinedData.apInvoice_payTo_number = bb.apInvoice_payTo_number
            $sortBy
         """.trimIndent(),
         params
      ){ rs, elements ->
         do {
            val tempVendor = if (currentVendor?.number != rs.getLongOrNull("apInvoice_payTo_number")) {
               val localVendor = mapRowVendorBalance(rs)
               vendors.add(localVendor)
               currentVendor = localVendor
               runningBalance = localVendor.balance
               localVendor
            } else {
               currentVendor!!
            }

            mapVendorBalanceInvoice(rs).let { it ->
               val vendorBalanceInvoiceEntity = VendorBalanceInvoiceEntity()

               vendorBalanceInvoiceEntity.amount = it.amount
               if (it.action == "payment") {
                  vendorBalanceInvoiceEntity.balance = runningBalance.plus(it.amount ?: BigDecimal.ZERO)
               } else {
                  vendorBalanceInvoiceEntity.balance = it.amount?.plus(runningBalance)
               }
               vendorBalanceInvoiceEntity.invoiceNumber = it.invoiceNumber
               vendorBalanceInvoiceEntity.invoiceDate= it.invoiceDate
               vendorBalanceInvoiceEntity.poNumber = it.poNumber
               vendorBalanceInvoiceEntity.action = it.action
               vendorBalanceInvoiceEntity.expenseDate = it.expenseDate
               runningBalance = vendorBalanceInvoiceEntity.balance!!
               tempVendor.invoiceList?.add(vendorBalanceInvoiceEntity)
            }

         } while (rs.next())
      }
      return vendors.map { VendorBalanceDTO(it) }
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting Account Payable Invoice with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE account_payable_invoice
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id
         """,
         mapOf("id" to id, "company_id" to company.id),
         "account_payable_invoice"
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapRow(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY
   ): AccountPayableInvoiceEntity {
      val vendor = vendorRepository.mapRow(rs, company, "${columnPrefix}vendor_")
      val payTo = vendorRepository.mapRow(rs, company, "${columnPrefix}payTo_")
      val employee = employeeRepository.mapRowOrNull(
         rs,
         "${columnPrefix}employee_",
         "${columnPrefix}employee_comp_",
         "${columnPrefix}employee_comp_address_",
         "${columnPrefix}employee_dept_",
         "${columnPrefix}employee_store_"
      )
      val selected = selectedRepository.mapRow(rs, "${columnPrefix}selected_")
      val type = typeRepository.mapRow(rs, "${columnPrefix}type_")
      val status = statusRepository.mapRow(rs, "${columnPrefix}status_")
      val purchaseOrder =
         rs.getUuidOrNull("${columnPrefix}purchase_order_id")?.let { purchaseOrderRepository.findOne(it, company) }

      return AccountPayableInvoiceEntity(
         id = rs.getUuid("${columnPrefix}id"),
         vendor = vendor,
         invoice = rs.getString("${columnPrefix}invoice"),
         purchaseOrder = purchaseOrder,
         invoiceDate = rs.getLocalDate("${columnPrefix}invoice_date"),
         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         discountAmount = rs.getBigDecimal("${columnPrefix}discount_amount"),
         discountPercent = rs.getBigDecimal("${columnPrefix}discount_percent"),
         autoDistributionApplied = rs.getBoolean("${columnPrefix}auto_distribution_applied"),
         discountTaken = rs.getBigDecimal("${columnPrefix}discount_taken"),
         entryDate = rs.getLocalDate("${columnPrefix}entry_date"),
         expenseDate = rs.getLocalDate("${columnPrefix}expense_date"),
         discountDate = rs.getLocalDateOrNull("${columnPrefix}discount_date"),
         employee = employee,
         originalInvoiceAmount = rs.getBigDecimal("${columnPrefix}original_invoice_amount"),
         message = rs.getString("${columnPrefix}message"),
         selected = selected,
         multiplePaymentIndicator = rs.getBoolean("${columnPrefix}multiple_payment_indicator"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paid_amount"),
         selectedAmount = rs.getBigDecimal("${columnPrefix}selected_amount"),
         type = type,
         status = status,
         dueDate = rs.getLocalDate("${columnPrefix}due_date"),
         payTo = payTo,
         separateCheckIndicator = rs.getBoolean("${columnPrefix}separate_check_indicator"),
         useTaxIndicator = rs.getBoolean("${columnPrefix}use_tax_indicator"),
         receiveDate = rs.getLocalDateOrNull("${columnPrefix}receive_date"),
         location = rs.getIntOrNull("${columnPrefix}location_id_sfk")?.let { SimpleLegacyIdentifiableEntity(it) }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      entity: AccountPayableInvoiceEntity,
      columnPrefix: String = EMPTY
   ): AccountPayableInvoiceEntity {
      return AccountPayableInvoiceEntity(
         id = rs.getUuid("${columnPrefix}id"),
         vendor = entity.vendor,
         invoice = rs.getString("${columnPrefix}invoice"),
         purchaseOrder = entity.purchaseOrder,
         invoiceDate = rs.getLocalDate("${columnPrefix}invoice_date"),
         invoiceAmount = rs.getBigDecimal("${columnPrefix}invoice_amount"),
         discountAmount = rs.getBigDecimal("${columnPrefix}discount_amount"),
         discountPercent = rs.getBigDecimal("${columnPrefix}discount_percent"),
         autoDistributionApplied = rs.getBoolean("${columnPrefix}auto_distribution_applied"),
         discountTaken = rs.getBigDecimal("${columnPrefix}discount_taken"),
         entryDate = rs.getLocalDate("${columnPrefix}entry_date"),
         expenseDate = rs.getLocalDate("${columnPrefix}expense_date"),
         discountDate = rs.getLocalDateOrNull("${columnPrefix}discount_date"),
         employee = entity.employee,
         originalInvoiceAmount = rs.getBigDecimal("${columnPrefix}original_invoice_amount"),
         message = rs.getString("${columnPrefix}message"),
         selected = entity.selected,
         multiplePaymentIndicator = rs.getBoolean("${columnPrefix}multiple_payment_indicator"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paid_amount"),
         selectedAmount = rs.getBigDecimal("${columnPrefix}selected_amount"),
         type = entity.type,
         status = entity.status,
         dueDate = rs.getLocalDate("${columnPrefix}due_date"),
         payTo = entity.payTo,
         separateCheckIndicator = rs.getBoolean("${columnPrefix}separate_check_indicator"),
         useTaxIndicator = rs.getBoolean("${columnPrefix}use_tax_indicator"),
         receiveDate = rs.getLocalDateOrNull("${columnPrefix}receive_date"),
         location = entity.location
      )
   }

   private fun mapRowVendor(rs: ResultSet): AccountPayableInvoiceListByVendorDTO {
      val status = statusRepository.mapRow(rs, "apInvoice_status_")

      return AccountPayableInvoiceListByVendorDTO(
         vendorNumber = rs.getInt("vendor_number"),
         vendorName = rs.getString("vendor_name"),
         invoice = rs.getString("invoice"),
         invoiceDate = rs.getLocalDate("invoice_date"),
         invoiceAmount = rs.getBigDecimal("invoice_amount"),
         poNbr = rs.getInt("purchase_order_number"),
         status = AccountPayableInvoiceStatusTypeDTO(status)
      )
   }

   private fun mapRowVendorBalance(rs: ResultSet): VendorBalanceEntity {
      return VendorBalanceEntity(
         name = rs.getString("apInvoice_payTo_name"),
         number = rs.getLong("apInvoice_payTo_number"),
         balance = rs.getBigDecimal("balance") ?: BigDecimal.ZERO
      )
   }

   private fun mapVendorBalanceInvoice(rs: ResultSet): VendorBalanceInvoiceEntity {
      return VendorBalanceInvoiceEntity(
         expenseDate = rs.getLocalDate("apinvoice_expense_date"),
         action = rs.getString("action"),
         invoiceNumber = rs.getString("apinvoice_invoice"),
         invoiceDate = rs.getLocalDate("apinvoice_invoice_date"),
         poNumber = rs.getString("apinvoice_purchase_order_number"),
         amount = if (rs.getString("action") == "payment") rs.getBigDecimal("apinvoice_invoice_amount").negate() else rs.getBigDecimal("apinvoice_invoice_amount"),
         balance = rs.getBigDecimal("apinvoice_invoice_amount")
      )
   }

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam"
      else if (begin) " >= :$beginningParam"
      else " <= :$endingParam"
   }
}
