package com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.getUuidOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceSelectedTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceEntity
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AccountPayableInvoiceRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val vendorRepository: VendorRepository,
   private val employeeRepository: EmployeeRepository,
   private val selectedRepository: AccountPayableInvoiceSelectedTypeRepository,
   private val statusRepository: AccountPayableInvoiceStatusTypeRepository,
   private val typeRepository: AccountPayableInvoiceTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableInvoiceRepository::class.java)

   private fun selectBaseQuery(): String {
      return """
         WITH vendor AS (
            ${vendorRepository.baseSelectQuery()}
         ),
         employee AS (
            ${employeeRepository.employeeBaseQuery()}
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
            apInvoice.pay_to_id                                         AS apInvoice_pay_to_id,
            apInvoice.separate_check_indicator                          AS apInvoice_separate_check_indicator,
            apInvoice.use_tax_indicator                                 AS apInvoice_use_tax_indicator,
            apInvoice.receive_date                                      AS apInvoice_receive_date,
            apInvoice.location_id_sfk                                   AS apInvoice_location_id_sfk,
            vendor.v_id                                                 AS apInvoice_vendor_id,
            vendor.v_time_created                                       AS apInvoice_vendor_time_created,
            vendor.v_time_updated                                       AS apInvoice_vendor_time_updated,
            vendor.v_company_id                                         AS apInvoice_vendor_company_id,
            vendor.v_number                                             AS apInvoice_vendor_number,
            vendor.v_name                                               AS apInvoice_vendor_name,
            vendor.v_address_id                                         AS apInvoice_vendor_address_id,
            vendor.v_account_number                                     AS apInvoice_vendor_account_number,
            vendor.v_pay_to_id                                          AS apInvoice_vendor_pay_to_id,
            vendor.v_freight_on_board_type_id                           AS apInvoice_vendor_freight_on_board_type_id,
            vendor.v_vendor_payment_term_id                             AS apInvoice_vendor_vendor_payment_term_id,
            vendor.v_normal_days                                        AS apInvoice_vendor_normal_days,
            vendor.v_return_policy                                      AS apInvoice_vendor_return_policy,
            vendor.v_ship_via_id                                        AS apInvoice_vendor_ship_via_id,
            vendor.v_vendor_group_id                                    AS apInvoice_vendor_group_id,
            vendor.v_minimum_quantity                                   AS apInvoice_vendor_minimum_quantity,
            vendor.v_minimum_amount                                     AS apInvoice_vendor_minimum_amount,
            vendor.v_free_ship_quantity                                 AS apInvoice_vendor_free_ship_quantity,
            vendor.v_free_ship_amount                                   AS apInvoice_vendor_free_ship_amount,
            vendor.v_vendor_1099                                        AS apInvoice_vendor_vendor_1099,
            vendor.v_federal_id_number                                  AS apInvoice_vendor_federal_id_number,
            vendor.v_sales_representative_name                          AS apInvoice_vendor_sales_representative_name,
            vendor.v_sales_representative_fax                           AS apInvoice_vendor_sales_representative_fax,
            vendor.v_separate_check                                     AS apInvoice_vendor_separate_check,
            vendor.v_bump_percent                                       AS apInvoice_vendor_bump_percent,
            vendor.v_freight_calc_method_type_id                        AS apInvoice_vendor_freight_calc_method_type_id,
            vendor.v_freight_percent                                    AS apInvoice_vendor_freight_percent,
            vendor.v_freight_amount                                     AS apInvoice_vendor_freight_amount,
            vendor.v_charge_inventory_tax_1                             AS apInvoice_vendor_charge_inventory_tax_1,
            vendor.v_charge_inventory_tax_2                             AS apInvoice_vendor_charge_inventory_tax_2,
            vendor.v_charge_inventory_tax_3                             AS apInvoice_vendor_charge_inventory_tax_3,
            vendor.v_charge_inventory_tax_4                             AS apInvoice_vendor_charge_inventory_tax_4,
            vendor.v_federal_id_number_verification                     AS apInvoice_vendor_federal_id_number_verification,
            vendor.v_email_address                                      AS apInvoice_vendor_email_address,
            vendor.v_purchase_order_submit_email_address                AS apInvoice_vendor_purchase_order_submit_email_address,
            vendor.v_allow_drop_ship_to_customer                        AS apInvoice_vendor_allow_drop_ship_to_customer,
            vendor.v_auto_submit_purchase_order                         AS apInvoice_vendor_auto_submit_purchase_order,
            vendor.v_note                                               AS apInvoice_vendor_note,
            vendor.v_phone_number                                       AS apInvoice_vendor_phone_number,
            vendor.v_comp_id                                            AS apInvoice_vendor_comp_id,
            vendor.v_comp_time_created                                  AS apInvoice_vendor_comp_time_created,
            vendor.v_comp_time_updated                                  AS apInvoice_vendor_comp_time_updated,
            vendor.v_comp_name                                          AS apInvoice_vendor_comp_name,
            vendor.v_comp_doing_business_as                             AS apInvoice_vendor_comp_doing_business_as,
            vendor.v_comp_client_code                                   AS apInvoice_vendor_comp_client_code,
            vendor.v_comp_client_id                                     AS apInvoice_vendor_comp_client_id,
            vendor.v_comp_dataset_code                                  AS apInvoice_vendor_comp_dataset_code,
            vendor.v_comp_federal_id_number                             AS apInvoice_vendor_comp_federal_id_number,
            vendor.v_comp_address_id                                    AS apInvoice_vendor_comp_address_id,
            vendor.v_comp_address_name                                  AS apInvoice_vendor_comp_address_name,
            vendor.v_comp_address_address1                              AS apInvoice_vendor_comp_address_address1,
            vendor.v_comp_address_address2                              AS apInvoice_vendor_comp_address_address2,
            vendor.v_comp_address_city                                  AS apInvoice_vendor_comp_address_city,
            vendor.v_comp_address_state                                 AS apInvoice_vendor_comp_address_state,
            vendor.v_comp_address_postal_code                           AS apInvoice_vendor_comp_address_postal_code,
            vendor.v_comp_address_latitude                              AS apInvoice_vendor_comp_address_latitude,
            vendor.v_comp_address_longitude                             AS apInvoice_vendor_comp_address_longitude,
            vendor.v_comp_address_country                               AS apInvoice_vendor_comp_address_country,
            vendor.v_comp_address_county                                AS apInvoice_vendor_comp_address_county,
            vendor.v_comp_address_phone                                 AS apInvoice_vendor_comp_address_phone,
            vendor.v_comp_address_fax                                   AS apInvoice_vendor_comp_address_fax,
            vendor.v_onboard_id                                         AS apInvoice_vendor_onboard_id,
            vendor.v_onboard_value                                      AS apInvoice_vendor_onboard_value,
            vendor.v_onboard_description                                AS apInvoice_vendor_onboard_description,
            vendor.v_onboard_localization_code                          AS apInvoice_vendor_onboard_localization_code,
            vendor.v_method_id                                          AS apInvoice_vendor_method_id,
            vendor.v_method_value                                       AS apInvoice_vendor_method_value,
            vendor.v_method_description                                 AS apInvoice_vendor_method_description,
            vendor.v_method_localization_code                           AS apInvoice_vendor_method_localization_code,
            vendor.v_address_id                                         AS apInvoice_vendor_address_id,
            vendor.v_address_time_created                               AS apInvoice_vendor_address_time_created,
            vendor.v_address_time_updated                               AS apInvoice_vendor_address_time_updated,
            vendor.v_address_number                                     AS apInvoice_vendor_address_number,
            vendor.v_address_name                                       AS apInvoice_vendor_address_name,
            vendor.v_address_address1                                   AS apInvoice_vendor_address_address1,
            vendor.v_address_address2                                   AS apInvoice_vendor_address_address2,
            vendor.v_address_city                                       AS apInvoice_vendor_address_city,
            vendor.v_address_state                                      AS apInvoice_vendor_address_state,
            vendor.v_address_postal_code                                AS apInvoice_vendor_address_postal_code,
            vendor.v_address_latitude                                   AS apInvoice_vendor_address_latitude,
            vendor.v_address_longitude                                  AS apInvoice_vendor_address_longitude,
            vendor.v_address_country                                    AS apInvoice_vendor_address_country,
            vendor.v_address_county                                     AS apInvoice_vendor_address_county,
            vendor.v_address_phone                                      AS apInvoice_vendor_address_phone,
            vendor.v_address_fax                                        AS apInvoice_vendor_address_fax,
            vendor.v_vpt_id                                             AS apInvoice_vendor_vpt_id,
            vendor.v_vpt_time_created                                   AS apInvoice_vendor_vpt_time_created,
            vendor.v_vpt_time_updated                                   AS apInvoice_vendor_vpt_time_updated,
            vendor.v_vpt_company_id                                     AS apInvoice_vendor_vpt_company_id,
            vendor.v_vpt_description                                    AS apInvoice_vendor_vpt_description,
            vendor.v_vpt_number                                         AS apInvoice_vendor_vpt_number,
            vendor.v_vpt_number_of_payments                             AS apInvoice_vendor_vpt_number_of_payments,
            vendor.v_vpt_discount_month                                 AS apInvoice_vendor_vpt_discount_month,
            vendor.v_vpt_discount_days                                  AS apInvoice_vendor_vpt_discount_days,
            vendor.v_vpt_discount_percent                               AS apInvoice_vendor_vpt_discount_percent,
            vendor.v_shipVia_id                                         AS apInvoice_vendor_shipVia_id,
            vendor.v_shipVia_time_created                               AS apInvoice_vendor_shipVia_time_created,
            vendor.v_shipVia_time_updated                               AS apInvoice_vendor_shipVia_time_updated,
            vendor.v_shipVia_description                                AS apInvoice_vendor_shipVia_description,
            vendor.v_shipVia_number                                     AS apInvoice_vendor_shipVia_number,
            vendor.v_vgrp_id                                            AS apInvoice_vendor_vgrp_id,
            vendor.v_vgrp_time_created                                  AS apInvoice_vendor_vgrp_time_created,
            vendor.v_vgrp_time_updated                                  AS apInvoice_vendor_vgrp_time_updated,
            vendor.v_vgrp_company_id                                    AS apInvoice_vendor_vgrp_company_id,
            vendor.v_vgrp_value                                         AS apInvoice_vendor_vgrp_value,
            vendor.v_vgrp_description                                   AS apInvoice_vendor_vgrp_description,
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
            count(*) OVER()                                             AS total_elements
         FROM account_payable_invoice apInvoice
            JOIN company comp                                           ON apInvoice.company_id = comp.id
            JOIN vendor                                                 ON apInvoice.vendor_id = vendor.v_id
            JOIN employee                                               ON apInvoice.employee_number_id_sfk = employee.emp_number AND employee.comp_id = comp.id
            JOIN account_payable_invoice_selected_type_domain selected  ON apInvoice.selected_id = selected.id
            JOIN account_payable_invoice_type_domain type               ON apInvoice.type_id = type.id
            JOIN account_payable_invoice_status_type_domain status      ON apInvoice.status_id = status.id
      """
   }

   fun findOne(id: UUID, company: Company): AccountPayableInvoiceEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE apInvoice.id = :id AND apInvoice.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "apInvoice_")
      }

      logger.trace("Searching for AccountPayableInvoice: {} resulted in {}", company, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<AccountPayableInvoiceEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE apInvoice.company_id = :comp_id
            ORDER BY apInvoice_${page.snakeSortBy()} ${page.sortDirection()}
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
            elements.add(mapRow(rs, company, "apInvoice_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(entity: AccountPayableInvoiceEntity, company: Company): AccountPayableInvoiceEntity {
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
            "company_id" to company.myId(),
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
            "employee_number_id_sfk" to entity.employee.number,
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
   fun update(entity: AccountPayableInvoiceEntity, company: Company): AccountPayableInvoiceEntity {
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
            "company_id" to company.myId(),
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
            "employee_number_id_sfk" to entity.employee.number,
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

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): AccountPayableInvoiceEntity {
      val vendor = vendorRepository.mapRow(rs, company, "${columnPrefix}vendor_")
      val employee = employeeRepository.mapRow(rs, "${columnPrefix}employee_", "${columnPrefix}employee_comp_", "${columnPrefix}employee_comp_address_", "${columnPrefix}employee_dept_", "${columnPrefix}employee_store_")
      val selected = selectedRepository.mapRow(rs, "${columnPrefix}selected_")
      val type = typeRepository.mapRow(rs, "${columnPrefix}type_")
      val status = statusRepository.mapRow(rs, "${columnPrefix}status_")

      return AccountPayableInvoiceEntity(
         id = rs.getUuid("${columnPrefix}id"),
         vendor = vendor,
         invoice = rs.getString("${columnPrefix}invoice"),
         purchaseOrder = rs.getUuidOrNull("${columnPrefix}purchase_order_id")?.let { SimpleIdentifiableEntity(it) },
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
         payTo = SimpleIdentifiableEntity(rs.getUuid("${columnPrefix}pay_to_id")),
         separateCheckIndicator = rs.getBoolean("${columnPrefix}separate_check_indicator"),
         useTaxIndicator = rs.getBoolean("${columnPrefix}use_tax_indicator"),
         receiveDate = rs.getLocalDateOrNull("${columnPrefix}receive_date"),
         location = rs.getIntOrNull("${columnPrefix}location_id_sfk")?.let { SimpleLegacyIdentifiableEntity(it) }
      )
   }

   private fun mapRow(rs: ResultSet, entity: AccountPayableInvoiceEntity, columnPrefix: String = EMPTY): AccountPayableInvoiceEntity {
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
}
