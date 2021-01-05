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
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
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
   private val storeRepository: StoreRepository,
   private val typeRepository: PurchaseOrderTypeRepository,
   private val vendorRepository: VendorRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH account AS (
            ${accountRepository.selectBaseQuery()}
         ),
         employee AS (
            ${employeeRepository.employeeBaseQuery()}
         ),
         ship_via AS (
            ${shipViaRepository.baseSelectQuery()}
         ),
         vendor AS (
            ${vendorRepository.baseSelectQuery()}
         ),
         vendor_payment_term AS (
            ${paymentTermRepository.findOneQuery()}
         )
         SELECT
            po.id                                               AS po_id,
            po.uu_row_id                                        AS po_uu_row_id,
            po.time_created                                     AS po_time_created,
            po.time_updated                                     AS po_time_updated,
            po.number                                           AS po_number,
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
            vendor.v_id                                         AS po_vendor_id,
            vendor.v_uu_row_id                                  AS po_vendor_uu_row_id,
            vendor.v_time_created                               AS po_vendor_time_created,
            vendor.v_time_updated                               AS po_vendor_time_updated,
            vendor.v_company_id                                 AS po_vendor_company_id,
            vendor.v_number                                     AS po_vendor_number,
            vendor.v_name                                       AS po_vendor_name,
            vendor.v_account_number                             AS po_vendor_account_number,
            vendor.v_pay_to_id                                  AS po_vendor_pay_to_id,
            vendor.v_freight_on_board_type_id                   AS po_vendor_freight_on_board_type_id,
            vendor.v_payment_terms_id                           AS po_vendor_payment_terms_id,
            vendor.v_normal_days                                AS po_vendor_normal_days,
            vendor.v_return_policy                              AS po_vendor_return_policy,
            vendor.v_ship_via_id                                AS po_vendor_ship_via_id,
            vendor.v_group_id                                   AS po_vendor_group_id,
            vendor.v_minimum_quantity                           AS po_vendor_minimum_quantity,
            vendor.v_minimum_amount                             AS po_vendor_minimum_amount,
            vendor.v_free_ship_quantity                         AS po_vendor_free_ship_quantity,
            vendor.v_free_ship_amount                           AS po_vendor_free_ship_amount,
            vendor.v_vendor_1099                                AS po_vendor_vendor_1099,
            vendor.v_federal_id_number                          AS po_vendor_federal_id_number,
            vendor.v_sales_representative_name                  AS po_vendor_sales_representative_name,
            vendor.v_sales_representative_fax                   AS po_vendor_sales_representative_fax,
            vendor.v_separate_check                             AS po_vendor_separate_check,
            vendor.v_bump_percent                               AS po_vendor_bump_percent,
            vendor.v_freight_calc_method_type_id                AS po_vendor_freight_calc_method_type_id,
            vendor.v_freight_percent                            AS po_vendor_freight_percent,
            vendor.v_freight_amount                             AS po_vendor_freight_amount,
            vendor.v_charge_inventory_tax_1                     AS po_vendor_charge_inventory_tax_1,
            vendor.v_charge_inventory_tax_2                     AS po_vendor_charge_inventory_tax_2,
            vendor.v_charge_inventory_tax_3                     AS po_vendor_charge_inventory_tax_3,
            vendor.v_charge_inventory_tax_4                     AS po_vendor_charge_inventory_tax_4,
            vendor.v_federal_id_number_verification             AS po_vendor_federal_id_number_verification,
            vendor.v_email_address                              AS po_vendor_email_address,
            vendor.v_purchase_order_submit_email_address        AS po_vendor_purchase_order_submit_email_address,
            vendor.v_allow_drop_ship_to_customer                AS po_vendor_allow_drop_ship_to_customer,
            vendor.v_auto_submit_purchase_order                 AS po_vendor_auto_submit_purchase_order,
            vendor.v_note                                       AS po_vendor_note,
            vendor.v_phone_number                               AS po_vendor_phone_number,
            vendor.v_comp_id                                    AS po_vendor_comp_id,
            vendor.v_comp_uu_row_id                             AS po_vendor_comp_uu_row_id,
            vendor.v_comp_time_created                          AS po_vendor_comp_time_created,
            vendor.v_comp_time_updated                          AS po_vendor_comp_time_updated,
            vendor.v_comp_name                                  AS po_vendor_comp_name,
            vendor.v_comp_doing_business_as                     AS po_vendor_comp_doing_business_as,
            vendor.v_comp_client_code                           AS po_vendor_comp_client_code,
            vendor.v_comp_client_id                             AS po_vendor_comp_client_id,
            vendor.v_comp_dataset_code                          AS po_vendor_comp_dataset_code,
            vendor.v_comp_federal_id_number                     AS po_vendor_comp_federal_id_number,
            vendor.v_comp_address_id                            AS po_vendor_comp_address_id,
            vendor.v_comp_address_name                          AS po_vendor_comp_address_name,
            vendor.v_comp_address_address1                      AS po_vendor_comp_address_address1,
            vendor.v_comp_address_address2                      AS po_vendor_comp_address_address2,
            vendor.v_comp_address_city                          AS po_vendor_comp_address_city,
            vendor.v_comp_address_state                         AS po_vendor_comp_address_state,
            vendor.v_comp_address_postal_code                   AS po_vendor_comp_address_postal_code,
            vendor.v_comp_address_latitude                      AS po_vendor_comp_address_latitude,
            vendor.v_comp_address_longitude                     AS po_vendor_comp_address_longitude,
            vendor.v_comp_address_country                       AS po_vendor_comp_address_country,
            vendor.v_comp_address_county                        AS po_vendor_comp_address_county,
            vendor.v_comp_address_phone                         AS po_vendor_comp_address_phone,
            vendor.v_comp_address_fax                           AS po_vendor_comp_address_fax,
            vendor.v_onboard_id                                 AS po_vendor_onboard_id,
            vendor.v_onboard_value                              AS po_vendor_onboard_value,
            vendor.v_onboard_description                        AS po_vendor_onboard_description,
            vendor.v_onboard_localization_code                  AS po_vendor_onboard_localization_code,
            vendor.v_method_id                                  AS po_vendor_method_id,
            vendor.v_method_value                               AS po_vendor_method_value,
            vendor.v_method_description                         AS po_vendor_method_description,
            vendor.v_method_localization_code                   AS po_vendor_method_localization_code,
            vendor.v_address_id                                 AS po_vendor_address_id,
            vendor.v_address_uu_row_id                          AS po_vendor_address_uu_row_id,
            vendor.v_address_time_created                       AS po_vendor_address_time_created,
            vendor.v_address_time_updated                       AS po_vendor_address_time_updated,
            vendor.v_address_number                             AS po_vendor_address_number,
            vendor.v_address_name                               AS po_vendor_address_name,
            vendor.v_address_address1                           AS po_vendor_address_address1,
            vendor.v_address_address2                           AS po_vendor_address_address2,
            vendor.v_address_city                               AS po_vendor_address_city,
            vendor.v_address_state                              AS po_vendor_address_state,
            vendor.v_address_postal_code                        AS po_vendor_address_postal_code,
            vendor.v_address_latitude                           AS po_vendor_address_latitude,
            vendor.v_address_longitude                          AS po_vendor_address_longitude,
            vendor.v_address_country                            AS po_vendor_address_country,
            vendor.v_address_county                             AS po_vendor_address_county,
            vendor.v_address_phone                              AS po_vendor_address_phone,
            vendor.v_address_fax                                AS po_vendor_address_fax,
            vendor.v_vpt_id                                     AS po_vendor_vpt_id,
            vendor.v_vpt_uu_row_id                              AS po_vendor_vpt_uu_row_id,
            vendor.v_vpt_time_created                           AS po_vendor_vpt_time_created,
            vendor.v_vpt_time_updated                           AS po_vendor_vpt_time_updated,
            vendor.v_vpt_company_id                             AS po_vendor_vpt_company_id,
            vendor.v_vpt_description                            AS po_vendor_vpt_description,
            vendor.v_vpt_number                                 AS po_vendor_vpt_number,
            vendor.v_vpt_number_of_payments                     AS po_vendor_vpt_number_of_payments,
            vendor.v_vpt_discount_month                         AS po_vendor_vpt_discount_month,
            vendor.v_vpt_discount_days                          AS po_vendor_vpt_discount_days,
            vendor.v_vpt_discount_percent                       AS po_vendor_vpt_discount_percent,
            vendor.v_shipVia_id                                 AS po_vendor_shipVia_id,
            vendor.v_shipVia_uu_row_id                          AS po_vendor_shipVia_uu_row_id,
            vendor.v_shipVia_time_created                       AS po_vendor_shipVia_time_created,
            vendor.v_shipVia_time_updated                       AS po_vendor_shipVia_time_updated,
            vendor.v_shipVia_description                        AS po_vendor_shipVia_description,
            vendor.v_shipVia_number                             AS po_vendor_shipVia_number,
            vendor.v_vgrp_id                                    AS po_vendor_vgrp_id,
            vendor.v_vgrp_uu_row_id                             AS po_vendor_vgrp_uu_row_id,
            vendor.v_vgrp_time_created                          AS po_vendor_vgrp_time_created,
            vendor.v_vgrp_time_updated                          AS po_vendor_vgrp_time_updated,
            vendor.v_vgrp_company_id                            AS po_vendor_vgrp_company_id,
            vendor.v_vgrp_value                                 AS po_vendor_vgrp_value,
            vendor.v_vgrp_description                           AS po_vendor_vgrp_description,
            count(*) OVER()                                     AS total_elements,
            statusType.id                                       AS po_statusType_id,
            statusType.value                                    AS po_statusType_value,
            statusType.description                              AS po_statusType_description,
            statusType.localization_code                        AS po_statusType_localization_code,
            type.id                                             AS po_type_id,
            type.value                                          AS po_type_value,
            type.description                                    AS po_type_description,
            type.localization_code                              AS po_type_localization_code,
            freightOnboardType.id                               AS po_freightOnboardType_id,
            freightOnboardType.value                            AS po_freightOnboardType_value,
            freightOnboardType.description                      AS po_freightOnboardType_description,
            freightOnboardType.localization_code                AS po_freightOnboardType_localization_code,
            freightTermType.id                                  AS po_freightTermType_id,
            freightTermType.value                               AS po_freightTermType_value,
            freightTermType.description                         AS po_freightTermType_description,
            freightTermType.localization_code                   AS po_freightTermType_localization_code,
            shipLocType.id                                      AS po_shipLocType_id,
            shipLocType.value                                   AS po_shipLocType_value,
            shipLocType.description                             AS po_shipLocType_description,
            shipLocType.localization_code                       AS po_shipLocType_localization_code,
            approvedBy.emp_id                                   AS po_approvedBy_id,
            approvedBy.emp_number                               AS po_approvedBy_number,
            approvedBy.emp_last_name                            AS po_approvedBy_last_name,
            approvedBy.emp_first_name_mi                        AS po_approvedBy_first_name_mi,
            approvedBy.emp_type                                 AS po_approvedBy_type,
            approvedBy.emp_pass_code                            AS po_approvedBy_pass_code,
            approvedBy.emp_active                               AS po_approvedBy_active,
            approvedBy.emp_department                           AS po_approvedBy_department,
            approvedBy.emp_cynergi_system_admin                 AS po_approvedBy_cynergi_system_admin,
            approvedBy.emp_alternative_store_indicator          AS po_approvedBy_alternative_store_indicator,
            approvedBy.emp_alternative_area                     AS po_approvedBy_alternative_area,
            approvedBy.store_id                                 AS po_approvedBy_store_id,
            approvedBy.store_number                             AS po_approvedBy_store_number,
            approvedBy.store_name                               AS po_approvedBy_store_name,
            approvedBy.dept_id                                  AS po_approvedBy_dept_id,
            approvedBy.dept_code                                AS po_approvedBy_dept_code,
            approvedBy.dept_description                         AS po_approvedBy_dept_description,
            approvedBy.comp_id                                  AS po_approvedBy_comp_id,
            approvedBy.comp_uu_row_id                           AS po_approvedBy_comp_uu_row_id,
            approvedBy.comp_time_created                        AS po_approvedBy_comp_time_created,
            approvedBy.comp_time_updated                        AS po_approvedBy_comp_time_updated,
            approvedBy.comp_name                                AS po_approvedBy_comp_name,
            approvedBy.comp_doing_business_as                   AS po_approvedBy_comp_doing_business_as,
            approvedBy.comp_client_code                         AS po_approvedBy_comp_client_code,
            approvedBy.comp_client_id                           AS po_approvedBy_comp_client_id,
            approvedBy.comp_dataset_code                        AS po_approvedBy_comp_dataset_code,
            approvedBy.comp_federal_id_number                   AS po_approvedBy_comp_federal_id_number,
            approvedBy.address_id                               AS po_approvedBy_comp_address_id,
            approvedBy.address_name                             AS po_approvedBy_comp_address_name,
            approvedBy.address_address1                         AS po_approvedBy_comp_address_address1,
            approvedBy.address_address2                         AS po_approvedBy_comp_address_address2,
            approvedBy.address_city                             AS po_approvedBy_comp_address_city,
            approvedBy.address_state                            AS po_approvedBy_comp_address_state,
            approvedBy.address_postal_code                      AS po_approvedBy_comp_address_postal_code,
            approvedBy.address_latitude                         AS po_approvedBy_comp_address_latitude,
            approvedBy.address_longitude                        AS po_approvedBy_comp_address_longitude,
            approvedBy.address_country                          AS po_approvedBy_comp_address_country,
            approvedBy.address_county                           AS po_approvedBy_comp_address_county,
            approvedBy.address_phone                            AS po_approvedBy_comp_address_phone,
            approvedBy.address_fax                              AS po_approvedBy_comp_address_fax,
            purchaseAgent.emp_id                                AS po_purchaseAgent_id,
            purchaseAgent.emp_number                            AS po_purchaseAgent_number,
            purchaseAgent.emp_last_name                         AS po_purchaseAgent_last_name,
            purchaseAgent.emp_first_name_mi                     AS po_purchaseAgent_first_name_mi,
            purchaseAgent.emp_type                              AS po_purchaseAgent_type,
            purchaseAgent.emp_pass_code                         AS po_purchaseAgent_pass_code,
            purchaseAgent.emp_active                            AS po_purchaseAgent_active,
            purchaseAgent.emp_department                        AS po_purchaseAgent_department,
            purchaseAgent.emp_cynergi_system_admin              AS po_purchaseAgent_cynergi_system_admin,
            purchaseAgent.emp_alternative_store_indicator       AS po_purchaseAgent_alternative_store_indicator,
            purchaseAgent.emp_alternative_area                  AS po_purchaseAgent_alternative_area,
            purchaseAgent.store_id                              AS po_purchaseAgent_store_id,
            purchaseAgent.store_number                          AS po_purchaseAgent_store_number,
            purchaseAgent.store_name                            AS po_purchaseAgent_store_name,
            purchaseAgent.dept_id                               AS po_purchaseAgent_dept_id,
            purchaseAgent.dept_code                             AS po_purchaseAgent_dept_code,
            purchaseAgent.dept_description                      AS po_purchaseAgent_dept_description,
            purchaseAgent.comp_id                               AS po_purchaseAgent_comp_id,
            purchaseAgent.comp_uu_row_id                        AS po_purchaseAgent_comp_uu_row_id,
            purchaseAgent.comp_time_created                     AS po_purchaseAgent_comp_time_created,
            purchaseAgent.comp_time_updated                     AS po_purchaseAgent_comp_time_updated,
            purchaseAgent.comp_name                             AS po_purchaseAgent_comp_name,
            purchaseAgent.comp_doing_business_as                AS po_purchaseAgent_comp_doing_business_as,
            purchaseAgent.comp_client_code                      AS po_purchaseAgent_comp_client_code,
            purchaseAgent.comp_client_id                        AS po_purchaseAgent_comp_client_id,
            purchaseAgent.comp_dataset_code                     AS po_purchaseAgent_comp_dataset_code,
            purchaseAgent.comp_federal_id_number                AS po_purchaseAgent_comp_federal_id_number,
            purchaseAgent.address_id                            AS po_purchaseAgent_comp_address_id,
            purchaseAgent.address_name                          AS po_purchaseAgent_comp_address_name,
            purchaseAgent.address_address1                      AS po_purchaseAgent_comp_address_address1,
            purchaseAgent.address_address2                      AS po_purchaseAgent_comp_address_address2,
            purchaseAgent.address_city                          AS po_purchaseAgent_comp_address_city,
            purchaseAgent.address_state                         AS po_purchaseAgent_comp_address_state,
            purchaseAgent.address_postal_code                   AS po_purchaseAgent_comp_address_postal_code,
            purchaseAgent.address_latitude                      AS po_purchaseAgent_comp_address_latitude,
            purchaseAgent.address_longitude                     AS po_purchaseAgent_comp_address_longitude,
            purchaseAgent.address_country                       AS po_purchaseAgent_comp_address_country,
            purchaseAgent.address_county                        AS po_purchaseAgent_comp_address_county,
            purchaseAgent.address_phone                         AS po_purchaseAgent_comp_address_phone,
            purchaseAgent.address_fax                           AS po_purchaseAgent_comp_address_fax,
            shipVia.id                                          AS po_shipVia_id,
            shipVia.uu_row_id                                   AS po_shipVia_uu_row_id,
            shipVia.time_created                                AS po_shipVia_time_created,
            shipVia.time_updated                                AS po_shipVia_time_updated,
            shipVia.description                                 AS po_shipVia_description,
            shipVia.number                                      AS po_shipVia_number,
            shipVia.comp_id                                     AS po_shipVia_comp_id,
            shipVia.comp_uu_row_id                              AS po_shipVia_comp_uu_row_id,
            shipVia.comp_name                                   AS po_shipVia_comp_name,
            shipVia.comp_doing_business_as                      AS po_shipVia_comp_doing_business_as,
            shipVia.comp_client_code                            AS po_shipVia_comp_client_code,
            shipVia.comp_client_id                              AS po_shipVia_comp_client_id,
            shipVia.comp_dataset_code                           AS po_shipVia_comp_dataset_code,
            shipVia.comp_federal_id_number                      AS po_shipVia_comp_federal_id_number,
            shipVia.address_id                                  AS po_shipVia_address_id,
            shipVia.address_name                                AS po_shipVia_address_name,
            shipVia.address_address1                            AS po_shipVia_address_address1,
            shipVia.address_address2                            AS po_shipVia_address_address2,
            shipVia.address_city                                AS po_shipVia_address_city,
            shipVia.address_state                               AS po_shipVia_address_state,
            shipVia.address_postal_code                         AS po_shipVia_address_postal_code,
            shipVia.address_latitude                            AS po_shipVia_address_latitude,
            shipVia.address_longitude                           AS po_shipVia_address_longitude,
            shipVia.address_country                             AS po_shipVia_address_country,
            shipVia.address_county                              AS po_shipVia_address_county,
            shipVia.address_phone                               AS po_shipVia_address_phone,
            shipVia.address_fax                                 AS po_shipVia_address_fax,
            count(*) OVER()                                     AS total_elements,
            shipTo.id                                           AS po_shipTo_id,
            shipTo.number                                       AS po_shipTo_number,
            shipTo.name                                         AS po_shipTo_name,
            shipTo.dataset                                      AS po_shipTo_dataset,
            paymentTermType.vpt_id                              AS po_paymentTermType_vpt_id,
            paymentTermType.vpt_uu_row_id                       AS po_paymentTermType_vpt_uu_row_id,
            paymentTermType.vpt_time_created                    AS po_paymentTermType_vpt_time_created,
            paymentTermType.vpt_time_updated                    AS po_paymentTermType_vpt_time_updated,
            paymentTermType.vpt_company_id                      AS po_paymentTermType_vpt_company_id,
            paymentTermType.vpt_description                     AS po_paymentTermType_vpt_description,
            paymentTermType.vpt_number                          AS po_paymentTermType_vpt_number,
            paymentTermType.vpt_number_of_payments              AS po_paymentTermType_vpt_number_of_payments,
            paymentTermType.vpt_discount_month                  AS po_paymentTermType_vpt_discount_month,
            paymentTermType.vpt_discount_days                   AS po_paymentTermType_vpt_discount_days,
            paymentTermType.vpt_discount_percent                AS po_paymentTermType_vpt_discount_percent,
            paymentTermType.comp_id                             AS po_paymentTermType_comp_id,
            paymentTermType.comp_uu_row_id                      AS po_paymentTermType_comp_uu_row_id,
            paymentTermType.comp_time_created                   AS po_paymentTermType_comp_time_created,
            paymentTermType.comp_time_updated                   AS po_paymentTermType_comp_time_updated,
            paymentTermType.comp_name                           AS po_paymentTermType_comp_name,
            paymentTermType.comp_doing_business_as              AS po_paymentTermType_comp_doing_business_as,
            paymentTermType.comp_client_code                    AS po_paymentTermType_comp_client_code,
            paymentTermType.comp_client_id                      AS po_paymentTermType_comp_client_id,
            paymentTermType.comp_dataset_code                   AS po_paymentTermType_comp_dataset_code,
            paymentTermType.comp_federal_id_number              AS po_paymentTermType_comp_federal_id_number,
            paymentTermType.address_id                          AS po_paymentTermType_address_id,
            paymentTermType.address_name                        AS po_paymentTermType_address_name,
            paymentTermType.address_address1                    AS po_paymentTermType_address_address1,
            paymentTermType.address_address2                    AS po_paymentTermType_address_address2,
            paymentTermType.address_city                        AS po_paymentTermType_address_city,
            paymentTermType.address_state                       AS po_paymentTermType_address_state,
            paymentTermType.address_postal_code                 AS po_paymentTermType_address_postal_code,
            paymentTermType.address_latitude                    AS po_paymentTermType_address_latitude,
            paymentTermType.address_longitude                   AS po_paymentTermType_address_longitude,
            paymentTermType.address_country                     AS po_paymentTermType_address_country,
            paymentTermType.address_county                      AS po_paymentTermType_address_county,
            paymentTermType.address_phone                       AS po_paymentTermType_address_phone,
            paymentTermType.address_fax                         AS po_paymentTermType_address_fax,
            paymentTermType.vpts_id                             AS po_paymentTermType_vpts_id,
            paymentTermType.vpts_uu_row_id                      AS po_paymentTermType_vpts_uu_row_id,
            paymentTermType.vpts_time_created                   AS po_paymentTermType_vpts_time_created,
            paymentTermType.vpts_time_updated                   AS po_paymentTermType_vpts_time_updated,
            paymentTermType.vpts_payment_term_id                AS po_paymentTermType_vpts_payment_term_id,
            paymentTermType.vpts_due_month                      AS po_paymentTermType_vpts_due_month,
            paymentTermType.vpts_due_days                       AS po_paymentTermType_vpts_due_days,
            paymentTermType.vpts_due_percent                    AS po_paymentTermType_vpts_due_percent,
            paymentTermType.vpts_schedule_order_number          AS po_paymentTermType_vpts_schedule_order_number,
            count(*) OVER()                                     AS total_elements,
            exceptionIndType.id                                 AS po_exceptionIndType_id,
            exceptionIndType.value                              AS po_exceptionIndType_value,
            exceptionIndType.description                        AS po_exceptionIndType_description,
            exceptionIndType.localization_code                  AS po_exceptionIndType_localization_code,
            vendorSubmittedEmp.emp_id                           AS po_vendorSubmittedEmp_id,
            vendorSubmittedEmp.emp_number                       AS po_vendorSubmittedEmp_number,
            vendorSubmittedEmp.emp_last_name                    AS po_vendorSubmittedEmp_last_name,
            vendorSubmittedEmp.emp_first_name_mi                AS po_vendorSubmittedEmp_first_name_mi,
            vendorSubmittedEmp.emp_type                         AS po_vendorSubmittedEmp_type,
            vendorSubmittedEmp.emp_pass_code                    AS po_vendorSubmittedEmp_pass_code,
            vendorSubmittedEmp.emp_active                       AS po_vendorSubmittedEmp_active,
            vendorSubmittedEmp.emp_department                   AS po_vendorSubmittedEmp_department,
            vendorSubmittedEmp.emp_cynergi_system_admin         AS po_vendorSubmittedEmp_cynergi_system_admin,
            vendorSubmittedEmp.emp_alternative_store_indicator  AS po_vendorSubmittedEmp_alternative_store_indicator,
            vendorSubmittedEmp.emp_alternative_area             AS po_vendorSubmittedEmp_alternative_area,
            vendorSubmittedEmp.store_id                         AS po_vendorSubmittedEmp_store_id,
            vendorSubmittedEmp.store_number                     AS po_vendorSubmittedEmp_store_number,
            vendorSubmittedEmp.store_name                       AS po_vendorSubmittedEmp_store_name,
            vendorSubmittedEmp.dept_id                          AS po_vendorSubmittedEmp_dept_id,
            vendorSubmittedEmp.dept_code                        AS po_vendorSubmittedEmp_dept_code,
            vendorSubmittedEmp.dept_description                 AS po_vendorSubmittedEmp_dept_description,
            vendorSubmittedEmp.comp_id                          AS po_vendorSubmittedEmp_comp_id,
            vendorSubmittedEmp.comp_uu_row_id                   AS po_vendorSubmittedEmp_comp_uu_row_id,
            vendorSubmittedEmp.comp_time_created                AS po_vendorSubmittedEmp_comp_time_created,
            vendorSubmittedEmp.comp_time_updated                AS po_vendorSubmittedEmp_comp_time_updated,
            vendorSubmittedEmp.comp_name                        AS po_vendorSubmittedEmp_comp_name,
            vendorSubmittedEmp.comp_doing_business_as           AS po_vendorSubmittedEmp_comp_doing_business_as,
            vendorSubmittedEmp.comp_client_code                 AS po_vendorSubmittedEmp_comp_client_code,
            vendorSubmittedEmp.comp_client_id                   AS po_vendorSubmittedEmp_comp_client_id,
            vendorSubmittedEmp.comp_dataset_code                AS po_vendorSubmittedEmp_comp_dataset_code,
            vendorSubmittedEmp.comp_federal_id_number           AS po_vendorSubmittedEmp_comp_federal_id_number,
            vendorSubmittedEmp.address_id                       AS po_vendorSubmittedEmp_comp_address_id,
            vendorSubmittedEmp.address_name                     AS po_vendorSubmittedEmp_comp_address_name,
            vendorSubmittedEmp.address_address1                 AS po_vendorSubmittedEmp_comp_address_address1,
            vendorSubmittedEmp.address_address2                 AS po_vendorSubmittedEmp_comp_address_address2,
            vendorSubmittedEmp.address_city                     AS po_vendorSubmittedEmp_comp_address_city,
            vendorSubmittedEmp.address_state                    AS po_vendorSubmittedEmp_comp_address_state,
            vendorSubmittedEmp.address_postal_code              AS po_vendorSubmittedEmp_comp_address_postal_code,
            vendorSubmittedEmp.address_latitude                 AS po_vendorSubmittedEmp_comp_address_latitude,
            vendorSubmittedEmp.address_longitude                AS po_vendorSubmittedEmp_comp_address_longitude,
            vendorSubmittedEmp.address_country                  AS po_vendorSubmittedEmp_comp_address_country,
            vendorSubmittedEmp.address_county                   AS po_vendorSubmittedEmp_comp_address_county,
            vendorSubmittedEmp.address_phone                    AS po_vendorSubmittedEmp_comp_address_phone,
            vendorSubmittedEmp.address_fax                      AS po_vendorSubmittedEmp_comp_address_fax,
            custAcct.account_id                                 AS po_custAcct_id,
            custAcct.account_number                             AS po_custAcct_number,
            custAcct.account_name                               AS po_custAcct_name,
            custAcct.account_form_1099_field                    AS po_custAcct_form_1099_field,
            custAcct.account_corporate_account_indicator        AS po_custAcct_corporate_account_indicator,
            custAcct.account_comp_id                            AS po_custAcct_comp_id,
            custAcct.account_type_id                            AS po_custAcct_type_id,
            custAcct.account_type_value                         AS po_custAcct_type_value,
            custAcct.account_type_description                   AS po_custAcct_type_description,
            custAcct.account_type_localization_code             AS po_custAcct_type_localization_code,
            custAcct.account_balance_type_id                    AS po_custAcct_balance_type_id,
            custAcct.account_balance_type_value                 AS po_custAcct_balance_type_value,
            custAcct.account_balance_type_description           AS po_custAcct_balance_type_description,
            custAcct.account_balance_type_localization_code     AS po_custAcct_balance_type_localization_code,
            custAcct.account_status_id                          AS po_custAcct_status_id,
            custAcct.account_status_value                       AS po_custAcct_status_value,
            custAcct.account_status_description                 AS po_custAcct_status_description,
            custAcct.account_status_localization_code           AS po_custAcct_status_localization_code
         FROM purchase_order_header po
            JOIN company comp ON po.company_id = comp.id
            JOIN vendor                                           ON po.vendor_id = vendor.v_id
            JOIN purchase_order_status_type_domain statusType     ON po.status_type_id = statusType.id
            JOIN purchase_order_type_domain type                  ON po.type_id = type.id
            JOIN freight_on_board_type_domain freightOnboardType  ON po.freight_on_board_type_id = freightOnboardType.id
            JOIN freight_term_type_domain freightTermType         ON po.freight_term_type_id = freightTermType.id
            JOIN ship_location_type_domain shipLocType            ON po.ship_location_type_id = shipLocType.id
            LEFT JOIN employee approvedBy                         ON po.approved_by_id_sfk = approvedBy.emp_number AND po.company_id = approvedBy.comp_id
            LEFT JOIN employee purchaseAgent                      ON po.purchase_agent_id_sfk = purchaseAgent.emp_number AND po.company_id = purchaseAgent.comp_id
            JOIN ship_via shipVia                                 ON po.ship_via_id = shipVia.id
            JOIN fastinfo_prod_import.store_vw shipTo
               ON shipTo.dataset = comp.dataset_code
                  AND shipTo.number = po.ship_to_id_sfk
            JOIN vendor_payment_term paymentTermType              ON po.payment_term_type_id = paymentTermType.vpt_id
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
            "ship_to_id_sfk" to entity.shipTo.myNumber(),
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
            "ship_to_id_sfk" to entity.shipTo.myNumber(),
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
         orderDate = rs.getLocalDate("${columnPrefix}order_date"),
         type = typeRepository.mapRow(rs, "${columnPrefix}type_"),
         freightOnboardType = freightOnboardTypeRepository.mapRow(rs, "${columnPrefix}freightOnboardType_"),
         freightTermType = freightTermTypeRepository.mapRow(rs, "${columnPrefix}freightTermType_"),
         shipLocationType = shipLocationTypeRepository.mapRow(rs, "${columnPrefix}shipLocType_"),
         approvedBy = employeeRepository.mapRow(rs, "${columnPrefix}approvedBy_", "${columnPrefix}approvedBy_comp_", "${columnPrefix}approvedBy_comp_address_", "${columnPrefix}approvedBy_dept_", "${columnPrefix}approvedBy_store_"),
         totalAmount = rs.getBigDecimal("${columnPrefix}total_amount"),
         receivedAmount = rs.getBigDecimal("${columnPrefix}received_amount"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paid_amount"),
         purchaseAgent = employeeRepository.mapRow(rs, "${columnPrefix}purchaseAgent_", "${columnPrefix}purchaseAgent_comp_", "${columnPrefix}purchaseAgent_comp_address_", "${columnPrefix}purchaseAgent_dept_", "${columnPrefix}purchaseAgent_store_"),
         shipVia = shipViaRepository.mapRow(rs, "${columnPrefix}shipVia_"),
         requiredDate = rs.getLocalDate("${columnPrefix}required_date"),
         shipTo = storeRepository.mapRow(rs, company, "${columnPrefix}shipTo_"),
         paymentTermType = paymentTermRepository.mapRow(rs, "${columnPrefix}paymentTermType_"),
         message = rs.getString("${columnPrefix}message"),
         totalLandedAmount = rs.getBigDecimal("${columnPrefix}total_landed_amount"),
         totalFreightAmount = rs.getBigDecimal("${columnPrefix}total_freight_amount"),
         exceptionIndicatorType = exceptionIndicatorTypeRepository.mapRow(rs, "${columnPrefix}exceptionIndType_"),
         vendorSubmittedTime = rs.getOffsetDateTimeOrNull("${columnPrefix}vendor_submitted_time"),
         vendorSubmittedEmployee = employeeRepository.mapRowOrNull(rs, "${columnPrefix}vendorSubmittedEmp_", "${columnPrefix}vendorSubmittedEmp_comp_", "${columnPrefix}vendorSubmittedEmp_comp_address_", "${columnPrefix}vendorSubmittedEmp_dept_", "${columnPrefix}vendorSubmittedEmp_store_"),
         ecommerceIndicator = rs.getBoolean("${columnPrefix}ecommerce_indicator"),
         customerAccount = accountRepository.mapRowOrNull(rs, company, "${columnPrefix}custAcct_")
      )
   }

   private fun mapRow(rs: ResultSet, entity: PurchaseOrderEntity, columnPrefix: String = EMPTY): PurchaseOrderEntity {
      return PurchaseOrderEntity(
         id = rs.getLong("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         vendor = entity.vendor,
         statusType = entity.statusType,
         orderDate = rs.getLocalDate("${columnPrefix}order_date"),
         type = entity.type,
         freightOnboardType = entity.freightOnboardType,
         freightTermType = entity.freightTermType,
         shipLocationType = entity.shipLocationType,
         approvedBy = entity.approvedBy,
         totalAmount = rs.getBigDecimal("${columnPrefix}total_amount"),
         receivedAmount = rs.getBigDecimal("${columnPrefix}received_amount"),
         paidAmount = rs.getBigDecimal("${columnPrefix}paid_amount"),
         purchaseAgent = entity.purchaseAgent,
         shipVia = entity.shipVia,
         requiredDate = rs.getLocalDate("${columnPrefix}required_date"),
         shipTo = entity.shipTo,
         paymentTermType = entity.paymentTermType,
         message = rs.getString("${columnPrefix}message"),
         totalLandedAmount = rs.getBigDecimal("${columnPrefix}total_landed_amount"),
         totalFreightAmount = rs.getBigDecimal("${columnPrefix}total_freight_amount"),
         exceptionIndicatorType = entity.exceptionIndicatorType,
         vendorSubmittedTime = rs.getOffsetDateTimeOrNull("${columnPrefix}vendor_submitted_time"),
         vendorSubmittedEmployee = entity.vendorSubmittedEmployee,
         ecommerceIndicator = rs.getBoolean("${columnPrefix}ecommerce_indicator"),
         customerAccount = entity.customerAccount
      )
   }
}
