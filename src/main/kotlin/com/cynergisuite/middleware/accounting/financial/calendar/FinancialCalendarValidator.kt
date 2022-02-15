package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.Duplicate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class FinancialCalendarValidator @Inject constructor(
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(FinancialCalendarValidator::class.java)

   fun validateCreate(dto: FinancialCalendarDTO, company: CompanyEntity): FinancialCalendarEntity {
      logger.trace("Validating Create Financial Calendar{}", dto)
      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: FinancialCalendarDTO, company: CompanyEntity): FinancialCalendarEntity {
      logger.debug("Validating Update Financial Calendar{}", dto)

      val existingFinancialCalendar = financialCalendarRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, existingFinancialCalendar)
   }

   private fun doSharedValidation(dto: FinancialCalendarDTO, company: CompanyEntity, existingFinancialCalendar: FinancialCalendarEntity? = null): FinancialCalendarEntity {
      val overallPeriodEntity = overallPeriodTypeRepository.findOne(dto.overallPeriod!!.value) ?: throw NotFoundException(dto.overallPeriod!!.value)
      val financialCalendarExists = financialCalendarRepository.exists(company, overallPeriodEntity.id, dto.period!!)

      doValidation { errors ->
         if ((existingFinancialCalendar == null && financialCalendarExists) || (existingFinancialCalendar != null && existingFinancialCalendar.id != dto.id)) errors.add(ValidationError("id", Duplicate(dto.id)))
      }

      return FinancialCalendarEntity(
         dto,
         overallPeriodEntity
      )
   }
}
