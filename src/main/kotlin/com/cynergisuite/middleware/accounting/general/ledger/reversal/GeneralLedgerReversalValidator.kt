package com.cynergisuite.middleware.accounting.general.ledger.reversal

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class GeneralLedgerReversalValidator @Inject constructor(
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val generalLedgerReversalRepository: GeneralLedgerReversalRepository,
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalValidator::class.java)

   fun validateCreate(dto: GeneralLedgerReversalDTO, company: CompanyEntity): GeneralLedgerReversalEntity {
      logger.trace("Validating Save GeneralLedgerReversal {}", dto)

      return doSharedValidation(dto, company, null)
   }

   fun validateUpdate(id: UUID, dto: GeneralLedgerReversalDTO, company: CompanyEntity): GeneralLedgerReversalEntity {
      logger.trace("Validating Update GeneralLedgerReversal {}", dto)

      val existingReversal = generalLedgerReversalRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, existingReversal)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerReversalDTO,
      company: CompanyEntity,
      existingReversal: GeneralLedgerReversalEntity?
   ): GeneralLedgerReversalEntity {
      val source = generalLedgerSourceCodeRepository.findOne(dto.source!!.id!!, company)

      doValidation { errors ->
         source ?: errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))
      }

      return GeneralLedgerReversalEntity(existingReversal?.id, dto, source!!)
   }
}
