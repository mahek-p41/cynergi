package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.InventoryInquiryFilterRequest
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.DatasetRequiringRepository
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.*
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.area.AreaEntity
import com.cynergisuite.middleware.area.AreaType
import com.cynergisuite.middleware.area.toAreaTypeEntity
import com.cynergisuite.middleware.audit.AuditEntity
import com.cynergisuite.middleware.audit.status.Created
import com.cynergisuite.middleware.audit.status.InProgress
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.inventory.InventoryEntity
import com.cynergisuite.middleware.inventory.InventoryInquiryDTO
import com.cynergisuite.middleware.inventory.location.InventoryLocationType
import com.cynergisuite.middleware.location.infrastructure.LocationRepository
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.*
import javax.transaction.Transactional

@Singleton
class InventoryEomRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val companyRepository: CompanyRepository,
   private val storeRepository: StoreRepository,
   private val accountRepository: AccountRepository
  ) : DatasetRequiringRepository {
   private val logger: Logger = LoggerFactory.getLogger(InventoryEomRepository::class.java)

   private val selectBase =
      """
      SELECT
         i.store_number_sfk as store_number,
         i.year as year,
         i.month as month,
         i.serial_numer as serial_number,
         i.cost as cost,
         i.net_book_value as net_book_value,
         i.book_depreciation as book_depreciation,
         i.asset_account_id as asset_account.id,
         i.contra_asset_account_id as contra_asset_account.id,
         i.model as model,
         i.alternate_id as alternate_id,
         i.current_inv_indr as current_inv_indr,
         i.macrs_previous_fiscal_year_end_cost as macrs_previous_fiscal_year_end_cost,
         i.macrs_previous_fiscal_year_end_depr as macrs_previous_fiscal_year_end_depr,
         i.macrs_previous_fiscal_year_end_amt_depr as macrs_previous_fiscal_year_end_depr,
         i.macrs_previous_fiscal_year_end_date as macrs_previous_fiscal_year_end_date,
         i.macrs_latest_fiscal_year_end_cost as macrs_latest_fiscal_year_end_cost ,
         i.macrs_latest_fiscal_year_end_depr as macrs_latest_fiscal_year_end_depr,
         i.macrs_latest_fiscal_year_end_amt_depr as macrs_latest_fiscal_year_end_depr ,
         i.macrs_previous_fiscal_year_bonus as macrs_previous_fiscal_year_bonus,
         i.macrs_latest_fiscal_year_bonus as macrs_latest_fiscal_year_bonus,
         i.deleted as deleted,
         comp.id                       AS comp_id,
         comp.time_created             AS comp_time_created,
         comp.time_updated             AS comp_time_updated,
         comp.name                     AS comp_name,
         comp.doing_business_as        AS comp_doing_business_as,
         comp.client_code              AS comp_client_code,
         comp.client_id                AS comp_client_id,
         comp.dataset_code             AS comp_dataset_code,
         comp.federal_id_number        AS comp_federal_id_number,
         compAddress.id                AS comp_address_id,
         compAddress.name              AS comp_address_name,
         compAddress.address1          AS comp_address_address1,
         compAddress.address2          AS comp_address_address2,
         compAddress.city              AS comp_address_city,
         compAddress.state             AS comp_address_state,
         compAddress.postal_code       AS comp_address_postal_code,
         compAddress.latitude          AS comp_address_latitude,
         compAddress.longitude         AS comp_address_longitude,
         compAddress.country           AS comp_address_country,
         compAddress.county            AS comp_address_county,
         compAddress.phone             AS comp_address_phone,
         compAddress.fax               AS comp_address_fax,
         currentStore.id               AS current_store_id,
         currentStore.number           AS current_store_number,
         currentStore.name             AS current_store_name,
         currentStore.dataset          AS current_store_dataset
      FROM company comp
           JOIN inventory_end_of_month i ON comp.id = i.company_id
           LEFT JOIN address AS compAddress ON comp.address_id = compAddress.id AND compAddress.deleted = FALSE
           LEFT OUTER JOIN system_stores_fimvw currentStore ON comp.dataset_code = currentStore.dataset AND i.store_number_sfk = currentStore.number
      """.trimIndent()

   override fun exists(id: Long, company: CompanyEntity): Boolean {
      TODO("Not yet implemented")
   }
}

