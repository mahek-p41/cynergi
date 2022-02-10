package com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.infrastructure.GeneralLedgerReversalDistributionRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerReversalDistributionValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val generalLedgerReversalRepository: GeneralLedgerReversalRepository,
   private val generalLedgerReversalDistributionRepository: GeneralLedgerReversalDistributionRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalDistributionValidator::class.java)

   fun validateCreate(dto: GeneralLedgerReversalDistributionDTO, company: CompanyEntity): GeneralLedgerReversalDistributionEntity {
      logger.trace("Validating Create {}", dto)

      return doSharedValidation(dto, company, null)
   }

   fun validateUpdate(id: UUID, dto: GeneralLedgerReversalDistributionDTO, company: CompanyEntity): GeneralLedgerReversalDistributionEntity {
      logger.debug("Validating Update {}", dto)

      val existingGlReversalDistribution = generalLedgerReversalDistributionRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, existingGlReversalDistribution)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerReversalDistributionDTO,
      company: CompanyEntity,
      existingGlReversalDistribution: GeneralLedgerReversalDistributionEntity?
   ): GeneralLedgerReversalDistributionEntity {
      val glReversal = generalLedgerReversalRepository.findOne(dto.generalLedgerReversal!!.id!!, company)
      val glReversalDistAcct = accountRepository.findOne(dto.generalLedgerReversalDistributionAccount!!.id!!, company)
      val glReversalDistProfitCenter = storeRepository.findOne(dto.generalLedgerReversalDistributionProfitCenter!!.id!!, company)

      doValidation { errors ->
         glReversal ?: errors.add(ValidationError("generalLedgerReversal.id", NotFound(dto.generalLedgerReversal!!.id!!)))

         glReversalDistAcct ?: errors.add(ValidationError("generalLedgerReversalDistributionAccount.id", NotFound(dto.generalLedgerReversalDistributionAccount!!.id!!)))

         glReversalDistProfitCenter ?: errors.add(ValidationError("generalLedgerReversalDistributionProfitCenter.id", NotFound(dto.generalLedgerReversalDistributionProfitCenter!!.id!!)))
      }

      return GeneralLedgerReversalDistributionEntity(existingGlReversalDistribution?.id, dto, glReversal!!, glReversalDistAcct!!, glReversalDistProfitCenter!!)
   }
}
