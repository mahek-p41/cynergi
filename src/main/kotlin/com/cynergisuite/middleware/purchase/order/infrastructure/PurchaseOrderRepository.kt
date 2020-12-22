package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getOffsetDateTimeOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.purchase.order.PurchaseOrderEntity
import com.cynergisuite.middleware.purchase.order.type.infrastructure.ExceptionIndicatorTypeRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderStatusTypeRepository
import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderTypeRepository
import com.cynergisuite.middleware.shipping.freight.onboard.infrastructure.FreightOnboardTypeRepository
import com.cynergisuite.middleware.shipping.freight.term.infrastructure.FreightTermTypeRepository
import com.cynergisuite.middleware.shipping.location.infrastructure.ShipLocationTypeRepository
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
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
class PurchaseOrderRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val accountRepository: AccountRepository,
   private val employeeRepository: EmployeeRepository,
   private val exceptionIndicatorTypeRepository: ExceptionIndicatorTypeRepository,
   private val freightOnboardTypeRepository: FreightOnboardTypeRepository,
   private val freightTermTypeRepository: FreightTermTypeRepository,
   private val paymentTermRepository: VendorPaymentTermRepository,
   private val shipLocationTypeRepository: ShipLocationTypeRepository,
   private val shipViaRepository: ShipViaRepository,
   private val statusTypeRepository: PurchaseOrderStatusTypeRepository,
   private val typeRepository: PurchaseOrderTypeRepository,
   private val vendorRepository: VendorRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH vendor AS (
            ${vendorRepository.baseSelectQuery()}
         ),
         employee AS (
            ${employeeRepository.employeeBaseQuery()}
         ),
         account AS (
            ${accountRepository.selectBaseQuery()}
         )
         SELECT
            po.id                                               AS po_id,
            po.uu_row_id                                        AS po_uu_row_id,
            po.time_created                                     AS po_time_created,
            po.time_updated                                     AS po_time_updated,
            po.company_id                                       AS po_comp_id,
            po.order_date                                       AS po_order_date,
            po.total_amount                                     AS po_total_amount,
            po.received_amount                                  AS po_received_amount,
            po.paid_amount                                      AS po_paid_amount,
            po.required_date                                    AS po_required_date,
            po.message                                          AS po_message,
            po.total_landed_amount                              AS po_total_landed_amount,
            po.total_freight_amount                             AS po_total_freight_amount,
            po.vendor_submitted_time                            AS po_vendor_submitted_time,
            po.ecommerce_indicator                              AS po_ecommerce_indicator,
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
            vendor.comp_id                                      AS vendor_comp_id,
            vendor.comp_uu_row_id                               AS vendor_comp_uu_row_id,
            vendor.comp_time_created                            AS vendor_comp_time_created,
            vendor.comp_time_updated                            AS vendor_comp_time_updated,
            vendor.comp_name                                    AS vendor_comp_name,
            vendor.comp_doing_business_as                       AS vendor_comp_doing_business_as,
            vendor.comp_client_code                             AS vendor_comp_client_code,
            vendor.comp_client_id                               AS vendor_comp_client_id,
            vendor.comp_dataset_code                            AS vendor_comp_dataset_code,
            vendor.comp_federal_id_number                       AS vendor_comp_federal_id_number,
            vendor.comp_address_id                              AS vendor_comp_address_id,
            vendor.comp_address_name                            AS vendor_comp_address_name,
            vendor.comp_address_address1                        AS vendor_comp_address_address1,
            vendor.comp_address_address2                        AS vendor_comp_address_address2,
            vendor.comp_address_city                            AS vendor_comp_address_city,
            vendor.comp_address_state                           AS vendor_comp_address_state,
            vendor.comp_address_postal_code                     AS vendor_comp_address_postal_code,
            vendor.comp_address_latitude                        AS vendor_comp_address_latitude,
            vendor.comp_address_longitude                       AS vendor_comp_address_longitude,
            vendor.comp_address_country                         AS vendor_comp_address_country,
            vendor.comp_address_county                          AS vendor_comp_address_county,
            vendor.comp_address_phone                           AS vendor_comp_address_phone,
            vendor.comp_address_fax                             AS vendor_comp_address_fax,
            vendor.onboard_id                                   AS vendor_onboard_id,
            vendor.onboard_value                                AS vendor_onboard_value,
            vendor.onboard_description                          AS vendor_onboard_description,
            vendor.onboard_localization_code                    AS vendor_onboard_localization_code,
            vendor.method_id                                    AS vendor_method_id,
            vendor.method_value                                 AS vendor_method_value,
            vendor.method_description                           AS vendor_method_description,
            vendor.method_localization_code                     AS vendor_method_localization_code,
            vendor.address_id                                   AS vendor_address_id,
            vendor.address_uu_row_id                            AS vendor_address_uu_row_id,
            vendor.address_time_created                         AS vendor_address_time_created,
            vendor.address_time_updated                         AS vendor_address_time_updated,
            vendor.address_number                               AS vendor_address_number,
            vendor.address_name                                 AS vendor_address_name,
            vendor.address_address1                             AS vendor_address_address1,
            vendor.address_address2                             AS vendor_address_address2,
            vendor.address_city                                 AS vendor_address_city,
            vendor.address_state                                AS vendor_address_state,
            vendor.address_postal_code                          AS vendor_address_postal_code,
            vendor.address_latitude                             AS vendor_address_latitude,
            vendor.address_longitude                            AS vendor_address_longitude,
            vendor.address_country                              AS vendor_address_country,
            vendor.address_county                               AS vendor_address_county,
            vendor.address_phone                                AS vendor_address_phone,
            vendor.address_fax                                  AS vendor_address_fax,
            vendor.vpt_id                                       AS vendor_vpt_id,
            vendor.vpt_uu_row_id                                AS vendor_vpt_uu_row_id,
            vendor.vpt_time_created                             AS vendor_vpt_time_created,
            vendor.vpt_time_updated                             AS vendor_vpt_time_updated,
            vendor.vpt_company_id                               AS vendor_vpt_company_id,
            vendor.vpt_description                              AS vendor_vpt_description,
            vendor.vpt_number                                   AS vendor_vpt_number,
            vendor.vpt_number_of_payments                       AS vendor_vpt_number_of_payments,
            vendor.vpt_discount_month                           AS vendor_vpt_discount_month,
            vendor.vpt_discount_days                            AS vendor_vpt_discount_days,
            vendor.vpt_discount_percent                         AS vendor_vpt_discount_percent,
            vendor.shipVia_id                                   AS vendor_shipVia_id,
            vendor.shipVia_uu_row_id                            AS vendor_shipVia_uu_row_id,
            vendor.shipVia_time_created                         AS vendor_shipVia_time_created,
            vendor.shipVia_time_updated                         AS vendor_shipVia_time_updated,
            vendor.shipVia_description                          AS vendor_shipVia_description,
            vendor.shipVia_number                               AS vendor_shipVia_number,
            vendor.vgrp_id                                      AS vendor_vgrp_id,
            vendor.vgrp_uu_row_id                               AS vendor_vgrp_uu_row_id,
            vendor.vgrp_time_created                            AS vendor_vgrp_time_created,
            vendor.vgrp_time_updated                            AS vendor_vgrp_time_updated,
            vendor.vgrp_company_id                              AS vendor_vgrp_company_id,
            vendor.vgrp_value                                   AS vendor_vgrp_value,
            vendor.vgrp_description                             AS vendor_vgrp_description,
            count(*) OVER()                                     AS vendor_total_elements,
            statusType.id                                       AS statusType_id,
            statusType.value                                    AS statusType_value,
            statusType.description                              AS statusType_description,
            statusType.localization_code                        AS statusType_localization_code,
            type.id                                             AS type_id,
            type.value                                          AS type_value,
            type.description                                    AS type_description,
            type.localization_code                              AS type_localization_code,
            freightOnboardType.id                               AS freightOnboardType_id,
            freightOnboardType.value                            AS freightOnboardType_value,
            freightOnboardType.description                      AS freightOnboardType_description,
            freightOnboardType.localization_code                AS freightOnboardType_localization_code,
            freightTermType.id                                  AS freightTermType_id,
            freightTermType.value                               AS freightTermType_value,
            freightTermType.description                         AS freightTermType_description,
            freightTermType.localization_code                   AS freightTermType_localization_code,
            shipLocType.id                                      AS shipLocType_id,
            shipLocType.value                                   AS shipLocType_value,
            shipLocType.description                             AS shipLocType_description,
            shipLocType.localization_code                       AS shipLocType_localization_code,
            approvedBy.emp_id                                   AS approvedBy_id,
            approvedBy.emp_number                               AS approvedBy_number,
            approvedBy.emp_last_name                            AS approvedBy_last_name,
            approvedBy.emp_first_name_mi                        AS approvedBy_first_name_mi,
            approvedBy.emp_type                                 AS approvedBy_type,
            approvedBy.emp_pass_code                            AS approvedBy_pass_code,
            approvedBy.emp_active                               AS approvedBy_active,
            approvedBy.emp_department                           AS approvedBy_department,
            approvedBy.emp_cynergi_system_admin                 AS approvedBy_cynergi_system_admin,
            approvedBy.emp_alternative_store_indicator          AS approvedBy_alternative_store_indicator,
            approvedBy.emp_alternative_area                     AS approvedBy_alternative_area,
            approvedBy.store_id                                 AS approvedBy_store_id,
            approvedBy.store_number                             AS approvedBy_store_number,
            approvedBy.store_name                               AS approvedBy_store_name,
            approvedBy.dept_id                                  AS approvedBy_dept_id,
            approvedBy.dept_code                                AS approvedBy_dept_code,
            approvedBy.dept_description                         AS approvedBy_dept_description,
            approvedBy.comp_id                                  AS approvedBy_comp_id,
            approvedBy.comp_uu_row_id                           AS approvedBy_comp_uu_row_id,
            approvedBy.comp_time_created                        AS approvedBy_comp_time_created,
            approvedBy.comp_time_updated                        AS approvedBy_comp_time_updated,
            approvedBy.comp_name                                AS approvedBy_comp_name,
            approvedBy.comp_doing_business_as                   AS approvedBy_comp_doing_business_as,
            approvedBy.comp_client_code                         AS approvedBy_comp_client_code,
            approvedBy.comp_client_id                           AS approvedBy_comp_client_id,
            approvedBy.comp_dataset_code                        AS approvedBy_comp_dataset_code,
            approvedBy.comp_federal_id_number                   AS approvedBy_comp_federal_id_number,
            approvedBy.address_id                               AS approvedBy_comp_address_id,
            approvedBy.address_name                             AS approvedBy_comp_address_name,
            approvedBy.address_address1                         AS approvedBy_comp_address_address1,
            approvedBy.address_address2                         AS approvedBy_comp_address_address2,
            approvedBy.address_city                             AS approvedBy_comp_address_city,
            approvedBy.address_state                            AS approvedBy_comp_address_state,
            approvedBy.address_postal_code                      AS approvedBy_comp_address_postal_code,
            approvedBy.address_latitude                         AS approvedBy_comp_address_latitude,
            approvedBy.address_longitude                        AS approvedBy_comp_address_longitude,
            approvedBy.address_country                          AS approvedBy_comp_address_country,
            approvedBy.address_county                           AS approvedBy_comp_address_county,
            approvedBy.address_phone                            AS approvedBy_comp_address_phone,
            approvedBy.address_fax                              AS approvedBy_comp_address_fax,
            purchaseAgent.emp_id                                AS purchaseAgent_id,
            purchaseAgent.emp_number                            AS purchaseAgent_number,
            purchaseAgent.emp_last_name                         AS purchaseAgent_last_name,
            purchaseAgent.emp_first_name_mi                     AS purchaseAgent_first_name_mi,
            purchaseAgent.emp_type                              AS purchaseAgent_type,
            purchaseAgent.emp_pass_code                         AS purchaseAgent_pass_code,
            purchaseAgent.emp_active                            AS purchaseAgent_active,
            purchaseAgent.emp_department                        AS purchaseAgent_department,
            purchaseAgent.emp_cynergi_system_admin              AS purchaseAgent_cynergi_system_admin,
            purchaseAgent.emp_alternative_store_indicator       AS purchaseAgent_alternative_store_indicator,
            purchaseAgent.emp_alternative_area                  AS purchaseAgent_alternative_area,
            purchaseAgent.store_id                              AS purchaseAgent_store_id,
            purchaseAgent.store_number                          AS purchaseAgent_store_number,
            purchaseAgent.store_name                            AS purchaseAgent_store_name,
            purchaseAgent.dept_id                               AS purchaseAgent_dept_id,
            purchaseAgent.dept_code                             AS purchaseAgent_dept_code,
            purchaseAgent.dept_description                      AS purchaseAgent_dept_description,
            purchaseAgent.comp_id                               AS purchaseAgent_comp_id,
            purchaseAgent.comp_uu_row_id                        AS purchaseAgent_comp_uu_row_id,
            purchaseAgent.comp_time_created                     AS purchaseAgent_comp_time_created,
            purchaseAgent.comp_time_updated                     AS purchaseAgent_comp_time_updated,
            purchaseAgent.comp_name                             AS purchaseAgent_comp_name,
            purchaseAgent.comp_doing_business_as                AS purchaseAgent_comp_doing_business_as,
            purchaseAgent.comp_client_code                      AS purchaseAgent_comp_client_code,
            purchaseAgent.comp_client_id                        AS purchaseAgent_comp_client_id,
            purchaseAgent.comp_dataset_code                     AS purchaseAgent_comp_dataset_code,
            purchaseAgent.comp_federal_id_number                AS purchaseAgent_comp_federal_id_number,
            purchaseAgent.address_id                            AS purchaseAgent_comp_address_id,
            purchaseAgent.address_name                          AS purchaseAgent_comp_address_name,
            purchaseAgent.address_address1                      AS purchaseAgent_comp_address_address1,
            purchaseAgent.address_address2                      AS purchaseAgent_comp_address_address2,
            purchaseAgent.address_city                          AS purchaseAgent_comp_address_city,
            purchaseAgent.address_state                         AS purchaseAgent_comp_address_state,
            purchaseAgent.address_postal_code                   AS purchaseAgent_comp_address_postal_code,
            purchaseAgent.address_latitude                      AS purchaseAgent_comp_address_latitude,
            purchaseAgent.address_longitude                     AS purchaseAgent_comp_address_longitude,
            purchaseAgent.address_country                       AS purchaseAgent_comp_address_country,
            purchaseAgent.address_county                        AS purchaseAgent_comp_address_county,
            purchaseAgent.address_phone                         AS purchaseAgent_comp_address_phone,
            purchaseAgent.address_fax                           AS purchaseAgent_comp_address_fax,
            shipVia.id                                          AS shipVia_id,
            shipVia.uu_row_id                                   AS shipVia_uu_row_id,
            shipVia.time_created                                AS shipVia_time_created,
            shipVia.time_updated                                AS shipVia_time_updated,
            shipVia.description                                 AS shipVia_description,
            shipVia.number                                      AS shipVia_number,
            shipTo.v_id                                         AS shipTo_id,
            shipTo.v_uu_row_id                                  AS shipTo_uu_row_id,
            shipTo.v_time_created                               AS shipTo_time_created,
            shipTo.v_time_updated                               AS shipTo_time_updated,
            shipTo.v_company_id                                 AS shipTo_company_id,
            shipTo.v_number                                     AS shipTo_number,
            shipTo.v_name                                       AS shipTo_name,
            shipTo.v_address_id                                 AS shipTo_address_id,
            shipTo.v_account_number                             AS shipTo_account_number,
            shipTo.v_pay_to_id                                  AS shipTo_pay_to_id,
            shipTo.v_freight_on_board_type_id                   AS shipTo_freight_on_board_type_id,
            shipTo.v_payment_terms_id                           AS shipTo_payment_terms_id,
            shipTo.v_normal_days                                AS shipTo_normal_days,
            shipTo.v_return_policy                              AS shipTo_return_policy,
            shipTo.v_ship_via_id                                AS shipTo_ship_via_id,
            shipTo.v_group_id                                   AS shipTo_group_id,
            shipTo.v_minimum_quantity                           AS shipTo_minimum_quantity,
            shipTo.v_minimum_amount                             AS shipTo_minimum_amount,
            shipTo.v_free_ship_quantity                         AS shipTo_free_ship_quantity,
            shipTo.v_free_ship_amount                           AS shipTo_free_ship_amount,
            shipTo.v_vendor_1099                                AS shipTo_vendor_1099,
            shipTo.v_federal_id_number                          AS shipTo_federal_id_number,
            shipTo.v_sales_representative_name                  AS shipTo_sales_representative_name,
            shipTo.v_sales_representative_fax                   AS shipTo_sales_representative_fax,
            shipTo.v_separate_check                             AS shipTo_separate_check,
            shipTo.v_bump_percent                               AS shipTo_bump_percent,
            shipTo.v_freight_calc_method_type_id                AS shipTo_freight_calc_method_type_id,
            shipTo.v_freight_percent                            AS shipTo_freight_percent,
            shipTo.v_freight_amount                             AS shipTo_freight_amount,
            shipTo.v_charge_inventory_tax_1                     AS shipTo_charge_inventory_tax_1,
            shipTo.v_charge_inventory_tax_2                     AS shipTo_charge_inventory_tax_2,
            shipTo.v_charge_inventory_tax_3                     AS shipTo_charge_inventory_tax_3,
            shipTo.v_charge_inventory_tax_4                     AS shipTo_charge_inventory_tax_4,
            shipTo.v_federal_id_number_verification             AS shipTo_federal_id_number_verification,
            shipTo.v_email_address                              AS shipTo_email_address,
            shipTo.v_purchase_order_submit_email_address        AS shipTo_purchase_order_submit_email_address,
            shipTo.v_allow_drop_ship_to_customer                AS shipTo_allow_drop_ship_to_customer,
            shipTo.v_auto_submit_purchase_order                 AS shipTo_auto_submit_purchase_order,
            shipTo.v_note                                       AS shipTo_note,
            shipTo.v_phone_number                               AS shipTo_phone_number,
            shipTo.comp_id                                      AS shipTo_comp_id,
            shipTo.comp_uu_row_id                               AS shipTo_comp_uu_row_id,
            shipTo.comp_time_created                            AS shipTo_comp_time_created,
            shipTo.comp_time_updated                            AS shipTo_comp_time_updated,
            shipTo.comp_name                                    AS shipTo_comp_name,
            shipTo.comp_doing_business_as                       AS shipTo_comp_doing_business_as,
            shipTo.comp_client_code                             AS shipTo_comp_client_code,
            shipTo.comp_client_id                               AS shipTo_comp_client_id,
            shipTo.comp_dataset_code                            AS shipTo_comp_dataset_code,
            shipTo.comp_federal_id_number                       AS shipTo_comp_federal_id_number,
            shipTo.comp_address_id                              AS shipTo_comp_address_id,
            shipTo.comp_address_name                            AS shipTo_comp_address_name,
            shipTo.comp_address_address1                        AS shipTo_comp_address_address1,
            shipTo.comp_address_address2                        AS shipTo_comp_address_address2,
            shipTo.comp_address_city                            AS shipTo_comp_address_city,
            shipTo.comp_address_state                           AS shipTo_comp_address_state,
            shipTo.comp_address_postal_code                     AS shipTo_comp_address_postal_code,
            shipTo.comp_address_latitude                        AS shipTo_comp_address_latitude,
            shipTo.comp_address_longitude                       AS shipTo_comp_address_longitude,
            shipTo.comp_address_country                         AS shipTo_comp_address_country,
            shipTo.comp_address_county                          AS shipTo_comp_address_county,
            shipTo.comp_address_phone                           AS shipTo_comp_address_phone,
            shipTo.comp_address_fax                             AS shipTo_comp_address_fax,
            shipTo.onboard_id                                   AS shipTo_onboard_id,
            shipTo.onboard_value                                AS shipTo_onboard_value,
            shipTo.onboard_description                          AS shipTo_onboard_description,
            shipTo.onboard_localization_code                    AS shipTo_onboard_localization_code,
            shipTo.method_id                                    AS shipTo_method_id,
            shipTo.method_value                                 AS shipTo_method_value,
            shipTo.method_description                           AS shipTo_method_description,
            shipTo.method_localization_code                     AS shipTo_method_localization_code,
            shipTo.address_id                                   AS shipTo_address_id,
            shipTo.address_uu_row_id                            AS shipTo_address_uu_row_id,
            shipTo.address_time_created                         AS shipTo_address_time_created,
            shipTo.address_time_updated                         AS shipTo_address_time_updated,
            shipTo.address_number                               AS shipTo_address_number,
            shipTo.address_name                                 AS shipTo_address_name,
            shipTo.address_address1                             AS shipTo_address_address1,
            shipTo.address_address2                             AS shipTo_address_address2,
            shipTo.address_city                                 AS shipTo_address_city,
            shipTo.address_state                                AS shipTo_address_state,
            shipTo.address_postal_code                          AS shipTo_address_postal_code,
            shipTo.address_latitude                             AS shipTo_address_latitude,
            shipTo.address_longitude                            AS shipTo_address_longitude,
            shipTo.address_country                              AS shipTo_address_country,
            shipTo.address_county                               AS shipTo_address_county,
            shipTo.address_phone                                AS shipTo_address_phone,
            shipTo.address_fax                                  AS shipTo_address_fax,
            shipTo.vpt_id                                       AS shipTo_vpt_id,
            shipTo.vpt_uu_row_id                                AS shipTo_vpt_uu_row_id,
            shipTo.vpt_time_created                             AS shipTo_vpt_time_created,
            shipTo.vpt_time_updated                             AS shipTo_vpt_time_updated,
            shipTo.vpt_company_id                               AS shipTo_vpt_company_id,
            shipTo.vpt_description                              AS shipTo_vpt_description,
            shipTo.vpt_number                                   AS shipTo_vpt_number,
            shipTo.vpt_number_of_payments                       AS shipTo_vpt_number_of_payments,
            shipTo.vpt_discount_month                           AS shipTo_vpt_discount_month,
            shipTo.vpt_discount_days                            AS shipTo_vpt_discount_days,
            shipTo.vpt_discount_percent                         AS shipTo_vpt_discount_percent,
            shipTo.shipVia_id                                   AS shipTo_shipVia_id,
            shipTo.shipVia_uu_row_id                            AS shipTo_shipVia_uu_row_id,
            shipTo.shipVia_time_created                         AS shipTo_shipVia_time_created,
            shipTo.shipVia_time_updated                         AS shipTo_shipVia_time_updated,
            shipTo.shipVia_description                          AS shipTo_shipVia_description,
            shipTo.shipVia_number                               AS shipTo_shipVia_number,
            shipTo.vgrp_id                                      AS shipTo_vgrp_id,
            shipTo.vgrp_uu_row_id                               AS shipTo_vgrp_uu_row_id,
            shipTo.vgrp_time_created                            AS shipTo_vgrp_time_created,
            shipTo.vgrp_time_updated                            AS shipTo_vgrp_time_updated,
            shipTo.vgrp_company_id                              AS shipTo_vgrp_company_id,
            shipTo.vgrp_value                                   AS shipTo_vgrp_value,
            shipTo.vgrp_description                             AS shipTo_vgrp_description,
            count(*) OVER()                                     AS shipTo_total_elements,
            paymentTermType.id                                  AS paymentTermType_id,
            paymentTermType.uu_row_id                           AS paymentTermType_uu_row_id,
            paymentTermType.time_created                        AS paymentTermType_time_created,
            paymentTermType.time_updated                        AS paymentTermType_time_updated,
            paymentTermType.company_id                          AS paymentTermType_company_id,
            paymentTermType.description                         AS paymentTermType_description,
            paymentTermType.number                              AS paymentTermType_number,
            paymentTermType.number_of_payments                  AS paymentTermType_number_of_payments,
            paymentTermType.discount_month                      AS paymentTermType_discount_month,
            paymentTermType.discount_days                       AS paymentTermType_discount_days,
            paymentTermType.discount_percent                    AS paymentTermType_discount_percent,
            exceptionIndType.id                                 AS exceptionIndType_id,
            exceptionIndType.value                              AS exceptionIndType_value,
            exceptionIndType.description                        AS exceptionIndType_description,
            exceptionIndType.localization_code                  AS exceptionIndType_localization_code,
            vendorSubmittedEmp.emp_id                           AS vendorSubmittedEmp_id,
            vendorSubmittedEmp.emp_number                       AS vendorSubmittedEmp_number,
            vendorSubmittedEmp.emp_last_name                    AS vendorSubmittedEmp_last_name,
            vendorSubmittedEmp.emp_first_name_mi                AS vendorSubmittedEmp_first_name_mi,
            vendorSubmittedEmp.emp_type                         AS vendorSubmittedEmp_type,
            vendorSubmittedEmp.emp_pass_code                    AS vendorSubmittedEmp_pass_code,
            vendorSubmittedEmp.emp_active                       AS vendorSubmittedEmp_active,
            vendorSubmittedEmp.emp_department                   AS vendorSubmittedEmp_department,
            vendorSubmittedEmp.emp_cynergi_system_admin         AS vendorSubmittedEmp_cynergi_system_admin,
            vendorSubmittedEmp.emp_alternative_store_indicator  AS vendorSubmittedEmp_alternative_store_indicator,
            vendorSubmittedEmp.emp_alternative_area             AS vendorSubmittedEmp_alternative_area,
            vendorSubmittedEmp.store_id                         AS vendorSubmittedEmp_store_id,
            vendorSubmittedEmp.store_number                     AS vendorSubmittedEmp_store_number,
            vendorSubmittedEmp.store_name                       AS vendorSubmittedEmp_store_name,
            vendorSubmittedEmp.dept_id                          AS vendorSubmittedEmp_dept_id,
            vendorSubmittedEmp.dept_code                        AS vendorSubmittedEmp_dept_code,
            vendorSubmittedEmp.dept_description                 AS vendorSubmittedEmp_dept_description,
            vendorSubmittedEmp.comp_id                          AS vendorSubmittedEmp_comp_id,
            vendorSubmittedEmp.comp_uu_row_id                   AS vendorSubmittedEmp_comp_uu_row_id,
            vendorSubmittedEmp.comp_time_created                AS vendorSubmittedEmp_comp_time_created,
            vendorSubmittedEmp.comp_time_updated                AS vendorSubmittedEmp_comp_time_updated,
            vendorSubmittedEmp.comp_name                        AS vendorSubmittedEmp_comp_name,
            vendorSubmittedEmp.comp_doing_business_as           AS vendorSubmittedEmp_comp_doing_business_as,
            vendorSubmittedEmp.comp_client_code                 AS vendorSubmittedEmp_comp_client_code,
            vendorSubmittedEmp.comp_client_id                   AS vendorSubmittedEmp_comp_client_id,
            vendorSubmittedEmp.comp_dataset_code                AS vendorSubmittedEmp_comp_dataset_code,
            vendorSubmittedEmp.comp_federal_id_number           AS vendorSubmittedEmp_comp_federal_id_number,
            vendorSubmittedEmp.address_id                       AS vendorSubmittedEmp_comp_address_id,
            vendorSubmittedEmp.address_name                     AS vendorSubmittedEmp_comp_address_name,
            vendorSubmittedEmp.address_address1                 AS vendorSubmittedEmp_comp_address_address1,
            vendorSubmittedEmp.address_address2                 AS vendorSubmittedEmp_comp_address_address2,
            vendorSubmittedEmp.address_city                     AS vendorSubmittedEmp_comp_address_city,
            vendorSubmittedEmp.address_state                    AS vendorSubmittedEmp_comp_address_state,
            vendorSubmittedEmp.address_postal_code              AS vendorSubmittedEmp_comp_address_postal_code,
            vendorSubmittedEmp.address_latitude                 AS vendorSubmittedEmp_comp_address_latitude,
            vendorSubmittedEmp.address_longitude                AS vendorSubmittedEmp_comp_address_longitude,
            vendorSubmittedEmp.address_country                  AS vendorSubmittedEmp_comp_address_country,
            vendorSubmittedEmp.address_county                   AS vendorSubmittedEmp_comp_address_county,
            vendorSubmittedEmp.address_phone                    AS vendorSubmittedEmp_comp_address_phone,
            vendorSubmittedEmp.address_fax                      AS vendorSubmittedEmp_comp_address_fax,
            custAcct.account_id                                 AS custAcct_id,
            custAcct.account_number                             AS custAcct_number,
            custAcct.account_name                               AS custAcct_name,
            custAcct.account_form_1099_field                    AS custAcct_form_1099_field,
            custAcct.account_corporate_account_indicator        AS custAcct_corporate_account_indicator,
            custAcct.account_comp_id                            AS custAcct_comp_id,
            custAcct.account_type_id                            AS custAcct_type_id,
            custAcct.account_type_value                         AS custAcct_type_value,
            custAcct.account_type_description                   AS custAcct_type_description,
            custAcct.account_type_localization_code             AS custAcct_type_localization_code,
            custAcct.account_balance_type_id                    AS custAcct_balance_type_id,
            custAcct.account_balance_type_value                 AS custAcct_balance_type_value,
            custAcct.account_balance_type_description           AS custAcct_balance_type_description,
            custAcct.account_balance_type_localization_code     AS custAcct_balance_type_localization_code,
            custAcct.account_status_id                          AS custAcct_status_id,
            custAcct.account_status_value                       AS custAcct_status_value,
            custAcct.account_status_description                 AS custAcct_status_description,
            custAcct.account_status_localization_code           AS custAcct_status_localization_code,
         FROM purchase_order_header po
            LEFT JOIN vendor                                      ON po.vendor_id = vendor.v_id
            JOIN purchase_order_status_type_domain statusType     ON po.status_type_id = statusType.id
            JOIN purchase_order_type_domain type                  ON po.type_id = type.id
            JOIN freight_on_board_type_domain freightOnboardType  ON po.freight_on_board_type_id = freightOnboardType.id
            JOIN freight_term_type_domain freightTermType         ON po.freight_term_type_id = freightTermType.id
            JOIN ship_location_type_domain shipLocType            ON po.ship_location_type_id = shipLocType.id
            LEFT JOIN employee approvedBy                         ON po.approved_by_id_sfk = approvedBy.emp_number AND po.company_id = approvedBy.comp_id
            LEFT JOIN employee purchaseAgent                      ON po.purchase_agent_id_sfk = purchaseAgent.emp_number AND po.company_id = purchaseAgent.comp_id
            JOIN ship_via shipVia                                 ON po.ship_via_id = shipVia.id
            LEFT JOIN vendor shipTo                               ON po.ship_to_id_sfk = shipTo.v_id
            JOIN vendor_payment_term paymentTermType              ON po.payment_term_type_id = paymentTermType.id
            JOIN exception_ind_type_domain exceptionIndType       ON po.exception_ind_type_id = exceptionIndType.id
            LEFT JOIN employee vendorSubmittedEmp                 ON po.vendor_submitted_employee_sfk = vendorSubmittedEmp.emp_number AND po.company_id = vendorSubmittedEmp.comp_id
            JOIN account custAcct                                 ON po.customer_account_number_sfk = custAcct.account_id
      """
   }

   fun findOne(id: Long, company: Company): PurchaseOrderEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()}\nWHERE po.id = :id AND po.company_id = :comp_id"

      logger.debug("Searching for PurchaseOrder using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val purchaseOrder = mapRow(rs, company, "po_")

         purchaseOrder
      }

      logger.trace("Searching for PurchaseOrder: {} resulted in {}", id, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<PurchaseOrderEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE po.company_id = :comp_id
            ORDER BY po_${page.snakeSortBy()} ${page.sortDirection()}
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
            elements.add(mapRow(rs, company, "po_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: PurchaseOrderEntity, company: Company): PurchaseOrderEntity {
      logger.debug("Inserting PurchaseOrder {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO purchase_order_header(
            number,
            company_id,
            vendor_id,
            status_type_id,
            order_date,
            type_id,
            freight_on_board_type_id,
            freight_term_type_id,
            ship_location_type_id,
            approved_by_id_sfk,
            total_amount,
            received_amount,
            paid_amount,
            purchase_agent_id_sfk,
            ship_via_id,
            required_date,
            ship_to_id_sfk,
            payment_term_type_id,
            message,
            total_landed_amount,
            total_freight_amount,
            exception_ind_type_id,
            vendor_submitted_time,
            vendor_submitted_employee_sfk,
            ecommerce_indicator,
            customer_account_number_sfk
         )
         VALUES (
            :number,
            :company_id,
            :vendor_id,
            :status_type_id,
            :order_date,
            :type_id,
            :freight_on_board_type_id,
            :freight_term_type_id,
            :ship_location_type_id,
            :approved_by_id_sfk,
            :total_amount,
            :received_amount,
            :paid_amount,
            :purchase_agent_id_sfk,
            :ship_via_id,
            :required_date,
            :ship_to_id_sfk,
            :payment_term_type_id,
            :message,
            :total_landed_amount,
            :total_freight_amount,
            :exception_ind_type_id,
            :vendor_submitted_time,
            :vendor_submitted_employee_sfk,
            :ecommerce_indicator,
            :customer_account_number_sfk
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "number" to entity.number,
            "company_id" to company.myId(),
            "vendor_id" to entity.vendor.id,
            "status_type_id" to entity.statusType.id,
            "order_date" to entity.orderDate,
            "type_id" to entity.type.id,
            "freight_on_board_type_id" to entity.freightOnboardType.id,
            "freight_term_type_id" to entity.freightTermType.id,
            "ship_location_type_id" to entity.shipLocationType.id,
            "approved_by_id_sfk" to entity.approvedBy.number,
            "total_amount" to entity.totalAmount,
            "received_amount" to entity.receivedAmount,
            "paid_amount" to entity.paidAmount,
            "purchase_agent_id_sfk" to entity.purchaseAgent.number,
            "ship_via_id" to entity.shipVia.id,
            "required_date" to entity.requiredDate,
            "ship_to_id_sfk" to entity.shipTo.id,
            "payment_term_type_id" to entity.paymentTermType.id,
            "message" to entity.message,
            "total_landed_amount" to entity.totalLandedAmount,
            "total_freight_amount" to entity.totalFreightAmount,
            "exception_ind_type_id" to entity.exceptionIndicatorType.id,
            "vendor_submitted_time" to entity.vendorSubmittedTime,
            "vendor_submitted_employee_sfk" to entity.vendorSubmittedEmployee?.number,
            "ecommerce_indicator" to entity.ecommerceIndicator,
            "customer_account_number_sfk" to entity.customerAccount?.id
         ),
         RowMapper { rs, _ -> mapRow(rs, entity) }
      )
   }

   @Transactional
   fun update(entity: PurchaseOrderEntity, company: Company): PurchaseOrderEntity {
      logger.debug("Updating PurchaseOrder {}", entity)

      return jdbc.updateReturning(
         """
            UPDATE purchase_order_header
            SET
               number = :number,
               company_id = :company_id,
               vendor_id = :vendor_id,
               status_type_id = :status_type_id,
               order_date = :order_date,
               type_id = :type_id,
               freight_on_board_type_id = :freight_on_board_type_id,
               freight_term_type_id = :freight_term_type_id,
               ship_location_type_id = :ship_location_type_id,
               approved_by_id_sfk = :approved_by_id_sfk,
               total_amount = :total_amount,
               received_amount = :received_amount,
               paid_amount = :paid_amount,
               purchase_agent_id_sfk = :purchase_agent_id_sfk,
               ship_via_id = :ship_via_id,
               required_date = :required_date,
               ship_to_id_sfk = :ship_to_id_sfk,
               payment_term_type_id = :payment_term_type_id,
               message = :message,
               total_landed_amount = :total_landed_amount,
               total_freight_amount = :total_freight_amount,
               exception_ind_type_id = :exception_ind_type_id,
               vendor_submitted_time = :vendor_submitted_time,
               vendor_submitted_employee_sfk = :vendor_submitted_employee_sfk,
               ecommerce_indicator = :ecommerce_indicator,
               customer_account_number_sfk = :customer_account_number_sfk
            WHERE id = :id
            RETURNING
               *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "number" to entity.number,
            "company_id" to company.myId(),
            "vendor_id" to entity.vendor.id,
            "status_type_id" to entity.statusType.id,
            "order_date" to entity.orderDate,
            "type_id" to entity.type.id,
            "freight_on_board_type_id" to entity.freightOnboardType.id,
            "freight_term_type_id" to entity.freightTermType.id,
            "ship_location_type_id" to entity.shipLocationType.id,
            "approved_by_id_sfk" to entity.approvedBy.number,
            "total_amount" to entity.totalAmount,
            "received_amount" to entity.receivedAmount,
            "paid_amount" to entity.paidAmount,
            "purchase_agent_id_sfk" to entity.purchaseAgent.number,
            "ship_via_id" to entity.shipVia.id,
            "required_date" to entity.requiredDate,
            "ship_to_id_sfk" to entity.shipTo.id,
            "payment_term_type_id" to entity.paymentTermType.id,
            "message" to entity.message,
            "total_landed_amount" to entity.totalLandedAmount,
            "total_freight_amount" to entity.totalFreightAmount,
            "exception_ind_type_id" to entity.exceptionIndicatorType.id,
            "vendor_submitted_time" to entity.vendorSubmittedTime,
            "vendor_submitted_employee_sfk" to entity.vendorSubmittedEmployee?.number,
            "ecommerce_indicator" to entity.ecommerceIndicator,
            "customer_account_number_sfk" to entity.customerAccount?.id
         ),
         RowMapper { rs, _ -> mapRow(rs, entity) }
      )
   }

   @Transactional
   fun delete(id: Long, company: Company) {
      logger.debug("Deleting PurchaseOrder with id={}", id)

      val rowsAffected = jdbc.update(
         """
         DELETE FROM purchase_order_header
         WHERE id = :id AND company_id = :company_id
         """,
         mapOf("id" to id, "company_id" to company.myId())
      )

      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) {
         throw NotFoundException(id)
      }
   }

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): PurchaseOrderEntity {
      return PurchaseOrderEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         vendor = vendorRepository.mapRow(rs, company, "${columnPrefix}vendor_"),
         statusType = statusTypeRepository.mapRow(rs, "${columnPrefix}statusType_"),
         orderDate = rs.getLocalDate("${columnPrefix}orderDate"),
         type = typeRepository.mapRow(rs, "${columnPrefix}type_"),
         freightOnboardType = freightOnboardTypeRepository.mapRow(rs, "${columnPrefix}freightOnboardType_"),
         freightTermType = freightTermTypeRepository.mapRow(rs, "${columnPrefix}freightTermType_"),
         shipLocationType = shipLocationTypeRepository.mapRow(rs, "${columnPrefix}shipLocType_"),
         approvedBy = employeeRepository.mapRow(rs, "${columnPrefix}approvedBy_"),
         totalAmount = rs.getBigDecimal("${columnPrefix}totalAmount"),
         receivedAmount = rs.getBigDecimal("${columnPrefix}receivedAmount"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paidAmount"),
         purchaseAgent = employeeRepository.mapRow(rs, "${columnPrefix}purchaseAgent_"),
         shipVia = shipViaRepository.mapRow(rs, "${columnPrefix}shipVia_"),
         requiredDate = rs.getLocalDate("${columnPrefix}requiredDate"),
         shipTo = vendorRepository.mapRow(rs, company, "${columnPrefix}shipTo_"),
         paymentTermType = paymentTermRepository.mapRow(rs, "${columnPrefix}paymentTermType_"),
         message = rs.getString("${columnPrefix}message"),
         totalLandedAmount = rs.getBigDecimal("${columnPrefix}totalLandedAmount"),
         totalFreightAmount = rs.getBigDecimal("${columnPrefix}totalFreightAmount"),
         exceptionIndicatorType = exceptionIndicatorTypeRepository.mapRow(rs, "${columnPrefix}exceptionIndType_"),
         vendorSubmittedTime = rs.getOffsetDateTimeOrNull("${columnPrefix}vendorSubmittedTime"),
         vendorSubmittedEmployee = employeeRepository.mapRowOrNull(rs,"${columnPrefix}vendorSubmittedEmp_"),
         ecommerceIndicator = rs.getBoolean("${columnPrefix}ecommerceIndicator"),
         customerAccount = accountRepository.mapRowOrNull(rs, company, "${columnPrefix}custAcct_")
      )
   }

   private fun mapRow(rs: ResultSet, entity: PurchaseOrderEntity, columnPrefix: String = EMPTY): PurchaseOrderEntity {
      return PurchaseOrderEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         vendor = entity.vendor,
         statusType = entity.statusType,
         orderDate = rs.getLocalDate("${columnPrefix}orderDate"),
         type = entity.type,
         freightOnboardType = entity.freightOnboardType,
         freightTermType = entity.freightTermType,
         shipLocationType = entity.shipLocationType,
         approvedBy = entity.approvedBy,
         totalAmount = rs.getBigDecimal("${columnPrefix}totalAmount"),
         receivedAmount = rs.getBigDecimal("${columnPrefix}receivedAmount"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paidAmount"),
         purchaseAgent = entity.purchaseAgent,
         shipVia = entity.shipVia,
         requiredDate = rs.getLocalDate("${columnPrefix}requiredDate"),
         shipTo = entity.shipTo,
         paymentTermType = entity.paymentTermType,
         message = rs.getString("${columnPrefix}message"),
         totalLandedAmount = rs.getBigDecimal("${columnPrefix}totalLandedAmount"),
         totalFreightAmount = rs.getBigDecimal("${columnPrefix}totalFreightAmount"),
         exceptionIndicatorType = entity.exceptionIndicatorType,
         vendorSubmittedTime = rs.getOffsetDateTimeOrNull("${columnPrefix}vendorSubmittedTime"),
         vendorSubmittedEmployee = entity.vendorSubmittedEmployee,
         ecommerceIndicator = rs.getBoolean("${columnPrefix}ecommerceIndicator"),
         customerAccount = entity.customerAccount
      )
   }
}
