package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.inventory.InventoryEndOfMonthEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import javax.transaction.Transactional

@Singleton
class InventoryEomRepository @Inject constructor(
   private val jdbc: Jdbi,
  ) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(InventoryEomRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
         i.store_number_sfk                                   AS store_number,
         i.year                                               AS year,
         i.month                                              AS month,
         i.serial_number                                      AS serial_number,
         i.cost                                               AS cost,
         i.net_book_value                                     AS net_book_value,
         i.book_depreciation                                  AS book_depreciation,
         i.asset_account_id                                   AS asset_account_id,
         i.contra_asset_account_id                            AS contra_asset_account_id,
         i.model                                              AS model,
         i.alternate_id                                       AS alternate_id,
         i.current_inv_indr                                   AS current_inv_indr,
         i.macrs_previous_fiscal_year_end_cost                AS macrs_previous_fiscal_year_end_cost,
         i.macrs_previous_fiscal_year_end_depr                AS macrs_previous_fiscal_year_end_depr,
         i.macrs_previous_fiscal_year_end_amt_depr            AS macrs_previous_fiscal_year_end_depr,
         i.macrs_previous_fiscal_year_end_date                AS macrs_previous_fiscal_year_end_date,
         i.macrs_latest_fiscal_year_end_cost                  AS macrs_latest_fiscal_year_end_cost ,
         i.macrs_latest_fiscal_year_end_depr                  AS macrs_latest_fiscal_year_end_depr,
         i.macrs_latest_fiscal_year_end_amt_depr              AS macrs_latest_fiscal_year_end_depr ,
         i.macrs_previous_fiscal_year_bonus                   AS macrs_previous_fiscal_year_bonus,
         i.macrs_latest_fiscal_year_bonus                     AS macrs_latest_fiscal_year_bonus,
         i.deleted                                            AS deleted,
         comp.id                                              AS comp_id,
         comp.time_created                                    AS comp_time_created,
         comp.time_updated                                    AS comp_time_updated,
         comp.name                                            AS comp_name,
         comp.doing_business_as                               AS comp_doing_business_as,
         comp.client_code                                     AS comp_client_code,
         comp.client_id                                       AS comp_client_id,
         comp.dataset_code                                    AS comp_dataset_code,
         comp.federal_id_number                               AS comp_federal_id_number,
         comp.include_demo_inventory                          AS comp_include_demo_inventory,
         compAddress.id                                       AS comp_address_id,
         compAddress.name                                     AS comp_address_name,
         compAddress.address1                                 AS comp_address_address1,
         compAddress.address2                                 AS comp_address_address2,
         compAddress.city                                     AS comp_address_city,
         compAddress.state                                    AS comp_address_state,
         compAddress.postal_code                              AS comp_address_postal_code,
         compAddress.latitude                                 AS comp_address_latitude,
         compAddress.longitude                                AS comp_address_longitude,
         compAddress.country                                  AS comp_address_country,
         compAddress.county                                   AS comp_address_county,
         compAddress.phone                                    AS comp_address_phone,
         compAddress.fax                                      AS comp_address_fax,
         currentStore.id                                      AS current_store_id,
         currentStore.number                                  AS current_store_number,
         currentStore.name                                    AS current_store_name,
         currentStore.dataset                                 AS current_store_dataset
      FROM company comp
           JOIN inventory_end_of_month i ON comp.id = i.company_id
           LEFT JOIN address AS compAddress ON comp.address_id = compAddress.id AND compAddress.deleted = FALSE
           LEFT OUTER JOIN system_stores_fimvw currentStore ON comp.dataset_code = currentStore.dataset AND i.store_number_sfk = currentStore.number
      """.trimIndent()
   }

   override fun exists(id: Long, company: CompanyEntity): Boolean {
      TODO("Not yet implemented")
   }

   @Transactional
   fun insert(entity: InventoryEndOfMonthEntity, company: CompanyEntity): InventoryEndOfMonthEntity {
      logger.debug("Inserting GeneralLedgerJournal {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO inventory_end_of_month(
            company_id,
            store_number_sfk,
            year,
            month,
            serial_number,
            cost,
            net_book_value,
            book_depreciation,
            asset_account_id,
            contra_asset_account_id,
            model,
            alternate_id,
            current_inv_indr,
            macrs_previous_fiscal_year_end_cost,
            macrs_previous_fiscal_year_end_depr,
            macrs_previous_fiscal_year_end_amt_depr,
            macrs_previous_fiscal_year_end_date,
            macrs_latest_fiscal_year_end_cost,
            macrs_latest_fiscal_year_end_depr,
            macrs_latest_fiscal_year_end_amt_depr,
            macrs_previous_fiscal_year_bonus,
            macrs_latest_fiscal_year_bonus
         ) VALUES (
            :companyId,
            :storeNumber,
            :year,
            :month,
            :serialNumber,
            :cost,
            :netBookValue,
            :bookDepreciation,
            :assetAccountId,
            :contraAssetAccountId,
            :model,
            :alternateId,
            :currentInvIndr,
            :macrsPreviousFiscalYearEndCost,
            :macrsPreviousFiscalYearEndDepr,
            :macrsPreviousFiscalYearEndAmtDepr,
            :macrsPreviousFiscalYearEndDate,
            :macrsLatestFiscalYearEndCost,
            :macrsLatestFiscalYearEndDepr,
            :macrsLatestFiscalYearEndAmtDepr,
            :macrsPreviousFiscalYearBonus,
            :macrsLatestFiscalYearBonus
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "companyId" to company.id,
            "storeNumber" to entity.storeNumber,
            "year" to entity.year,
            "month" to entity.month,
            "serialNumber" to entity.serialNumber,
            "cost" to entity.cost,
            "netBookValue" to entity.netBookValue,
            "bookDepreciation" to entity.bookDepreciation,
            "assetAccountId" to entity.assetAccountId,
            "contraAssetAccountId" to entity.contraAssetAccountId,
            "model" to entity.model,
            "alternateId" to entity.alternateId,
            "currentInvIndr" to entity.currentInvIndr,
            "macrsPreviousFiscalYearEndCost" to entity.macrsPreviousFiscalYearEndCost,
            "macrsPreviousFiscalYearEndDepr" to entity.macrsPreviousFiscalYearEndDepr,
            "macrsPreviousFiscalYearEndAmtDepr" to entity.macrsPreviousFiscalYearEndAmtDepr,
            "macrsPreviousFiscalYearEndDate" to entity.macrsPreviousFiscalYearEndDate,
            "macrsLatestFiscalYearEndCost" to entity.macrsLatestFiscalYearEndCost,
            "macrsLatestFiscalYearEndDepr" to entity.macrsLatestFiscalYearEndDepr,
            "macrsLatestFiscalYearEndAmtDepr" to entity.macrsLatestFiscalYearEndAmtDepr,
            "macrsPreviousFiscalYearBonus" to entity.macrsPreviousFiscalYearBonus,
            "macrsLatestFiscalYearBonus" to entity.macrsLatestFiscalYearBonus,
         )
      ) { rs, _ -> mapRow(rs) }

   }

   fun mapRow(rs: ResultSet): InventoryEndOfMonthEntity {

      return InventoryEndOfMonthEntity(
         id = rs.getUuid("id"),
         companyId = rs.getUuid("company_id"),
         storeNumber = rs.getLong("store_number_sfk"),
         year = rs.getInt("year"),
         month = rs.getInt("month"),
         serialNumber = rs.getString("serial_number"),
         cost = rs.getBigDecimal("cost"),
         netBookValue = rs.getBigDecimal("net_book_value"),
         bookDepreciation = rs.getBigDecimal("book_depreciation"),
         assetAccountId = rs.getUuid("asset_account_id"),
         contraAssetAccountId = rs.getUuid("contra_asset_account_id"),
         model = rs.getString("model"),
         alternateId = rs.getString("alternate_id"),
         currentInvIndr = rs.getInt("current_inv_indr"),
         macrsPreviousFiscalYearEndCost = rs.getBigDecimal("macrs_previous_fiscal_year_end_cost"),
         macrsPreviousFiscalYearEndDepr = rs.getBigDecimal("macrs_previous_fiscal_year_end_depr"),
         macrsPreviousFiscalYearEndAmtDepr = rs.getBigDecimal("macrs_previous_fiscal_year_end_amt_depr"),
         macrsPreviousFiscalYearEndDate = rs.getLocalDate("macrs_previous_fiscal_year_end_date"),
         macrsLatestFiscalYearEndCost = rs.getBigDecimal("macrs_latest_fiscal_year_end_cost"),
         macrsLatestFiscalYearEndDepr = rs.getBigDecimal("macrs_latest_fiscal_year_end_depr"),
         macrsLatestFiscalYearEndAmtDepr = rs.getBigDecimal("macrs_latest_fiscal_year_end_amt_depr"),
         macrsPreviousFiscalYearBonus = rs.getBigDecimal("macrs_previous_fiscal_year_bonus"),
         macrsLatestFiscalYearBonus = rs.getBigDecimal("macrs_latest_fiscal_year_bonus"),
      )
   }
}

