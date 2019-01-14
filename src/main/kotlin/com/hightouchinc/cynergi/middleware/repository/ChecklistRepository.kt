package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Checklist
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.repository.spi.RepositoryBase
import io.micronaut.spring.tx.annotation.Transactional
import org.eclipse.collections.impl.factory.Maps
import org.intellij.lang.annotations.Language
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Singleton

@Singleton
class ChecklistRepository(
   jdbc: NamedParameterJdbcTemplate,
   private val checklistAutoRepository: ChecklistAutoRepository
): RepositoryBase<Checklist>(
   tableName = "checklist",
   jdbc = jdbc,
   entityRowMapper = CHECKLIST_ROW_MAPPER
) {
   private companion object {
      val CHECKLIST_ROW_MAPPER: RowMapper<Checklist> = RowMapper { rs: ResultSet, _: Int ->
         Checklist(
            id = rs.getLong("id"),
            uuRowId = rs.getObject("uu_row_id", UUID::class.java),
            timeCreated = rs.getObject("time_created", OffsetDateTime::class.java),
            timeUpdated = rs.getObject("time_updated", OffsetDateTime::class.java),
            customerAccount = rs.getString("customer_account"),
            customerComments = rs.getString("customer_comments"),
            verifiedBy = rs.getString("verified_by"),
            verifiedTime = rs.getObject("verified_time", OffsetDateTime::class.java),
            company = rs.getString("company"),
            auto = null // TODO add join to table and map columns using mapping from checklistAutoRepository
         )
      }

      @Language("PostgreSQL")
      val SELECT_CHECKLIST_BY_CUSTOMER_ACCOUNT = """
         SELECT
            *
         FROM Checklist c
         WHERE c.customer_account = :customerAccount
      """.trimIndent()

      @Language("PostgreSQL")
      val INSERT_CHECKLIST = """
         INSERT INTO Checklist (customer_account, customer_comments, verified_by, company)
         VALUES(:customerAccount, :customerComments, :verifiedBy, :company)
         RETURNING
            *
      """.trimIndent()

      @Language("PostgreSQL")
      val UPDATE_CHECKLIST = """
         UPDATE Checklist
         SET
            customer_account = :customerAccount,
            customer_comments = :customerComments,
            verified_by = :verifiedBy,
            verified_time = :verifiedTime
         WHERE id = :id
         RETURNING
            *
      """.trimIndent()
   }

   fun exists(customerAccount: String): Boolean =
      jdbc.queryForObject("SELECT EXISTS(SELECT customer_account FROM $tableName WHERE customer_account = :customerAccount)", mapOf("customerAccount" to customerAccount), Boolean::class.java)!!

   fun findByCustomerAccount(customerAccount: String): Checklist? =
      jdbc.findFirstOrNull(
         SELECT_CHECKLIST_BY_CUSTOMER_ACCOUNT,
         Maps.mutable.ofPairs("customerAccount" to customerAccount),
         entityRowMapper
      )

   @Transactional
   override fun insert(entity: Checklist): Checklist =
      jdbc.queryForObject(
         INSERT_CHECKLIST,
         Maps.mutable.ofPairs(
            "customerAccount" to entity.customerAccount,
            "customerComments" to entity.customerComments,
            "verifiedBy" to entity.verifiedBy,
            "company" to entity.company
         ),
         entityRowMapper
      )!!

   @Transactional
   override fun update(entity: Checklist): Checklist {
      val existing = findOne(id = entity.id!!)!!
      val verifiedTime: OffsetDateTime? = if (existing.verifiedBy != entity.verifiedBy) {
         null
      } else {
         existing.verifiedTime
      }

      return jdbc.queryForObject(
         UPDATE_CHECKLIST,
         Maps.mutable.ofPairs(
            "id" to entity.id,
            "customerAccount" to entity.customerAccount,
            "customerComments" to entity.customerComments,
            "verifiedBy" to entity.verifiedBy,
            "verifiedTime" to verifiedTime,
            "company" to entity.company
         ),
         entityRowMapper
      )!!
   }
}
