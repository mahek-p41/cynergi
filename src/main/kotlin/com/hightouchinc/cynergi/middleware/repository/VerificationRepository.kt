package com.hightouchinc.cynergi.middleware.repository

import com.hightouchinc.cynergi.middleware.entity.Verification
import com.hightouchinc.cynergi.middleware.entity.VerificationReference
import com.hightouchinc.cynergi.middleware.extensions.findFirstOrNullWithCrossJoin
import com.hightouchinc.cynergi.middleware.extensions.getOffsetDateTime
import com.hightouchinc.cynergi.middleware.extensions.getUuid
import com.hightouchinc.cynergi.middleware.extensions.insertReturning
import com.hightouchinc.cynergi.middleware.extensions.updateReturning
import io.micronaut.spring.tx.annotation.Transactional
import org.apache.commons.lang3.StringUtils
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val verificationAutoRepository: VerificationAutoRepository,
   private val verificationEmploymentRepository: VerificationEmploymentRepository,
   private val verificationLandlordRepository: VerificationLandlordRepository,
   private val verificationReferenceRepository: VerificationReferenceRepository
) : Repository<Verification> {
   private val logger: Logger = LoggerFactory.getLogger(VerificationRepository::class.java)
   private val simpleVerificationRowMapper: VerificationRowMapper = VerificationRowMapper()
   private val selectAllRowMapper: VerificationRowMapper = VerificationRowMapper(
      columnPrefix = "v_",
      verificationAutoRepository = verificationAutoRepository,
      verificationEmploymentRepository = verificationEmploymentRepository,
      verificationLandlordRepository = verificationLandlordRepository
   )

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
         va.verification_id AS va_verification_id,
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
         ve.verification_id AS ve_verification_id,
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
         vl.rent AS vl_rent,
         vl.verification_id AS vl_verification_id,
         vr.id AS vr_id,
         vr.uu_row_id AS vr_uu_row_id,
         vr.time_created AS vr_time_created,
         vr.time_updated AS vr_time_updated,
         vr.address AS vr_address,
         vr.has_home_phone AS vr_has_home_phone,
         vr.known AS vr_known,
         vr.leave_message AS vr_leave_message,
         vr.rating AS vr_rating,
         vr.relationship AS vr_relationship,
         vr.reliable AS vr_reliable,
         vr.time_frame AS vr_time_frame,
         vr.verify_phone AS vr_verify_phone,
         vr.verification_id AS vr_verification_id
      FROM verification v
         LEFT OUTER JOIN verification_auto va
           ON v.id = va.verification_id
         LEFT OUTER JOIN verification_employment ve
           ON v.id = ve.verification_id
         LEFT OUTER JOIN verification_landlord vl
           ON v.id = vl.verification_id
         JOIN verification_reference vr
           ON v.id = vr.verification_id
      """.trimIndent()

   override fun findOne(id: Long): Verification? {
      val found: Verification? = jdbc.findFirstOrNullWithCrossJoin("$selectAllBase \nWHERE v.id = :id", mapOf("id" to id), selectAllRowMapper) { verification, rs ->
         verificationReferenceRepository.mapRowPrefixedRow(rs)?.also { verification.references.add(it) }
      }

      logger.trace("Searched for {} found {}", id, found)

      return found
   }

   fun findByCustomerAccount(customerAccount: String): Verification? {
      val found: Verification? = jdbc.findFirstOrNullWithCrossJoin( "$selectAllBase \nWHERE v.customer_account = :customer_account", mapOf("customer_account" to customerAccount), selectAllRowMapper) { verification, rs ->
         verificationReferenceRepository.mapRowPrefixedRow(rs)?.also { verification.references.add(it) }
      }

      logger.debug("Search for Verification through Customer Account: {} resulted in {}", customerAccount, found)

      return found
   }

   override fun exists(id: Long): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT id FROM verification WHERE id = :id)", mapOf("id" to id), Boolean::class.java)!!

      logger.trace("Searching for existence of Verification through ID: {} resulted in {}", id, exists)

      return exists
   }

   fun exists(customerAccount: String): Boolean {
      val exists = jdbc.queryForObject("SELECT EXISTS(SELECT customer_account FROM verification WHERE customer_account = :customer_account)", mapOf("customer_account" to customerAccount), Boolean::class.java)!!

      logger.debug("Searching for existence of Verification through Customer Account: {} resulted in {}", customerAccount, exists)

      return exists
   }

   @Transactional
   override fun insert(entity: Verification): Verification {
      val paramMap = mapOf(
         "customer_account" to entity.customerAccount,
         "customer_comments" to entity.customerComments,
         "verified_by" to entity.verifiedBy,
         "company" to entity.company
      )

      logger.debug("Inserting Verification {}", paramMap)

      val inserted = jdbc.insertReturning("""
         INSERT INTO verification (customer_account, customer_comments, verified_by, company)
         VALUES(:customer_account, :customer_comments, :verified_by, :company)
         RETURNING
            *
         """.trimIndent(),
         paramMap,
         simpleVerificationRowMapper
      )

      val auto = entity.auto?.let { verificationAutoRepository.insert(entity = it.copy(verification = inserted)) }
      val employment = entity.employment?.let { verificationEmploymentRepository.insert(entity = it.copy(verification = inserted)) }
      val landlord = entity.landlord?.let { verificationLandlordRepository.insert(entity = it.copy(verification = inserted)) }
      val references = entity.references.asSequence().map { verificationReferenceRepository.insert(it.copy(verification = inserted)) }.toMutableSet()

      return if (auto != null || employment != null || landlord != null || references.isNotEmpty()) {
         inserted.copy(auto = auto, employment = employment, landlord = landlord, references = references)
      } else {
         inserted
      }
   }

   @Transactional
   override fun update(entity: Verification): Verification {
      logger.debug("Updating Verification {}", entity)

      val existing = findOne(id = entity.id!!)!!
      val verifiedTime: OffsetDateTime? = if (existing.verifiedBy == entity.verifiedBy) { existing.verifiedTime } else { entity.verifiedTime }

      val updated = jdbc.updateReturning("""
         UPDATE verification
         SET
            customer_account = :customer_account,
            customer_comments = :customer_comments,
            verified_by = :verified_by,
            verified_time = :verified_time
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "customer_account" to entity.customerAccount,
            "customer_comments" to entity.customerComments,
            "verified_by" to entity.verifiedBy,
            "verified_time" to verifiedTime,
            "company" to entity.company
         ),
         simpleVerificationRowMapper
      )

      val auto = entity.auto?.let { verificationAutoRepository.upsert(existing = existing.auto, requestedChange = it) }
      val employment = entity.employment?.let { verificationEmploymentRepository.upsert(existing = existing.employment, requestedChange = it) }
      val landlord = entity.landlord?.let { verificationLandlordRepository.upsert(existing = existing.landlord, requestedChange = it) }
      val references = doReferenceUpdates(entity, updated)

      doReferenceDeletes(existing, references)

      return if (auto != null || employment != null || landlord != null || references.isNotEmpty()) {
         updated.copy(auto = auto, employment = employment, landlord = landlord, references = references)
      } else {
         updated
      }
   }

   private fun doReferenceUpdates(entity: Verification, updated: Verification): MutableSet<VerificationReference> {
      return entity.references.asSequence()
         .map { verificationReferenceRepository.upsert(entity = it.copy(verification = updated)) }
         .toMutableSet()
   }

   private fun doReferenceDeletes(existing: Verification, references: MutableSet<VerificationReference>) {
      val referencesToDelete = existing.references.asSequence().filter { !references.contains(it) }.toList()

      if (referencesToDelete.isNotEmpty()) {
         verificationReferenceRepository.deleteAll(referencesToDelete)
      }
   }
}

private class VerificationRowMapper(
   private val columnPrefix: String = StringUtils.EMPTY,
   private val verificationAutoRepository: VerificationAutoRepository? = null,
   private val verificationEmploymentRepository: VerificationEmploymentRepository? = null,
   private val verificationLandlordRepository: VerificationLandlordRepository? = null
): RowMapper<Verification> {
   override fun mapRow(rs: ResultSet, row: Int): Verification {
      val auto = verificationAutoRepository?.mapRowPrefixedRow(rs = rs, row = row)
      val employment = verificationEmploymentRepository?.mapRowPrefixedRow(rs = rs, row = row)
      val landlord = verificationLandlordRepository?.mapRowPrefixedRow(rs = rs, row = row)

      return Verification(
         id = rs.getLong("${columnPrefix}id"),
         uuRowId = rs.getUuid("${columnPrefix}uu_row_id"),
         timeCreated = rs.getOffsetDateTime("${columnPrefix}time_created"),
         timeUpdated = rs.getOffsetDateTime("${columnPrefix}time_updated"),
         customerAccount = rs.getString("${columnPrefix}customer_account"),
         customerComments = rs.getString("${columnPrefix}customer_comments"),
         verifiedBy = rs.getString("${columnPrefix}verified_by"),
         verifiedTime = rs.getOffsetDateTime("${columnPrefix}verified_time"),
         company = rs.getString("${columnPrefix}company"),
         auto = auto,
         employment = employment,
         landlord = landlord
      )
   }
}
