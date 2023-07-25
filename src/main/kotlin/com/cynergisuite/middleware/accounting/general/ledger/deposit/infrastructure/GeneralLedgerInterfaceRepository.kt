package com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure

import com.cynergisuite.extensions.findFirst
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
   fun insert(record: CSVRecord, map: MutableMap<String, Any?>) {
      logger.debug("Inserting verify_staging {}", record)

      val verifyID: UUID = jdbc.updateReturning(
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
         RETURNING
            *
         """.trimIndent(),
         map
      ) { rs, _ ->
         rs.getUuid("id")
      }

      map["verify_id"] = verifyID
      val stagingDepositTypes: List<StagingDepositType> = stagingDepositTypeRepository.findAll()

      stagingDepositTypes.forEach {

         map["deposit_type_id"] = it.id
         map["deposit_amount"] = map[it.value]

         jdbc.update(
            """
            INSERT INTO deposits_staging(
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
            RETURNING
               *
            """.trimIndent(),
            map
         )
      }
   }

   @Transactional
   fun insertStagingAccountEntries(record: CSVRecord, map: Map<String, *>) {
      logger.debug("Inserting GeneralLedgerJournal from CSVRecord {}", record)

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
   fun fetchVerifyId(companyId: UUID?, storeNumber: Int, jeDate: LocalDate?): UUID {
      return jdbc.findFirst("SELECT id FROM verify_staging WHERE company_id = :comp_id AND store_number_sfk = :store_number AND business_date = :date AND deleted = false"
         , mapOf("comp_id" to companyId, "store_number" to storeNumber, "date" to jeDate)) { rs, _ ->
         rs.getUuid("id")
      }
   }

}
