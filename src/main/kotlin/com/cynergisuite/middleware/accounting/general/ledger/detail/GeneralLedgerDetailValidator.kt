package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.FinancialCalendarValidateDatesFilterRequest
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarService
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.CalendarThruDateIsBeforeFrom
import com.cynergisuite.middleware.localization.DatesMustBeWithinCurrentOrNextFiscalYear
import com.cynergisuite.middleware.localization.DatesSelectedMustBeWithinFinancialCalendar
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.SourceCodeDoesNotExist
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class GeneralLedgerDetailValidator @Inject constructor(
   private val financialCalendarService: FinancialCalendarService,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerDetailValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: GeneralLedgerDetailDTO, company: CompanyEntity): GeneralLedgerDetailEntity {
      logger.debug("Validating Create GeneralLedgerDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: UUID, dto: GeneralLedgerDetailDTO, company: CompanyEntity): GeneralLedgerDetailEntity {
      logger.debug("Validating Update GeneralLedgerDetail {}", dto)

      val generalLedgerDetailEntity = generalLedgerDetailRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, generalLedgerDetailEntity)
   }

   @Throws(ValidationException::class)
   fun validateTransfer(id: UUID, dto: GeneralLedgerDetailDTO, company: CompanyEntity): GeneralLedgerDetailEntity {
      logger.debug("Validating Update GeneralLedgerDetail {}", dto)

      val generalLedgerDetailEntity = generalLedgerDetailRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, generalLedgerDetailEntity)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerDetailDTO,
      company: CompanyEntity,
      entity: GeneralLedgerDetailEntity? = null
   ): GeneralLedgerDetailEntity {
      val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
      val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) }
      val source = dto.source?.id?.let { sourceCodeRepository.findOne(it, company) }

      doValidation { errors ->
         // account is not nullable
         account ?: errors.add(ValidationError("account.id", NotFound(dto.account!!.id!!)))

         // profitCenter is not nullable
         profitCenter ?: errors.add(ValidationError("profitCenter.id", NotFound(dto.profitCenter!!.id!!)))

         // source is not nullable
         source ?: errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))
      }

      return GeneralLedgerDetailEntity(entity?.id, dto, account!!, profitCenter!!, source!!)
   }

   fun validatePurgeDetails(purgeDTO: GeneralLedgerDetailPostPurgeDTO, company: CompanyEntity): GeneralLedgerDetailPostPurgeDTO {

      doValidation { errors ->
         val from = purgeDTO.fromDate
         val thru = purgeDTO.thruDate

         //Thru date cannot be before From date
         if (thru.isBefore(from)) {
            errors.add(ValidationError("fromDate", CalendarThruDateIsBeforeFrom(from, thru)))
         }

         //Check that the from date is even in a financial calendar
         if (!financialCalendarService.dateFoundInFinancialCalendar(company, from)) {
            errors.add(ValidationError("fromDate", DatesSelectedMustBeWithinFinancialCalendar(from, thru)))
         }

         //Check that the dates are in the same fiscal year
         val otherDTO = FinancialCalendarValidateDatesFilterRequest(from, thru)
         financialCalendarService.sameFiscalYear(company, otherDTO, "fromDate")

         //Check that the fiscal year is Current or Next
         val fiscalYear = financialCalendarService.fetchByDate(company, from)
         if (fiscalYear == null || (fiscalYear.overallPeriod!!.value != "C" && fiscalYear.overallPeriod!!.value != "N")) {
            errors.add(ValidationError("fromDate", DatesMustBeWithinCurrentOrNextFiscalYear(from, thru)))
         }

         //Check that the source code value is valid
         if (!generalLedgerSourceCodeRepository.exists(purgeDTO.sourceCode, company)) {
            errors.add(ValidationError("sourceCode", SourceCodeDoesNotExist(purgeDTO.sourceCode)))
         }
      }

      return purgeDTO
   }
}
