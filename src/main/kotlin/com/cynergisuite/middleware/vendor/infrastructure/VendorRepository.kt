package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getLongOrNull
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.shipping.freight.calc.method.FreightCalcMethodType
import com.cynergisuite.middleware.shipping.freight.onboard.FreightOnboardType
import com.cynergisuite.middleware.shipping.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.group.infrastructure.VendorGroupRepository
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
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
class VendorRepository @Inject constructor(
   private val addressRepository: AddressRepository,
   private val companyRepository: CompanyRepository,
   private val jdbc: NamedParameterJdbcTemplate,
   private val vendorGroupRepository: VendorGroupRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorRepository::class.java)
   fun baseSelectQuery() =
      """
         WITH company AS (
            ${companyRepository.companyBaseQuery()}
         )
         SELECT
            v.id                                  AS v_id,
            v.uu_row_id                           AS v_uu_row_id,
            v.time_created                        AS v_time_created,
            v.time_updated                        AS v_time_updated,
            v.company_id                          AS v_company_id,
            v.number                              AS v_number,
            v.name                                AS v_name,
            v.address_id                          AS v_address_id,
            v.account_number                  AS v_account_number,
            v.pay_to_id                           AS v_pay_to_id,
            v.freight_on_board_type_id            AS v_freight_on_board_type_id,
            v.payment_terms_id                    AS v_payment_terms_id,
            v.normal_days                         AS v_normal_days,
            v.return_policy                       AS v_return_policy,
            v.ship_via_id                         AS v_ship_via_id,
            v.group_id                            AS v_group_id,
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
            v.purchase_order_submit_email_address AS v_purchase_order_submit_email_address,
            v.allow_drop_ship_to_customer         AS v_allow_drop_ship_to_customer,
            v.auto_submit_purchase_order          AS v_auto_submit_purchase_order,
            v.note                                AS v_note,
            v.phone_number                        AS v_phone_number,
            comp.id                               AS comp_id,
            comp.uu_row_id                        AS comp_uu_row_id,
            comp.time_created                     AS comp_time_created,
            comp.time_updated                     AS comp_time_updated,
            comp.name                             AS comp_name,
            comp.doing_business_as                AS comp_doing_business_as,
            comp.client_code                      AS comp_client_code,
            comp.client_id                        AS comp_client_id,
            comp.dataset_code                     AS comp_dataset_code,
            comp.federal_id_number                AS comp_federal_id_number,
            comp.address_id                       AS comp_address_id,
            comp.address_name                     AS comp_address_name,
            comp.address_address1                 AS comp_address_address1,
            comp.address_address2                 AS comp_address_address2,
            comp.address_city                     AS comp_address_city,
            comp.address_state                    AS comp_address_state,
            comp.address_postal_code              AS comp_address_postal_code,
            comp.address_latitude                 AS comp_address_latitude,
            comp.address_longitude                AS comp_address_longitude,
            comp.address_country                  AS comp_address_country,
            comp.address_county                   AS comp_address_county,
            comp.address_phone                    AS comp_address_phone,
            comp.address_fax                      AS comp_address_fax,
            onboard.id                            AS onboard_id,
            onboard.value                         AS onboard_value,
            onboard.description                   AS onboard_description,
            onboard.localization_code             AS onboard_localization_code,
            method.id                             AS method_id,
            method.value                          AS method_value,
            method.description                    AS method_description,
            method.localization_code              AS method_localization_code,
            address.id                            AS address_id,
            address.uu_row_id                     AS address_uu_row_id,
            address.time_created                  AS address_time_created,
            address.time_updated                  AS address_time_updated,
            address.number                        AS address_number,
            address.name                          AS address_name,
            address.address1                      AS address_address1,
            address.address2                      AS address_address2,
            address.city                          AS address_city,
            address.state                         AS address_state,
            address.postal_code                   AS address_postal_code,
            address.latitude                      AS address_latitude,
            address.longitude                     AS address_longitude,
            address.country                       AS address_country,
            address.county                        AS address_county,
            address.phone                         AS address_phone,
            address.fax                           AS address_fax,
            vpt.id                                AS vpt_id,
            vpt.uu_row_id                         AS vpt_uu_row_id,
            vpt.time_created                      AS vpt_time_created,
            vpt.time_updated                      AS vpt_time_updated,
            vpt.company_id                        AS vpt_company_id,
            vpt.description                       AS vpt_description,
            vpt.number                            AS vpt_number,
            vpt.number_of_payments                AS vpt_number_of_payments,
            vpt.discount_month                    AS vpt_discount_month,
            vpt.discount_days                     AS vpt_discount_days,
            vpt.discount_percent                  AS vpt_discount_percent,
            shipVia.id                            AS shipVia_id,
            shipVia.uu_row_id                     AS shipVia_uu_row_id,
            shipVia.time_created                  AS shipVia_time_created,
            shipVia.time_updated                  AS shipVia_time_updated,
            shipVia.description                   AS shipVia_description,
            shipVia.number                        AS shipVia_number,
            vgrp.id                               AS vgrp_id,
            vgrp.uu_row_id                        AS vgrp_uu_row_id,
            vgrp.time_created                     AS vgrp_time_created,
            vgrp.time_updated                     AS vgrp_time_updated,
            vgrp.company_id                       AS vgrp_company_id,
            vgrp.value                            AS vgrp_value,
            vgrp.description                      AS vgrp_description,
            count(*) OVER()                       AS total_elements
         FROM vendor v
            JOIN company comp                            ON v.company_id = comp.id
            JOIN freight_on_board_type_domain onboard    ON onboard.id = v.freight_on_board_type_id
            JOIN freight_calc_method_type_domain method  ON method.id = v.freight_calc_method_type_id
            JOIN vendor_payment_term vpt                 ON vpt.id = v.payment_terms_id
            JOIN ship_via shipVia                        ON shipVia.id = v.ship_via_id
            LEFT OUTER JOIN address                      ON address.id = v.address_id
            LEFT OUTER JOIN vendor_group vgrp            ON vgrp.id = v.group_id
      """

   fun findOne(id: Long, company: Company): VendorEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${baseSelectQuery()}\nWHERE v.id = :id AND comp.id = :comp_id"

      logger.debug("Searching for Vendor using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val vendor = mapRow(rs, company)

         vendor
      }

      logger.trace("Searching for VendorPaymentTerm: {} resulted in {}", id, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<VendorEntity, PageRequest> {
      return jdbc.queryPaged(
         """
      ${baseSelectQuery()}
      WHERE comp.id = :comp_id
      ORDER BY v.${page.snakeSortBy()} ${page.sortDirection()}
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
            elements.add(mapRow(rs, company))
         } while (rs.next())
      }
   }

   fun search(company: Company, page: SearchPageRequest): RepositoryPage<VendorEntity, PageRequest> {
      var searchQuery = page.query
      val where = StringBuilder(" WHERE comp.id = :comp_id ")
      val sortBy = if (!searchQuery.isNullOrEmpty()) {
         if (page.fuzzy == false) {
            where.append(" AND (search_vector @@ to_tsquery(:search_query)) ")
            searchQuery = searchQuery.replace("\\s+".toRegex(), " & ")
            EMPTY
         } else {
            val fieldToSearch = " v.name "
            where.append(" AND $fieldToSearch <-> :search_query < 0.9 ")
            " ORDER BY $fieldToSearch <-> :search_query "
         }
      } else {
         EMPTY
      }

      return jdbc.queryPaged(
         """
         ${baseSelectQuery()}
         $where
         $sortBy
         LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId(),
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

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM vendor WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Vendor: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

   @Transactional
   fun insert(entity: VendorEntity): VendorEntity {
      logger.debug("Inserting vendor {}", entity)

      val address = entity.address?.let { addressRepository.insert(it) }

      return jdbc.insertReturning(
         """
         INSERT INTO vendor(
            company_id,
            name,
            address_id,
            account_number,
            pay_to_id,
            freight_on_board_type_id,
            payment_terms_id,
            normal_days,
            return_policy,
            ship_via_id,
            group_id,
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
            phone_number
         )
         VALUES (
            :company_id,
            :name,
            :address_id,
            :account_number,
            :pay_to_id,
            :freight_on_board_type_id,
            :payment_terms_id,
            :normal_days,
            :return_policy,
            :ship_via_id,
            :group_id,
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
            :phone_number
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company.myId(),
            "name" to entity.name,
            "address_id" to address?.id,
            "account_number" to entity.accountNumber,
            "pay_to_id" to entity.payTo?.myId(),
            "freight_on_board_type_id" to entity.freightOnboardType.id,
            "payment_terms_id" to entity.paymentTerm.id,
            "normal_days" to entity.normalDays,
            "return_policy" to entity.returnPolicy,
            "ship_via_id" to entity.shipVia.id,
            "group_id" to entity.vendorGroup?.id,
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
            "phone_number" to entity.phone
         ),
         RowMapper { rs, _ -> mapRowUpsert(rs, entity.company, address, entity.freightOnboardType, entity.paymentTerm, entity.shipVia, entity.vendorGroup, entity.freightCalcMethodType) }
      )
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
            payment_terms_id = :paymentTerm,
            normal_days = :normalDays,
            return_policy = :returnPolicy,
            ship_via_id = :shipVia,
            group_id = :vendorGroup,
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
            phone_number = :phone_number
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to toUpdate.id,
            "companyId" to toUpdate.company.myId(),
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
            "phone_number" to toUpdate.phone
         ),
         RowMapper { rs, _ -> mapRowUpsert(rs, toUpdate.company, vendorAddress, toUpdate.freightOnboardType, toUpdate.paymentTerm, toUpdate.shipVia, toUpdate.vendorGroup, toUpdate.freightCalcMethodType) }
      )

      addressToDelete?.id?.let { addressRepository.delete(it) } // delete address if it exists, done this way because it avoids the race condition compilation error

      return updatedVendor
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String? = "v_"): VendorEntity {
      val payToId = rs.getLongOrNull("${columnPrefix}pay_to_id")

      return VendorEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company,
         name = rs.getString("${columnPrefix}name"),
         address = addressRepository.mapAddressOrNull(rs, "address_"),
         accountNumber = rs.getString("${columnPrefix}account_number"),
         payTo = if (payToId != null) SimpleIdentifiableEntity(payToId) else null,
         freightOnboardType = mapOnboard(rs, "onboard_"),
         paymentTerm = mapPaymentTerm(rs, company, "vpt_"),
         normalDays = rs.getIntOrNull("${columnPrefix}normal_days"),
         returnPolicy = rs.getBoolean("${columnPrefix}return_policy"),
         shipVia = mapShipVia(rs, company, "shipVia_"),
         vendorGroup = vendorGroupRepository.mapRowOrNull(rs, company, "vgrp_"),
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
         freightCalcMethodType = mapMethod(rs, "method_"),
         freightPercent = rs.getBigDecimal("${columnPrefix}freight_percent"),
         freightAmount = rs.getBigDecimal("${columnPrefix}freight_amount"),
         chargeInventoryTax1 = rs.getBoolean("${columnPrefix}charge_inventory_tax_1"),
         chargeInventoryTax2 = rs.getBoolean("${columnPrefix}charge_inventory_tax_2"),
         chargeInventoryTax3 = rs.getBoolean("${columnPrefix}charge_inventory_tax_3"),
         chargeInventoryTax4 = rs.getBoolean("${columnPrefix}charge_inventory_tax_4"),
         federalIdNumberVerification = rs.getBoolean("${columnPrefix}federal_id_number_verification"),
         emailAddress = rs.getString("${columnPrefix}email_address"),
         purchaseOrderSubmitEmailAddress = rs.getString("${columnPrefix}purchase_order_submit_email_address"),
         allowDropShipToCustomer = rs.getBoolean("${columnPrefix}allow_drop_ship_to_customer"),
         autoSubmitPurchaseOrder = rs.getBoolean("${columnPrefix}auto_submit_purchase_order"),
         number = rs.getInt("${columnPrefix}number"),
         note =  rs.getString("${columnPrefix}note"),
         phone =  rs.getString("${columnPrefix}phone_number")
      )
   }

   fun mapRowOrNull(rs: ResultSet, company: Company, columnPrefix: String? = "v_"): VendorEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   private fun mapRowUpsert(rs: ResultSet, company: Company, address: AddressEntity?, freightOnboardType: FreightOnboardType, paymentTerm: VendorPaymentTermEntity, shipVia: ShipViaEntity, vendorGroup: VendorGroupEntity?, freightCalcMethodType: FreightCalcMethodType): VendorEntity {
      val payToId = rs.getLongOrNull("pay_to_id")

      return VendorEntity(
         id = rs.getLong("id"),
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
         note =  rs.getString("note"),
         phone =  rs.getString("phone_number")
      )
   }

   private fun mapOnboard(rs: ResultSet, columnPrefix: String): FreightOnboardType =
      FreightOnboardType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapMethod(rs: ResultSet, columnPrefix: String): FreightCalcMethodType =
      FreightCalcMethodType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapPaymentTerm(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): VendorPaymentTermEntity =
      VendorPaymentTermEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company,
         description = rs.getString("${columnPrefix}description"),
         discountMonth = rs.getInt("${columnPrefix}discount_month"),
         discountDays = rs.getInt("${columnPrefix}discount_days"),
         discountPercent = rs.getBigDecimal("${columnPrefix}discount_percent")
      )

   private fun mapShipVia(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): ShipViaEntity =
      ShipViaEntity(
         id = rs.getLong("${columnPrefix}id"),
         description = rs.getString("${columnPrefix}description"),
         number = rs.getInt("${columnPrefix}number"),
         company = company
      )
}
