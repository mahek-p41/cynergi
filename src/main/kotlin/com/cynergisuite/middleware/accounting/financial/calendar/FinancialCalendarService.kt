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

//   fun create(dto: FinancialCalendarDTO, company: CompanyEntity): FinancialCalendarDTO {
//      val toCreate = financialCalendarValidator.validateCreate(dto, company)
//
//      return transformEntity(financialCalendarRepository.insert(toCreate, company))
//   }

   fun create(date: LocalDate, company: CompanyEntity): List<FinancialCalendarDTO> {
      var currentYear = date.year
      val overallPeriods = overallPeriodTypeService.fetchAll()
      var fromDate = date
      var toDate = date.plusMonths(1).minusDays(1)
      var calList: MutableList<FinancialCalendarDTO> = mutableListOf<FinancialCalendarDTO>()
      for(i in 1..4) {
         for(j in 1..12) {

            val dto = FinancialCalendarDTO(
               null,
               overallPeriod = OverallPeriodTypeDTO(overallPeriods[i-1]),
               period = j,
               periodFrom = fromDate,
               periodTo = toDate,
               fiscalYear = currentYear - 2,
               generalLedgerOpen = false,
               accountPayableOpen = false
            )
            calList.add(dto)
            fromDate = fromDate.plusMonths(1)
            toDate = toDate.plusMonths(1)
         }
         currentYear = currentYear.plus(1)
      }



      val toCreate = financialCalendarValidator.validateCreate(calList, company)

      return transformEntity(financialCalendarRepository.insertFinancialCalendar(toCreate, company))
   }

//   fun update(id: UUID, dto: FinancialCalendarDTO, company: CompanyEntity): FinancialCalendarDTO {
//      val toUpdate = financialCalendarValidator.validateUpdate(id, dto, company)
//
//      return transformEntity(financialCalendarRepository.update(toUpdate, company))
//   }

   fun openGLAccountsForPeriods(dateRangeDTO: FinancialCalendarDateRangeDTO, company: CompanyEntity) =
      financialCalendarRepository.openGLAccountsForPeriods(dateRangeDTO, company)

   fun openAPAccountsForPeriods(dateRangeDTO: FinancialCalendarDateRangeDTO, company: CompanyEntity) =
      financialCalendarRepository.openAPAccountsForPeriods(dateRangeDTO, company)

   private fun transformEntity(financialCalendarEntity: List<FinancialCalendarEntity>): List<FinancialCalendarDTO> {
      return financialCalendarEntity.stream().map {entity -> FinancialCalendarDTO(entity)}.toList()
   }
}
