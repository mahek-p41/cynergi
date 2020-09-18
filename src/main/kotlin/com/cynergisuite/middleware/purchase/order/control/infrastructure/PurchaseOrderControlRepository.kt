package com.cynergisuite.middleware.purchase.order.control.infrastructure

import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.DefaultAccountPayableStatusType
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.DefaultAccountPayableStatusTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.purchase.order.ApprovalRequiredFlagType
import com.cynergisuite.middleware.purchase.order.DefaultPurchaseOrderType
import com.cynergisuite.middleware.purchase.order.UpdatePurchaseOrderCostType
import com.cynergisuite.middleware.purchase.order.control.PurchaseOrderControlEntity
import com.cynergisuite.middleware.purchase.order.infrastructure.ApprovalRequiredFlagTypeRepository
import com.cynergisuite.middleware.purchase.order.infrastructure.DefaultPurchaseOrderTypeRepository
import com.cynergisuite.middleware.purchase.order.infrastructure.UpdatePurchaseOrderCostTypeRepository
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
class PurchaseOrderControlRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val defaultAccountPayableStatusTypeRepository: DefaultAccountPayableStatusTypeRepository,
   private val vendorRepository: VendorRepository,
   private val updatePurchaseOrderCostTypeRepository: UpdatePurchaseOrderCostTypeRepository,
   private val defaultPurchaseOrderTypeRepository: DefaultPurchaseOrderTypeRepository,
   private val employeeRepository: EmployeeRepository,
   private val approvalRequiredFlagTypeRepository: ApprovalRequiredFlagTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderControlRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH vendor AS (
            ${vendorRepository.baseSelectQuery()}
         ),
         employee AS (
            ${employeeRepository.employeeBaseQuery()}
         )
         SELECT
            purchaseOrderControl.id                                              AS purchaseOrderControl_id,
            purchaseOrderControl.uu_row_id                                       AS purchaseOrderControl_uu_row_id,
            purchaseOrderControl.time_created                                    AS purchaseOrderControl_time_created,
            purchaseOrderControl.time_updated                                    AS purchaseOrderControl_time_updated,
            purchaseOrderControl.company_id                                      AS purchaseOrderControl_company_id,
            purchaseOrderControl.drop_five_characters_on_model_number            AS purchaseOrderControl_drop_five_characters_on_model_number,
            purchaseOrderControl.update_account_payable                          AS purchaseOrderControl_update_account_payable,
            purchaseOrderControl.print_second_description                        AS purchaseOrderControl_print_second_description,
            purchaseOrderControl.print_vendor_comments                           AS purchaseOrderControl_print_vendor_comments,
            purchaseOrderControl.include_freight_in_cost                         AS purchaseOrderControl_include_freight_in_cost,
            purchaseOrderControl.update_cost_on_model                            AS purchaseOrderControl_update_cost_on_model,
            purchaseOrderControl.sort_by_ship_to_on_print                        AS purchaseOrderControl_sort_by_ship_to_on_print,
            purchaseOrderControl.invoice_by_location                             AS purchaseOrderControl_invoice_by_location,
            purchaseOrderControl.validate_inventory                              AS purchaseOrderControl_validate_inventory,
            defaultAPStatusType.id                                               AS defaultAPStatusType_id,
            defaultAPStatusType.value                                            AS defaultAPStatusType_value,
            defaultAPStatusType.description                                      AS defaultAPStatusType_description,
            defaultAPStatusType.localization_code                                AS defaultAPStatusType_localization_code,
            defVen.v_id                                                          AS defVen_id,
            defVen.v_uu_row_id                                                   AS defVen_uu_row_id,
            defVen.v_time_created                                                AS defVen_time_created,
            defVen.v_time_updated                                                AS defVen_time_updated,
            defVen.v_company_id                                                  AS defVen_company_id,
            defVen.v_number                                                      AS defVen_number,
            defVen.v_name                                                        AS defVen_name,
            defVen.v_address_id                                                  AS defVen_address_id,
            defVen.v_our_account_number                                          AS defVen_our_account_number,
            defVen.v_pay_to_id                                                   AS defVen_pay_to_id,
            defVen.v_freight_on_board_type_id                                    AS defVen_freight_on_board_type_id,
            defVen.v_payment_terms_id                                            AS defVen_payment_terms_id,
            defVen.v_float_days                                                  AS defVen_float_days,
            defVen.v_normal_days                                                 AS defVen_normal_days,
            defVen.v_return_policy                                               AS defVen_return_policy,
            defVen.v_ship_via_id                                                 AS defVen_ship_via_id,
            defVen.v_group_id                                                    AS defVen_group_id,
            defVen.v_minimum_quantity                                            AS defVen_minimum_quantity,
            defVen.v_minimum_amount                                              AS defVen_minimum_amount,
            defVen.v_free_ship_quantity                                          AS defVen_free_ship_quantity,
            defVen.v_free_ship_amount                                            AS defVen_free_ship_amount,
            defVen.v_vendor_1099                                                 AS defVen_vendor_1099,
            defVen.v_federal_id_number                                           AS defVen_federal_id_number,
            defVen.v_sales_representative_name                                   AS defVen_sales_representative_name,
            defVen.v_sales_representative_fax                                    AS defVen_sales_representative_fax,
            defVen.v_separate_check                                              AS defVen_separate_check,
            defVen.v_bump_percent                                                AS defVen_bump_percent,
            defVen.v_freight_calc_method_type_id                                 AS defVen_freight_calc_method_type_id,
            defVen.v_freight_percent                                             AS defVen_freight_percent,
            defVen.v_freight_amount                                              AS defVen_freight_amount,
            defVen.v_charge_inventory_tax_1                                      AS defVen_charge_inventory_tax_1,
            defVen.v_charge_inventory_tax_2                                      AS defVen_charge_inventory_tax_2,
            defVen.v_charge_inventory_tax_3                                      AS defVen_charge_inventory_tax_3,
            defVen.v_charge_inventory_tax_4                                      AS defVen_charge_inventory_tax_4,
            defVen.v_federal_id_number_verification                              AS defVen_federal_id_number_verification,
            defVen.v_email_address                                               AS defVen_email_address,
            defVen.v_purchase_order_submit_email_address                         AS defVen_purchase_order_submit_email_address,
            defVen.v_allow_drop_ship_to_customer                                 AS defVen_allow_drop_ship_to_customer,
            defVen.v_auto_submit_purchase_order                                  AS defVen_auto_submit_purchase_order,
            defVen.comp_id                                                       AS comp_id,
            defVen.comp_uu_row_id                                                AS comp_uu_row_id,
            defVen.comp_time_created                                             AS comp_time_created,
            defVen.comp_time_updated                                             AS comp_time_updated,
            defVen.comp_name                                                     AS comp_name,
            defVen.comp_doing_business_as                                        AS comp_doing_business_as,
            defVen.comp_client_code                                              AS comp_client_code,
            defVen.comp_client_id                                                AS comp_client_id,
            defVen.comp_dataset_code                                             AS comp_dataset_code,
            defVen.comp_federal_id_number                                        AS comp_federal_id_number,
            defVen.comp_address_id                                               AS comp_address_id,
            defVen.comp_address_name                                             AS comp_address_name,
            defVen.comp_address_address1                                         AS comp_address_address1,
            defVen.comp_address_address2                                         AS comp_address_address2,
            defVen.comp_address_city                                             AS comp_address_city,
            defVen.comp_address_state                                            AS comp_address_state,
            defVen.comp_address_postal_code                                      AS comp_address_postal_code,
            defVen.comp_address_latitude                                         AS comp_address_latitude,
            defVen.comp_address_longitude                                        AS comp_address_longitude,
            defVen.comp_address_country                                          AS comp_address_country,
            defVen.comp_address_county                                           AS comp_address_county,
            defVen.comp_address_phone                                            AS comp_address_phone,
            defVen.comp_address_fax                                              AS comp_address_fax,
            defVen.onboard_id                                                    AS onboard_id,
            defVen.onboard_value                                                 AS onboard_value,
            defVen.onboard_description                                           AS onboard_description,
            defVen.onboard_localization_code                                     AS onboard_localization_code,
            defVen.method_id                                                     AS method_id,
            defVen.method_value                                                  AS method_value,
            defVen.method_description                                            AS method_description,
            defVen.method_localization_code                                      AS method_localization_code,
            defVen.address_id                                                    AS address_id,
            defVen.address_uu_row_id                                             AS address_uu_row_id,
            defVen.address_time_created                                          AS address_time_created,
            defVen.address_time_updated                                          AS address_time_updated,
            defVen.address_number                                                AS address_number,
            defVen.address_name                                                  AS address_name,
            defVen.address_address1                                              AS address_address1,
            defVen.address_address2                                              AS address_address2,
            defVen.address_city                                                  AS address_city,
            defVen.address_state                                                 AS address_state,
            defVen.address_postal_code                                           AS address_postal_code,
            defVen.address_latitude                                              AS address_latitude,
            defVen.address_longitude                                             AS address_longitude,
            defVen.address_country                                               AS address_country,
            defVen.address_county                                                AS address_county,
            defVen.address_phone                                                 AS address_phone,
            defVen.address_fax                                                   AS address_fax,
            defVen.vpt_id                                                        AS vpt_id,
            defVen.vpt_uu_row_id                                                 AS vpt_uu_row_id,
            defVen.vpt_time_created                                              AS vpt_time_created,
            defVen.vpt_time_updated                                              AS vpt_time_updated,
            defVen.vpt_company_id                                                AS vpt_company_id,
            defVen.vpt_description                                               AS vpt_description,
            defVen.vpt_number                                                    AS vpt_number,
            defVen.vpt_number_of_payments                                        AS vpt_number_of_payments,
            defVen.vpt_discount_month                                            AS vpt_discount_month,
            defVen.vpt_discount_days                                             AS vpt_discount_days,
            defVen.vpt_discount_percent                                          AS vpt_discount_percent,
            defVen.shipVia_id                                                    AS shipVia_id,
            defVen.shipVia_uu_row_id                                             AS shipVia_uu_row_id,
            defVen.shipVia_time_created                                          AS shipVia_time_created,
            defVen.shipVia_time_updated                                          AS shipVia_time_updated,
            defVen.shipVia_description                                           AS shipVia_description,
            defVen.shipVia_number                                                AS shipVia_number,
            defVen.vgrp_id                                                       AS vgrp_id,
            defVen.vgrp_uu_row_id                                                AS vgrp_uu_row_id,
            defVen.vgrp_time_created                                             AS vgrp_time_created,
            defVen.vgrp_time_updated                                             AS vgrp_time_updated,
            defVen.vgrp_company_id                                               AS vgrp_company_id,
            defVen.vgrp_value                                                    AS vgrp_value,
            defVen.vgrp_description                                              AS vgrp_description,
            updatePOCostType.id                                                  AS updatePOCostType_id,
            updatePOCostType.value                                               AS updatePOCostType_value,
            updatePOCostType.description                                         AS updatePOCostType_description,
            updatePOCostType.localization_code                                   AS updatePOCostType_localization_code,
            defaultPOType.id                                                     AS defaultPOType_id,
            defaultPOType.value                                                  AS defaultPOType_value,
            defaultPOType.description                                            AS defaultPOType_description,
            defaultPOType.localization_code                                      AS defaultPOType_localization_code,
            defApp.emp_id                                                        AS defApp_id,
            defApp.emp_number                                                    AS defApp_number,
            defApp.emp_last_name                                                 AS defApp_last_name,
            defApp.emp_first_name_mi                                             AS defApp_first_name_mi,
            defApp.emp_type                                                      AS defApp_type,
            defApp.emp_pass_code                                                 AS defApp_pass_code,
            defApp.emp_active                                                    AS defApp_active,
            defApp.emp_department                                                AS defApp_department,
            defApp.emp_cynergi_system_admin                                      AS defApp_cynergi_system_admin,
            defApp.emp_alternative_store_indicator                               AS defApp_alternative_store_indicator,
            defApp.emp_alternative_area                                          AS defApp_alternative_area,
            defApp.store_id                                                      AS defApp_store_id,
            defApp.store_number                                                  AS defApp_store_number,
            defApp.store_name                                                    AS defApp_store_name,
            defApp.dept_id                                                       AS defApp_dept_id,
            defApp.dept_code                                                     AS defApp_dept_code,
            defApp.dept_description                                              AS defApp_dept_description,
            defApp.comp_id                                                       AS defApp_comp_id,
            defApp.comp_uu_row_id                                                AS defApp_comp_uu_row_id,
            defApp.comp_time_created                                             AS defApp_comp_time_created,
            defApp.comp_time_updated                                             AS defApp_comp_time_updated,
            defApp.comp_name                                                     AS defApp_comp_name,
            defApp.comp_doing_business_as                                        AS defApp_comp_doing_business_as,
            defApp.comp_client_code                                              AS defApp_comp_client_code,
            defApp.comp_client_id                                                AS defApp_comp_client_id,
            defApp.comp_dataset_code                                             AS defApp_comp_dataset_code,
            defApp.comp_federal_id_number                                        AS defApp_comp_federal_id_number,
            defApp.address_id                                                    AS defApp_comp_address_id,
            defApp.address_name                                                  AS defApp_comp_address_name,
            defApp.address_address1                                              AS defApp_comp_address_address1,
            defApp.address_address2                                              AS defApp_comp_address_address2,
            defApp.address_city                                                  AS defApp_comp_address_city,
            defApp.address_state                                                 AS defApp_comp_address_state,
            defApp.address_postal_code                                           AS defApp_comp_address_postal_code,
            defApp.address_latitude                                              AS defApp_comp_address_latitude,
            defApp.address_longitude                                             AS defApp_comp_address_longitude,
            defApp.address_country                                               AS defApp_comp_address_country,
            defApp.address_county                                                AS defApp_comp_address_county,
            defApp.address_phone                                                 AS defApp_comp_address_phone,
            defApp.address_fax                                                   AS defApp_comp_address_fax,
            appReqFlagType.id                                                    AS appReqFlagType_id,
            appReqFlagType.value                                                 AS appReqFlagType_value,
            appReqFlagType.description                                           AS appReqFlagType_description,
            appReqFlagType.localization_code                                     AS appReqFlagType_localization_code
         FROM purchase_order_control purchaseOrderControl
            JOIN default_account_payable_status_type_domain defaultAPStatusType ON purchaseOrderControl.default_account_payable_status_type_id = defaultAPStatusType.id
            JOIN update_purchase_order_cost_type_domain updatePOCostType ON purchaseOrderControl.update_purchase_order_cost_type_id = updatePOCostType.id
            JOIN default_purchase_order_type_domain defaultPOType ON purchaseOrderControl.default_purchase_order_type_id = defaultPOType.id
            JOIN approval_required_flag_type_domain appReqFlagType ON purchaseOrderControl.approval_required_flag_type_id = appReqFlagType.id
            LEFT JOIN vendor defVen ON purchaseOrderControl.default_vendor_id = defVen.v_id
            LEFT JOIN employee defApp ON purchaseOrderControl.default_approver_id_sfk = defApp.emp_id AND defApp.emp_type = 'eli'
      """
   }

   fun findOne(company: Company): PurchaseOrderControlEntity? {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE purchaseOrderControl.company_id = :comp_id"

      logger.trace("Searching for PurchaseOrderControl:\n{}\nParams:{}", query, company)

      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            val defaultAccountPayableStatusType = defaultAccountPayableStatusTypeRepository.mapRow(rs, "defaultAPStatusType_")
            val defaultVendor = vendorRepository.mapRowOrNull(rs, company, "defVen_")
            val updatePurchaseOrderCostType = updatePurchaseOrderCostTypeRepository.mapRow(rs, "updatePOCostType_")
            val defaultPurchaseOrderType = defaultPurchaseOrderTypeRepository.mapRow(rs, "defaultPOType_")
            val defaultApprover = employeeRepository.mapRowOrNull(rs, "defApp_", "defApp_comp_", "defApp_comp_address_", "defApp_dept_","defApp_store_")
            val approvalRequiredFlagType = approvalRequiredFlagTypeRepository.mapRow(rs, "appReqFlagType_")

            mapRow(rs, defaultAccountPayableStatusType, defaultVendor, updatePurchaseOrderCostType, defaultPurchaseOrderType, defaultApprover, approvalRequiredFlagType, "purchaseOrderControl_")
         }
      )

      logger.trace("Searching for PurchaseOrderControl resulted in {}", found)

      return found
   }

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT id FROM purchase_order_control WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if PurchaseOrderControl: {} exists resulted in {}", id, exists)

      return exists
   }

   fun exists(company: Company): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS (SELECT company_id FROM purchase_order_control WHERE company_id = :company_id)", mapOf("company_id" to company.myId()), Boolean::class.java)!!

      logger.trace("Checking if PurchaseOrderControl: {} exists resulted in {}", company, exists)

      return exists
   }

   @Transactional
   fun insert(entity: PurchaseOrderControlEntity, company: Company): PurchaseOrderControlEntity {
      logger.debug("Inserting purchase_order_control {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO purchase_order_control(
            company_id,
            drop_five_characters_on_model_number,
            update_account_payable,
            print_second_description,
            default_account_payable_status_type_id,
            print_vendor_comments,
            include_freight_in_cost,
            update_cost_on_model,
            default_vendor_id,
            update_purchase_order_cost_type_id,
            default_purchase_order_type_id,
            sort_by_ship_to_on_print,
            invoice_by_location,
            validate_inventory,
            default_approver_id_sfk,
            approval_required_flag_type_id
         )
         VALUES (
            :company_id,
            :drop_five_characters_on_model_number,
            :update_account_payable,
            :print_second_description,
            :default_account_payable_status_type_id,
            :print_vendor_comments,
            :include_freight_in_cost,
            :update_cost_on_model,
            :default_vendor_id,
            :update_purchase_order_cost_type_id,
            :default_purchase_order_type_id,
            :sort_by_ship_to_on_print,
            :invoice_by_location,
            :validate_inventory,
            :default_approver_id_sfk,
            :approval_required_flag_type_id
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "drop_five_characters_on_model_number" to entity.dropFiveCharactersOnModelNumber,
            "update_account_payable" to entity.updateAccountPayable,
            "print_second_description" to entity.printSecondDescription,
            "default_account_payable_status_type_id" to entity.defaultAccountPayableStatusType.id,
            "print_vendor_comments" to entity.printVendorComments,
            "include_freight_in_cost" to entity.includeFreightInCost,
            "update_cost_on_model" to entity.updateCostOnModel,
            "default_vendor_id" to entity.defaultVendor?.id,
            "update_purchase_order_cost_type_id" to entity.updatePurchaseOrderCost.id,
            "default_purchase_order_type_id" to entity.defaultPurchaseOrderType.id,
            "sort_by_ship_to_on_print" to entity.sortByShipToOnPrint,
            "invoice_by_location" to entity.invoiceByLocation,
            "validate_inventory" to entity.validateInventory,
            "default_approver_id_sfk" to entity.defaultApprover?.id,
            "approval_required_flag_type_id" to entity.approvalRequiredFlagType.id
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.defaultAccountPayableStatusType,
               entity.defaultVendor,
               entity.updatePurchaseOrderCost,
               entity.defaultPurchaseOrderType,
               entity.defaultApprover,
               entity.approvalRequiredFlagType
            )
         }
      )
   }

   @Transactional
   fun update(entity: PurchaseOrderControlEntity, company: Company): PurchaseOrderControlEntity {
      logger.debug("Updating purchase_order_control {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE purchase_order_control
         SET
            company_id = :company_id,
            drop_five_characters_on_model_number = :drop_five_characters_on_model_number,
            update_account_payable = :update_account_payable,
            print_second_description = :print_second_description,
            default_account_payable_status_type_id = :default_account_payable_status_type_id,
            print_vendor_comments = :print_vendor_comments,
            include_freight_in_cost = :include_freight_in_cost,
            update_cost_on_model = :update_cost_on_model,
            default_vendor_id = :default_vendor_id,
            update_purchase_order_cost_type_id = :update_purchase_order_cost_type_id,
            default_purchase_order_type_id = :default_purchase_order_type_id,
            sort_by_ship_to_on_print = :sort_by_ship_to_on_print,
            invoice_by_location = :invoice_by_location,
            validate_inventory = :validate_inventory,
            default_approver_id_sfk = :default_approver_id_sfk,
            approval_required_flag_type_id = :approval_required_flag_type_id
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "drop_five_characters_on_model_number" to entity.dropFiveCharactersOnModelNumber,
            "update_account_payable" to entity.updateAccountPayable,
            "print_second_description" to entity.printSecondDescription,
            "default_account_payable_status_type_id" to entity.defaultAccountPayableStatusType.id,
            "print_vendor_comments" to entity.printVendorComments,
            "include_freight_in_cost" to entity.includeFreightInCost,
            "update_cost_on_model" to entity.updateCostOnModel,
            "default_vendor_id" to entity.defaultVendor?.id,
            "update_purchase_order_cost_type_id" to entity.updatePurchaseOrderCost.id,
            "default_purchase_order_type_id" to entity.defaultPurchaseOrderType.id,
            "sort_by_ship_to_on_print" to entity.sortByShipToOnPrint,
            "invoice_by_location" to entity.invoiceByLocation,
            "validate_inventory" to entity.validateInventory,
            "default_approver_id_sfk" to entity.defaultApprover?.id,
            "approval_required_flag_type_id" to entity.approvalRequiredFlagType.id
         ),
         RowMapper { rs, _ ->
            mapRow(
               rs,
               entity.defaultAccountPayableStatusType,
               entity.defaultVendor,
               entity.updatePurchaseOrderCost,
               entity.defaultPurchaseOrderType,
               entity.defaultApprover,
               entity.approvalRequiredFlagType
            )
         }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      defaultAccountPayableStatusType: DefaultAccountPayableStatusType,
      defaultVendor: VendorEntity?,
      updatePurchaseOrderCost: UpdatePurchaseOrderCostType,
      defaultPurchaseOrderType: DefaultPurchaseOrderType,
      defaultApprover: EmployeeEntity?,
      approvalRequiredFlagType: ApprovalRequiredFlagType,
      columnPrefix: String = EMPTY
   ): PurchaseOrderControlEntity {
      return PurchaseOrderControlEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         dropFiveCharactersOnModelNumber = rs.getBoolean("${columnPrefix}drop_five_characters_on_model_number"),
         updateAccountPayable = rs.getBoolean("${columnPrefix}update_account_payable"),
         printSecondDescription = rs.getBoolean("${columnPrefix}print_second_description"),
         defaultAccountPayableStatusType = defaultAccountPayableStatusType,
         printVendorComments = rs.getBoolean("${columnPrefix}print_vendor_comments"),
         includeFreightInCost = rs.getBoolean("${columnPrefix}include_freight_in_cost"),
         updateCostOnModel = rs.getBoolean("${columnPrefix}update_cost_on_model"),
         defaultVendor = defaultVendor,
         updatePurchaseOrderCost = updatePurchaseOrderCost,
         defaultPurchaseOrderType = defaultPurchaseOrderType,
         sortByShipToOnPrint = rs.getBoolean("${columnPrefix}sort_by_ship_to_on_print"),
         invoiceByLocation = rs.getBoolean("${columnPrefix}invoice_by_location"),
         validateInventory = rs.getBoolean("${columnPrefix}validate_inventory"),
         defaultApprover = defaultApprover,
         approvalRequiredFlagType = approvalRequiredFlagType
      )
   }
}
