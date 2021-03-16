package com.cynergisuite.middleware.accounting.routine.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.routine.RoutineDateRangeDTO
import com.cynergisuite.middleware.accounting.routine.RoutineEntity
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodType
import com.cynergisuite.middleware.accounting.routine.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.company.Company
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class RoutineRepository @Inject constructor(
   private val jdbc: NamedParameterJdbcTemplate,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(RoutineRepository::class.java)

   fun selectBaseQuery(): String {
      return """
         SELECT
            r.id                                   AS r_id,
            r.company_id                           AS r_company_id,
            r.overall_period_id                    AS r_overall_period_id,
            r.period                               AS r_period,
            r.period_from                          AS r_period_from,
            r.period_to                            AS r_period_to,
            r.fiscal_year                          AS r_fiscal_year,
            r.fiscal_year                          AS r_fiscal_year,
            r.general_ledger_open                  AS r_general_ledger_open,
            r.account_payable_open                 AS r_account_payable_open,
            overallPeriod.id                       AS r_overallPeriod_id,
            overallPeriod.value                    AS r_overallPeriod_value,
            overallPeriod.abbreviation             AS r_overallPeriod_abbreviation,
            overallPeriod.description              AS r_overallPeriod_description,
            overallPeriod.localization_code        AS r_overallPeriod_localization_code,
            count(*) OVER()                        AS total_elements
         FROM financial_calendar r
            JOIN overall_period_type_domain overallPeriod ON overallPeriod.id = r.overall_period_id
      """
   }

   fun findOne(id: Long, company: Company): RoutineEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.myId())
      val query = "${selectBaseQuery()} WHERE r.id = :id AND r.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query, params,
         RowMapper { rs, _ ->
            mapRow(
               rs,
               "r_"
            )
         }
      )

      logger.trace("Searching for Routine: {} resulted in {}", company, found)

      return found
   }

   fun findAll(company: Company, page: PageRequest): RepositoryPage<RoutineEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE r.company_id = :comp_id
            ORDER BY r_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.myId(),
            "limit" to page.size(),
            "offset" to page.offset()
         ),
         page
      ) { rs, elements ->
         do {
            elements.add(mapRow(rs, "r_"))
         } while (rs.next())
      }
   }

   fun exists(company: Company, overallPeriodTypeId: Long, period: Int): Boolean {
      val params = mutableMapOf("comp_id" to company.myId(), "overallPeriodId" to overallPeriodTypeId, "period" to period)
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT id
                        FROM financial_calendar
                        WHERE company_id = :comp_id AND overall_period_id = :overallPeriodId AND period = :period)
         """.trimIndent(),
         params, Boolean::class.java
      )!!

      logger.trace("Checking if financial_calendar: {} exists resulted in {}", params, exists)

      return exists
   }

   @Transactional
   fun insert(entity: RoutineEntity, company: Company): RoutineEntity {
      logger.debug("Inserting financial_calendar {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO financial_calendar (
            company_id,
            overall_period_id,
            period,
            period_from,
            period_to,
            fiscal_year,
            general_ledger_open,
            account_payable_open
         )
         VALUES (
            :company_id,
            :overall_period_id,
            :period,
            :period_from,
            :period_to,
            :fiscal_year,
            :general_ledger_open,
            :account_payable_open
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "overall_period_id" to entity.overallPeriod.id,
            "period" to entity.period,
            "period_from" to entity.periodFrom,
            "period_to" to entity.periodTo,
            "fiscal_year" to entity.fiscalYear,
            "general_ledger_open" to entity.generalLedgerOpen,
            "account_payable_open" to entity.accountPayableOpen
         ),
         RowMapper { rs, _ ->
            mapRowUpsert(rs, entity.overallPeriod)
         }
      )
   }

   @Transactional
   fun update(entity: RoutineEntity, company: Company): RoutineEntity {
      logger.debug("Updating financial_calendar {}", entity)

      return jdbc.updateReturning(
         """
         UPDATE financial_calendar
         SET
            company_id = :company_id,
            overall_period_id = :overall_period_id,
            period = :period,
            period_from = :period_from,
            period_to = :period_to,
            fiscal_year = :fiscal_year,
            general_ledger_open = :general_ledger_open,
            account_payable_open = :account_payable_open
         WHERE id = :id
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "id" to entity.id,
            "company_id" to company.myId(),
            "overall_period_id" to entity.overallPeriod.id,
            "period" to entity.period,
            "period_from" to entity.periodFrom,
            "period_to" to entity.periodTo,
            "fiscal_year" to entity.fiscalYear,
            "general_ledger_open" to entity.generalLedgerOpen,
            "account_payable_open" to entity.accountPayableOpen
         ),
         RowMapper { rs, _ ->
            mapRowUpsert(rs, entity.overallPeriod)
         }
      )
   }

   @Transactional
   fun openGLAccountsForPeriods(dateRangeDTO: RoutineDateRangeDTO, company: Company) {
      logger.debug("Set GLAccounts to false")

      val affectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar
         SET
            general_ledger_open = :general_ledger_open
         FROM financial_calendar finCal
            JOIN company ON finCal.company_id = company.id
            JOIN overall_period_type_domain overallPeriod ON overallPeriod.id = finCal.overall_period_id
         WHERE finCal.company_id = :company_id
            AND overallPeriod.value = :financial_period
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "financial_period" to "C",
            "general_ledger_open" to false
         )
      )

      logger.info("Affected row count {}", affectedRowCount)

      logger.debug("Set GLAccounts to true for selected period(s)")

      val newAffectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar finCal
         SET
            general_ledger_open = :general_ledger_open
         FROM overall_period_type_domain overallPeriod
         WHERE overallPeriod.id = finCal.overall_period_id
            AND finCal.company_id = :company_id
            AND overallPeriod.value = :financial_period
            AND finCal.period_from BETWEEN :from_date AND :to_date
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "financial_period" to "C",
            "general_ledger_open" to true,
            "from_date" to dateRangeDTO.periodFrom,
            "to_date" to dateRangeDTO.periodTo
         )
      )

      logger.info("Affected row count when opening GLAccounts {}", newAffectedRowCount)
   }

   @Transactional
   fun openAPAccountsForPeriods(dateRangeDTO: RoutineDateRangeDTO, company: Company) {
      logger.debug("Set APAccounts to false")

      val affectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar
         SET
            account_payable_open = :account_payable_open
         FROM financial_calendar finCal
            JOIN company ON finCal.company_id = company.id
            JOIN overall_period_type_domain overallPeriod ON overallPeriod.id = finCal.overall_period_id
         WHERE finCal.company_id = :company_id
            AND overallPeriod.value = :financial_period
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "financial_period" to "C",
            "account_payable_open" to false
         )
      )

      logger.info("Affected row count {}", affectedRowCount)

      logger.debug("Set APAccounts to true for selected period(s)")

      val newAffectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar finCal
         SET
            account_payable_open = :account_payable_open
         FROM overall_period_type_domain overallPeriod
         WHERE overallPeriod.id = finCal.overall_period_id
            AND finCal.company_id = :company_id
            AND overallPeriod.value = :financial_period
            AND finCal.period_from BETWEEN :from_date AND :to_date
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "financial_period" to "C",
            "account_payable_open" to true,
            "from_date" to dateRangeDTO.periodFrom,
            "to_date" to dateRangeDTO.periodTo
         )
      )

      logger.info("Affected row count when opening APAccounts {}", newAffectedRowCount)
   }

   @Transactional
   fun insertFinancialCalendar(entity: RoutineEntity, company: Company): RoutineEntity {
      logger.debug("Creating entire financial_calendar {}", company)

      return jdbc.insertReturning(
         """
         INSERT INTO financial_calendar (
            company_id,
            overall_period_id,
            period,
            period_from,
            period_to,
            fiscal_year,
            general_ledger_open,
            account_payable_open
         )
         VALUES (
            :company_id,
            :overall_period_id,
            :period,
            :period_from,
            :period_to,
            :fiscal_year,
            :general_ledger_open,
            :account_payable_open
         )
         RETURNING
            *
         """.trimIndent(),
         mapOf(
            "company_id" to company.myId(),
            "overall_period_id" to entity.overallPeriod.id,
            "period" to entity.period,
            "period_from" to entity.periodFrom,
            "period_to" to entity.periodTo,
            "fiscal_year" to entity.fiscalYear,
            "general_ledger_open" to entity.generalLedgerOpen,
            "account_payable_open" to entity.accountPayableOpen
         ),
         RowMapper { rs, _ ->
            mapRowUpsert(rs, entity.overallPeriod)
         }
      )
   }

   private fun mapRow(
      rs: ResultSet,
      columnPrefix: String = EMPTY
   ): RoutineEntity {
      return RoutineEntity(
         id = rs.getLong("${columnPrefix}id"),
         overallPeriod = overallPeriodTypeRepository.mapRow(rs, "${columnPrefix}overallPeriod_"),
         period = rs.getInt("${columnPrefix}period"),
         periodFrom = rs.getLocalDate("${columnPrefix}period_from"),
         periodTo = rs.getLocalDate("${columnPrefix}period_to"),
         fiscalYear = rs.getInt("${columnPrefix}fiscal_year"),
         generalLedgerOpen = rs.getBoolean("${columnPrefix}general_ledger_open"),
         accountPayableOpen = rs.getBoolean("${columnPrefix}account_payable_open")
      )
   }

   private fun mapRowUpsert(
      rs: ResultSet,
      overallPeriodType: OverallPeriodType,
      columnPrefix: String = EMPTY
   ): RoutineEntity {
      return RoutineEntity(
         id = rs.getLong("${columnPrefix}id"),
         overallPeriod = overallPeriodType,
         period = rs.getInt("${columnPrefix}period"),
         periodFrom = rs.getLocalDate("${columnPrefix}period_from"),
         periodTo = rs.getLocalDate("${columnPrefix}period_to"),
         fiscalYear = rs.getInt("${columnPrefix}fiscal_year"),
         generalLedgerOpen = rs.getBoolean("${columnPrefix}general_ledger_open"),
         accountPayableOpen = rs.getBoolean("${columnPrefix}account_payable_open")
      )
   }
}
