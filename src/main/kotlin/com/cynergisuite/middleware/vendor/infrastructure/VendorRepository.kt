package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.vendor.VendorEntity
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VendorRepository @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val employeeRepository: EmployeeRepository,
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorRepository::class.java)
   private fun findOneQuery() = """
         SELECT
            v.id                             AS v_id,
            v.uu_row_id                      AS v_uu_row_id,
            v.time_created                   AS v_time_created,
            v.time_updated                   AS v_time_updated,
            v.company_id                     AS v_company_id,
            v.number                         AS v_number,
            v.name_key                       AS v_name_key,
            v.address                        AS v_address,
            v.our_account_number             AS v_our_account_number,
            v.pay_to                         AS v_pay_to,
            v.freight_on_board               AS v_freight_on_board,
            v.payment_terms                  AS v_payment_terms,
            v.float_days                     AS v_float_days,
            v.normal_days                    AS v_normal_days,
            v.return_policy                  AS v_return_policy,
            v.ship_via                       AS v_ship_via,
            v.vendor_group                   AS v_vendor_group,
            v.shutdown_from date             AS v_shutdown_from date,
            v.shutdown_thru date             AS v_shutdown_thru date,
            v.minimum_quantity               AS v_minimum_quantity,
            v.minimum_amount                 AS v_minimum_amount,
            v.free_ship_quantity             AS v_free_ship_quantity,
            v.free_ship_amount               AS v_free_ship_amount,
            v.vendor_1099                    AS v_vendor_1099,
            v.federal_id_number              AS v_federal_id_number,
            v.sales_rep_name                 AS v_sales_rep_name,
            v.sales_rep_fax                  AS v_sales_rep_fax,
            v.separate_check                 AS v_separate_check,
            v.bump_percent                   AS v_bump_percent,
            v.freight_calc_method            AS v_freight_calc_method,
            v.freight_percent                AS v_freight_percent,
            v.freight_amount                 AS v_freight_amount,
            v.charge_inv_tax_1               AS v_charge_inv_tax_1,
            v.charge_inv_tax_2               AS v_charge_inv_tax_2,
            v.charge_inv_tax_3               AS v_charge_inv_tax_3,
            v.charge_inv_tax_4               AS v_charge_inv_tax_4,
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
            comp.federal_id_number           AS comp_federal_id_number
         FROM vendor v
         JOIN company comp ON vpt.company_id = comp.id
      """

   fun findOne(id: Long, company: Company): VendorEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${findOneQuery()}\nWHERE vpt.id = :id AND comp.id = :comp_id"

      logger.debug("Searching for Vendor using {} {}", query, params)

      val found = jdbc.findFirstOrNull(query, params) { rs ->
         val vendorPaymentTerm = mapRow(rs)

         do {
            mapRowVendorPaymentTermSchedule(rs)?.also { vendorPaymentTerm.scheduleRecords.add(it) }
         } while(rs.next())

         vendorPaymentTerm
      }

      logger.trace("Searching for VendorPaymentTerm: {} resulted in {}", id, found)

      return found
   }


   /*
   fun findAll(pageRequest: StandardPageRequest, company: Long): RepositoryPage<VendorEntity, StandardPageRequest> {
      val params = mutableMapOf<String, Any?>()
      val status = pageRequest.status
      val whereBuilder = StringBuilder("WHERE v.companyId = :company ")
      val from = pageRequest.from
      val thru = pageRequest.thru

      if (from != null && thru != null) {
         params["from"] = from
         params["thru"] = thru
         whereBuilder.append(" AND v.time_created BETWEEN :from AND :thru ")
      }

      val sql = """
            SELECT
               v.id                             AS v_id,
               v.uu_row_id                      AS v_uu_row_id,
               v.time_created                   AS v_time_created,
               v.time_updated                   AS v_time_updated,
               v.company_id                     AS v_company_id,
               v.number                         AS v_number,
               v.name_key                       AS v_name_key,
               v.address                        AS v_address,
               v.our_account_number             AS v_our_account_number,
               v.pay_to                         AS v_pay_to,
   ???            v.freight_on_board               AS v_freight_on_board,
               v.payment_terms                  AS v_payment_terms,
               v.float_days                     AS v_float_days,
               v.normal_days                    AS v_normal_days,
               v.return_policy                  AS v_return_policy,
               v.ship_via                       AS v_ship_via,
               v.vendor_group                   AS v_vendor_group,
               v.shutdown_from date             AS v_shutdown_from date,
               v.shutdown_thru date             AS v_shutdown_thru date,
               v.minimum_quantity               AS v_minimum_quantity,
               v.minimum_amount                 AS v_minimum_amount,
               v.free_ship_quantity             AS v_free_ship_quantity,
               v.free_ship_amount               AS v_free_ship_amount,
               v.vendor_1099                    AS v_vendor_1099,
               v.federal_id_number              AS v_federal_id_number,
               v.sales_rep_name                 AS v_sales_rep_name,
               v.sales_rep_fax                  AS v_sales_rep_fax,
               v.separate_check                 AS v_separate_check,
               v.bump_percent                   AS v_bump_percent,
               v.freight_calc_method            AS v_freight_calc_method,
               v.freight_percent                AS v_freight_percent,
               v.freight_amount                 AS v_freight_amount,
               v.charge_inv_tax_1               AS v_charge_inv_tax_1,
               v.charge_inv_tax_2               AS v_charge_inv_tax_2,
               v.charge_inv_tax_3               AS v_charge_inv_tax_3,
               v.charge_inv_tax_4               AS v_charge_inv_tax_4,
               v.federal_id_number_verification AS v_federal_id_number_verification,
               v.email_address                  AS v_email_address
               FROM vendor v
               $whereBuilder
               ORDER BY ${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
               LIMIT ${pageRequest.size()}
               OFFSET ${pageRequest.offset()}
               )
         SELECT
            v.id AS a_id,
            v.uu_row_id AS a_uu_row_id,
            v.time_created AS a_time_created,
            v.time_updated AS a_time_updated,
            v.store_number AS store_number,
            v.number AS a_number,
            v.total_details AS a_total_details,
            v.total_exceptions AS a_total_exceptions,
            v.current_status AS current_status,
            v.last_updated AS a_last_updated,
            v.inventory_count AS a_inventory_count,
            v.exception_has_notes AS a_exception_has_notes,
            v.dataset AS a_dataset,
            av.id AS aa_id,
            av.uu_row_id AS aa_uu_row_id,
            av.time_created AS aa_time_created,
            av.time_updated AS aa_time_updated,
            astd.id AS astd_id,
            astd.value AS astd_value,
            astd.description AS astd_description,
            astd.color AS astd_color,
            astd.localization_code AS astd_localization_code,
            aer.e_id AS aer_id,
            aer.e_number AS aer_number,
            aer.e_dataset AS aer_dataset,
            aer.e_last_name AS aer_last_name,
            aer.e_first_name_mi AS aer_first_name_mi,
            aer.e_pass_code AS aer_pass_code,
            aer.e_active AS aer_active,
            aer.e_department AS aer_department,
            aer.e_employee_type AS aer_employee_type,
            aer.e_allow_auto_store_assign AS aer_allow_auto_store_assign,
            s.id AS s_id,
            s.name AS s_name,
            s.number AS s_number,
            s.dataset AS s_dataset,
            se.id AS se_id,
            se.name AS se_name,
            se.dataset AS s_dataset,
            total_elements AS total_elements
         FROM vendors a
              JOIN vendor_action aa
                  ON v.id = av.vendor_id
              JOIN vendor_status_type_domain astd
                  ON av.status_id = astd.id
              JOIN employees aer
                  ON av.changed_by = aer.e_number
              JOIN fastinfo_prod_import.store_vw s
                  ON v.store_number = s.number
                  AND s.dataset = :dataset
              LEFT OUTER JOIN fastinfo_prod_import.store_vw se
                  ON aer.s_number = se.number
                  AND se.dataset = :dataset
         ORDER BY a_${pageRequest.snakeSortBy()} ${pageRequest.sortDirection()}
      """.trimIndent()

      logger.trace("Finding all vendors for {} using {}\n{}", pageRequest, params, sql)

      val repoPage = jdbc.queryPaged<VendorEntity, StandardPageRequest>(sql, params, pageRequest) { rs, elements ->
         var currentId: Long = -1
         var currentParentEntity: VendorEntity? = null

         do {
            val tempId = rs.getLong("a_id")
            val tempParentEntity: VendorEntity = if (tempId != currentId) {
               currentId = tempId
               currentParentEntity = mapRow(rs)
               elements.add(currentParentEntity)
               currentParentEntity
            } else {
               currentParentEntity!!
            }

            tempParentEntity.actions.add(vendorRepository.mapRow(rs))
         } while(rs.next())
      }

      return repoPage.copy(elements = repoPage.elements.onEach(this::loadNextStates))
   }

    */

   fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM vendor WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Checking if Vendor: {} exists resulted in {}", id, exists)

      return exists
   }

   fun doesNotExist(id: Long): Boolean = !exists(id)

   @Transactional
   fun insert(entity: VendorEntity): VendorEntity {
      logger.debug("Inserting vendor {}", entity)

      val vendor = jdbc.insertReturning(
         """
        INSERT INTO vendor(company_id,
                           number,
                           name_key,
                           address,
                           our_account_number,
                           pay_to,
                           freight_on_board_type_id,
                           payment_terms,
                           float_days,
                           normal_days,
                           return_policy,
                           ship_via,
                           group_id,
                           shutdown_from,
                           shutdown_thru,
                           minimum_quantity,
                           minimum_amount,
                           free_ship_quantity,
                           free_ship_amount,
                           vendor_1099,
                           federal_id_number,
                           sales_rep_name,
                           sales_rep_fax,
                           separate_check,
                           bump_percent,
                           freight_calc_method_type,
                           freight_percent,
                           freight_amount,
                           charge_inv_tax_1,
                           charge_inv_tax_2,
                           charge_inv_tax_3,
                           charge_inv_tax_4,
                           federal_id_number_verification,
                           email_address)
         VALUES (
            :company_id,
            :number,
            :name_key,
            :address,
            :our_account_number,
            :pay_to,
            :freight_on_board_type_id,
            :payment_terms,
            :float_days,
            :normal_days,
            :return_policy,
            :ship_via,
            :group_id,
            :shutdown_from,
            :shutdown_thru,
            :minimum_quantity,
            :minimum_amount,
            :free_ship_quantity,
            :free_ship_amount,
            :vendor_1099,
            :federal_id_number,
            :sales_rep_name,
            :sales_rep_fax,
            :separate_check,
            :bump_percent,
            :freight_calc_method_type,
            :freight_percent,
            :freight_amount,
            :charge_inv_tax_1,
            :charge_inv_tax_2,
            :charge_inv_tax_3,
            :charge_inv_tax_4,
            :federal_id_number_verification,
            :email_address
         )
         RETURNING
            *
         """.trimMargin(),
         //mapOf("store_number" to entity.store.number, "dataset" to entity.store.dataset),
         mapOf(
            "company_id" to entity.company.myId(),
            "number" to entity.vendorNumber,
            "name_key" to entity.nameKey,
            "address_id" to entity.addressId,
            "our_account_number" to entity.ourAccountNumber,
            "pay_to_id" to entity.payTo,
            "freight_on_board_type_id" to entity.freightOnBoardTypeId,
            "payment_terms_id" to entity.paymentTermsId,
            "float_days" to entity.floatDays,
            "normal_days" to entity.normalDays,
            "return_policy" to entity.returnPolicy,
            "ship_via_id" to entity.shipViaId,
            "vend_group_id" to entity.vendorGroupId,
            "shutdown_from" to entity.shutdownFrom,
            "shutdown_thru" to entity.shutdownThru,
            "minimum_quantity" to entity.minimumQuantity,
            "minimum_amount" to entity.minimumAmount,
            "free_ship_quantity" to entity.freeShipQuantity,
            "free_ship_amount" to entity.freeShipAmount,
            "vendor_1099" to entity.vendor1099,
            "federal_id_number" to entity.federalIdNumber,
            "sales_rep_name" to entity.salesRepName,
            "sales_rep_fax" to entity.salesRepFax,
            "separate_check" to entity.separateCheck,
            "bump_percent" to entity.bumpPercent,
            "freight_calc_method_type" to entity.freightCalcMethodType,
            "freight_percent" to entity.freightPercent,
            "freight_amount" to entity.freightAmount,
            "charge_inv_tax_1" to entity.chargeInvTax1,
            "charge_inv_tax_2" to entity.chargeInvTax2,
            "charge_inv_tax_3" to entity.chargeInvTax3,
            "charge_inv_tax_4" to entity.chargeInvTax4,
            "federal_id_number_verification" to entity.federalIdNumberVerification,
            "email_address" to entity.emailAddress
            ),

         //TODO  I am thinking I should not have the v_ on these.
         RowMapper { rs, _ ->
            VendorEntity(
               id = rs.getLong("v_id"),
               uuRowId = rs.getUuid("v_uu_row_id"),
               timeCreated = rs.getOffsetDateTime("v_time_created"),
               timeUpdated = rs.getOffsetDateTime("v_time_updated"),
               companyId = rs.getLong("v_company_id"),
               vendorNumber = rs.getInt("v_number"),
               nameKey = rs.getString("v_name_key"),
               addressId = rs.getInt("v_address"),
               ourAccountNumber = rs.getInt("v_our_account_number"),
               payTo = rs.getInt("v_pay_to"),
               freightOnBoard = rs.getString("v_freight_on_board"),
               paymentTerms = rs.getInt("v_payment_terms"),
               floatDays = rs.getInt("v_float_days"),
               normalDays = rs.getInt("v_normal_days"),
               returnPolicy = rs.getString("v_return_policy"),
               shipViaId = rs.getInt("v_ship_via"),
               vendorGroup = rs.getString("v_vendor_group"),
               shutdownFrom = rs.getOffsetDateTime("v_shutdown_from"),
               shutdownThru = rs.getOffsetDateTime("v_shutdown_thru"),
               minimumQuantity = rs.getInt("v_minimum_quantity"),
               minimumAmount = rs.getBigDecimal("v_minimum_amount"),
               freeShipQuantity = rs.getInt("v_free_ship_quantity"),
               freeShipAmount = rs.getBigDecimal("v_free_ship_amount"),
               vendor1099 = rs.getBoolean("v_vendor_1099"),
               federalIdNumber = rs.getString("v_federal_id_number"),
               salesRepName = rs.getString("v_sales_rep_name"),
               salesRepFax = rs.getString("v_sales_rep_fax"),
               separateCheck = rs.getBoolean("v_separate_check"),
               bumpPercent = rs.getBigDecimal("v_bump_percent"),
               freightCalcMethod = rs.getString("v_freight_calc_method"),
               freightPercent = rs.getBigDecimal("v_freight_percent"),
               freightAmount = rs.getBigDecimal("v_freight_amount"),
               chargeInvTax1 = rs.getString("v_charge_inv_tax_1"),
               chargeInvTax2 = rs.getString("v_charge_inv_tax_2"),
               chargeInvTax3 = rs.getString("v_charge_inv_tax_3"),
               chargeInvTax4 = rs.getString("v_charge_inv_tax_4"),
               federalIdNumberVerification = rs.getBoolean("v_federal_id_number_verification"),
               emailAddress = rs.getString("v_email_address")
            )
         }
      )

      return vendor
   }

   /*
   @Transactional
   fun update(entity: VendorEntity): VendorEntity {
      logger.debug("Updating Vendor {}", entity)

      val actions = entity.actions.asSequence()
         .map { vendorActionRepository.upsert(entity, it) }
         .toMutableSet()

      return entity.copy(actions = actions)
   }

   fun mapRowOrNull(rs: ResultSet, rowPrefix: String = "v_"): VendorEntity? =
      //rs.getString("${rowPrefix}id")?.let { mapRow(rs, rowPrefix) }
      rs.getString("v_id")?.let { mapRow(rs) }
    */

   //TODO I removed use of the prefix here, and everywhere else I can think of. Not 100% sure that is right.
   private fun mapRow(rs: ResultSet): VendorEntity =
      VendorEntity(
         id = rs.getLong("v_id"),
         uuRowId = rs.getUuid("v_uu_row_id"),
         timeCreated = rs.getOffsetDateTime("v_time_created"),
         timeUpdated = rs.getOffsetDateTime("v_time_updated"),
         companyId = rs.getLong("v_company_id"),
         vendorNumber = rs.getInt("v_number"),
         nameKey = rs.getString("v_name_key"),
         addressId = rs.getInt("v_address"),
         ourAccountNumber = rs.getInt("v_our_account_number"),
         payTo = rs.getInt("v_pay_to"),
         freightOnBoard = rs.getString("v_freight_on_board"),
         paymentTerms = rs.getInt("v_payment_terms"),
         floatDays = rs.getInt("v_float_days"),
         normalDays = rs.getInt("v_normal_days"),
         returnPolicy = rs.getString("v_return_policy"),
         shipViaId = rs.getInt("v_ship_via"),
         vendorGroup = rs.getString("v_vendor_group"),
         shutdownFrom = rs.getOffsetDateTime("v_shutdown_from"),
         shutdownThru = rs.getOffsetDateTime("v_shutdown_thru"),
         minimumQuantity = rs.getInt("v_minimum_quantity"),
         minimumAmount = rs.getBigDecimal("v_minimum_amount"),
         freeShipQuantity = rs.getInt("v_free_ship_quantity"),
         freeShipAmount = rs.getBigDecimal("v_free_ship_amount"),
         vendor1099 = rs.getBoolean("v_vendor_1099"),
         federalIdNumber = rs.getString("v_federal_id_number"),
         salesRepName = rs.getString("v_sales_rep_name"),
         salesRepFax = rs.getString("v_sales_rep_fax"),
         separateCheck = rs.getBoolean("v_separate_check"),
         bumpPercent = rs.getBigDecimal("v_bump_percent"),
         freightCalcMethod = rs.getString("v_freight_calc_method"),
         freightPercent = rs.getBigDecimal("v_freight_percent"),
         freightAmount = rs.getBigDecimal("v_freight_amount"),
         chargeInvTax1 = rs.getString("v_charge_inv_tax_1"),
         chargeInvTax2 = rs.getString("v_charge_inv_tax_2"),
         chargeInvTax3 = rs.getString("v_charge_inv_tax_3"),
         chargeInvTax4 = rs.getString("v_charge_inv_tax_4"),
         federalIdNumberVerification = rs.getBoolean("v_federal_id_number_verification"),
         emailAddress = rs.getString("v_email_address")
      )
}
