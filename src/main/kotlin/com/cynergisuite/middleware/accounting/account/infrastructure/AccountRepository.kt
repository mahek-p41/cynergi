package com.cynergisuite.middleware.accounting.account.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getIntOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.isNumber
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountStatusType
import com.cynergisuite.middleware.accounting.account.AccountType
import com.cynergisuite.middleware.accounting.account.NormalAccountBalanceType
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.transaction.annotation.ReadOnly
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.transaction.Transactional

@Singleton
class AccountRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            account.id                                   AS account_id,
            account.number                               AS account_number,
            account.name                                 AS account_name,
            account.form_1099_field                      AS account_form_1099_field,
            account.corporate_account_indicator          AS account_corporate_account_indicator,
            account.company_id                           AS account_comp_id,
            account.deleted                              AS account_deleted,
            type.id                                      AS account_type_id,
            type.value                                   AS account_type_value,
            type.description                             AS account_type_description,
            type.localization_code                       AS account_type_localization_code,
            balance_type.id                              AS account_balance_type_id,
            balance_type.value                           AS account_balance_type_value,
            balance_type.description                     AS account_balance_type_description,
            balance_type.localization_code               AS account_balance_type_localization_code,
            status.id                                    AS account_status_id,
            status.value                                 AS account_status_value,
            status.description                           AS account_status_description,
            status.localization_code                     AS account_status_localization_code
         FROM account
               JOIN company comp
                     ON comp.id = account.company_id AND comp.deleted = FALSE
               JOIN account_type_domain type
                     ON type.id = account.type_id
               JOIN normal_account_balance_type_domain balance_type
                     ON balance_type.id = account.normal_account_balance_type_id
               JOIN account_status_type_domain status
                     ON status.id = account.status_type_id
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AccountEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE account.id = :id AND comp.id = :comp_id AND account.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "account_")
      }

      logger.trace("Searching for Account id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   @ReadOnly
   fun findByNumber(number: Long, company: CompanyEntity): AccountEntity? {
      val params = mutableMapOf<String, Any?>("number" to number, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE account.number = :number AND comp.id = :comp_id and account.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "account_")
      }

      logger.trace("Searching for Account number {}: \nQuery {} \nResulted in {}", number, query, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<AccountEntity, PageRequest> {
      var totalElements: Long? = null
      val resultList: MutableList<AccountEntity> = mutableListOf()

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE comp.id = :comp_id AND account.deleted = FALSE
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by account_${page.snakeSortBy()} ${page.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset()
         )
      ) { rs, _ ->
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

   @ReadOnly
   fun search(company: CompanyEntity, page: SearchPageRequest): RepositoryPage<AccountEntity, PageRequest> {
      var searchQuery = page.query
      val where = StringBuilder(" WHERE comp.id = :comp_id AND account.deleted = FALSE")
      val sortBy = if (!searchQuery.isNullOrEmpty()) {
         if (page.fuzzy == false) {
            where.append(" AND (search_vector @@ to_tsquery(:search_query)) ")
            searchQuery = searchQuery.replace("\\s+".toRegex(), " & ")
            EMPTY
         } else {
            val splitedWords = searchQuery.split(" ")
            val fieldToSearch = if (splitedWords.first().isNumber() && splitedWords.size == 1) {
               " account.number::text "
            } else {
               "COALESCE(account.number::text, '') || ' ' || COALESCE(account.name, '')"
            }
            where.append(" AND $fieldToSearch <-> :search_query < 0.865 ")
            " ORDER BY $fieldToSearch <-> :search_query "
         }
      } else {
         EMPTY
      }

      return jdbc.queryPaged(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            $where
            $sortBy
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
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
            elements.add(mapRow(rs, company, "account_"))
         } while (rs.next())
      }
   }

   @Transactional
   fun insert(account: AccountEntity, company: CompanyEntity): AccountEntity {
      logger.debug("Inserting bank {}", account)

      return jdbc.insertReturning(
         """
         INSERT INTO account(number, company_id, name, type_id, normal_account_balance_type_id, status_type_id, form_1099_field, corporate_account_indicator)
	      VALUES (:number, :company_id, :name, :type_id, :normal_account_balance_type_id, :status_type_id, :form_1099_field, :corporate_account_indicator)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "number" to account.number,
            "company_id" to company.id,
            "name" to account.name,
            "type_id" to account.type.id,
            "normal_account_balance_type_id" to account.normalAccountBalance.id,
            "status_type_id" to account.status.id,
            "form_1099_field" to account.form1099Field,
            "corporate_account_indicator" to account.corporateAccountIndicator
         )
      ) { rs, _ ->
         mapRow(rs, account)
      }
   }

   @Transactional
   fun update(account: AccountEntity, company: CompanyEntity): AccountEntity {
      logger.debug("Updating account {}", account)

      return jdbc.updateReturning(
         """
         UPDATE account
         SET
            number = :number,
            company_id = :company_id,
            name = :name,
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
            "number" to account.number,
            "company_id" to company.id,
            "name" to account.name,
            "type_id" to account.type.id,
            "normal_account_balance_type_id" to account.normalAccountBalance.id,
            "status_type_id" to account.status.id,
            "form_1099_field" to account.form1099Field,
            "corporate_account_indicator" to account.corporateAccountIndicator
         )
      ) { rs, _ ->
         mapRow(rs, account)
      }
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting account with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE account
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "account"
      )

      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): AccountEntity {
      return AccountEntity(
         id = rs.getUuid("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         type = mapAccountType(rs, "${columnPrefix}type_"),
         normalAccountBalance = mapNormalAccountBalanceType(rs, "${columnPrefix}balance_type_"),
         status = mapAccountStatusType(rs, "${columnPrefix}status_"),
         form1099Field = rs.getIntOrNull("${columnPrefix}form_1099_field"),
         corporateAccountIndicator = rs.getBoolean("${columnPrefix}corporate_account_indicator")
      )
   }

   fun mapRowOrNull(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): AccountEntity? =
      if (rs.getString("${columnPrefix}id") != null) {
         mapRow(rs, company, columnPrefix)
      } else {
         null
      }

   private fun mapRow(rs: ResultSet, account: AccountEntity, columnPrefix: String = EMPTY): AccountEntity {
      return AccountEntity(
         id = rs.getUuid("${columnPrefix}id"),
         number = rs.getLong("${columnPrefix}number"),
         name = rs.getString("${columnPrefix}name"),
         type = account.type,
         normalAccountBalance = account.normalAccountBalance,
         status = account.status,
         form1099Field = rs.getIntOrNull("${columnPrefix}form_1099_field"),
         corporateAccountIndicator = rs.getBoolean("${columnPrefix}corporate_account_indicator")
      )
   }

   private fun mapAccountType(rs: ResultSet, columnPrefix: String): AccountType =
      AccountType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapNormalAccountBalanceType(rs: ResultSet, columnPrefix: String): NormalAccountBalanceType =
      NormalAccountBalanceType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )

   private fun mapAccountStatusType(rs: ResultSet, columnPrefix: String): AccountStatusType =
      AccountStatusType(
         id = rs.getInt("${columnPrefix}id"),
         value = rs.getString("${columnPrefix}value"),
         description = rs.getString("${columnPrefix}description"),
         localizationCode = rs.getString("${columnPrefix}localization_code")
      )
}
