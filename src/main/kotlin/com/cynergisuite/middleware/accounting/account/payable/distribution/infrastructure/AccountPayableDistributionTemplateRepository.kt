package com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.softDelete
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.account.payable.distribution.AccountPayableDistributionTemplateEntity
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class AccountPayableDistributionTemplateRepository @Inject constructor(
   private val jdbc: Jdbi,
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionTemplateRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            apDist.id                                                            AS apDist_id,
            apDist.name                                                          AS apDist_name,
            apDist.company_id                                                    AS apDist_company_id
         FROM account_payable_distribution_template apDist
            JOIN company comp ON apDist.company_id = comp.id AND comp.deleted = FALSE
            JOIN account_payable_distribution_template_detail apDistDetail ON apDist.id = apDistDetail.template_id
      """
   }

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): AccountPayableDistributionTemplateEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE apDist.id = :id AND apDist.deleted = FALSE AND apDist.company_id = :comp_id AND comp.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "apDist_")
      }

      logger.trace("Searching for AccountPayableDistributionTemplate id {}: \nQuery {} \nResulted in {}", id, query, found)

      return found
   }

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      page: PageRequest
   ): RepositoryPage<AccountPayableDistributionTemplateEntity, PageRequest> {
      var totalElements: Long? = null
      val resultList: MutableList<AccountPayableDistributionTemplateEntity> = mutableListOf()

      jdbc.query(
         """
         WITH paged AS (
            ${selectBaseQuery()}
            WHERE apDist.company_id = :comp_id AND apDist.deleted = FALSE AND comp.deleted = FALSE
         )
         SELECT
            p.*,
            count(*) OVER() as total_elements
         FROM paged AS p
         ORDER by apDist_name ${page.sortDirection()}
         LIMIT :limit OFFSET :offset
         """,
         mapOf(
            "comp_id" to company.id,
            "limit" to page.size(),
            "offset" to page.offset()
         )
      ) { rs, _ ->
         resultList.add(mapRow(rs, company, "apDist_"))
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
   fun findByName( name: String, company: CompanyEntity ) : AccountPayableDistributionTemplateEntity? {
      val params = mutableMapOf<String, Any?>("name" to name.uppercase(), "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE UPPER(apDist.name) = :name AND apDist.deleted = FALSE AND apDist.company_id = :comp_id AND comp.deleted = FALSE"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(rs, company, "apDist_")
      }

      logger.trace("Searching for AccountPayableDistributionTemplate name {}: \nQuery {} \nResulted in {}", name, query, found)

      return found
   }

   @Transactional
   fun insert(entity: AccountPayableDistributionTemplateEntity, company: CompanyEntity): AccountPayableDistributionTemplateEntity {
      logger.debug("Inserting AccountPayableDistributionTemplate {}", entity)

      return jdbc.insertReturning(
         """
         INSERT INTO account_payable_distribution_template(name, company_id)
	      VALUES (:name, :company_id)
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "name" to entity.name,
            "company_id" to company.id,
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun update(entity: AccountPayableDistributionTemplateEntity, company: CompanyEntity): AccountPayableDistributionTemplateEntity {
      logger.debug("Updating AccountPayableDistributionTemplate {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE account_payable_distribution_template
         SET
            company_id = :company_id,
            name = :name
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.id,
            "name" to entity.name,
         )
      ) { rs, _ ->
         mapRow(rs, entity)
      }
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      logger.debug("Deleting AccountPayableDistributionTemplate with id={}", id)

      val rowsAffected = jdbc.softDelete(
         """
         UPDATE account_payable_distribution_template
         SET deleted = TRUE
         WHERE id = :id AND company_id = :company_id AND deleted = FALSE
         """,
         mapOf("id" to id, "company_id" to company.id),
         "account_payable_distribution_template"
      )
      logger.info("Row affected {}", rowsAffected)

      if (rowsAffected == 0) throw NotFoundException(id)
   }

   fun mapRow(rs: ResultSet, company: CompanyEntity, columnPrefix: String = EMPTY): AccountPayableDistributionTemplateEntity {
      val templateId = rs.getUuid("${columnPrefix}id")
      return AccountPayableDistributionTemplateEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name"),
      )
   }

   private fun mapRow(
      rs: ResultSet,
      entity: AccountPayableDistributionTemplateEntity,
      columnPrefix: String = EMPTY
   ): AccountPayableDistributionTemplateEntity {
      return AccountPayableDistributionTemplateEntity(
         id = rs.getUuid("${columnPrefix}id"),
         name = rs.getString("${columnPrefix}name")
      )
   }
}
