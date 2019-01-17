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
         v.id AS v_id,
         v.uu_row_id AS v_uu_row_id,
         v.time_created AS v_time_created,
         v.time_updated AS v_time_updated,
         v.customer_account AS v_customer_account,
         v.customer_comments AS v_customer_comments,
         v.verified_by AS v_verified_by,
         v.verified_time AS v_verified_time,
         v.company AS v_company,
         va.id AS va_id,
         va.uu_row_id AS va_uu_row_id,
         va.time_created AS va_time_created,
         va.time_updated AS va_time_updated,
         va.address AS va_address,
         va.comment AS va_comment,
         va.dealer_phone AS va_dealer_phone,
         va.diff_address AS va_diff_address,
         va.diff_employee AS va_diff_employee,
         va.diff_phone AS va_diff_phone,
         va.dmv_verify AS va_dmv_verify,
         va.employer AS va_employer,
         va.last_payment AS va_last_payment,
         va.name AS va_name,
         va.next_payment AS va_next_payment,
         va.note AS va_note,
         va.payment_frequency AS va_payment_frequency,
         va.payment AS va_payment,
         va.pending_action AS va_pending_action,
         va.phone AS va_phone,
         va.previous_loan AS va_previous_loan,
         va.purchase_date AS va_purchase_date,
         va.related AS va_related,
         ve.id AS ve_id,
         ve.uu_row_id AS ve_uu_row_id,
         ve.time_created AS ve_time_created,
         ve.time_updated AS ve_time_updated,
         ve.department AS ve_department,
         ve.hire_date AS ve_hire_date,
         ve.leave_message AS ve_leave_message,
         ve.name AS ve_name,
         ve.reliable AS ve_reliable,
         ve.title AS ve_title,
         vl.id AS vl_id,
         vl.uu_row_id AS vl_uu_row_id,
         vl.time_created AS vl_time_created,
         vl.time_updated AS vl_time_updated,
         vl.address AS vl_address,
         vl.alt_phone AS vl_alt_phone,
         vl.lease_type AS vl_lease_type,
         vl.leave_message AS vl_leave_message,
         vl.length AS vl_length,
         vl.name AS vl_name,
         vl.paid_rent AS vl_paid_rent,
         vl.phone AS vl_phone,
         vl.reliable AS vl_reliable,
         vl.rent AS vl_rent
      FROM verification v
         LEFT OUTER JOIN verification_auto va
           ON v.auto_id = va.id
         LEFT OUTER JOIN verification_employment ve
           ON v.employment_id = ve.id
         LEFT OUTER JOIN verification_landlord vl
           ON v.landlord_id = vl.id
      """.trimIndent()

   init {
       selectAllRowMapper = VerificationRowMapper(
          rowPrefix = "v_",
          verificationAutoRepository = verificationAutoRepository,
          verificationEmploymentRepository = verificationEmploymentRepository,
          verificationLandlordRepository = verificationLandlordRepository
       )
   }

   override fun findOne(id: Long): Verification? {
      val found: Verification? = jdbc.findFirstOrNull(
         "$selectAllBase \nWHERE v.id = :id", Maps.mutable.ofPairs("id" to id),
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
         "$selectAllBase \nWHERE v.customer_account = :customerAccount".trimIndent(),
         Maps.mutable.ofPairs("customerAccount" to customerAccount),
         selectAllRowMapper
      )

      logger.debug("search for verification through Customer Account: {} resulted in {}", customerAccount, found)

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
