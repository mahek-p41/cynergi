package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.MustBeInRangeOf
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableDistributionValidator @Inject constructor(
   private val accountPayableDistributionRepository: AccountPayableDistributionRepository,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionValidator::class.java)

   fun validateCreate(dto: AccountPayableDistributionDTO, company: Company): AccountPayableDistributionEntity {
      logger.debug("Validating Create AccountPayableDistribution {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: Long, dto: AccountPayableDistributionDTO, company: Company): AccountPayableDistributionEntity {
      logger.debug("Validating Update AccountPayableDistribution {}", dto)

      val accountPayableDistributionEntity = accountPayableDistributionRepository.findOne(id, company)

      return doSharedValidation(dto, company, accountPayableDistributionEntity)
   }

   private fun doSharedValidation(dto: AccountPayableDistributionDTO, company: Company, entity: AccountPayableDistributionEntity? = null): AccountPayableDistributionEntity {
      val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) }
      val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
      val percent = dto.percent

      doValidation { errors ->
         profitCenter
            ?: errors.add(ValidationError("profitCenter.id", NotFound(dto.profitCenter!!.id!!)))

         account
            ?: errors.add(ValidationError("account.id", NotFound(dto.account!!.id!!)))

         if ((percent != null) && (percent > BigDecimal.ONE)) {
            errors.add(ValidationError("percent", MustBeInRangeOf("(0, 1]")))
         }
      }

      return AccountPayableDistributionEntity(
         dto,
         profitCenter = profitCenter!!,
         account = account!!
      )
   }
}
