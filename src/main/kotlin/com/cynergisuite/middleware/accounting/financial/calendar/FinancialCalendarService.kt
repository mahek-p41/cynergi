package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeService
import com.cynergisuite.middleware.company.CompanyEntity
import java.sql.Date
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.streams.toList

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

   fun create(dto: FinancialCalendarDTO, company: CompanyEntity): FinancialCalendarDTO {
      val toCreate = financialCalendarValidator.validateCreate(dto, company)

      return transformEntity(financialCalendarRepository.insert(toCreate, company))
   }

   fun create(date: LocalDate, company: CompanyEntity): List<FinancialCalendarDTO> {
      val overallPeriods = overallPeriodTypeService.fetchAll()
      var calList: MutableList<FinancialCalendarEntity> = mutableListOf()
      for(overallPeriod in overallPeriods) {
         for(j in 1..12) {
            var baseDate = date.plusYears(overallPeriod.id - 3L)
            var periodFrom = baseDate.plusMonths(j-1L)
            var periodTo = periodFrom.plusMonths(1)
            var currentYear = date.year
            var fiscalYear = currentYear.plus(overallPeriod.id - 3)
            val dto = FinancialCalendarDTO(
               null,
               overallPeriod = OverallPeriodTypeDTO(overallPeriod),
               period = j,
               periodFrom = periodFrom,
               periodTo = periodTo,
               fiscalYear =  fiscalYear,
               generalLedgerOpen = false,
               accountPayableOpen = false
            )

            calList.add(financialCalendarValidator.validateCreate(dto, company))
         }
      }

      val created = calList.map{financialCalendarRepository.insert(it, company)}.toList()
      return created.map{transformEntity(it)}.toList()
   }

   fun update(id: UUID, dto: FinancialCalendarDTO, company: CompanyEntity): FinancialCalendarDTO {
      val toUpdate = financialCalendarValidator.validateUpdate(id, dto, company)

      return transformEntity(financialCalendarRepository.update(toUpdate, company))
   }

   fun openGLAccountsForPeriods(dateRangeDTO: FinancialCalendarDateRangeDTO, company: CompanyEntity) =
      financialCalendarRepository.openGLAccountsForPeriods(dateRangeDTO, company)

   fun openAPAccountsForPeriods(dateRangeDTO: FinancialCalendarDateRangeDTO, company: CompanyEntity) =
      financialCalendarRepository.openAPAccountsForPeriods(dateRangeDTO, company)

   private fun transformEntity(financialCalendarEntity: FinancialCalendarEntity): FinancialCalendarDTO {
      return FinancialCalendarDTO(financialCalendarEntity)
   }
}
