package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure.GeneralLedgerRecurringDistributionRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.BalanceMustBeZero
import com.cynergisuite.middleware.localization.EndDateBeforeStart
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerRecurringEntriesValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val generalLedgerRecurringDistributionRepository: GeneralLedgerRecurringDistributionRepository,
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository,
   private val recurringTypeRepository: GeneralLedgerRecurringTypeRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringEntriesValidator::class.java)

   fun validateCreate(dto: GeneralLedgerRecurringEntriesDTO, company: CompanyEntity): GeneralLedgerRecurringEntriesEntity {
      logger.trace("Validating Create {}", dto)

      return doSharedValidation(dto, company, null, null)
   }

   fun validateUpdate(glRecurringId: UUID, dto: GeneralLedgerRecurringEntriesDTO, company: CompanyEntity): GeneralLedgerRecurringEntriesEntity {
      logger.debug("Validating Update {}", dto)

      val existingGlRecurring = generalLedgerRecurringRepository.findOne(glRecurringId, company) ?: throw NotFoundException(glRecurringId)

      val existingGlDistributions = generalLedgerRecurringDistributionRepository.findAllByRecurringId(glRecurringId, company)

      return doSharedValidation(dto, company, existingGlRecurring, existingGlDistributions)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerRecurringEntriesDTO,
      company: CompanyEntity,
      existingGlRecurring: GeneralLedgerRecurringEntity?,
      existingGlDistributions: List<GeneralLedgerRecurringDistributionEntity>?
   ): GeneralLedgerRecurringEntriesEntity {
      val glRecurringSourceCode = sourceCodeRepository.findOne(dto.generalLedgerRecurring!!.source!!.id!!, company)
      val glRecurringType = recurringTypeRepository.findOne(dto.generalLedgerRecurring!!.type!!.value)
      val beginDate = dto.generalLedgerRecurring!!.beginDate
      val endDate = dto.generalLedgerRecurring!!.endDate

      val glDistributionAccts = mutableListOf<AccountEntity>()
      val glDistributionProfitCenters = mutableListOf<Store>()
      dto.generalLedgerRecurringDistributions.forEach {
         glDistributionAccts.add(accountRepository.findOne(it.generalLedgerDistributionAccount!!.id!!, company)!!)
         glDistributionProfitCenters.add(storeRepository.findOne(it.generalLedgerDistributionProfitCenter!!.id!!, company)!!)
      }

      doValidation { errors ->
         // GL recurring validations
         glRecurringSourceCode ?: errors.add(ValidationError("generalLedgerRecurring.source.id", NotFound(dto.generalLedgerRecurring!!.source!!.id!!)))
         glRecurringType ?: errors.add(ValidationError("generalLedgerRecurring.type.value", NotFound(dto.generalLedgerRecurring!!.type!!.value)))

         if (beginDate!!.isAfter(endDate)) {
            errors.add(ValidationError("generalLedgerRecurring.beginDate", EndDateBeforeStart(endDate.toString(), beginDate.toString())))
         }

         // GL recurring distribution validations
         glDistributionAccts.forEachIndexed { index, account ->
            account ?: errors.add(ValidationError("generalLedgerDistributions[index].generalLedgerDistributionAccount.id", NotFound(account.id!!)))
         }
         glDistributionProfitCenters.forEachIndexed { index, profitCenter ->
            profitCenter ?: errors.add(ValidationError("generalLedgerDistributions[index].generalLedgerDistributionProfitCenter.id", NotFound(profitCenter.myId())))
         }

         // balance must be zero before records can be inserted/updated
         if (dto.balance != BigDecimal.ZERO) {
            errors.add(ValidationError("balance", BalanceMustBeZero(dto.balance!!)))
         }
      }

      return GeneralLedgerRecurringEntriesEntity(dto, existingGlRecurring?.id, glRecurringSourceCode!!, glRecurringType!!, existingGlDistributions, glDistributionAccts, glDistributionProfitCenters)
   }
}
