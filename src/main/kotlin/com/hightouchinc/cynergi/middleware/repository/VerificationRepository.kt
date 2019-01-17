package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Verification
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNull
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.ofPairs
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils
import org.eclipse.collections.impl.factory.Maps
import org.intellij.lang.annotations.Language
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
class VerificationRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val verificationAutoRepository: VerificationAutoRepository,
   private val verificationEmploymentRepository: VerificationEmploymentRepository,
   private val verificationLandlordRepository: VerificationLandlordRepository
) : Repository<Verification> {

   private val selectAllRowMapper: RowMapper<Verification>
   private val logger: Logger = LoggerFactory.getLogger(VerificationRepository::class.java)
   private val simpleVerificationRowMapper: RowMapper<Verification> = VerificationRowMapper()

   @Language("PostgreSQL")
   private val selectAllBase = """
          SELECT
            c.id AS c_id,
            c.uu_row_id AS c_uu_row_id,
            c.time_created AS c_time_created,
            c.time_updated AS c_time_updated,
            c.customer_account AS c_customer_account,
            c.customer_comments AS c_customer_comments,
            c.verified_by AS c_verified_by,
            c.verified_time AS c_verified_time,
            c.company AS c_company,
            ca.id AS ca_id,
            ca.uu_row_id AS ca_uu_row_id,
            ca.time_created AS ca_time_created,
            ca.time_updated AS ca_time_updated,
            ca.address AS ca_address,
            ca.comment AS ca_comment,
            ca.dealer_phone AS ca_dealer_phone,
            ca.diff_address AS ca_diff_address,
            ca.diff_employee AS ca_diff_employee,
            ca.diff_phone AS ca_diff_phone,
            ca.dmv_verify AS ca_dmv_verify,
            ca.employer AS ca_employer,
            ca.last_payment AS ca_last_payment,
            ca.name AS ca_name,
            ca.next_payment AS ca_next_payment,
            ca.note AS ca_note,
            ca.payment_frequency AS ca_payment_frequency,
            ca.payment AS ca_payment,
            ca.pending_action AS ca_pending_action,
            ca.phone AS ca_phone,
            ca.previous_loan AS ca_previous_loan,
            ca.purchase_date AS ca_purchase_date,
            ca.related AS ca_related,
            ce.id AS ce_id,
            ce.uu_row_id AS ce_uu_row_id,
            ce.time_created AS ce_time_created,
            ce.time_updated AS ce_time_updated,
            ce.department AS ce_department,
            ce.hire_date AS ce_hire_date,
            ce.leave_message AS ce_leave_message,
            ce.name AS ce_name,
            ce.reliable AS ce_reliable,
            ce.title AS ce_title,
            cl.id AS cl_id,
            cl.uu_row_id AS cl_uu_row_id,
            cl.time_created AS cl_time_created,
            cl.time_updated AS cl_time_updated,
            cl.address AS cl_address,
            cl.alt_phone AS cl_alt_phone,
            cl.lease_type AS cl_lease_type,
            cl.leave_message AS cl_leave_message,
            cl.length AS cl_length,
            cl.name AS cl_name,
            cl.paid_rent AS cl_paid_rent,
            cl.phone AS cl_phone,
            cl.reliable AS cl_reliable,
            cl.rent AS cl_rent
         FROM verification c
            LEFT OUTER JOIN verification_auto ca
              ON c.auto_id = ca.id
            LEFT OUTER JOIN verification_employment ce
              ON c.employment_id = ce.id
            LEFT OUTER JOIN verification_landlord cl
              ON c.landlord_id = cl.id
         """.trimIndent()

   init {
       selectAllRowMapper = VerificationRowMapper(
          rowPrefix = "c_",
          verificationAutoRepository = verificationAutoRepository,
          verificationEmploymentRepository = verificationEmploymentRepository,
          verificationLandlordRepository = verificationLandlordRepository
       )
   }

   override fun findOne(id: Long): Verification? {
      val found: Verification? = jdbc.findFirstOrNull(
         "$selectAllBase \nWHERE c.id = :id", Maps.mutable.ofPairs("id" to id),
         selectAllRowMapper
      )

      logger.trace("Searched for {} found {}", id, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("searching for existence through ID: {} resulted in {}", id, exists)

      return exists
   }

   fun exists(customerAccount: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT customer_account FROM verification WHERE customer_account = :customerAccount)", mapOf("customerAccount" to customerAccount), Boolean::class.java)!!

      logger.debug("searching for existence through Customer Account: {} resulted in {}", customerAccount, exists)

      return exists
   }

   fun findByCustomerAccount(customerAccount: String): Verification? {
      val found = jdbc.findFirstOrNull(
         "$selectAllBase \nWHERE c.customer_account = :customerAccount".trimIndent(),
         Maps.mutable.ofPairs("customerAccount" to customerAccount),
         selectAllRowMapper
      )

      logger.debug("search for verification through Customer Aaccount: {} resulted in {}", customerAccount, found)

      return found
   }

   @Transactional
   override fun insert(entity: Verification): Verification {
      val auto = entity.auto?.let { verificationAutoRepository.insert(entity = it) }
      val employment = entity.employment?.let { verificationEmploymentRepository.insert(entity = it) }
      val landlord = entity.landlord?.let { verificationLandlordRepository.insert(entity = it) }
      val paramMap = Maps.mutable.ofPairs(
         "customer_account" to entity.customerAccount,
         "customer_comments" to entity.customerComments,
         "verified_by" to entity.verifiedBy,
         "company" to entity.company,
         "auto_id" to auto?.id,
         "employment_id" to employment?.id,
         "landlord_id" to landlord?.id
      )

      val inserted = jdbc.insertReturning("""
         INSERT INTO Verification (customer_account, customer_comments, verified_by, company, auto_id, employment_id, landlord_id)
         VALUES(:customer_account, :customer_comments, :verified_by, :company, :auto_id, :employment_id, :landlord_id)
         RETURNING
            *
         """.trimIndent(),
         paramMap,
         simpleVerificationRowMapper
      )

      return if (auto != null || employment != null || landlord != null) {
         inserted.copy(auto = auto, employment = employment, landlord = landlord)
      } else {
         inserted
      }
   }

   @Transactional
   override fun update(entity: Verification): Verification {
      val existing = findOne(id = entity.id!!)!!
      val auto = entity.auto?.let { verificationAutoRepository.upsert(existing = existing.auto, requestedChange = it) }
      val employment = entity.employment?.let { verificationEmploymentRepository.upsert(existing = existing.employment, requestedChange= it) }
      val landlord = entity.landlord?.let { verificationLandlordRepository.upsert(existing = existing.landlord, requestedChange = it) }
      val verifiedTime: OffsetDateTime? = if (existing.verifiedBy == entity.verifiedBy) {
         existing.verifiedTime
      } else {
         null
      }

      val updated = jdbc.updateReturning("""
         UPDATE Verification
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
         simpleVerificationRowMapper
      )

      return if (auto != null || employment != null || landlord != null) {
         updated.copy(auto = auto, employment = employment, landlord = landlord)
      } else {
         updated
      }
   }
}

private class VerificationRowMapper(
   private val rowPrefix: String = StringUtils.EMPTY,
   private val verificationAutoRepository: VerificationAutoRepository? = null,
   private val verificationEmploymentRepository: VerificationEmploymentRepository? = null,
   private val verificationLandlordRepository: VerificationLandlordRepository? = null
): RowMapper<Verification> {
   override fun mapRow(rs: ResultSet, row: Int): Verification? {
      val auto = verificationAutoRepository?.mapRowPrefixedRow(rs = rs, row = row)
      val employment = verificationEmploymentRepository?.mapRowPrefixedRow(rs = rs, row = row)
      val landlord = verificationLandlordRepository?.mapRowPrefixedRow(rs = rs, row = row)

      return Verification(
         id = rs.getLong("${rowPrefix}id"),
         uuRowId = rs.getObject("${rowPrefix}uu_row_id", UUID::class.java),
         timeCreated = rs.getObject("${rowPrefix}time_created", OffsetDateTime::class.java),
         timeUpdated = rs.getObject("${rowPrefix}time_updated", OffsetDateTime::class.java),
         customerAccount = rs.getString("${rowPrefix}customer_account"),
         customerComments = rs.getString("${rowPrefix}customer_comments"),
         verifiedBy = rs.getString("${rowPrefix}verified_by"),
         verifiedTime = rs.getObject("${rowPrefix}verified_time", OffsetDateTime::class.java),
         company = rs.getString("${rowPrefix}company"),
         auto = auto,
         employment = employment,
         landlord = landlord
      )
   }
}
