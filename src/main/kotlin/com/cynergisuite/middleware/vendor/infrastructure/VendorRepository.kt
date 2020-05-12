package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.domain.SimpleIdentifiableValueObject
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.shipvia.ShipViaEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import com.cynergisuite.middleware.vendor.freight.method.FreightMethodTypeEntity
import com.cynergisuite.middleware.vendor.freight.onboard.FreightOnboardTypeEntity
import com.cynergisuite.middleware.vendor.group.VendorGroupEntity
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermEntity
import com.cynergisuite.middleware.vendor.payment.term.infrastructure.VendorPaymentTermRepository
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepository @Inject constructor(
   private val addressRepository: AddressRepository,
   private val companyRepository: CompanyRepository,
   private val vendorPaymentTermRepository: VendorPaymentTermRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorRepository::class.java)
   private fun baseSelectQuery() = """
         SELECT
            v.id                             AS v_id,
            v.uu_row_id                      AS v_uu_row_id,
            v.time_created                   AS v_time_created,
            v.time_updated                   AS v_time_updated,
            v.company_id                     AS v_company_id,
            v.number                         AS v_number,
            v.name_key                       AS v_name_key,
            v.address_id                     AS v_address_id,
            v.our_account_number             AS v_our_account_number,
            v.pay_to_id                      AS v_pay_to_id,
            v.freight_on_board_type_id       AS v_freight_on_board_type_id,
            v.payment_terms_id               AS v_payment_terms_id,
            v.float_days                     AS v_float_days,
            v.normal_days                    AS v_normal_days,
            v.return_policy                  AS v_return_policy,
            v.ship_via_id                    AS v_ship_via_id,
            v.group_id                       AS v_group_id,
            v.shutdown_from                  AS v_shutdown_from,
            v.shutdown_thru                  AS v_shutdown_thru,
            v.minimum_quantity               AS v_minimum_quantity,
            v.minimum_amount                 AS v_minimum_amount,
            v.free_ship_quantity             AS v_free_ship_quantity,
            v.free_ship_amount               AS v_free_ship_amount,
            v.vendor_1099                    AS v_vendor_1099,
            v.federal_id_number              AS v_federal_id_number,
            v.sales_representative_name      AS v_sales_representative_name,
            v.sales_representative_fax       AS v_sales_representative_fax,
            v.separate_check                 AS v_separate_check,
            v.bump_percent                   AS v_bump_percent,
            v.freight_calc_method_type_id    AS v_freight_calc_method_type_id,
            v.freight_percent                AS v_freight_percent,
            v.freight_amount                 AS v_freight_amount,
            v.charge_inventory_tax_1         AS v_charge_inventory_tax_1,
            v.charge_inventory_tax_2         AS v_charge_inventory_tax_2,
            v.charge_inventory_tax_3         AS v_charge_inventory_tax_3,
            v.charge_inventory_tax_4         AS v_charge_inventory_tax_4,
            v.federal_id_number_verification AS v_federal_id_number_verification,
            v.email_address                  AS v_email_address,
            comp.id                          AS comp_id,
            comp.uu_row_id                   AS comp_uu_row_id,
            comp.time_created                AS comp_time_created,
            comp.time_updated                AS comp_time_updated,
            comp.name                        AS comp_name,
            comp.doing_business_as           AS comp_doing_business_as,
            comp.client_code                 AS comp_client_code,
            comp.client_id                   AS comp_client_id,
            comp.dataset_code                AS comp_dataset_code,
            comp.federal_id_number           AS comp_federal_id_number,
            onboard.id                       AS onboard_id,
            onboard.value                    AS onboard_value,
            onboard.description              AS onboard_description,
            onboard.localization_code        AS onboard_localization_code,
            method.id                        AS method_id,
            method.value                     AS method_value,
            method.description               AS method_description,
            method.localization_code         AS method_localization_code,
            address.id                                AS address_id,
            address.uu_row_id                         AS address_uu_row_id,
            address.time_created                      AS address_time_created,
            address.time_updated                      AS address_time_updated,
            address.number                            AS address_number,
            address.name                              AS address_name,
            address.address1                          AS address_address1,
            address.address2                          AS address_address2,
            address.city                              AS address_city,
            address.state                             AS address_state,
            address.postal_code                       AS address_postal_code,
            address.latitude                          AS address_latitude,
            address.longitude                         AS address_longitude,
            address.country                           AS address_country,
            address.county                            AS address_county,
            address.phone                             AS address_phone,
            address.fax                               AS address_fax,
            vpt.id                                    AS vpt_id,
            vpt.uu_row_id                             AS vpt_uu_row_id,
            vpt.time_created                          AS vpt_time_created,
            vpt.time_updated                          AS vpt_time_updated,
            vpt.company_id                            AS vpt_company_id,
            vpt.description                           AS vpt_description,
            vpt.number                                AS vpt_number,
            vpt.number_of_payments                    AS vpt_number_of_payments,
            vpt.discount_month                        AS vpt_discount_month,
            vpt.discount_days                         AS vpt_discount_days,
            vpt.discount_percent                      AS vpt_discount_percent,
            shipVia.id                                AS shipVia_id,
            shipVia.uu_row_id                         AS shipVia_uu_row_id,
            shipVia.time_created                      AS shipVia_time_created,
            shipVia.time_updated                      AS shipVia_time_updated,
            shipVia.description                       AS shipVia_description,
            shipVia.number                            AS shipVia_number,
            vgrp.id                                   AS vgrp_id,
            vgrp.uu_row_id                            AS vgrp_uu_row_id,
            vgrp.time_created                         AS vgrp_time_created,
            vgrp.time_updated                         AS vgrp_time_updated,
            vgrp.company_id                           AS vgrp_company_id,
            vgrp.value                                AS vgrp_value,
            vgrp.description                          AS vgrp_description,
            count(*) OVER()                           AS total_elements
         FROM vendor v
         JOIN company comp                            ON v.company_id = comp.id
         JOIN freight_on_board_type_domain onboard    ON onboard.id = v.freight_on_board_type_id
         JOIN freight_calc_method_type_domain method  ON method.id = v.freight_calc_method_type_id
         JOIN address                                 ON address.id = v.address_id
         JOIN vendor_payment_term vpt                 ON vpt.id = v.payment_terms_id
         JOIN ship_via shipVia                        ON shipVia.id = v.ship_via_id
         JOIN vendor_group vgrp                       ON vgrp.id = v.group_id
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
         return jdbc.queryPaged("""
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
            } while(rs.next())
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

      val address = addressRepository.insert(entity.address)
      val entity = entity.copy(address = address)
      val payToId = if (entity.payTo != null) entity.payTo.myId() else null

      val vendor = jdbc.insertReturning(
         """
        INSERT INTO vendor(company_id,
                           number,
                           name_key,
                           address_id,
                           our_account_number,
                           pay_to_id,
                           freight_on_board_type_id,
                           payment_terms_id,
                           float_days,
                           normal_days,
                           return_policy,
                           ship_via_id,
                           group_id,
                           shutdown_from,
                           shutdown_thru,
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
                           email_address)
         VALUES (
            :company_id,
            :number,
            :name_key,
            :address_id,
            :our_account_number,
            :pay_to_id,
            :freight_on_board_type_id,
            :payment_terms_id,
            :float_days,
            :normal_days,
            :return_policy,
            :ship_via_id,
            :group_id,
            :shutdown_from,
            :shutdown_thru,
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
            :email_address
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company.myId(),
            "number" to entity.vendorNumber,
            "name_key" to entity.nameKey,
            "address_id" to entity.address.id,
            "our_account_number" to entity.ourAccountNumber,
            "pay_to_id" to payToId,
            "freight_on_board_type_id" to entity.freightOnboardType.id,
            "payment_terms_id" to entity.paymentTerm.id,
            "float_days" to entity.floatDays,
            "normal_days" to entity.normalDays,
            "return_policy" to entity.returnPolicy,
            "ship_via_id" to entity.shipVia.id,
            "group_id" to entity.vendorGroup.id,
            "shutdown_from" to entity.shutdownFrom,
            "shutdown_thru" to entity.shutdownThru,
            "minimum_quantity" to entity.minimumQuantity,
            "minimum_amount" to entity.minimumAmount,
            "free_ship_quantity" to entity.freeShipQuantity,
            "free_ship_amount" to entity.freeShipAmount,
            "vendor_1099" to entity.vendor1099,
            "federal_id_number" to entity.federalIdNumber,
            "sales_representative_name" to entity.salesRepName,
            "sales_representative_fax" to entity.salesRepFax,
            "separate_check" to entity.separateCheck,
            "bump_percent" to entity.bumpPercent,
            "freight_calc_method_type_id" to entity.freightMethodType.id,
            "freight_percent" to entity.freightPercent,
            "freight_amount" to entity.freightAmount,
            "charge_inventory_tax_1" to entity.chargeInvTax1,
            "charge_inventory_tax_2" to entity.chargeInvTax2,
            "charge_inventory_tax_3" to entity.chargeInvTax3,
            "charge_inventory_tax_4" to entity.chargeInvTax4,
            "federal_id_number_verification" to entity.federalIdNumberVerification,
            "email_address" to entity.emailAddress
            ),
         RowMapper { rs, _ -> mapRowUpsert(rs, entity) }
      )

      return vendor
   }

   @Transactional
   fun update(entity: VendorEntity): VendorEntity {
      logger.debug("Updating Vendor {}", entity)
      val payToId = if (entity.payTo != null) entity.payTo.myId() else null

      val updated = jdbc.updateReturning("""
         UPDATE vendor
         SET
         company_id = :companyId,
         number = :number,
         name_key = :nameKey,
         address_id = :addressId,
         our_account_number = :ourAccountNumber,
         pay_to_id = :payTo,
         freight_on_board_type_id = :freightOnboardType,
         payment_terms_id = :paymentTerm,
         float_days = :floatDays,
         normal_days = :normalDays,
         return_policy = :returnPolicy,
         ship_via_id = :shipVia,
         group_id = :vendorGroup,
         shutdown_from = :shutdownFrom,
         shutdown_thru = :shutdownThru,
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
         email_address = :emailAddress
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "companyId" to entity.company.myId(),
            "number" to entity.vendorNumber,
            "nameKey" to entity.nameKey,
            "addressId" to entity.address.id,
            "ourAccountNumber" to entity.ourAccountNumber,
            "payTo" to payToId,
            "freightOnboardType" to entity.freightOnboardType.id,
            "paymentTerm" to entity.paymentTerm.id,
            "floatDays" to entity.floatDays,
            "normalDays" to entity.normalDays,
            "returnPolicy" to entity.returnPolicy,
            "shipVia" to entity.shipVia.id,
            "vendorGroup" to entity.vendorGroup.id,
            "shutdownFrom" to entity.shutdownFrom,
            "shutdownThru" to entity.shutdownThru,
            "minimumQuantity" to entity.minimumQuantity,
            "minimumAmount" to entity.minimumAmount,
            "freeShipQuantity" to entity.freeShipQuantity,
            "freeShipAmount" to entity.freeShipAmount,
            "vendor1099" to entity.vendor1099,
            "federalIdNumber" to entity.federalIdNumber,
            "salesRepName" to entity.salesRepName,
            "salesRepFax" to entity.salesRepFax,
            "separateCheck" to entity.separateCheck,
            "bumpPercent" to entity.bumpPercent,
            "freightMethodType" to entity.freightMethodType.id,
            "freightPercent" to entity.freightPercent,
            "freightAmount" to entity.freightAmount,
            "chargeInvTax1" to entity.chargeInvTax1,
            "chargeInvTax2" to entity.chargeInvTax2,
            "chargeInvTax3" to entity.chargeInvTax3,
            "chargeInvTax4" to entity.chargeInvTax4,
            "federalIdNumberVerification" to entity.federalIdNumberVerification,
            "emailAddress" to entity.emailAddress
         ),
         RowMapper { rs, _ -> mapRowUpsert(rs, entity) }
      )

      logger.debug("Updated Vendor {}", updated)

      return updated
   }

   private fun mapRow(rs: ResultSet, company: Company): VendorEntity {
      val payToId = rs.getLongOrNull("v_pay_to_id")
      return VendorEntity(
         id = rs.getLong("v_id"),
         company = company,
         vendorNumber = rs.getInt("v_number"),
         nameKey = rs.getString("v_name_key"),
         address = mapAddress(rs, "address_"),
         ourAccountNumber = rs.getInt("v_our_account_number"),
         payTo = if (payToId != null) SimpleIdentifiableEntity(payToId) else null,
         freightOnboardType = mapOnboard(rs, "onboard_"),
         paymentTerm = mapPaymentTerm(rs, company,"vpt_"),
         floatDays = rs.getIntOrNull("v_float_days"),
         normalDays = rs.getIntOrNull("v_normal_days"),
         returnPolicy = rs.getBoolean("v_return_policy"),
         shipVia = mapShipVia(rs, company,"shipVia_"),
         vendorGroup = mapVendorGroup(rs, company,"vgrp_"),
         shutdownFrom = rs.getLocalDate("v_shutdown_from"),
         shutdownThru = rs.getLocalDate("v_shutdown_thru"),
         minimumQuantity = rs.getInt("v_minimum_quantity"),
         minimumAmount = rs.getBigDecimal("v_minimum_amount"),
         freeShipQuantity = rs.getInt("v_free_ship_quantity"),
         freeShipAmount = rs.getBigDecimal("v_free_ship_amount"),
         vendor1099 = rs.getBoolean("v_vendor_1099"),
         federalIdNumber = rs.getString("v_federal_id_number"),
         salesRepName = rs.getString("v_sales_representative_name"),
         salesRepFax = rs.getString("v_sales_representative_fax"),
         separateCheck = rs.getBoolean("v_separate_check"),
         bumpPercent = rs.getBigDecimal("v_bump_percent"),
         freightMethodType = mapMethod(rs, "method_"),
         freightPercent = rs.getBigDecimal("v_freight_percent"),
         freightAmount = rs.getBigDecimal("v_freight_amount"),
         chargeInvTax1 = rs.getBoolean("v_charge_inventory_tax_1"),
         chargeInvTax2 = rs.getBoolean("v_charge_inventory_tax_2"),
         chargeInvTax3 = rs.getBoolean("v_charge_inventory_tax_3"),
         chargeInvTax4 = rs.getBoolean("v_charge_inventory_tax_4"),
         federalIdNumberVerification = rs.getBoolean("v_federal_id_number_verification"),
         emailAddress = rs.getString("v_email_address")
      )
   }

   private fun mapRowUpsert(rs: ResultSet, entity: VendorEntity): VendorEntity {
      val payToId = rs.getLongOrNull("pay_to_id")
      return VendorEntity(
         id = rs.getLong("id"),
         company = entity.company,
         vendorNumber = rs.getInt("number"),
         nameKey = rs.getString("name_key"),
         address = entity.address, //This needs to have the newly inserted address passed in
         ourAccountNumber = rs.getInt("our_account_number"),
         payTo = if (payToId != null) SimpleIdentifiableEntity(payToId) else null,
         freightOnboardType = entity.freightOnboardType,
         paymentTerm = entity.paymentTerm,
         floatDays = rs.getIntOrNull("float_days"),
         normalDays = rs.getIntOrNull("normal_days"),
         returnPolicy = rs.getBoolean("return_policy"),
         shipVia = entity.shipVia,
         vendorGroup = entity.vendorGroup,
         shutdownFrom = rs.getLocalDate("shutdown_from"),
         shutdownThru = rs.getLocalDate("shutdown_thru"),
         minimumQuantity = rs.getInt("minimum_quantity"),
         minimumAmount = rs.getBigDecimal("minimum_amount"),
         freeShipQuantity = rs.getInt("free_ship_quantity"),
         freeShipAmount = rs.getBigDecimal("free_ship_amount"),
         vendor1099 = rs.getBoolean("vendor_1099"),
         federalIdNumber = rs.getString("federal_id_number"),
         salesRepName = rs.getString("sales_representative_name"),
         salesRepFax = rs.getString("sales_representative_fax"),
         separateCheck = rs.getBoolean("separate_check"),
         bumpPercent = rs.getBigDecimal("bump_percent"),
         freightMethodType = entity.freightMethodType,
         freightPercent = rs.getBigDecimal("freight_percent"),
         freightAmount = rs.getBigDecimal("freight_amount"),
         chargeInvTax1 = rs.getBoolean("charge_inventory_tax_1"),
         chargeInvTax2 = rs.getBoolean("charge_inventory_tax_2"),
         chargeInvTax3 = rs.getBoolean("charge_inventory_tax_3"),
         chargeInvTax4 = rs.getBoolean("charge_inventory_tax_4"),
         federalIdNumberVerification = rs.getBoolean("federal_id_number_verification"),
         emailAddress = rs.getString("email_address")
      )
   }

   private fun mapOnboard(rs: ResultSet, columnPrefix: String): FreightOnboardTypeEntity =
      FreightOnboardTypeEntity(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapMethod(rs: ResultSet, columnPrefix: String): FreightMethodTypeEntity =
      FreightMethodTypeEntity(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapAddress(rs: ResultSet, columnPrefix: String = StringUtils.EMPTY): AddressEntity =
         AddressEntity(
            id = rs.getLong("${columnPrefix}id"),
            timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
            timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
            number = rs.getInt("${columnPrefix}number"),
            name = rs.getString("${columnPrefix}name"),
            address1 = rs.getString("${columnPrefix}address1"),
            address2 = rs.getString("${columnPrefix}address2"),
            city = rs.getString("${columnPrefix}city"),
            state = rs.getString("${columnPrefix}state"),
            postalCode = rs.getString("${columnPrefix}postal_code"),
            latitude = rs.getDouble("${columnPrefix}latitude"),
            longitude = rs.getDouble("${columnPrefix}longitude"),
            country = rs.getString("${columnPrefix}country"),
            county = rs.getString("${columnPrefix}county"),
            phone = rs.getString("${columnPrefix}phone"),
            fax = rs.getString("${columnPrefix}fax")
         )

   private fun mapPaymentTerm(rs: ResultSet, company: Company, columnPrefix: String = StringUtils.EMPTY): VendorPaymentTermEntity =
      VendorPaymentTermEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company,
         description = rs.getString("${columnPrefix}description"),
         discountMonth = rs.getInt("${columnPrefix}discount_month"),
         discountDays = rs.getInt("${columnPrefix}discount_days"),
         discountPercent = rs.getBigDecimal("${columnPrefix}discount_percent")
      )

   private fun mapShipVia(rs: ResultSet, company: Company, columnPrefix: String = StringUtils.EMPTY): ShipViaEntity =
      ShipViaEntity(
         id = rs.getLong("${columnPrefix}id"),
         description = rs.getString("${columnPrefix}description"),
         number = rs.getInt("${columnPrefix}number"),
         company = company
      )

   private fun mapVendorGroup(rs: ResultSet, company: Company, columnPrefix: String = StringUtils.EMPTY): VendorGroupEntity =
      VendorGroupEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company,
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description")
      )

}
