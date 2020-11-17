package com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
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
class BankReconciliationRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val bankRepository: BankRepository,
   private val bankReconciliationTypeRepository: BankReconciliationTypeRepository,
   private val companyRepository: CompanyRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(BankReconciliationRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         WITH bank AS (
            ${bankRepository.selectBaseQuery()}
         ), company AS (
            ${companyRepository.companyBaseQuery()}
         )
         SELECT
            bankRecon.id                              AS bankRecon_id,
            bankRecon.date                            AS bankRecon_date,
            bankRecon.cleared_date                    AS bankRecon_cleared_date,
            bankRecon.amount                          AS bankRecon_amount,
            bankRecon.description                     AS bankRecon_description,
            bankRecon.document                        AS bankRecon_document,
            comp.id                                   AS comp_id,
            comp.uu_row_id                            AS comp_uu_row_id,
            comp.time_created                         AS comp_time_created,
            comp.time_updated                         AS comp_time_updated,
            comp.name                                 AS comp_name,
            comp.doing_business_as                    AS comp_doing_business_as,
            comp.client_code                          AS comp_client_code,
            comp.client_id                            AS comp_client_id,
            comp.dataset_code                         AS comp_dataset_code,
            comp.federal_id_number                    AS comp_federal_id_number,
            comp.address_id                           AS comp_address_id,
            comp.address_name                         AS address_name,
            comp.address_address1                     AS address_address1,
            comp.address_address2                     AS address_address2,
            comp.address_city                         AS address_city,
            comp.address_state                        AS address_state,
            comp.address_postal_code                  AS address_postal_code,
            comp.address_latitude                     AS address_latitude,
            comp.address_longitude                    AS address_longitude,
            comp.address_country                      AS address_country,
            comp.address_county                       AS address_county,
            comp.address_phone                        AS address_phone,
            comp.address_fax                          AS address_fax,
            bank.bank_id                              AS bank_id,
            bank.bank_name                            AS bank_name,
            bank.bank_number                          AS bank_number,
            bank.comp_id                              AS bank_comp_id,
            bank.comp_uu_row_id                       AS bank_comp_uu_row_id,
            bank.comp_time_created                    AS bank_comp_time_created,
            bank.comp_time_updated                    AS bank_comp_time_updated,
            bank.comp_name                            AS bank_comp_name,
            bank.comp_doing_business_as               AS bank_comp_doing_business_as,
            bank.comp_client_code                     AS bank_comp_client_code,
            bank.comp_client_id                       AS bank_comp_client_id,
            bank.comp_dataset_code                    AS bank_comp_dataset_code,
            bank.comp_federal_id_number               AS bank_comp_federal_id_number,
            bank.comp_address_id                      AS bank_comp_address_id,
            bank.address_name                         AS bank_address_name,
            bank.address_address1                     AS bank_address_address1,
            bank.address_address2                     AS bank_address_address2,
            bank.address_city                         AS bank_address_city,
            bank.address_state                        AS bank_address_state,
            bank.address_postal_code                  AS bank_address_postal_code,
            bank.address_latitude                     AS bank_address_latitude,
            bank.address_longitude                    AS bank_address_longitude,
            bank.address_country                      AS bank_address_country,
            bank.address_county                       AS bank_address_county,
            bank.address_phone                        AS bank_address_phone,
            bank.address_fax                          AS bank_address_fax,
            bank.account_*,
            bank.glProfitCenter_id                    AS bank_glProfitCenter_id,
            bank.glProfitCenter_number                AS bank_glProfitCenter_number,
            bank.glProfitCenter_name                  AS bank_glProfitCenter_name,
            bank.glProfitCenter_dataset               AS bank_glProfitCenter_dataset,
            bankReconType.id                          AS bankReconType_id,
            bankReconType.value                       AS bankReconType_value,
            bankReconType.description                 AS bankReconType_description,
            bankReconType.localization_code           AS bankReconType_localization_code
         FROM bank_reconciliation bankRecon
               JOIN company comp ON bankRecon.company_id = comp.id
               JOIN bank ON bankRecon.bank_id = bank.id
               JOIN bank_reconciliation_type bankReconType ON bankRecon.type_id = bankReconType.id
      """
   }

   fun findOne(id: Long, company: Company): BankReconciliationEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE bankRecon.id = :id AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            mapRow(rs, company, "bankRecon_")
         }
      )

      logger.trace("Searching for BankReconciliation id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<BankReconciliationEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query =
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by bankRecon_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT ${page.size()} OFFSET ${page.offset()}
      """
      var totalElements: Long? = null
      val resultList: MutableList<BankReconciliationEntity> = mutableListOf()

      jdbc.query(query, params) { rs ->
         resultList.add(mapRow(rs, company, "bankRecon_"))
         if (totalElements == null) {
            totalElements = rs.getLong("total_elements")
         }
      }

      return RepositoryPage(
         requested = page,
         elements = resultList,
         totalElements = totalElements ?: 0
      )
   }

   @Transactional
   fun insert(entity: BankReconciliationEntity): BankReconciliationEntity {
      logger.debug("Inserting bank_reconciliation {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO bank_reconciliation(
            company_id,
            bank_id,
            type_id,
            transaction_date,
            cleared_date,
            amount,
            description,
            document
         )
	      VALUES (
            :company_id,
            :bank_id,
            :type_id,
            :transaction_date,
            :cleared_date,
            :amount,
            :description,
            :document
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to entity.company.myId(),
            "bank_id" to entity.bank.id,
            "type_id" to entity.type.id,
            "transaction_date" to entity.date,
            "cleared_date" to entity.clearedDate,
            "amount" to entity.amount,
            "description" to entity.description,
            "document" to entity.document
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity)
         }
      )
   }

   @Transactional
   fun update(entity: BankReconciliationEntity): BankReconciliationEntity {
      logger.debug("Updating bank {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE bank
         SET
            company_id = :company_id,
            bank_id = :bank_id,
            type_id = :type_id,
            transaction_date = :transaction_date,
            cleared_date = :cleared_date,
            amount = :amount,
            description = :description,
            document = :document
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to entity.company.myId(),
            "bank_id" to entity.bank.id,
            "type_id" to entity.type.id,
            "transaction_date" to entity.date,
            "cleared_date" to entity.clearedDate,
            "amount" to entity.amount,
            "description" to entity.description,
            "document" to entity.document
         ),
         RowMapper { rs, _ ->
            mapRow(rs, entity)
         }
      )
   }

   private fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): BankReconciliationEntity {
      return BankReconciliationEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = company,
         bank = bankRepository.mapRow(rs, company, "bank_"),
         type = bankReconciliationTypeRepository.mapRow(rs, "bankReconType_"),
         date = rs.getLocalDate("${columnPrefix}transaction_date"),
         clearedDate = rs.getLocalDate("${columnPrefix}cleared_date"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         description = rs.getString("${columnPrefix}description"),
         document = rs.getInt("${columnPrefix}document")
      )
   }

   private fun mapRow(rs: ResultSet, entity: BankReconciliationEntity, columnPrefix: String = EMPTY): BankReconciliationEntity {
      return BankReconciliationEntity(
         id = rs.getLong("${columnPrefix}id"),
         company = entity.company,
         bank = entity.bank,
         type = entity.type,
         date = rs.getLocalDate("${columnPrefix}transaction_date"),
         clearedDate = rs.getLocalDate("${columnPrefix}cleared_date"),
         amount = rs.getBigDecimal("${columnPrefix}amount"),
         description = rs.getString("${columnPrefix}description"),
         document = rs.getInt("${columnPrefix}document")
      )
   }
}
