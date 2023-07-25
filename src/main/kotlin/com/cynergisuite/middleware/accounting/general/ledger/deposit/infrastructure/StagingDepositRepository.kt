package com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositEntity
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositPageRequest
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet

@Singleton
class StagingDepositRepository @Inject constructor(
   private val jdbc: Jdbi
) {
   private val logger: Logger = LoggerFactory.getLogger(StagingDepositRepository::class.java)

   @ReadOnly
   fun findAll(
      company: CompanyEntity,
      filterRequest: StagingDepositPageRequest
   ): RepositoryPage<StagingDepositEntity, PageRequest> {
      val params = mutableMapOf<String, Any?>("comp_id" to company.id)
      val whereClause = StringBuilder(" WHERE vs.deleted = false AND vs.company_id = :comp_id AND dep.value IN ('DEP_1', 'DEP_2', 'DEP_3', 'DEP_4', 'DEP_5', 'DEP_6', 'DEP_7') ")

      if (filterRequest.verifiedSuccessful != null) {
         params["verifiedSuccessful"] = filterRequest.verifiedSuccessful
         whereClause.append(" AND vs.verify_successful = :verifiedSuccessful")
      }

      if (filterRequest.movedToJe != null) {
         params["movedToJe"] = filterRequest.movedToJe
         whereClause.append(" AND vs.moved_to_pending_journal_entries = :movedToJe")
      }

      if (filterRequest.from != null || filterRequest.thru != null) {
         params["from"] = filterRequest.from
         params["thru"] = filterRequest.thru
         whereClause.append(" AND vs.business_date")
            .append(
               buildFilterString(
                  filterRequest.from != null,
                  filterRequest.thru != null,
                  "from",
                  "thru"
               )
            )
      }

      if (filterRequest.beginStore != null || filterRequest.endStore != null) {
         params["beginStore"] = filterRequest.beginStore
         params["endStore"] = filterRequest.endStore
         whereClause.append(" AND vs.store_number_sfk")
            .append(
               buildFilterString(
                  filterRequest.beginStore != null,
                  filterRequest.endStore != null,
                  "beginStore",
                  "endStore"
               )
            )
      }

      return jdbc.queryPaged(
      """
         SELECT
             vs.verify_successful,
             vs.business_date,
             vs.moved_to_pending_journal_entries,
             vs.store_number_sfk,
             sv.name AS store_name,
             vs.error_amount,
             SUM(CASE WHEN dep.value = 'DEP_1' THEN ds.deposit_amount ELSE 0 END)   AS deposit_1,
             SUM(CASE WHEN dep.value = 'DEP_2' THEN ds.deposit_amount ELSE 0 END)   AS deposit_2,
             SUM(CASE WHEN dep.value = 'DEP_3' THEN ds.deposit_amount ELSE 0 END)   AS deposit_3,
             SUM(CASE WHEN dep.value = 'DEP_4' THEN ds.deposit_amount ELSE 0 END)   AS deposit_4,
             SUM(CASE WHEN dep.value = 'DEP_5' THEN ds.deposit_amount ELSE 0 END)   AS deposit_5,
             SUM(CASE WHEN dep.value = 'DEP_6' THEN ds.deposit_amount ELSE 0 END)   AS deposit_6,
             SUM(CASE WHEN dep.value = 'DEP_7' THEN ds.deposit_amount ELSE 0 END)   AS deposit_7,
             SUM(ds.deposit_amount)                                                 AS deposit_total,
             count(*) OVER()                                                        AS total_elements
         FROM
             verify_staging vs
             JOIN deposits_staging ds ON vs.company_id = ds.company_id AND vs.id = ds.verify_id
             JOIN company comp ON vs.company_id = comp.id AND comp.deleted = FALSE
             JOIN fastinfo_prod_import.store_vw sv
                    ON sv.dataset = comp.dataset_code
                       AND sv.number = vs.store_number_sfk
             JOIN deposits_staging_deposit_type_domain dep ON ds.deposit_type_id = dep.id
         $whereClause
         GROUP BY
             vs.verify_successful,
             vs.business_date,
             vs.moved_to_pending_journal_entries,
             vs.store_number_sfk,
             sv.name,
             vs.error_amount
         ORDER BY vs.verify_successful, vs.business_date DESC, vs.store_number_sfk
               """.trimIndent()
      , params
      , filterRequest
      )
      { rs, elements ->
         do {
            elements.add(mapRow(rs))
         }  while (rs.next())
      }
   }
   fun mapRow(rs: ResultSet): StagingDepositEntity =
      StagingDepositEntity(
         verifySuccessful = rs.getBoolean("verify_successful"),
         businessDate = rs.getLocalDate("business_date"),
         movedToPendingJournalEntries = rs.getBoolean("moved_to_pending_journal_entries"),
         store = rs.getInt("store_number_sfk"),
         storeName = rs.getString("store_name"),
         errorAmount = rs.getBigDecimal("error_amount"),
         deposit1Cash = rs.getBigDecimal("deposit_1"),
         deposit2PmtForOtherStores = rs.getBigDecimal("deposit_2"),
         deposit3PmtFromOtherStores = rs.getBigDecimal("deposit_3"),
         deposit4CCInStr = rs.getBigDecimal("deposit_4"),
         deposit5ACHOLP = rs.getBigDecimal("deposit_5"),
         deposit6CCOLP = rs.getBigDecimal("deposit_6"),
         deposit7DebitCard = rs.getBigDecimal("deposit_7"),
         depositTotal = rs.getBigDecimal("deposit_total")
      )

   private fun buildFilterString(begin: Boolean, end: Boolean, beginningParam: String, endingParam: String): String {
      return if (begin && end) " BETWEEN :$beginningParam AND :$endingParam "
      else if (begin) " >= :$beginningParam "
      else " <= :$endingParam "
   }
}
