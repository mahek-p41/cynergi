package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.domain.FinancialCalendarValidateDatesFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.LocalDate
import java.util.UUID

@Singleton
class FinancialCalendarService @Inject constructor(
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val financialCalendarValidator: FinancialCalendarValidator,
   private val overallPeriodTypeService: OverallPeriodTypeService
) {

   fun fetchById(id: UUID, company: CompanyEntity): FinancialCalendarDTO? =
      financialCalendarRepository.findOne(id, company)?.let { FinancialCalendarDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<FinancialCalendarDTO> {
      val found = financialCalendarRepository.findAll(company, pageRequest)

      return found.toPage { financialCalendarEntity: FinancialCalendarEntity ->
         FinancialCalendarDTO(financialCalendarEntity)
      }
   }

   fun fetchFiscalYears(company: CompanyEntity): List<FiscalYearDTO> {
      return financialCalendarRepository.findFiscalYears(company)
   }

   fun create(dto: FinancialCalendarDTO, company: CompanyEntity): FinancialCalendarDTO {
      val toCreate = financialCalendarValidator.validateCreate(dto, company)

      return transformEntity(financialCalendarRepository.insert(toCreate, company))
   }

   fun create(date: LocalDate, year: Int, company: CompanyEntity): List<FinancialCalendarDTO> {
      val overallPeriods = overallPeriodTypeService.fetchAll()
      val calList: MutableList<FinancialCalendarEntity> = mutableListOf()
      for (overallPeriod in overallPeriods) {
         for (j in 1..12) {
            val baseDate = date.plusYears(overallPeriod.id - 3L)
            val periodFrom = baseDate.plusMonths(j - 1L)
            val periodTo = periodFrom.plusMonths(1).minusDays(1)
            val fiscalYear = year.plus(overallPeriod.id - 3)
            val dto = FinancialCalendarDTO(
               null,
               overallPeriod = OverallPeriodTypeDTO(overallPeriod),
               period = j,
               periodFrom = periodFrom,
               periodTo = periodTo,
               fiscalYear = fiscalYear,
               generalLedgerOpen = false,
               accountPayableOpen = false
            )

            calList.add(financialCalendarValidator.validateCreate(dto, company))
         }
      }

      val created = calList.map { financialCalendarRepository.insert(it, company) }.toList()
      return created.map { transformEntity(it) }.toList()
   }

   fun update(id: UUID, dto: FinancialCalendarDTO, company: CompanyEntity): FinancialCalendarDTO {
      val toUpdate = financialCalendarValidator.validateUpdate(id, dto, company)

      return transformEntity(financialCalendarRepository.update(toUpdate, company))
   }

   fun openGLAccountsForPeriods(dateRangeDTO: FinancialCalendarDateRangeDTO, company: CompanyEntity) {
      val openAPRangeExists = financialCalendarRepository.openAPDateRangeFound(company)
      if (openAPRangeExists) {
         val openedAP = fetchDateRangeWhenAPIsOpen(company)
         val toBeOpened = financialCalendarValidator.validateOpenGLDates(dateRangeDTO, openedAP, true)
         financialCalendarRepository.openGLAccountsForPeriods(toBeOpened, company)
      } else {
         val openedAP = Pair(LocalDate.now(), LocalDate.now()) //Just to give it something to send, though it won't be used.
         val toBeOpened = financialCalendarValidator.validateOpenGLDates(dateRangeDTO, openedAP, false)
         financialCalendarRepository.openGLAccountsForPeriods(toBeOpened, company)
      }
   }

   fun openAPAccountsForPeriods(dateRangeDTO: FinancialCalendarDateRangeDTO, company: CompanyEntity) {
      val openGLRangeExists = financialCalendarRepository.openGLDateRangeFound(company)
      if (openGLRangeExists) {
         val openedGL = fetchDateRangeWhenGLIsOpen(company)
         val toBeOpened = financialCalendarValidator.validateOpenAPDates(dateRangeDTO, openedGL)
         financialCalendarRepository.openAPAccountsForPeriods(toBeOpened, company)
      } else {
         throw NotFoundException("AP open range cannot be set with no GL open range set")
      }
   }

   fun fetchDateRangeWhenGLIsOpen(company: CompanyEntity): Pair<LocalDate, LocalDate> =
      financialCalendarRepository.findDateRangeWhenGLIsOpen(company)

   fun fetchDateRangeWhenAPIsOpen(company: CompanyEntity): Pair<LocalDate, LocalDate> =
      financialCalendarRepository.findDateRangeWhenAPIsOpen(company)

   fun fetchByDate(company: CompanyEntity, date: LocalDate): FinancialCalendarDTO? =
      financialCalendarRepository.fetchByDate(company, date)?.let { FinancialCalendarDTO(it) }

   fun dateFoundInFinancialCalendar(company: CompanyEntity, date: LocalDate): Boolean {
      val isDateFound = financialCalendarRepository.dateFoundInFinancialCalendar(company, date)

      financialCalendarValidator.validateDateFoundInFinancialCalendar(isDateFound, date)

      return isDateFound
   }

   fun sameFiscalYear(company: CompanyEntity, filterRequest: FinancialCalendarValidateDatesFilterRequest): Boolean {
      return if (filterRequest.fromDate != filterRequest.thruDate) {
         val fromDateOverallPeriodId = financialCalendarRepository.findOverallPeriodIdAndPeriod(company, filterRequest.fromDate!!).first
         val thruDateOverallPeriodId = financialCalendarRepository.findOverallPeriodIdAndPeriod(company, filterRequest.thruDate!!).first
         val sameFiscalYear = fromDateOverallPeriodId == thruDateOverallPeriodId

         financialCalendarValidator.validateSameFiscalYear(sameFiscalYear, filterRequest.fromDate!!, filterRequest.thruDate!!)

         sameFiscalYear
      } else {
         true
      }
   }

   private fun transformEntity(financialCalendarEntity: FinancialCalendarEntity): FinancialCalendarDTO {
      return FinancialCalendarDTO(financialCalendarEntity)
   }
}
