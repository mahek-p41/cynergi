package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionEntity
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.infrastructure.GeneralLedgerReversalDistributionRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.infrastructure.GeneralLedgerReversalRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.BalanceMustBeZero
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerReversalEntryValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val generalLedgerReversalDistributionRepository: GeneralLedgerReversalDistributionRepository,
   private val generalLedgerReversalRepository: GeneralLedgerReversalRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerReversalEntryValidator::class.java)

   fun validateCreate(dto: GeneralLedgerReversalEntryDTO, company: CompanyEntity): GeneralLedgerReversalEntryEntity {
      logger.trace("Validating Create {}", dto)

      return doSharedValidation(dto, company, null, null)
   }

   fun validateUpdate(glReversalId: UUID, dto: GeneralLedgerReversalEntryDTO, company: CompanyEntity): GeneralLedgerReversalEntryEntity {
      logger.debug("Validating Update {}", dto)

      val existingGlReversal = generalLedgerReversalRepository.findOne(glReversalId, company) ?: throw NotFoundException(glReversalId)

      val existingGlDistributions = generalLedgerReversalDistributionRepository.findAllByReversalId(glReversalId, company)

      return doSharedValidation(dto, company, existingGlReversal, existingGlDistributions)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerReversalEntryDTO,
      company: CompanyEntity,
      existingGlReversal: GeneralLedgerReversalEntity?,
      existingGlDistributions: List<GeneralLedgerReversalDistributionEntity>?
   ): GeneralLedgerReversalEntryEntity {
      val glReversalSourceCode = sourceCodeRepository.findOne(dto.generalLedgerReversal!!.source!!.id!!, company)
      val glReversalDistAccts = mutableListOf<AccountEntity>()
      val glReversalDistProfitCenters = mutableListOf<Store>()

      doValidation { errors ->
         // GL reversal validation
         glReversalSourceCode ?: errors.add(ValidationError("generalLedgerReversal.source.id", NotFound(dto.generalLedgerReversal!!.source!!.id!!)))

         // GL reversal distribution validations
         dto.generalLedgerReversalDistributions.forEach {
            accountRepository.findOne(it.generalLedgerReversalDistributionAccount!!.id!!, company) ?: errors.add(ValidationError("generalLedgerReversalDistributions[index].generalLedgerReversalDistributionAccount.id", NotFound(it.generalLedgerReversalDistributionAccount!!.id!!)))
            storeRepository.findOne(it.generalLedgerReversalDistributionProfitCenter!!.id!!, company) ?: errors.add(
               ValidationError("generalLedgerReversalDistributions[index].generalLedgerReversalDistributionProfitCenter.id", NotFound(it.generalLedgerReversalDistributionProfitCenter!!.id!!))
            )
         }

         // balance must be zero before records can be inserted/updated
         if (dto.balance != BigDecimal.ZERO) {
            errors.add(ValidationError("balance", BalanceMustBeZero(dto.balance!!)))
         }
      }

      dto.generalLedgerReversalDistributions.forEach {
         glReversalDistAccts.add(accountRepository.findOne(it.generalLedgerReversalDistributionAccount!!.id!!, company)!!)
         glReversalDistProfitCenters.add(storeRepository.findOne(it.generalLedgerReversalDistributionProfitCenter!!.id!!, company)!!)
      }

      return GeneralLedgerReversalEntryEntity(dto, existingGlReversal?.id, glReversalSourceCode!!, existingGlDistributions, glReversalDistAccts, glReversalDistProfitCenters)
   }
}
