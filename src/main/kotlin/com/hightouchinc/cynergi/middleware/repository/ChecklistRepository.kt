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
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Singleton

@Singleton
class ChecklistRepository(
   jdbc: NamedParameterJdbcTemplate
): RepositoryBase<Checklist>(
   tableName = "checklist",
   jdbc = jdbc,
   entityRowMapper = CHECKLIST_ROW_MAPPER,
   selectOneQuery = SELECT_CHECKLIST_BY_ID
) {
   private companion object {
      val CHECKLIST_ROW_MAPPER: RowMapper<Checklist> = RowMapper { rs: ResultSet, _: Int ->
         Checklist(
            id = rs.getLong("id"),
            uuid = rs.getObject("uuid", UUID::class.java),
            timeCreated = rs.getObject("timeCreated", LocalDateTime::class.java),
            timeUpdated = rs.getObject("timeUpdated", LocalDateTime::class.java),
            customerAccount = rs.getString("customerAccount"),
            customerComments = rs.getString("customerComments"),
            verifiedBy = rs.getString("verifiedBy"),
            verifiedTime = rs.getObject("verifiedTime", LocalDateTime::class.java),
            company = rs.getString("company")
         )
      }

      @Language("PostgreSQL")
      val SELECT_CHECKLIST_BY_ID = """
         SELECT
            c.id AS id,
            c.uuid AS uuid,
            c.time_created AS timeCreated,
            c.time_updated AS timeUpdated,
            c.customer_account AS customerAccount,
            c.customer_comments AS customerComments,
            c.verified_by AS verifiedBy,
            c.verified_time AS verifiedTime,
            c.company AS company
         FROM Checklist c
         WHERE c.id = :id
      """.trimIndent()

      @Language("PostgreSQL")
      val SELECT_CHECKLIST_BY_CUSTOMER_ACCOUNT = """
         SELECT
            c.id AS id,
            c.uuid AS uuid,
            c.time_created AS timeCreated,
            c.time_updated AS timeUpdated,
            c.customer_account AS customerAccount,
            c.customer_comments AS customerComments,
            c.verified_by AS verifiedBy,
            c.verified_time AS verifiedTime,
            c.company AS company
         FROM Checklist c
         WHERE c.customer_account = :customerAccount
      """.trimIndent()

      @Language("PostgreSQL")
      val INSERT_CHECKLIST = """
         INSERT INTO Checklist (customer_account, customer_comments, verified_by, company)
         VALUES(:customerAccount, :customerComments, :verifiedBy, :company)
         RETURNING
            id AS id,
            uuid AS uuid,
            time_created AS timeCreated,
            time_updated AS timeUpdated,
            customer_account AS customerAccount,
            customer_comments AS customerComments,
            verified_by AS verifiedBy,
            verified_time AS verifiedTime,
            company AS company
      """.trimIndent()

      @Language("PostgreSQL")
      val UPDATE_CHECKLIST = """
         UPDATE Checklist
         SET customer_account = :customerAccount,
             customer_comments = :customerComments,
             verified_by = :verifiedBy,
             verified_time = :verifiedTime
         WHERE id = :id
         RETURNING
            id AS id,
            uuid AS uuid,
            time_created AS timeCreated,
            time_updated AS timeUpdated,
            customer_account AS customerAccount,
            customer_comments AS customerComments,
            verified_by AS verifiedBy,
            verified_time AS verifiedTime,
            company AS company
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
      val verifiedTime: LocalDateTime? = if (existing.verifiedBy != entity.verifiedBy) {
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
