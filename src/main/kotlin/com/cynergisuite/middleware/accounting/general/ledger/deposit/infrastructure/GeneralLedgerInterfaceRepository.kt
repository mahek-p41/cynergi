package com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure

import com.cynergisuite.extensions.delete
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositType
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVRecord
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class GeneralLedgerInterfaceRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val stagingDepositTypeRepository: StagingDepositTypeRepository,
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerInterfaceRepository::class.java)

   @Transactional
   fun upsert(record: CSVRecord, map: MutableMap<String, Any?>) {
      logger.debug("Upserting verify_staging {}", record)

      if (!movedToPendingJournalEntries(
            map["company_id"] as UUID,
            map["store_number_sfk"] as Int,
            map["business_date"] as LocalDate?
         )
      ) {
         var verifyID = findVerifyID(
            map["company_id"] as UUID,
            map["store_number_sfk"] as Int,
            map["business_date"] as LocalDate
         )
         if (verifyID == null) {
            verifyID = jdbc.updateReturning(
               """
                  INSERT INTO verify_staging(
                      company_id,
                      store_number_sfk,
                      business_date,
                      verify_successful,
                      error_amount,
                      moved_to_pending_journal_entries
                  )
                  VALUES (
                      :company_id,
                      :store_number_sfk,
                      :business_date,
                      :verify_successful,
                      :error_amount,
                      :moved_to_pending_journal_entries
                  )
                  RETURNING *
               """.trimIndent(),
               map
            ) { rs, _ ->
               rs.getUuid("id")
            }
            map["verify_id"] = verifyID
         } else {
            map["verify_id"] = verifyID
            jdbc.update(
               """
                  UPDATE verify_staging
                  SET
                     verify_successful = :verify_successful,
                     error_amount = :error_amount
                  WHERE id = :verify_id AND deleted = false
               """.trimIndent(),
               map
            )
         }

         val stagingDepositTypes: List<StagingDepositType> = stagingDepositTypeRepository.findAll()

         stagingDepositTypes.forEach {
            map["deposit_type_id"] = it.id
            map["deposit_amount"] = map[it.value]

            val depositID = findDepositID(verifyID!!, it.id)

            if (depositID == null) {
               jdbc.update(
                  """
                  INSERT INTO deposits_staging (
                      company_id,
                      verify_id,
                      store_number_sfk,
                      business_date,
                      deposit_type_id,
                      deposit_amount
                  )
                  VALUES (
                     :company_id,
                     :verify_id,
                     :store_number_sfk,
                     :business_date,
                     :deposit_type_id,
                     :deposit_amount
                     )
               """.trimIndent(),
                  map
               )
            } else {
               jdbc.update(
                  """
                  UPDATE deposits_staging
                  SET deposit_amount = :deposit_amount
                  WHERE verify_id = :verify_id
                     AND deposit_type_id = :deposit_type_id
                     AND deleted = FALSE
               """.trimIndent(),
                  map
               )
            }
         }
      }

   }

   @Transactional
   fun insertStagingAccountEntries(record: CSVRecord, map: Map<String, *>) {
      logger.debug("Inserting accounting_entries_staging from CSVRecord {}", record)

      jdbc.update(
         """
            INSERT INTO accounting_entries_staging(
               company_id,
               verify_id,
               store_number_sfk,
               business_date,
               account_id,
               profit_center_id_sfk,
               source_id,
               journal_entry_amount,
               message
            )
            VALUES (
               :company_id,
               :verify_id,
               :store_number_sfk,
               :business_date,
               :account_id,
               :profit_center_id_sfk,
               :source_id,
               :journal_entry_amount,
               :message
            )
         """.trimIndent(),
         map
      )

   }

   @ReadOnly
   fun fetchVerifyId(companyId: UUID?, storeNumber: Int, jeDate: LocalDate?): UUID? {
      return jdbc.findFirstOrNull("SELECT id FROM verify_staging WHERE company_id = :comp_id AND store_number_sfk = :store_number AND business_date = :date AND deleted = false"
         , mapOf("comp_id" to companyId, "store_number" to storeNumber, "date" to jeDate)) { rs, _ ->
         rs.getUuid("id")
      }
   }

   @ReadOnly
   fun movedToPendingJournalEntries(companyId: UUID, storeNumber: Int, jeDate: LocalDate?): Boolean {
      return jdbc.findFirstOrNull("""
         SELECT moved_to_pending_journal_entries
         FROM verify_staging
         WHERE company_id = :comp_id
            AND store_number_sfk = :store_number
            AND business_date = :date AND deleted = false
         """.trimIndent()
         , mapOf("comp_id" to companyId, "store_number" to storeNumber, "date" to jeDate)) { rs, _ ->
         rs.getBoolean("moved_to_pending_journal_entries")
      } ?: false
   }

   @ReadOnly
   fun findVerifyID(companyId: UUID, storeNumber: Int, businessDate: LocalDate): UUID? {
      return jdbc.findFirstOrNull(
         """SELECT id
            FROM verify_staging
            WHERE company_id = :comp_id
              AND store_number_sfk = :storeNumber
              AND business_date = :date
              AND deleted = FALSE""",
         mapOf<String, Any>(
            "comp_id" to companyId,
            "storeNumber" to storeNumber,
            "date" to businessDate
         )
      )
      { rs, _ -> rs.getUuid("id") }
   }

   @ReadOnly
   fun findDepositID(verifyID: UUID, depositTypeID: Int): UUID? {
      return jdbc.findFirstOrNull(
         """SELECT id
            FROM deposits_staging
            WHERE verify_id = :verifyId
               AND deposit_type_id = :depositTypeID
               AND deleted = FALSE""",
         mapOf<String, Any>(
            "verifyId" to verifyID,
            "depositTypeID" to depositTypeID,
         )
      )
      { rs, _ -> rs.getUuid("id") }
   }

   @Transactional
   fun deleteUnmovedStagingAccountEntries(companyId: UUID, date: LocalDate, uploadedStores: List<Int>) {
      logger.debug("Deleting Staging Account Entries that have not been moved and belong to uploaded stores.")

      val rowsAffected = jdbc.delete(
         """
         DELETE FROM accounting_entries_staging
         USING verify_staging
         WHERE accounting_entries_staging.verify_id = verify_staging.id
             AND verify_staging.company_id = :comp_id
             AND verify_staging.business_date = :date
             AND verify_staging.store_number_sfk IN (<stores>)
             AND verify_staging.moved_to_pending_journal_entries = FALSE
         """,
         mapOf("comp_id" to companyId, "date" to date, "stores" to uploadedStores)
      )
      logger.info("Row affected {}", rowsAffected)
   }

}
