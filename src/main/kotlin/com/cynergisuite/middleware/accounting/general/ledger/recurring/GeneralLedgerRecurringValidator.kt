package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerRecurringValidator @Inject constructor(
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val recurringRepository: GeneralLedgerRecurringRepository,
   private val recurringTypeRepository: GeneralLedgerRecurringTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringValidator::class.java)

   fun validateCreate(dto: GeneralLedgerRecurringDTO, company: CompanyEntity): GeneralLedgerRecurringEntity {
      logger.trace("Validating Create GeneralLedgerRecurring{}", dto)

      dto.lastTransferDate = null

      return doSharedValidation(dto, company, null)
   }

   fun validateUpdate(id: UUID, dto: GeneralLedgerRecurringDTO, company: CompanyEntity): GeneralLedgerRecurringEntity {
      logger.debug("Validating Update GeneralLedgerRecurring{}", dto)

      val existingGeneralLedgerRecurring = recurringRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, existingGeneralLedgerRecurring)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerRecurringDTO,
      company: CompanyEntity,
      existingGeneralLedgerRecurring: GeneralLedgerRecurringEntity?
   ): GeneralLedgerRecurringEntity {
      val source = sourceCodeRepository.findOne(dto.source!!.id!!, company)
      val recurringType = recurringTypeRepository.findOne(dto.type!!.value)

      doValidation { errors ->
         // non-nullable validations
         source ?: errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))
         recurringType ?: errors.add(ValidationError("type.value", NotFound(dto.type!!.value)))
      }

      return GeneralLedgerRecurringEntity(
         existingGeneralLedgerRecurring?.id,
         dto,
         source!!,
         recurringType!!
      )
   }
}
