package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getOffsetDateTime
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountStatusType
import com.cynergisuite.middleware.accounting.account.AccountType
import com.cynergisuite.middleware.accounting.account.NormalAccountBalanceType
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
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
class AccountRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            account.id                                   AS account_id,
            account.uu_row_id                            AS account_uu_row_id,
            account.time_created                         AS account_time_created,
            account.time_updated                         AS account_time_updated,
            account.number                               AS account_number,
            account.description                          AS account_description,
            account.form_1099_field                      AS account_form_1099_field,
            account.corporate_account_indicator          AS account_corporate_account_indicator,
            comp.id                                      AS comp_id,
            comp.uu_row_id                               AS comp_uu_row_id,
            comp.time_created                            AS comp_time_created,
            comp.time_updated                            AS comp_time_updated,
            comp.name                                    AS comp_name,
            comp.doing_business_as                       AS comp_doing_business_as,
            comp.client_code                             AS comp_client_code,
            comp.client_id                               AS comp_client_id,
            comp.dataset_code                            AS comp_dataset_code,
            comp.federal_id_number                       AS comp_federal_id_number,
            type.id                                      AS type_id,
            type.value                                   AS type_value,
            type.description                             AS type_description,
            type.localization_code                       AS type_localization_code,
            balance_type.id                              AS balance_type_id,
            balance_type.value                           AS balance_type_value,
            balance_type.description                     AS balance_type_description,
            balance_type.localization_code               AS balance_type_localization_code,
            status.id                                    AS status_id,
            status.value                                 AS status_value,
            status.description                           AS status_description,
            status.localization_code                     AS status_localization_code
         FROM account
               JOIN company comp
                     ON comp.id = account.company_id
               JOIN account_type_domain type
                     ON type.id = account.type_id
               JOIN normal_account_balance_type_domain balance_type
                     ON balance_type.id = account.normal_account_balance_type_id
               JOIN status_type_domain status
                     ON status.id = account.status_type_id
      """
   }

   fun findOne(id: Long, company: Company): AccountEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE account.id = :id AND comp.id = :comp_id"
      val found = jdbc.findFirstOrNull(query, params, RowMapper { rs, _ ->
         mapRow(rs, company, "account_")
         }
      )

      logger.trace("Searching for Account id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<AccountEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.myId())
      val query = """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by account_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT ${page.size()} OFFSET ${page.offset()}
      """
      var totalElements: Long? = null
      val resultList: MutableList<AccountEntity> = mutableListOf()

      jdbc.query(query, params) { rs ->
         resultList.add(mapRow(rs, company, "account_"))
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
   fun insert(account: AccountEntity): AccountEntity {
      logger.debug("Inserting bank {}", account)

      return jdbc.insertReturning("""
         INSERT INTO account(company_id, description, type_id, normal_account_balance_type_id, status_type_id, form_1099_field, corporate_account_indicator)
	      VALUES (:company_id, :description, :type_id, :normal_account_balance_type_id, :status_type_id, :form_1099_field, :corporate_account_indicator)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to account.company.myId(),
            "description" to account.description,
            "type_id" to account.type.id,
            "normal_account_balance_type_id" to account.normalAccountBalance.id,
            "status_type_id" to account.status.id,
            "form_1099_field" to account.form1099Field,
            "corporate_account_indicator" to account.corporateAccountIndicator
         ),
         RowMapper { rs, _ ->
            mapRow(rs, account)
         }
      )
   }

   @Transactional
   fun update(account: AccountEntity): AccountEntity {
      logger.debug("Updating account {}", account)

      return jdbc.updateReturning("""
         UPDATE account
         SET
            company_id = :company_id,
            description = :description,
            type_id = :type_id,
            normal_account_balance_type_id = :normal_account_balance_type_id,
            status_type_id = :status_type_id,
            form_1099_field = :form_1099_field,
            corporate_account_indicator = :corporate_account_indicator
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to account.id,
            "company_id" to account.company.myId(),
            "description" to account.description,
            "type_id" to account.type.id,
            "normal_account_balance_type_id" to account.normalAccountBalance.id,
            "status_type_id" to account.status.id,
            "form_1099_field" to account.form1099Field,
            "corporate_account_indicator" to account.corporateAccountIndicator
         ),
         RowMapper { rs, _ ->
            mapRow(rs, account)
         }
      )
   }

   fun mapRow(rs: ResultSet, company: Company, columnPrefix: String = EMPTY): AccountEntity {
      return AccountEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         company = company,
         number = rs.getInt("${columnPrefix}number"),
         description = rs.getString("${columnPrefix}description"),
         type = mapAccountType(rs, "type_"),
         normalAccountBalance = mapNormalAccountBalanceType(rs, "balance_type_"),
         status = mapAccountStatusType(rs, "status_"),
         form1099Field = rs.getInt("${columnPrefix}form_1099_field"),
         corporateAccountIndicator = rs.getBoolean("${columnPrefix}corporate_account_indicator")
      )
   }

   private fun mapRow(rs: ResultSet, account: AccountEntity, columnPrefix: String = EMPTY): AccountEntity {
      return AccountEntity(
         id = rs.getLong("${columnPrefix}id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         company = account.company,
         number = rs.getInt("${columnPrefix}number"),
         description = rs.getString("${columnPrefix}description"),
         type = account.type,
         normalAccountBalance = account.normalAccountBalance,
         status = account.status,
         form1099Field = rs.getInt("${columnPrefix}form_1099_field"),
         corporateAccountIndicator = rs.getBoolean("${columnPrefix}corporate_account_indicator")
      )
   }

   private fun mapAccountType(rs: ResultSet, columnPrefix: String): AccountType =
      AccountType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapNormalAccountBalanceType(rs: ResultSet, columnPrefix: String): NormalAccountBalanceType =
      NormalAccountBalanceType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapAccountStatusType(rs: ResultSet, columnPrefix: String): AccountStatusType =
      AccountStatusType(
         id = rs.getLong("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapCompany(rs: ResultSet, columnPrefix: String): CompanyEntity =
      CompanyEntity(
         id = rs.getLong("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
         doingBusinessAs = rs.getString("${columnPrefix}doing_business_as"),
         clientCode = rs.getString("${columnPrefix}client_code"),
         clientId = rs.getInt("${columnPrefix}client_id"),
         datasetCode = rs.getString("${columnPrefix}dataset_code"),
         federalIdNumber = rs.getString("${columnPrefix}federal_id_number")
      )

}
