package com.cynergisuite.middleware.accounting.financial.calendar

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.*
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.xml.datatype.DatatypeConstants.DAYS

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

   fun validateDateFoundInFinancialCalendar(isDateFound: Boolean, date: LocalDate) {
      if (!isDateFound) {
         throw NotFoundException(date)
      }
   }

   fun validateSameFiscalYear(sameFiscalYear: Boolean, startingDate: LocalDate, endingDate: LocalDate) =
      doValidation { errors ->
         if (!sameFiscalYear) errors.add(ValidationError("startingDate", DatesMustBeInSameFiscalYear(startingDate, endingDate)))
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

   fun validateOpenGLDates(dateRangeDTO: FinancialCalendarDateRangeDTO, openedAP: Pair<LocalDate, LocalDate>?): FinancialCalendarDateRangeDTO {

      doValidation { errors ->
         val from = dateRangeDTO.periodFrom
         val thru = dateRangeDTO.periodTo!!.plusMonths(1).minusDays(1)
         val daysBetween = ChronoUnit.DAYS.between(from, thru)

         //Thru date cannot be before From date
         if (thru != null && from != null && thru.isBefore(from)) {
            errors.add(ValidationError("from", CalendarThruDateIsBeforeFrom(from, thru)))
         }

         //Total range requested cannot span more than 2 years
         if (daysBetween > 731) {
            errors.add(ValidationError("from", CalendarDatesSpanMoreThanTwoYears(from!!, thru!!)))
         }

         //If there is no existing AP range, we do not need to compare anything here. Otherwise, the new GL range must
         //encompass the existing AP range.
         if (thru != null && from != null && openedAP != null && (openedAP.first < from || openedAP.second > thru)) {
            errors.add(ValidationError("from", GLDatesSelectedOutsideAPDatesSet(from, thru, openedAP.first, openedAP.second)))
         }
      }

      return dateRangeDTO
   }

   fun validateOpenAPDates(dateRangeDTO: FinancialCalendarDateRangeDTO, openedGL: Pair<LocalDate, LocalDate>?): FinancialCalendarDateRangeDTO {

      doValidation { errors ->
         val from = dateRangeDTO.periodFrom
         val thru = dateRangeDTO.periodTo!!.plusMonths(1).minusDays(1)
         val daysBetween = ChronoUnit.DAYS.between(from, thru)

         //Thru date cannot be before From date
         if (thru != null && from != null && thru.isBefore(from)) {
            errors.add(ValidationError("from", CalendarThruDateIsBeforeFrom(from, thru)))
         }

         //Total range requested cannot span more than 2 years
         if (daysBetween > 731) {
            errors.add(ValidationError("from", CalendarDatesSpanMoreThanTwoYears(from!!, thru!!)))
         }

         //If all dates exist, the AP range requested must fit within the existing GL range
         if (thru != null && from != null && openedGL?.first != null && (openedGL.first > from || openedGL.second.plusMonths(1).minusDays(1) < thru)) {
            errors.add(ValidationError("from", APDatesSelectedOutsideGLDatesSet(from, thru, openedGL.first, openedGL.second)))
         }

         if (openedGL == null) {
            errors.add(ValidationError("from", NoGLDatesSet()))
         }
      }

      return dateRangeDTO
   }
}
