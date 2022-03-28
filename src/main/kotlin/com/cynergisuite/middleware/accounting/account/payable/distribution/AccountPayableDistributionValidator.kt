package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.MustBeInRangeOf
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.PercentTotalGreaterThan100
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.util.UUID

@Singleton
class AccountPayableDistributionValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val accountPayableDistributionRepository: AccountPayableDistributionRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionValidator::class.java)

   fun validateCreate(dto: AccountPayableDistributionDTO, company: CompanyEntity): AccountPayableDistributionEntity {
      logger.debug("Validating Create AccountPayableDistribution {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: AccountPayableDistributionDTO, company: CompanyEntity): AccountPayableDistributionEntity {
      logger.debug("Validating Update AccountPayableDistribution {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateBulkUpdate(dto: List<AccountPayableDistributionDTO>, company: CompanyEntity): List<AccountPayableDistributionEntity> {
      logger.debug("Validating Bulk Update AccountPayableDistribution {}", dto)

      return doBulkValidation(dto, company)
   }

   private fun doSharedValidation(dto: AccountPayableDistributionDTO, company: CompanyEntity): AccountPayableDistributionEntity {
      val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) } // FIXME change to loading using the id provided via the URL on update
      val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
      val percent = dto.percent
      val percentTotal = accountPayableDistributionRepository.percentTotalForGroup(company, dto.name!!)

      doValidation { errors ->
         profitCenter
            ?: errors.add(ValidationError("profitCenter.id", NotFound(dto.profitCenter!!.id!!)))

         account
            ?: errors.add(ValidationError("account.id", NotFound(dto.account!!.id!!)))

         if ((percent != null) && (percent < BigDecimal.ZERO || percent > ONE)) {
            errors.add(ValidationError("percent", MustBeInRangeOf("[0, 1]")))
         }

         if (percentTotal > ONE) {
            errors.add(ValidationError("percent", PercentTotalGreaterThan100(percent!!)))
         }
      }

      return AccountPayableDistributionEntity(
         dto,
         profitCenter = profitCenter!!,
         account = account!!
      )
   }

   private fun doBulkValidation(dtoList: List<AccountPayableDistributionDTO>, company: CompanyEntity): List<AccountPayableDistributionEntity> {
      val percentTotal = dtoList.sumOf { dto -> dto.percent!! }
      val updateEntities : MutableList<AccountPayableDistributionEntity> = mutableListOf()

      for (dto in dtoList) {
         val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) }
         val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
         val percent = dto.percent

         doValidation { errors ->
            profitCenter
               ?: errors.add(ValidationError("profitCenter.id", NotFound(dtoList[0].profitCenter!!.id!!)))

            account
               ?: errors.add(ValidationError("account.id", NotFound(dtoList[0].account!!.id!!)))

            if ((percent != null) && (percent < BigDecimal.ZERO || percent > ONE)) {
               errors.add(ValidationError("percent", MustBeInRangeOf("[0, 1]")))
            }

            if (percentTotal > ONE) {
               errors.add(ValidationError("percent", PercentTotalGreaterThan100(percent!!)))
            }
         }

         updateEntities.add(AccountPayableDistributionEntity(dto, profitCenter!!, account!!))
      }

      return updateEntities

   }
}
