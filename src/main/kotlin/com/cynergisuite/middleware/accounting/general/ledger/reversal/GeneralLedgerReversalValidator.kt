package com.cynergisuite.middleware.accounting.general.ledger.reversal

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerReversalValidator @Inject constructor(
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalValidator::class.java)

   fun validateCreate(dto: GeneralLedgerReversalDTO, company: Company): GeneralLedgerReversalEntity {
      logger.trace("Validating Save GeneralLedgerReversal {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(dto: GeneralLedgerReversalDTO, company: Company): GeneralLedgerReversalEntity {
      logger.trace("Validating Update GeneralLedgerReversal {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: GeneralLedgerReversalDTO, company: Company): GeneralLedgerReversalEntity {
      val source = generalLedgerSourceCodeRepository.findOne(dto.source!!.id!!, company)
      val generalLedgerDetail = generalLedgerDetailRepository.findOne(dto.generalLedgerDetail!!.id!!, company)

      doValidation { errors ->
         source ?: errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))

         generalLedgerDetail ?: errors.add(ValidationError("generalLedgerDetail.id", NotFound(dto.generalLedgerDetail!!.id!!)))
      }

      return GeneralLedgerReversalEntity(dto, source!!, generalLedgerDetail!!)
   }
}
