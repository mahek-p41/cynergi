package com.cynergisuite.middleware.accounting.financial.calendar.infrastructure

import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.extensions.findFirstOrNull
import com.cynergisuite.extensions.getLocalDate
import com.cynergisuite.extensions.getUuid
import com.cynergisuite.extensions.insertReturning
import com.cynergisuite.extensions.query
import com.cynergisuite.extensions.queryForObject
import com.cynergisuite.extensions.queryPaged
import com.cynergisuite.extensions.update
import com.cynergisuite.extensions.updateReturning
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarEntity
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarGLAPDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FiscalYearDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodType
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.transaction.annotation.ReadOnly
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.lang3.StringUtils.EMPTY
import org.jdbi.v3.core.Jdbi
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.time.LocalDate
import java.util.UUID
import javax.transaction.Transactional

@Singleton
class FinancialCalendarRepository @Inject constructor(
   private val jdbc: Jdbi,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) {
   private val logger: Logger = LoggerFactory.getLogger(FinancialCalendarRepository::class.java)

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

   @ReadOnly
   fun findOne(id: UUID, company: CompanyEntity): FinancialCalendarEntity? {
      val params = mutableMapOf<String, Any?>("id" to id, "comp_id" to company.id)
      val query = "${selectBaseQuery()} WHERE r.id = :id AND r.company_id = :comp_id"
      val found = jdbc.findFirstOrNull(
         query,
         params
      ) { rs, _ ->
         mapRow(
            rs,
            "r_"
         )
      }

      logger.trace("Searching for Financial Calendar: {} resulted in {}", company, found)

      return found
   }

   @ReadOnly
   fun findAll(company: CompanyEntity, page: PageRequest): RepositoryPage<FinancialCalendarEntity, PageRequest> {
      return jdbc.queryPaged(
         """
            ${selectBaseQuery()}
            WHERE r.company_id = :comp_id
            ORDER BY r_${page.snakeSortBy()} ${page.sortDirection()}
            LIMIT :limit OFFSET :offset
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
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

   @ReadOnly
   fun fetchByDate(company: CompanyEntity, date: LocalDate): FinancialCalendarEntity? {
      return jdbc.findFirstOrNull(
         """
            ${selectBaseQuery()}
            WHERE r.company_id = :comp_id AND :date BETWEEN r.period_from AND r.period_to

         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "date" to date
         )
      ) { rs, _ ->
         mapRow(
            rs,
            "r_"
         )
      }
   }

   @ReadOnly
   fun exists(company: CompanyEntity, overallPeriodTypeId: Int, period: Int): Boolean {
      val params = mutableMapOf("comp_id" to company.id, "overallPeriodId" to overallPeriodTypeId, "period" to period)
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT id
                        FROM financial_calendar
                        WHERE company_id = :comp_id AND overall_period_id = :overallPeriodId AND period = :period)
         """.trimIndent(),
         params,
         Boolean::class.java
      )

      logger.trace("Checking if financial_calendar: {} exists resulted in {}", params, exists)

      return exists
   }

   @Transactional
   fun insert(entity: FinancialCalendarEntity, company: CompanyEntity): FinancialCalendarEntity {
      logger.debug("Inserting financial_calendar {}", entity)

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
            "company_id" to company.id,
            "overall_period_id" to entity.overallPeriod.id,
            "period" to entity.period,
            "period_from" to entity.periodFrom,
            "period_to" to entity.periodTo,
            "fiscal_year" to entity.fiscalYear,
            "general_ledger_open" to entity.generalLedgerOpen,
            "account_payable_open" to entity.accountPayableOpen
         )
      ) { rs, _ ->
         mapRowUpsert(rs, entity.overallPeriod)
      }
   }

   @Transactional
   fun update(entity: FinancialCalendarEntity, company: CompanyEntity): FinancialCalendarEntity {
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
            "company_id" to company.id,
            "overall_period_id" to entity.overallPeriod.id,
            "period" to entity.period,
            "period_from" to entity.periodFrom,
            "period_to" to entity.periodTo,
            "fiscal_year" to entity.fiscalYear,
            "general_ledger_open" to entity.generalLedgerOpen,
            "account_payable_open" to entity.accountPayableOpen
         )
      ) { rs, _ ->
         mapRowUpsert(rs, entity.overallPeriod)
      }
   }

   @Transactional
   fun openGLAccountsForPeriods(dateRangeDTO: FinancialCalendarDateRangeDTO, company: CompanyEntity) {
      logger.debug("Set GLAccounts to false")

      logger.info("Closing GL Account range for company {}", company.id)

      val affectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar
         SET
            general_ledger_open = :general_ledger_open
         WHERE financial_calendar.id IN (
            select finCal.id FROM financial_calendar finCal
               JOIN company ON finCal.company_id = company.id AND company.deleted = FALSE
               JOIN overall_period_type_domain overallPeriod ON overallPeriod.id = finCal.overall_period_id
               WHERE ((finCal.company_id = :comp_id) AND (overallPeriod.value = :financial_period1 OR overallPeriod.value = :financial_period2)))
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "financial_period1" to "C",
            "financial_period2" to "N",
            "general_ledger_open" to false
         )
      )

      logger.info("Affected row count {}", affectedRowCount)

      logger.debug("Set GLAccounts to true for selected period(s)")

      logger.info("Opening GL Account range for company {}", company.id)

      val newAffectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar finCal
         SET
            general_ledger_open = :general_ledger_open
         FROM overall_period_type_domain overallPeriod
         WHERE overallPeriod.id = finCal.overall_period_id
            AND finCal.company_id = :comp_id
            AND (overallPeriod.value = :financial_period1 OR
                 overallPeriod.value = :financial_period2)
            AND finCal.period_from BETWEEN :from_date AND :to_date
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "financial_period1" to "C",
            "financial_period2" to "N",
            "general_ledger_open" to true,
            "from_date" to dateRangeDTO.periodFrom,
            "to_date" to dateRangeDTO.periodTo
         )
      )

      logger.info("Affected row count when opening GLAccounts {}", newAffectedRowCount)
   }

   @Transactional
   fun openAPAccountsForPeriods(dateRangeDTO: FinancialCalendarDateRangeDTO, company: CompanyEntity) {
      logger.debug("Set APAccounts to false")

      logger.info("Closing AP Account range for company {}", company.id)

      val affectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar
         SET
            account_payable_open = :account_payable_open
         WHERE financial_calendar.id IN (
            select finCal.id FROM financial_calendar finCal
               JOIN company ON finCal.company_id = company.id AND company.deleted = FALSE
               JOIN overall_period_type_domain overallPeriod ON overallPeriod.id = finCal.overall_period_id
               WHERE ((finCal.company_id = :comp_id) AND (overallPeriod.value = :financial_period1 OR overallPeriod.value = :financial_period2)))

         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "financial_period1" to "C",
            "financial_period2" to "N",
            "account_payable_open" to false
         )
      )

      logger.info("Affected row count {}", affectedRowCount)

      logger.debug("Set APAccounts to true for selected period(s)")

      logger.info("Opening AP Account range for company {}", company.id)

      val newAffectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar finCal
         SET
            account_payable_open = :account_payable_open
         FROM overall_period_type_domain overallPeriod
         WHERE overallPeriod.id = finCal.overall_period_id
            AND finCal.company_id = :comp_id
            AND (overallPeriod.value = :financial_period1 OR
                 overallPeriod.value = :financial_period2)
            AND finCal.period_from BETWEEN :from_date AND :to_date
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "financial_period1" to "C",
            "financial_period2" to "N",
            "account_payable_open" to true,
            "from_date" to dateRangeDTO.periodFrom,
            "to_date" to dateRangeDTO.periodTo
         )
      )

      logger.info("Affected row count when opening APAccounts {}", newAffectedRowCount)
   }

   @Transactional
   fun openGLAPAccountsForPeriods(dateRangeDTO: FinancialCalendarGLAPDateRangeDTO, company: CompanyEntity) {
      logger.debug("Set GLAccounts to false")

      logger.info("Closing GL Account range for company {}", company.id)

      val glAffectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar
         SET
            general_ledger_open = :general_ledger_open
         WHERE financial_calendar.id IN (
            select finCal.id FROM financial_calendar finCal
               JOIN company ON finCal.company_id = company.id AND company.deleted = FALSE
               JOIN overall_period_type_domain overallPeriod ON overallPeriod.id = finCal.overall_period_id
               WHERE ((finCal.company_id = :comp_id) AND (overallPeriod.value = :financial_period1 OR overallPeriod.value = :financial_period2)))
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "financial_period1" to "C",
            "financial_period2" to "N",
            "general_ledger_open" to false
         )
      )

      logger.info("Affected row count {}", glAffectedRowCount)

      logger.debug("Set GLAccounts to true for selected period(s)")

      logger.info("Opening GL Account range for company {}", company.id)

      val glNewAffectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar finCal
         SET
            general_ledger_open = :general_ledger_open
         FROM overall_period_type_domain overallPeriod
         WHERE overallPeriod.id = finCal.overall_period_id
            AND finCal.company_id = :comp_id
            AND (overallPeriod.value = :financial_period1 OR
                 overallPeriod.value = :financial_period2)
            AND finCal.period_from BETWEEN :from_date AND :to_date
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "financial_period1" to "C",
            "financial_period2" to "N",
            "general_ledger_open" to true,
            "from_date" to dateRangeDTO.glPeriodFrom,
            "to_date" to dateRangeDTO.glPeriodTo
         )
      )

      logger.info("Affected row count when opening GLAccounts {}", glNewAffectedRowCount)

      logger.debug("Set APAccounts to false")

      logger.info("Closing AP Account range for company {}", company.id)

      val apAffectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar
         SET
            account_payable_open = :account_payable_open
         WHERE financial_calendar.id IN (
            select finCal.id FROM financial_calendar finCal
               JOIN company ON finCal.company_id = company.id AND company.deleted = FALSE
               JOIN overall_period_type_domain overallPeriod ON overallPeriod.id = finCal.overall_period_id
               WHERE ((finCal.company_id = :comp_id) AND (overallPeriod.value = :financial_period1 OR overallPeriod.value = :financial_period2)))

         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "financial_period1" to "C",
            "financial_period2" to "N",
            "account_payable_open" to false
         )
      )

      logger.info("Affected row count {}", apAffectedRowCount)

      logger.debug("Set APAccounts to true for selected period(s)")

      logger.info("Opening AP Account range for company {}", company.id)

      val apNewAffectedRowCount = jdbc.update(
         """
         UPDATE financial_calendar finCal
         SET
            account_payable_open = :account_payable_open
         FROM overall_period_type_domain overallPeriod
         WHERE overallPeriod.id = finCal.overall_period_id
            AND finCal.company_id = :comp_id
            AND (overallPeriod.value = :financial_period1 OR
                 overallPeriod.value = :financial_period2)
            AND finCal.period_from BETWEEN :from_date AND :to_date
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "financial_period1" to "C",
            "financial_period2" to "N",
            "account_payable_open" to true,
            "from_date" to dateRangeDTO.apPeriodFrom,
            "to_date" to dateRangeDTO.apPeriodTo
         )
      )

      logger.info("Affected row count when opening APAccounts {}", apNewAffectedRowCount)
   }

   @ReadOnly
   fun openGLDateRangeFound(company: CompanyEntity): Boolean {
      val params = mutableMapOf("comp_id" to company.id)
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS ( ${selectBaseQuery()}
                        WHERE company_id = :comp_id AND general_ledger_open = true)
         """.trimIndent(),
         params,
         Boolean::class.java
      )

      logger.trace("Validating open GL range exists resulted in {}", params, exists)

      return exists
   }

   @ReadOnly
   fun openAPDateRangeFound(company: CompanyEntity): Boolean {
      val params = mutableMapOf("comp_id" to company.id)
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS ( ${selectBaseQuery()}
                        WHERE company_id = :comp_id AND account_payable_open = true)
         """.trimIndent(),
         params,
         Boolean::class.java
      )

      logger.trace("Validating open AP range exists resulted in {}", params, exists)

      return exists
   }

   @Transactional
   fun findDateRangeWhenGLIsOpen(company: CompanyEntity): Pair<LocalDate, LocalDate>? {
      logger.debug("Find periods where GL is open")

      val periods = mutableListOf<FinancialCalendarEntity>()

      jdbc.query(
         """
            ${selectBaseQuery()}
            WHERE r.company_id = :comp_id AND general_ledger_open = :general_ledger_open
            ORDER BY r_period_from
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "general_ledger_open" to true
         )
      ) { rs, _ ->
         do {
            periods.add(mapRow(rs, "r_"))
         } while (rs.next())
      }

      return if (periods.isNotEmpty()) {
         Pair(periods.first().periodFrom, periods.last().periodTo)
      } else {
         null
      }
   }

   @Transactional
   fun findDateRangeWhenAPIsOpen(company: CompanyEntity): Pair<LocalDate, LocalDate>? {
      logger.debug("Find periods where AP is open")

      val periods = mutableListOf<FinancialCalendarEntity>()

      jdbc.query(
         """
            ${selectBaseQuery()}
            WHERE r.company_id = :comp_id AND account_payable_open = :account_payable_open
            ORDER BY r_period_from
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "account_payable_open" to true
         )
      ) { rs, _ ->
         do {
            periods.add(mapRow(rs, "r_"))
         } while (rs.next())
      }

      return if (periods.isNotEmpty()) {
         Pair(periods.first().periodFrom, periods.last().periodTo)
      } else {
         null
      }
   }

   @ReadOnly
   fun dateFoundInFinancialCalendar(company: CompanyEntity, date: LocalDate): Boolean {
      val params = mutableMapOf("comp_id" to company.id, "date" to date)
      val exists = jdbc.queryForObject(
         """
         SELECT EXISTS (SELECT id
                        FROM financial_calendar
                        WHERE company_id = :comp_id AND :date BETWEEN period_from AND period_to)
         """.trimIndent(),
         params,
         Boolean::class.java
      )

      logger.trace("Validating financial_calendar exists for date {} resulted in {}", params, exists)

      return exists
   }

   @ReadOnly
   fun findOverallPeriodIdAndPeriod(company: CompanyEntity, date: LocalDate): Pair<Int, Int>? {
      val found = jdbc.query(
         """
         SELECT overall_period_id, period
            FROM financial_calendar
            WHERE company_id = :comp_id AND :date BETWEEN period_from AND period_to
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "date" to date
         )
      ) { rs, _ ->
         mapRow(rs)
      }

      logger.trace("Find overall period id and period for date {} resulted in {}", date, found)

      return if (found.isEmpty()) {
         null
      } else {
         found.first()
      }
   }

   @ReadOnly
   fun findFirstDateOfFiscalYear(company: CompanyEntity, overallPeriodId: Int): LocalDate {
      val found = jdbc.query(
         """
         SELECT period_from
            FROM financial_calendar
            WHERE company_id = :comp_id AND overall_period_id = :overallPeriodId AND period = 1
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "overallPeriodId" to overallPeriodId
         )
      ) { rs, _ ->
         mapDate(rs)
      }

      logger.trace("Find first date of fiscal year with overall period id {} resulted in {}", overallPeriodId, found.first())

      return found.first()
   }

   @ReadOnly
   fun findEndDateOfFiscalYear(company: CompanyEntity, overallPeriodId: Int): LocalDate {
      val found = jdbc.query(
         """
         SELECT period_to
            FROM financial_calendar
            WHERE company_id = :comp_id AND overall_period_id = :overallPeriodId AND period = 12
         """.trimIndent(),
         mapOf(
            "comp_id" to company.id,
            "overallPeriodId" to overallPeriodId
         )
      ) { rs, _ ->
         mapDate(rs)
      }

      logger.trace("Find end date of fiscal year with overall period id {} resulted in {}", overallPeriodId, found.first())

      return found.first()
   }

   fun rollOneFinancialYear(company: CompanyEntity) {
      logger.debug("Roll one financial year for financial_calendar {}", company)
      jdbc.update("""
         DELETE FROM financial_calendar
         WHERE company_id = :comp_id
               AND overall_period_id = 1;

         UPDATE public.financial_calendar
         SET overall_period_id = 1
         WHERE company_id = :comp_id
               AND overall_period_id in (2);

         UPDATE public.financial_calendar
         SET overall_period_id = 2
         WHERE company_id = :comp_id
               AND overall_period_id in (3);

         UPDATE public.financial_calendar
         SET overall_period_id = 3
         WHERE company_id = :comp_id
               AND overall_period_id in (4);

         INSERT INTO public.financial_calendar(company_id, overall_period_id, period, period_from, period_to, fiscal_year)
         SELECT company_id,
                4,
                period,
                period_from + interval '1 year',
                period_to + interval '1 year',
                fiscal_year + 1
         FROM public.financial_calendar
         WHERE company_id = :comp_id
               AND overall_period_id = 3;
         """.trimIndent(),
         mapOf("comp_id" to company.id)
      )
   }

   private fun mapRow(
      rs: ResultSet,
      columnPrefix: String = EMPTY
   ): FinancialCalendarEntity {
      return FinancialCalendarEntity(
         id = rs.getUuid("${columnPrefix}id"),
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
   ): FinancialCalendarEntity {
      return FinancialCalendarEntity(
         id = rs.getUuid("${columnPrefix}id"),
         overallPeriod = overallPeriodType,
         period = rs.getInt("${columnPrefix}period"),
         periodFrom = rs.getLocalDate("${columnPrefix}period_from"),
         periodTo = rs.getLocalDate("${columnPrefix}period_to"),
         fiscalYear = rs.getInt("${columnPrefix}fiscal_year"),
         generalLedgerOpen = rs.getBoolean("${columnPrefix}general_ledger_open"),
         accountPayableOpen = rs.getBoolean("${columnPrefix}account_payable_open")
      )
   }

   private fun mapFiscalYearDTO(
      rs: ResultSet
   ): FiscalYearDTO {
      return FiscalYearDTO(
         fiscalYear = rs.getInt("fiscal_year"),
         overallPeriod = OverallPeriodTypeDTO(overallPeriodTypeRepository.mapRow(rs, "overallPeriod_")),
         begin = rs.getLocalDate("begin_date"),
         end = rs.getLocalDate("end_date"),
      )
   }

   @ReadOnly
   fun findFiscalYears(company: CompanyEntity): List<FiscalYearDTO> {
      logger.info("Fetching Fiscal years")
      return jdbc.query("""
         SELECT
            c1.fiscal_year,
            overallPeriod.id                       AS overallPeriod_id,
            overallPeriod.value                    AS overallPeriod_value,
            overallPeriod.abbreviation             AS overallPeriod_abbreviation,
            overallPeriod.description              AS overallPeriod_description,
            overallPeriod.localization_code        AS overallPeriod_localization_code,
            c1.period_from                         AS begin_date,
            c2.period_to                           AS end_date
         FROM financial_calendar c1
               JOIN financial_calendar c2 ON c1.fiscal_year = c2.fiscal_year AND c1.period = 1 AND c2.period = 12
               JOIN overall_period_type_domain overallPeriod ON overallPeriod.id = c1.overall_period_id
         WHERE c1.company_id = :comp_id AND c1.company_id = c2.company_id
         ORDER BY c1.fiscal_year
      """, mapOf("comp_id" to company.id)
      ) { rs, _ ->
         mapFiscalYearDTO(rs)
      }
   }

   private fun mapRow(
      rs: ResultSet
   ): Pair<Int, Int> {
      return Pair(
         first = rs.getInt("overall_period_id"),
         second = rs.getInt("period")
      )
   }

   private fun mapDate(
      rs: ResultSet
   ): LocalDate {
      return rs.getLocalDate("period_from")
   }
}

