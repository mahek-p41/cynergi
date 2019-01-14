package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Checklist
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import io.micronaut.spring.tx.annotation.Transactional
import org.eclipse.collections.impl.factory.Maps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecklistRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val checklistAutoRepository: ChecklistAutoRepository
): Repository<Checklist> {

   private companion object {
      val logger: Logger = LoggerFactory.getLogger(ChecklistRepository::class.java)

      val DML_CHECKLIST_ROW_MAPPER: RowMapper<Checklist> = RowMapper { rs: ResultSet, _: Int ->
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
   }

   override fun findOne(id: Long): Checklist? {
      val found:Checklist? = jdbc.findFirstOrNull(
         """
         SELECT
            c.id AS checklistId,
            c.uu_row_id,
            c.time_created,
            c.time_updated,
            c.customer_account,
            c.customer_comments,
            c.verified_by,
            c.verified_time,
            c.company,
            c.auto_id,
            c.employment_id,
            c.landlord_id,
            ca.id,
            ca.uu_row_id,
            ca.time_created,
            ca.time_updated,
            ca.address,
            ca.comment,
            ca.dealer_phone,
            ca.diff_address,
            ca.diff_employee,
            ca.diff_phone,
            ca.dmv_verify,
            ca.employer,
            ca.last_payment,
            ca.name,
            ca.next_payment,
            ca.note,
            ca.payment_frequency,
            ca.payment,
            ca.pending_action,
            ca.phone,
            ca.previous_loan,
            ca.purchase_date,
            ca.related
         FROM checklist c
            LEFT OUTER JOIN checklist_auto ca
              ON c.auto_id = ca.id
         WHERE c.id = :id
         """.trimIndent(), Maps.mutable.ofPairs("id" to id),
         RowMapper { rs: ResultSet, row: Int ->
            val auto = if (rs.getString("ca.id") != null) {
               checklistAutoRepository.joinRowMapperWithCATableName().mapRow(rs, row)
            } else{
               null
            }

            Checklist(
               id = rs.getLong("c.id"),
               uuRowId = rs.getObject("c.uu_row_id", UUID::class.java),
               timeCreated = rs.getObject("c.time_created", OffsetDateTime::class.java),
               timeUpdated = rs.getObject("c.time_updated", OffsetDateTime::class.java),
               customerAccount = rs.getString("c.customer_account"),
               customerComments = rs.getString("c.customer_comments"),
               verifiedBy = rs.getString("c.verified_by"),
               verifiedTime = rs.getObject("c.verified_time", OffsetDateTime::class.java),
               company = rs.getString("c.company"),
               auto = auto
            )
         }
      )

      logger.trace("Searched for {} found {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      return jdbc.queryForObject("SELECT EXISTS(SELECT id FROM checklist WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!
   }

   fun exists(customerAccount: String): Boolean =
      jdbc.queryForObject("SELECT EXISTS(SELECT customer_account FROM checlist WHERE customer_account = :customerAccount)", mapOf("customerAccount" to customerAccount), Boolean::class.java)!!

   fun findByCustomerAccount(customerAccount: String): Checklist? =
      jdbc.findFirstOrNull(
         """
         SELECT
            *
         FROM Checklist c
         WHERE c.customer_account = :customerAccount
         """.trimIndent(),
         Maps.mutable.ofPairs("customerAccount" to customerAccount),
         DML_CHECKLIST_ROW_MAPPER
      )

   @Transactional
   override fun insert(entity: Checklist): Checklist {
      val checklistAuto = entity.auto?.let { checklistAutoRepository.insert(entity = it) }
      val paramMap = Maps.mutable.ofPairs(
         "customerAccount" to entity.customerAccount,
         "customerComments" to entity.customerComments,
         "verifiedBy" to entity.verifiedBy,
         "company" to entity.company,
         "checklist_auto_id" to checklistAuto?.id
      )

      val inserted = jdbc.queryForObject(
         """
         INSERT INTO Checklist (customer_account, customer_comments, verified_by, company)
         VALUES(:customerAccount, :customerComments, :verifiedBy, :company)
         RETURNING
            *
         """.trimIndent(),
         paramMap,
         DML_CHECKLIST_ROW_MAPPER
      )!!

      return if (checklistAuto != null) {
         inserted.copy(auto = checklistAuto)
      } else {
         inserted
      }
   }

   @Transactional
   override fun update(entity: Checklist): Checklist {
      val existing = findOne(id = entity.id!!)!!
      val verifiedTime: OffsetDateTime? = if (existing.verifiedBy != entity.verifiedBy) {
         null
      } else {
         existing.verifiedTime
      }

      return jdbc.queryForObject(
         """
         UPDATE Checklist
         SET
            customer_account = :customerAccount,
            customer_comments = :customerComments,
            verified_by = :verifiedBy,
            verified_time = :verifiedTime
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         Maps.mutable.ofPairs(
            "id" to entity.id,
            "customerAccount" to entity.customerAccount,
            "customerComments" to entity.customerComments,
            "verifiedBy" to entity.verifiedBy,
            "verifiedTime" to verifiedTime,
            "company" to entity.company
         ),
         DML_CHECKLIST_ROW_MAPPER
      )!!
   }
}
