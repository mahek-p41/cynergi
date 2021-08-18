package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialCalendarService @Inject constructor(
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val financialCalendarValidator: FinancialCalendarValidator
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
