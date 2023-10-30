package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.domain.CashFlowFilterRequest
import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.Vendor1099FilterRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getLocalDateOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.getUuidOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.isNumber
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryFullList
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.cashflow.AccountPayableCashFlowDTO
import com.cynergisuite.middleware.accounting.account.payable.cashflow.AccountPayableCashFlowEntity
import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowBalanceEntity
import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowReportInvoiceDetailEntity
import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowVendorEntity
import com.cynergisuite.middleware.accounting.account.payable.cashout.CashRequirementBalanceEnum
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerJournalEntity
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerPendingReportDetailsTemplate
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerReportSortEnum
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodType
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardType
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.Form1099ReportDTO
import com.cynergisuite.middleware.vendor.Form1099ReportEntity
import com.cynergisuite.middleware.vendor.Form1099ReportInvoiceDetailEntity
import com.cynergisuite.middleware.vendor.Form1099TotalsDTO
import com.cynergisuite.middleware.vendor.Form1099TotalsEntity
import com.cynergisuite.middleware.vendor.Form1099VendorDTO
import com.cynergisuite.middleware.vendor.Form1099VendorEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.LocalDate
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class VendorRepository @Inject constructor(
   private val addressRepository: AddressRepository,
   private val companyRepository: CompanyRepository,
   private val jdbc: Jdbi,
   private val statusRepository: AccountPayableInvoiceStatusTypeRepository,
   private val vendorGroupRepository: VendorGroupRepository,
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorRepository::class.java)
   fun baseSelectQuery() =
      """
         WITH company AS (
            ${companyRepository.companyBaseQuery()}
         )
         SELECT
            v.id                                  AS v_id,
            v.time_created                        AS v_time_created,
            v.time_updated                        AS v_time_updated,
            v.company_id                          AS v_company_id,
            v.number                              AS v_number,
            v.name                                AS v_name,
            v.account_number                      AS v_account_number,
            v.pay_to_id                           AS v_pay_to_id,
            v.freight_on_board_type_id            AS v_freight_on_board_type_id,
            v.vendor_payment_term_id              AS v_vendor_payment_term_id,
            v.normal_days                         AS v_normal_days,
            v.return_policy                       AS v_return_policy,
            v.ship_via_id                         AS v_ship_via_id,
            v.vendor_group_id                     AS v_vendor_group_id,
            v.minimum_quantity                    AS v_minimum_quantity,
            v.minimum_amount                      AS v_minimum_amount,
            v.free_ship_quantity                  AS v_free_ship_quantity,
            v.free_ship_amount                    AS v_free_ship_amount,
            v.vendor_1099                         AS v_vendor_1099,
            v.federal_id_number                   AS v_federal_id_number,
            v.sales_representative_name           AS v_sales_representative_name,
            v.sales_representative_fax            AS v_sales_representative_fax,
            v.separate_check                      AS v_separate_check,
            v.bump_percent                        AS v_bump_percent,
            v.freight_calc_method_type_id         AS v_freight_calc_method_type_id,
            v.freight_percent                     AS v_freight_percent,
            v.freight_amount                      AS v_freight_amount,
            v.charge_inventory_tax_1              AS v_charge_inventory_tax_1,
            v.charge_inventory_tax_2              AS v_charge_inventory_tax_2,
            v.charge_inventory_tax_3              AS v_charge_inventory_tax_3,
            v.charge_inventory_tax_4              AS v_charge_inventory_tax_4,
            v.federal_id_number_verification      AS v_federal_id_number_verification,
            v.email_address                       AS v_email_address,
            v.purchase_order_submit_email_address AS v_purchase_order_submit_email,
            v.allow_drop_ship_to_customer         AS v_allow_drop_ship_to_customer,
            v.auto_submit_purchase_order          AS v_auto_submit_purchase_order,
            v.note                                AS v_note,
            v.phone_number                        AS v_phone_number,
            v.deleted                             AS v_deleted,
            v.active                              AS v_active,
            comp.id                               AS v_comp_id,
            comp.time_created                     AS v_comp_time_created,
            comp.time_updated                     AS v_comp_time_updated,
            comp.name                             AS v_comp_name,
            comp.doing_business_as                AS v_comp_doing_business_as,
            comp.client_code                      AS v_comp_client_code,
            comp.client_id                        AS v_comp_client_id,
            comp.dataset_code                     AS v_comp_dataset_code,
            comp.federal_id_number                AS v_comp_federal_id_number,
            comp.address_id                       AS v_comp_address_id,
            comp.address_name                     AS v_comp_address_name,
            comp.address_address1                 AS v_comp_address_address1,
            comp.address_address2                 AS v_comp_address_address2,
            comp.address_city                     AS v_comp_address_city,
            comp.address_state                    AS v_comp_address_state,
            comp.address_postal_code              AS v_comp_address_postal_code,
            comp.address_latitude                 AS v_comp_address_latitude,
            comp.address_longitude                AS v_comp_address_longitude,
            comp.address_country                  AS v_comp_address_country,
            comp.address_county                   AS v_comp_address_county,
            comp.address_phone                    AS v_comp_address_phone,
            comp.address_fax                      AS v_comp_address_fax,
            onboard.id                            AS v_onboard_id,
            onboard.value                         AS v_onboard_value,
            onboard.description                   AS v_onboard_description,
            onboard.localization_code             AS v_onboard_localization_code,
            method.id                             AS v_method_id,
            method.value                          AS v_method_value,
            method.description                    AS v_method_description,
            method.localization_code              AS v_method_localization_code,
            address.id                            AS v_address_id,
            address.time_created                  AS v_address_time_created,
            address.time_updated                  AS v_address_time_updated,
            address.number                        AS v_address_number,
            address.name                          AS v_address_name,
            address.address1                      AS v_address_address1,
            address.address2                      AS v_address_address2,
            address.city                          AS v_address_city,
            address.state                         AS v_address_state,
            address.postal_code                   AS v_address_postal_code,
            address.latitude                      AS v_address_latitude,
            address.longitude                     AS v_address_longitude,
            address.country                       AS v_address_country,
            address.county                        AS v_address_county,
            address.phone                         AS v_address_phone,
            address.fax                           AS v_address_fax,
            vpt.id                                AS v_vpt_id,
            vpt.time_created                      AS v_vpt_time_created,
            vpt.time_updated                      AS v_vpt_time_updated,
            vpt.company_id                        AS v_vpt_company_id,
            vpt.description                       AS v_vpt_description,
            vpt.number                            AS v_vpt_number,
            vpt.number_of_payments                AS v_vpt_number_of_payments,
            vpt.discount_month                    AS v_vpt_discount_month,
            vpt.discount_days                     AS v_vpt_discount_days,
            vpt.discount_percent                  AS v_vpt_discount_percent,
            shipVia.id                            AS v_shipVia_id,
            shipVia.time_created                  AS v_shipVia_time_created,
            shipVia.time_updated                  AS v_shipVia_time_updated,
            shipVia.description                   AS v_shipVia_description,
            shipVia.number                        AS v_shipVia_number,
            vgrp.id                               AS v_vgrp_id,
            vgrp.time_created                     AS v_vgrp_time_created,
            vgrp.time_updated                     AS v_vgrp_time_updated,
            vgrp.company_id                       AS v_vgrp_company_id,
            vgrp.value                            AS v_vgrp_value,
            vgrp.description                      AS v_vgrp_description,
            count(*) OVER()                       AS total_elements
         FROM vendor v
            JOIN company comp                            ON v.company_id = comp.id AND comp.deleted = FALSE
            JOIN freight_on_board_type_domain onboard    ON onboard.id = v.freight_on_board_type_id
            JOIN freight_calc_method_type_domain method  ON method.id = v.freight_calc_method_type_id
            JOIN vendor_payment_term vpt                 ON vpt.id = v.vendor_payment_term_id AND vpt.deleted = FALSE
            JOIN ship_via shipVia                        ON shipVia.id = v.ship_via_id AND shipVia.deleted = FALSE
            LEFT OUTER JOIN address                      ON address.id = v.address_id AND address.deleted = FALSE
            LEFT OUTER JOIN vendor_group vgrp            ON vgrp.id = v.vendor_group_id AND vgrp.deleted = FALSE
      """

   fun select1099ReportBaseQuery() =
      """
         WITH vend AS (
            ${baseSelectQuery()}
         )
         SELECT
            apInvoice.id                                                AS 1099_apInvoice_id,
            apInvoice.company_id                                        AS 1099_apInvoice_company_id,
            apInvoice.vendor_id                                         AS 1099_apInvoice_vendor_id,
            apInvoice.invoice                                           AS 1099_apInvoice_invoice,
            apInvoice.invoice_date                                      AS 1099_apInvoice_invoice_date,
            apInvoice.invoice_amount                                    AS 1099_apInvoice_invoice_amount,
            apInvoice.discount_amount                                   AS 1099_apInvoice_discount_amount,
            apInvoice.discount_percent                                  AS 1099_apInvoice_discount_percent,
            apInvoice.auto_distribution_applied                         AS 1099_apInvoice_auto_distribution_applied,
            apInvoice.discount_taken                                    AS 1099_apInvoice_discount_taken,
            apInvoice.entry_date                                        AS 1099_apInvoice_entry_date,
            apInvoice.expense_date                                      AS 1099_apInvoice_expense_date,
            apInvoice.discount_date                                     AS 1099_apInvoice_discount_date,
            apInvoice.employee_number_id_sfk                            AS 1099_apInvoice_employee_number_id_sfk,
            apInvoice.original_invoice_amount                           AS 1099_apInvoice_original_invoice_amount,
            apInvoice.message                                           AS 1099_apInvoice_message,
            apInvoice.multiple_payment_indicator                        AS 1099_apInvoice_multiple_payment_indicator,
            apInvoice.paid_amount                                       AS 1099_apInvoice_paid_amount,
            apInvoice.selected_amount                                   AS 1099_apInvoice_selected_amount,
            apInvoice.due_date                                          AS 1099_apInvoice_due_date,
            apInvoice.separate_check_indicator                          AS 1099_apInvoice_separate_check_indicator,
            apInvoice.use_tax_indicator                                 AS 1099_apInvoice_use_tax_indicator,
            apInvoice.receive_date                                      AS 1099_apInvoice_receive_date,
            apInvoice.location_id_sfk                                   AS 1099_apInvoice_location_id_sfk,

            vend.v_id                                                   AS 1099_vendor_id,
            vend.v_company_id                                           AS 1099_vendor_company_id,
            vend.v_number                                               AS 1099_vendor_number,
            vend.v_name                                                 AS 1099_vendor_name,
            vend.v_account_number                                       AS 1099_vendor_account_number,
            vend.v_pay_to_id                                            AS 1099_vendor_pay_to_id,
            vend.v_federal_id_number                                    AS 1099_vendor_federal_id_number,
            vend.v_active                                               AS 1099_vendor_active,

            vend.v_address_id                                           AS 1099_vendor_address_id,
            vend.v_address_name                                         AS 1099_vendor_address_name,
            vend.v_address_address1                                     AS 1099_vendor_address_address1,
            vend.v_address_address2                                     AS 1099_vendor_address_address2,
            vend.v_address_city                                         AS 1099_vendor_address_city,
            vend.v_address_state                                        AS 1099_vendor_address_state,
            vend.v_address_postal_code                                  AS 1099_vendor_address_postal_code,
            vend.v_address_latitude 					                     AS 1099_vendor_address_latitude,
            vend.v_address_longitude 					                     AS 1099_vendor_address_longitude,
            vend.v_address_country					                        AS 1099_vendor_address_country,
            vend.v_address_county					                        AS 1099_vendor_address_county,
            vend.v_address_phone					                           AS 1099_vendor_address_phone,
            vend.v_address_fax						                        AS 1099_vendor_address_fax,

            vend.v_comp_address_id					                        AS 1099_comp_address_id,
            vend.v_comp_address_name					                     AS 1099_comp_address_name,
            vend.v_comp_address_address1				                     AS 1099_comp_address_address1,
            vend.v_comp_address_address2				                     AS 1099_comp_address_address2,
            vend.v_comp_address_city					                     AS 1099_comp_address_city,
            vend.v_comp_address_state					                     AS 1099_comp_address_state,
            vend.v_comp_address_postal_code				                  AS 1099_comp_address_postal_code,
            vend.v_comp_address_latitude				                     AS 1099_comp_address_latitude,
            vend.v_comp_address_longitude				                     AS 1099_comp_address_longitude,
            vend.v_comp_address_country					                  AS 1099_comp_address_country,
            vend.v_comp_address_county					                     AS 1099_comp_address_county,
            vend.v_comp_address_phone					                     AS 1099_comp_address_phone,
            vend.v_comp_address_fax					                        AS 1099_comp_address_fax,

	         vgrp.id							                                 AS 1099_vgrp_id,
	         vgrp.value							                              AS 1099_vgrp_value,

            pmt.payment_number                                          AS 1099_apPayment_number,
            pmt.payment_date                                            AS 1099_apPayment_payment_date,
            pmtDetail.id                                                AS 1099_apPayment_detail_id,
            pmtDetail.amount                                            AS 1099_apPayment_detail_amount,

	         invDist.distribution_amount					                  AS 1099_invdist_distribution_amount,

            account.id                                     		         AS 1099_account_id,
            account.number                                 		         AS 1099_account_number,
            account.name                                   		         AS 1099_account_name,
            account.form_1099_field                        		         AS 1099_account_form_1099_field,
            account.corporate_account_indicator            		         AS 1099_account_corporate_account_indicator,
            account.company_id                             		         AS 1099_account_comp_id,
            account.deleted                                		         AS 1099_account_deleted,
         FROM account_payable_invoice apInvoice
            JOIN company comp                                           ON apInvoice.company_id = comp.id ???AND comp.deleted = FALSE???
            JOIN vend                                                   ON apInvoice.vendor_id = vend.v_id
            LEFT OUTER JOIN vendor_group vgrp                           ON vgrp.id = vend.vendor_group_id ???AND vgrp.deleted = FALSE???
            JOIN account_payable_invoice_type_domain type               ON apInvoice.type_id = type.id
            JOIN account_payable_invoice_status_type_domain status      ON apInvoice.status_id = status.id
            LEFT JOIN account_payable_payment_detail pmtDetail          ON apInvoice.id = pmtDetail.account_payable_invoice_id
            LEFT JOIN account_payable_payment pmt                       ON pmtDetail.payment_number_id = pmt.id
            JOIN account_payable_invoice_distribution invDist           ON apInvoice.id = invDist.invoice_id
            JOIN account ON apInvoice.company_id = account.company_id AND invDist.distribution_account = account.id ???AND account.deleted = FALSE???
      """

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): VendorEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${baseSelectQuery()}\nWHERE v.id = :id AND comp.id = :comp_id AND v.deleted = FALSE"

      logger.debug("Searching for Vendor using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs, _ ->
         val vendor = mapRow(rs, company)

         vendor
      }

      logger.trace("Searching for Vendor: {} resulted in {}", id, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: VendorPageRequest): RepositoryPage<VendorEntity, VendorPageRequest> {
      val whereClause = StringBuilder(" WHERE comp.id = :comp_id AND v.deleted = FALSE ")

      if (page.active != null) {
         whereClause.append(" AND v.active = ${page.active} ")
      }

      return jdbc.queryPaged(
         """
      ${baseSelectQuery()}
      $whereClause
      ORDER BY v.${page.snakeSortBy()} ${page.sortDirection()}
      LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset(),
            "active" to page.active
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun findVendorIdsByRebate(rebateId: UUID, company: CompanyEntity): List<Identifiable> {
      return jdbc.queryFullList(
         """
            SELECT
               vendor_id
            FROM rebate_to_vendor
            WHERE rebate_id = :rebate_id
         """.trimIndent(),
         mapOf(
            "rebate_id" to rebateId
         )
      ) { rs, _, elements ->
         do {
            elements.add(SimpleIdentifiableEntity(rs.getUuid("vendor_id")))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun search(company: CompanyEntity, page: VendorSearchPageRequest): RepositoryPage<VendorEntity, PageRequest> {
      val searchQuery = page.query?.trim()
      val where = StringBuilder(" WHERE comp.id = :comp_id AND v.deleted = FALSE ")

      if (page.active != null) {
         where.append(" AND v.active = ${page.active} ")
      }

      val sortBy = StringBuilder("")
      if (!searchQuery.isNullOrEmpty()) {
         val splitSearchQuery = searchQuery.split(" - ")
         val searchQueryBeginsWith = "$searchQuery%"

         where.append("AND (")
         sortBy.append("ORDER BY ")
         if (searchQuery.isNumber()) {
            if (page.fuzzy!!)  {
               where.append("v.number::text LIKE \'$searchQueryBeginsWith\' OR ")
               sortBy.append("v.number::text <-> :search_query, v.number::text ASC, ")
               sortBy.append("v.name <-> :search_query, v.name ASC")
            } else {
               where.append("v.number = $searchQuery OR ")
               sortBy.append("v.name ASC")
            }
            where.append("v.name ILIKE \'$searchQueryBeginsWith\')")
         } else if (splitSearchQuery.first().isNumber()) {
            val nameBeginsWith = "${splitSearchQuery.drop(1).joinToString(" - ")}%"
            where.append("v.number = ${splitSearchQuery.first()} AND v.name ILIKE \'$nameBeginsWith\')")
            sortBy.append("v.name ASC")
         } else {
            where.append("v.name ILIKE \'$searchQueryBeginsWith\')")
            sortBy.append("v.name ASC")
         }
      }

      return jdbc.queryPaged(
         """
         ${baseSelectQuery()}
         $where
         $sortBy
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset(),
            "search_query" to searchQuery
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, company))
         } while (rs.next())
      }
   }

   @ReadOnly
   fun exists(id: UUID): Boolean {
      val exists = jdbc.queryForObject(
         "SELECT EXISTS(SELECT id FROM vendor WHERE id = :id AND vendor.deleted = FALSE)",
         mapOf("id" to id),
         Boolean::class.java
      )

      logger.trace("Checking if Vendor: {} exists resulted in {}", id, exists)

      return exists
   }

   @ReadOnly
   fun doesNotExist(id: UUID): Boolean = !exists(id)

   @Transactional
   fun insert(entity: VendorEntity): VendorEntity {
      logger.debug("Inserting vendor {}", entity)

      val address = entity.address?.let { addressRepository.save(it) }

      return jdbc.insertReturning(
         """
         INSERT INTO vendor(
            company_id,
            name,
            address_id,
            account_number,
            pay_to_id,
            freight_on_board_type_id,
            vendor_payment_term_id,
            normal_days,
            return_policy,
            ship_via_id,
            vendor_group_id,
            minimum_quantity,
            minimum_amount,
            free_ship_quantity,
            free_ship_amount,
            vendor_1099,
            federal_id_number,
            sales_representative_name,
            sales_representative_fax,
            separate_check,
            bump_percent,
            freight_calc_method_type_id,
            freight_percent,
            freight_amount,
            charge_inventory_tax_1,
            charge_inventory_tax_2,
            charge_inventory_tax_3,
            charge_inventory_tax_4,
            federal_id_number_verification,
            email_address,
            purchase_order_submit_email_address,
            allow_drop_ship_to_customer,
            auto_submit_purchase_order,
            note,
            phone_number,
            active
         )
         VALUES (
            :company_id,
            :name,
            :address_id,
            :account_number,
            :pay_to_id,
            :freight_on_board_type_id,
            :vendor_payment_term_id,
            :normal_days,
            :return_policy,
            :ship_via_id,
            :vendor_group_id,
            :minimum_quantity,
            :minimum_amount,
            :free_ship_quantity,
            :free_ship_amount,
            :vendor_1099,
            :federal_id_number,
            :sales_representative_name,
            :sales_representative_fax,
            :separate_check,
            :bump_percent,
            :freight_calc_method_type_id,
            :freight_percent,
            :freight_amount,
            :charge_inventory_tax_1,
            :charge_inventory_tax_2,
            :charge_inventory_tax_3,
            :charge_inventory_tax_4,
            :federal_id_number_verification,
            :email_address,
            :purchase_order_submit_email_address,
            :allow_drop_ship_to_customer,
            :auto_submit_purchase_order,
            :note,
            :phone_number,
            :active
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company.id,
            "name" to entity.name,
            "address_id" to address?.id,
            "account_number" to entity.accountNumber,
            "pay_to_id" to entity.payTo?.myId(),
            "freight_on_board_type_id" to entity.freightOnboardType.id,
            "vendor_payment_term_id" to entity.paymentTerm.id,
            "normal_days" to entity.normalDays,
            "return_policy" to entity.returnPolicy,
            "ship_via_id" to entity.shipVia.id,
            "vendor_group_id" to entity.vendorGroup?.id,
            "minimum_quantity" to entity.minimumQuantity,
            "minimum_amount" to entity.minimumAmount,
            "free_ship_quantity" to entity.freeShipQuantity,
            "free_ship_amount" to entity.freeShipAmount,
            "vendor_1099" to entity.vendor1099,
            "federal_id_number" to entity.federalIdNumber,
            "sales_representative_name" to entity.salesRepresentativeName,
            "sales_representative_fax" to entity.salesRepresentativeFax,
            "separate_check" to entity.separateCheck,
            "bump_percent" to entity.bumpPercent,
            "freight_calc_method_type_id" to entity.freightCalcMethodType.id,
            "freight_percent" to entity.freightPercent,
            "freight_amount" to entity.freightAmount,
            "charge_inventory_tax_1" to entity.chargeInventoryTax1,
            "charge_inventory_tax_2" to entity.chargeInventoryTax2,
            "charge_inventory_tax_3" to entity.chargeInventoryTax3,
            "charge_inventory_tax_4" to entity.chargeInventoryTax4,
            "federal_id_number_verification" to entity.federalIdNumberVerification,
            "email_address" to entity.emailAddress,
            "purchase_order_submit_email_address" to entity.purchaseOrderSubmitEmailAddress,
            "allow_drop_ship_to_customer" to entity.allowDropShipToCustomer,
            "auto_submit_purchase_order" to entity.autoSubmitPurchaseOrder,
            "note" to entity.note,
            "phone_number" to entity.phone,
            "active" to entity.isActive
         )
      ) { rs, _ ->
         mapRowUpsert(
            rs,
            entity.company,
            address,
            entity.freightOnboardType,
            entity.paymentTerm,
            entity.shipVia,
            entity.vendorGroup,
            entity.freightCalcMethodType
         )
      }
   }

   @Transactional
   fun update(existingVendor: VendorEntity, toUpdate: VendorEntity): VendorEntity {
      logger.debug("Updating Vendor {}", toUpdate)
      var addressToDelete: AddressEntity? = null

      val vendorAddress = if (existingVendor.address?.id != null && toUpdate.address == null) {
         addressToDelete = existingVendor.address

         null
      } else if (toUpdate.address != null) {
         addressRepository.upsert(toUpdate.address)
      } else {
         null
      }

      val updatedVendor = jdbc.updateReturning(
         """
         UPDATE vendor
         SET
            company_id = :companyId,
            name = :name,
            address_id = :addressId,
            account_number = :accountNumber,
            pay_to_id = :payTo,
            freight_on_board_type_id = :freightOnboardType,
            vendor_payment_term_id = :paymentTerm,
            normal_days = :normalDays,
            return_policy = :returnPolicy,
            ship_via_id = :shipVia,
            vendor_group_id = :vendorGroup,
            minimum_quantity = :minimumQuantity,
            minimum_amount = :minimumAmount,
            free_ship_quantity = :freeShipQuantity,
            free_ship_amount = :freeShipAmount,
            vendor_1099 = :vendor1099,
            federal_id_number = :federalIdNumber,
            sales_representative_name = :salesRepName,
            sales_representative_fax = :salesRepFax,
            separate_check = :separateCheck,
            bump_percent = :bumpPercent,
            freight_calc_method_type_id = :freightMethodType,
            freight_percent = :freightPercent,
            freight_amount = :freightAmount,
            charge_inventory_tax_1 = :chargeInvTax1,
            charge_inventory_tax_2 = :chargeInvTax2,
            charge_inventory_tax_3 = :chargeInvTax3,
            charge_inventory_tax_4 = :chargeInvTax4,
            federal_id_number_verification = :federalIdNumberVerification,
            email_address = :emailAddress,
            purchase_order_submit_email_address = :purchase_order_submit_email_address,
            allow_drop_ship_to_customer = :allow_drop_ship_to_customer,
            auto_submit_purchase_order = :auto_submit_purchase_order,
            note = :note,
            phone_number = :phone_number,
            active = :active
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to toUpdate.id,
            "companyId" to toUpdate.company.id,
            "name" to toUpdate.name,
            "addressId" to vendorAddress?.id,
            "accountNumber" to toUpdate.accountNumber,
            "payTo" to toUpdate.payTo?.myId(),
            "freightOnboardType" to toUpdate.freightOnboardType.id,
            "paymentTerm" to toUpdate.paymentTerm.id,
            "normalDays" to toUpdate.normalDays,
            "returnPolicy" to toUpdate.returnPolicy,
            "shipVia" to toUpdate.shipVia.id,
            "vendorGroup" to toUpdate.vendorGroup?.id,
            "minimumQuantity" to toUpdate.minimumQuantity,
            "minimumAmount" to toUpdate.minimumAmount,
            "freeShipQuantity" to toUpdate.freeShipQuantity,
            "freeShipAmount" to toUpdate.freeShipAmount,
            "vendor1099" to toUpdate.vendor1099,
            "federalIdNumber" to toUpdate.federalIdNumber,
            "salesRepName" to toUpdate.salesRepresentativeName,
            "salesRepFax" to toUpdate.salesRepresentativeFax,
            "separateCheck" to toUpdate.separateCheck,
            "bumpPercent" to toUpdate.bumpPercent,
            "freightMethodType" to toUpdate.freightCalcMethodType.id,
            "freightPercent" to toUpdate.freightPercent,
            "freightAmount" to toUpdate.freightAmount,
            "chargeInvTax1" to toUpdate.chargeInventoryTax1,
            "chargeInvTax2" to toUpdate.chargeInventoryTax2,
            "chargeInvTax3" to toUpdate.chargeInventoryTax3,
            "chargeInvTax4" to toUpdate.chargeInventoryTax4,
            "federalIdNumberVerification" to toUpdate.federalIdNumberVerification,
            "emailAddress" to toUpdate.emailAddress,
            "purchase_order_submit_email_address" to toUpdate.purchaseOrderSubmitEmailAddress,
            "allow_drop_ship_to_customer" to toUpdate.allowDropShipToCustomer,
            "auto_submit_purchase_order" to toUpdate.autoSubmitPurchaseOrder,
            "note" to toUpdate.note,
            "phone_number" to toUpdate.phone,
            "active" to toUpdate.isActive
         )
      ) { rs, _ ->
         mapRowUpsert(
            rs,
            toUpdate.company,
            vendorAddress,
            toUpdate.freightOnboardType,
            toUpdate.paymentTerm,
            toUpdate.shipVia,
            toUpdate.vendorGroup,
            toUpdate.freightCalcMethodType
         )
      }

      addressToDelete?.let { addressRepository.deleteById(it.id!!) } // delete address if it exists, done this way because it avoids the race condition compilation error

      return updatedVendor
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting vendor with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE vendor
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "vendor"
      )

      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   @ReadOnly
   fun fetch1099Report(company: CompanyEntity, filterRequest: Vendor1099FilterRequest) : Form1099ReportDTO {
      val vendors = mutableListOf <Form1099VendorEntity> ()
      var currentVendor: Form1099VendorEntity? = null
      var companyName = company.name
      val form1099Totals = Form1099TotalsEntity()

      val params = mutableMapOf<String, Any?>("comp_id" to company.id, "limit" to filterRequest.size(), "offset" to filterRequest.offset())
      val whereClause = StringBuilder("WHERE comp.company_id = :comp_id AND vend.vendor_1099 = 'Y' AND apInvoice.status_id NOT IN (1, 2, 4) AND pmt.account_payable_payment_status != 2")

      if (filterRequest.form1099Type != null) {
         if (filterRequest.form1099Type == "M") {
            whereClause.append(" AND account.form_1099_field < 100")
         } else {
            whereClause.append(" AND account.form_1099_field > 100")
         }
      }

      if (filterRequest.beginningAccountNumber != null || filterRequest.endingAccountNumber != null) {
         params["beginningAccountNumber"] = filterRequest.beginningAccountNumber
         params["endingAccountNumber"] = filterRequest.endingAccountNumber
         whereClause.append(" AND account.number ")
            .append(buildFilterString(filterRequest.beginningAccountNumber != null, filterRequest.endingAccountNumber != null, "beginningAccountNumber", "endingAccountNumber"))
      }

      if (filterRequest.vendorGroup != null) {
         whereClause.append(" AND vgrp.value = ${filterRequest.vendorGroup} ")
      }

      if (filterRequest.beginningPaymentDate != null || filterRequest.endingPaymentDate != null) {
         params["beginningSource"] = filterRequest.beginningPaymentDate
         params["endingSource"] = filterRequest.endingPaymentDate
         whereClause.append(" AND pmt.payment_date ")
            .append(buildFilterString(filterRequest.beginningPaymentDate != null, filterRequest.endingPaymentDate != null, "beginningPaymentDate", "endingPaymentDate"))
      }
      whereClause.append(')')
      jdbc.query(
         """
                  ${select1099ReportBaseQuery()}
                  $whereClause
                  ORDER BY vend.v_name, vend.v_id, naturalsort(apInvoice.invoice), apInvoice.invoice_date ${filterRequest.sortDirection()}
               """.trimIndent(),
         params,
      ) {
         rs, elements ->
         do {
            val tempVendor = if (currentVendor ?.vendorNumber != rs.getIntOrNull("apInvoiceDetail_vendor_number")){
               val localVendor = map1099Vendor(rs, "1099_")
               vendors.add(localVendor)
               currentVendor = localVendor

               localVendor
            } else{
               currentVendor !!
            }

            var invoiceFlag = false
            mapRowInvoiceDetail(rs, "apInvoiceDetail_").let {
               tempVendor.invoices?.add(it)
               invoiceFlag = true

               if (invoiceFlag) {
                  //We need to add 1099_invdist_distribution_amount to (1099_apInvoice_discount_taken * -1)
                     form1099Totals.grandTotal = form1099Totals.grandTotal.plus(it.distributionAmount)
                     form1099Totals.grandTotal = form1099Totals.grandTotal.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                  }

                  when (it.fieldNumber) {
                     1 -> {
                        tempVendor.vendorTotals.ten99Field1 =
                           tempVendor.vendorTotals.ten99Field1.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field1 =
                           tempVendor.vendorTotals.ten99Field1.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     2 -> {
                        tempVendor.vendorTotals.ten99Field2 =
                           tempVendor.vendorTotals.ten99Field2.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field2 =
                           tempVendor.vendorTotals.ten99Field2.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     3 -> {
                        tempVendor.vendorTotals.ten99Field3 =
                           tempVendor.vendorTotals.ten99Field3.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field3 =
                           tempVendor.vendorTotals.ten99Field3.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     4 -> {
                        tempVendor.vendorTotals.ten99Field4 =
                           tempVendor.vendorTotals.ten99Field4.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field4 =
                           tempVendor.vendorTotals.ten99Field4.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     5 -> {
                        tempVendor.vendorTotals.ten99Field5 =
                           tempVendor.vendorTotals.ten99Field5.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field5 =
                           tempVendor.vendorTotals.ten99Field5.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     6 -> {
                        tempVendor.vendorTotals.ten99Field6 =
                           tempVendor.vendorTotals.ten99Field6.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field6 =
                           tempVendor.vendorTotals.ten99Field6.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     7 -> {
                        tempVendor.vendorTotals.ten99Field7 =
                           tempVendor.vendorTotals.ten99Field7 .plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field7  =
                           tempVendor.vendorTotals.ten99Field7.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     8 -> {
                        tempVendor.vendorTotals.ten99Field8 =
                           tempVendor.vendorTotals.ten99Field8.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field8 =
                           tempVendor.vendorTotals.ten99Field8.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     9 -> {
                        tempVendor.vendorTotals.ten99Field9 =
                           tempVendor.vendorTotals.ten99Field9.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field9 =
                           tempVendor.vendorTotals.ten99Field9.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     10 -> {
                        tempVendor.vendorTotals.ten99Field10 =
                           tempVendor.vendorTotals.ten99Field10.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field10 =
                           tempVendor.vendorTotals.ten99Field10.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     11 -> {
                        tempVendor.vendorTotals.ten99Field11 =
                           tempVendor.vendorTotals.ten99Field11.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field11 =
                           tempVendor.vendorTotals.ten99Field11.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     12 -> {
                        tempVendor.vendorTotals.ten99Field12 =
                           tempVendor.vendorTotals.ten99Field12.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field12 =
                           tempVendor.vendorTotals.ten99Field12.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     13 -> {
                        tempVendor.vendorTotals.ten99Field13 =
                           tempVendor.vendorTotals.ten99Field13.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field13 =
                           tempVendor.vendorTotals.ten99Field13.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     14 -> {
                        tempVendor.vendorTotals.ten99Field14 =
                           tempVendor.vendorTotals.ten99Field14.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field14 =
                           tempVendor.vendorTotals.ten99Field14.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     15 -> {
                        tempVendor.vendorTotals.ten99Field15 =
                           tempVendor.vendorTotals.ten99Field15.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field15 =
                           tempVendor.vendorTotals.ten99Field15.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     16 -> {
                        tempVendor.vendorTotals.ten99Field16 =
                           tempVendor.vendorTotals.ten99Field16.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field16 =
                           tempVendor.vendorTotals.ten99Field16.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }
                     17 -> {
                        tempVendor.vendorTotals.ten99Field17 =
                           tempVendor.vendorTotals.ten99Field17.plus(it.distributionAmount)
                        tempVendor.vendorTotals.ten99Field17 =
                           tempVendor.vendorTotals.ten99Field17.add(it.invoiceDiscountTaken.multiply(BigDecimal(-1)))
                     }

                     else -> {}
                  }
               }
         } while (rs.next())

         vendors.removeIf {
            it.vendorTotals.grandTotal < filterRequest.excludeBelow
         }
      }

      val entity = Form1099ReportEntity(companyName, vendors, form1099Totals)

      return Form1099ReportDTO(entity)
   }

   private fun mapRowInvoiceDetail(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): Form1099ReportInvoiceDetailEntity {
      val invoiceAmount = rs.getBigDecimal("${columnPrefix}apInvoice_invoice_amount")
      val invoicePaidAmount = rs.getBigDecimal("${columnPrefix}apInvoice_paid_amount")
      val invoiceDiscountTaken = rs.getBigDecimal("${columnPrefix}apInvoice_discount_taken")
      val invoiceDiscountDate = rs.getLocalDateOrNull("${columnPrefix}apInvoice_discount_date")
      val invoiceDiscountPercent = rs.getBigDecimal("${columnPrefix}apInvoice_discount_percent")
      val balance = invoiceAmount - invoicePaidAmount

      return Form1099ReportInvoiceDetailEntity(
         invoiceCompanyId = rs.getUuid("${columnPrefix}apInvoice_company_id"),
         invoiceVendorId = rs.getUuid("${columnPrefix}apInvoice_vendor_id"),
         invoice = rs.getString("${columnPrefix}apInvoice_invoice"),
         invoiceDate = rs.getLocalDate("${columnPrefix}apInvoice_invoice_date"),
         invoiceAmount = invoiceAmount,
         invoiceDiscountAmount = rs.getBigDecimal("${columnPrefix}apInvoice_discount_amount"),
         invoiceDiscountDate = invoiceDiscountDate,
         invoiceExpenseDate = rs.getLocalDate("${columnPrefix}apInvoice_expense_date"),
         invoicePaidAmount = invoicePaidAmount,
         invoiceDiscountTaken = invoiceDiscountTaken,
         invoiceDiscountPercent = invoiceDiscountPercent,
         invoiceDueDate = rs.getLocalDate("${columnPrefix}apInvoice_due_date"),
         discountAmount = null,
         lostAmount = null,
         balance = balance,
         fieldNumber = rs.getInt("${columnPrefix}account_form_1099_field"),
         distributionAmount = rs.getBigDecimal("${columnPrefix}invdist_distribution_amount"),
      )
   }

   private fun mapBalanceDisplay(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): CashRequirementBalanceEnum {

      return CashRequirementBalanceEnum.valueOf(rs.getString("${columnPrefix}balance_display"))
   }

   private fun map1099Vendor(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): Form1099VendorEntity {
      return Form1099VendorEntity(
         id = rs.getUuid("${columnPrefix}vendor_company_id"),
         vendorName = rs.getString("${columnPrefix}vendor_name"),
         vendorNumber = rs.getInt("${columnPrefix}vendor_number"),
         vendorAddress = addressRepository.mapAddressOrNull(rs, "${columnPrefix}1099_vendor_address_"),
         companyAddress = addressRepository.mapAddressOrNull(rs, "${columnPrefix}1099_comp_address_"),
         federalIdNumber = rs.getString("${columnPrefix}vendor_federal_id_number"),
         form1099Field = rs.getInt("${columnPrefix}account_id"),
         apPaymentPaymentDate = rs.getLocalDate("${columnPrefix}apPayment_payment_date"),
         accountName = rs.getString("${columnPrefix}account_name"),
         accountNumber = rs.getString("${columnPrefix}account_number"),
         distributionAmount = rs.getBigDecimal("${columnPrefix}invdist_distribution_amount"),
         isActive = rs.getBoolean("${columnPrefix}vendor_active"),
      )
   }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String? = "v_"): VendorEntity {
      val payToId = rs.getUuidOrNull("${columnPrefix}pay_to_id")

      return VendorEntity(
         id = rs.getUuid("${columnPrefix}id"),
         company = company,
         name = rs.getString("${columnPrefix}name"),
         address = addressRepository.mapAddressOrNull(rs, "${columnPrefix}address_"),
         accountNumber = rs.getString("${columnPrefix}account_number"),
         payTo = if (payToId != null) SimpleIdentifiableEntity(payToId) else null,
         freightOnboardType = mapOnboard(rs, "${columnPrefix}onboard_"),
         paymentTerm = mapPaymentTerm(rs, company, "${columnPrefix}vpt_"),
         normalDays = rs.getIntOrNull("${columnPrefix}normal_days"),
         returnPolicy = rs.getBoolean("${columnPrefix}return_policy"),
         shipVia = mapShipVia(rs, company, "${columnPrefix}shipVia_"),
         vendorGroup = vendorGroupRepository.mapRowOrNull(rs, company, "${columnPrefix}vgrp_"),
         minimumQuantity = rs.getIntOrNull("${columnPrefix}minimum_quantity"),
         minimumAmount = rs.getBigDecimal("${columnPrefix}minimum_amount"),
         freeShipQuantity = rs.getIntOrNull("${columnPrefix}free_ship_quantity"),
         freeShipAmount = rs.getBigDecimal("${columnPrefix}free_ship_amount"),
         vendor1099 = rs.getBoolean("${columnPrefix}vendor_1099"),
         federalIdNumber = rs.getString("${columnPrefix}federal_id_number"),
         salesRepresentativeName = rs.getString("${columnPrefix}sales_representative_name"),
         salesRepresentativeFax = rs.getString("${columnPrefix}sales_representative_fax"),
         separateCheck = rs.getBoolean("${columnPrefix}separate_check"),
         bumpPercent = rs.getBigDecimal("${columnPrefix}bump_percent"),
         freightCalcMethodType = mapMethod(rs, "${columnPrefix}method_"),
         freightPercent = rs.getBigDecimal("${columnPrefix}freight_percent"),
         freightAmount = rs.getBigDecimal("${columnPrefix}freight_amount"),
         chargeInventoryTax1 = rs.getBoolean("${columnPrefix}charge_inventory_tax_1"),
         chargeInventoryTax2 = rs.getBoolean("${columnPrefix}charge_inventory_tax_2"),
         chargeInventoryTax3 = rs.getBoolean("${columnPrefix}charge_inventory_tax_3"),
         chargeInventoryTax4 = rs.getBoolean("${columnPrefix}charge_inventory_tax_4"),
         federalIdNumberVerification = rs.getBoolean("${columnPrefix}federal_id_number_verification"),
         emailAddress = rs.getString("${columnPrefix}email_address"),
         purchaseOrderSubmitEmailAddress = rs.getString("${columnPrefix}purchase_order_submit_email"),
         allowDropShipToCustomer = rs.getBoolean("${columnPrefix}allow_drop_ship_to_customer"),
         autoSubmitPurchaseOrder = rs.getBoolean("${columnPrefix}auto_submit_purchase_order"),
         number = rs.getInt("${columnPrefix}number"),
         note = rs.getString("${columnPrefix}note"),
         phone = rs.getString("${columnPrefix}phone_number"),
         isActive = rs.getBoolean("${columnPrefix}active")
      )
   }

   fun mapRowOrNull(rs: ResultSet, company: CompanyEntity, columnPrefix: String? = "v_"): VendorEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   private fun mapRowUpsert(
      rs: ResultSet,
      company: CompanyEntity,
      address: AddressEntity?,
      freightOnboardType: FreightOnboardType,
      paymentTerm: VendorPaymentTermEntity,
      shipVia: ShipViaEntity,
      vendorGroup: VendorGroupEntity?,
      freightCalcMethodType: FreightCalcMethodType
   ): VendorEntity {
      val payToId = rs.getUuidOrNull("pay_to_id")

      return VendorEntity(
         id = rs.getUuid("id"),
         company = company,
         name = rs.getString("name"),
         address = address, // This needs to have the newly inserted address passed in
         accountNumber = rs.getString("account_number"),
         payTo = if (payToId != null) SimpleIdentifiableEntity(payToId) else null,
         freightOnboardType = freightOnboardType,
         paymentTerm = paymentTerm,
         normalDays = rs.getIntOrNull("normal_days"),
         returnPolicy = rs.getBoolean("return_policy"),
         shipVia = shipVia,
         vendorGroup = vendorGroup,
         minimumQuantity = rs.getInt("minimum_quantity"),
         minimumAmount = rs.getBigDecimal("minimum_amount"),
         freeShipQuantity = rs.getInt("free_ship_quantity"),
         freeShipAmount = rs.getBigDecimal("free_ship_amount"),
         vendor1099 = rs.getBoolean("vendor_1099"),
         federalIdNumber = rs.getString("federal_id_number"),
         salesRepresentativeName = rs.getString("sales_representative_name"),
         salesRepresentativeFax = rs.getString("sales_representative_fax"),
         separateCheck = rs.getBoolean("separate_check"),
         bumpPercent = rs.getBigDecimal("bump_percent"),
         freightCalcMethodType = freightCalcMethodType,
         freightPercent = rs.getBigDecimal("freight_percent"),
         freightAmount = rs.getBigDecimal("freight_amount"),
         chargeInventoryTax1 = rs.getBoolean("charge_inventory_tax_1"),
         chargeInventoryTax2 = rs.getBoolean("charge_inventory_tax_2"),
         chargeInventoryTax3 = rs.getBoolean("charge_inventory_tax_3"),
         chargeInventoryTax4 = rs.getBoolean("charge_inventory_tax_4"),
         federalIdNumberVerification = rs.getBoolean("federal_id_number_verification"),
         emailAddress = rs.getString("email_address"),
         purchaseOrderSubmitEmailAddress = rs.getString("purchase_order_submit_email_address"),
         allowDropShipToCustomer = rs.getBoolean("allow_drop_ship_to_customer"),
         autoSubmitPurchaseOrder = rs.getBoolean("auto_submit_purchase_order"),
         number = rs.getInt("number"),
         note = rs.getString("note"),
         phone = rs.getString("phone_number"),
         isActive = rs.getBoolean("active")
      )
   }

   private fun mapOnboard(rs: ResultSet, columnPrefix: String): FreightOnboardType =
      FreightOnboardType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapMethod(rs: ResultSet, columnPrefix: String): FreightCalcMethodType =
      FreightCalcMethodType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapPaymentTerm(
      rs: ResultSet,
      company: CompanyEntity,
      columnPrefix: String = EMPTY
   ): VendorPaymentTermEntity =
      VendorPaymentTermEntity(
         id = rs.getUuid("${columnPrefix}id"),
         company = company,
         description = rs.getString("${columnPrefix}description"),
         discountMonth = rs.getInt("${columnPrefix}discount_month"),
         discountDays = rs.getInt("${columnPrefix}discount_days"),
         discountPercent = rs.getBigDecimal("${columnPrefix}discount_percent")
      )

   private fun mapShipVia(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): ShipViaEntity =
      ShipViaEntity(
         id = rs.getUuid("${columnPrefix}id"),
         description = rs.getString("${columnPrefix}description"),
         number = rs.getInt("${columnPrefix}number"),
         company = company
      )

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam "
      else if (begin) " >= :$beginningParam "
      else " <= :$endingParam "
   }
}
