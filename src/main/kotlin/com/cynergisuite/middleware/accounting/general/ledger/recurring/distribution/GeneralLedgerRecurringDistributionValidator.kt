package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure.GeneralLedgerRecurringDistributionRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.company.Company
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
class GeneralLedgerRecurringDistributionValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository,
   private val generalLedgerRecurringDistributionRepository: GeneralLedgerRecurringDistributionRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerRecurringDistributionValidator::class.java)

   fun validateCreate(dto: GeneralLedgerRecurringDistributionDTO, company: Company): GeneralLedgerRecurringDistributionEntity {
      logger.trace("Validating Create {}", dto)

      return doSharedValidation(dto, company, null)
   }

   fun validateUpdate(id: UUID, dto: GeneralLedgerRecurringDistributionDTO, company: Company): GeneralLedgerRecurringDistributionEntity {
      logger.debug("Validating Update {}", dto)

      val existingGlRecurringDistribution = generalLedgerRecurringDistributionRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, existingGlRecurringDistribution)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerRecurringDistributionDTO,
      company: Company,
      existingGlRecurringDistribution: GeneralLedgerRecurringDistributionEntity?
   ): GeneralLedgerRecurringDistributionEntity {
      val glRecurring = generalLedgerRecurringRepository.findOne(dto.generalLedgerRecurring!!.id!!, company)
      val glDistributionAcct = accountRepository.findOne(dto.generalLedgerDistributionAccount!!.id!!, company)
      val glDistributionProfitCenter = storeRepository.findOne(dto.generalLedgerDistributionProfitCenter!!.id!!, company)

      doValidation { errors ->
         glRecurring ?: errors.add(ValidationError("generalLedgerRecurring.id", NotFound(dto.generalLedgerRecurring!!.id!!)))

         glDistributionAcct ?: errors.add(ValidationError("generalLedgerDistributionAccount.id", NotFound(dto.generalLedgerDistributionAccount!!.id!!)))

         glDistributionProfitCenter ?: errors.add(ValidationError("generalLedgerDistributionProfitCenter.id", NotFound(dto.generalLedgerDistributionProfitCenter!!.id!!)))
      }

      return GeneralLedgerRecurringDistributionEntity(existingGlRecurringDistribution?.id, dto, glRecurring!!, glDistributionAcct!!)
   }
}
