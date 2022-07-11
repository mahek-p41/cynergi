package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionDetailRepository
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionTemplateRepository
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
class AccountPayableDistributionDetailValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val accountPayableDistributionDetailRepository: AccountPayableDistributionDetailRepository,
   private val accountPayableDistributionTemplateRepository: AccountPayableDistributionTemplateRepository,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionDetailValidator::class.java)

   fun validateCreate(dto: AccountPayableDistributionDetailDTO, company: CompanyEntity): AccountPayableDistributionDetailEntity {
      logger.debug("Validating Create AccountPayableDistributionDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: AccountPayableDistributionDetailDTO, company: CompanyEntity): AccountPayableDistributionDetailEntity {
      logger.debug("Validating Update AccountPayableDistributionDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateBulkUpdate(dto: List<AccountPayableDistributionDetailDTO>, company: CompanyEntity): List<AccountPayableDistributionDetailEntity> {
      logger.debug("Validating Bulk Update AccountPayableDistributionDetail {}", dto)

      return doBulkValidation(dto, company)
   }

   private fun doSharedValidation(dto: AccountPayableDistributionDetailDTO, company: CompanyEntity): AccountPayableDistributionDetailEntity {
      val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) } // FIXME change to loading using the id provided via the URL on update
      val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
      val percent = dto.percent
      val percentTotal = accountPayableDistributionDetailRepository.percentTotalForGroup(dto, company)
      val distributionTemplate = accountPayableDistributionTemplateRepository.findOne(dto.distributionTemplate!!.id!!, company)

      doValidation { errors ->
         profitCenter
            ?: errors.add(ValidationError("profitCenter.id", NotFound(dto.profitCenter!!.id!!)))

         account
            ?: errors.add(ValidationError("account.id", NotFound(dto.account!!.id!!)))

         distributionTemplate
            ?: errors.add(ValidationError("distributionTemplate.id", NotFound(dto.distributionTemplate!!.id!!)))

         if ((percent != null) && (percent < BigDecimal.ZERO || percent > ONE)) {
            errors.add(ValidationError("percent", MustBeInRangeOf("[0, 1]")))
         }

         if (percentTotal > ONE) {
            errors.add(ValidationError("percent", PercentTotalGreaterThan100(percent!!)))
         }
      }

      return AccountPayableDistributionDetailEntity(
         dto,
         profitCenter = profitCenter!!,
         account = account!!,
         distributionTemplate = distributionTemplate!!
      )
   }

   private fun doBulkValidation(dtoList: List<AccountPayableDistributionDetailDTO>, company: CompanyEntity): List<AccountPayableDistributionDetailEntity> {
      val percentTotal = dtoList.sumOf { dto -> dto.percent!! }
      val updateEntities: MutableList<AccountPayableDistributionDetailEntity> = mutableListOf()

      for (dto in dtoList) {
         val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) }
         val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
         val percent = dto.percent
         val distributionTemplate = accountPayableDistributionTemplateRepository.findOne(dto.distributionTemplate!!.id!!, company)

         doValidation { errors ->
            profitCenter
               ?: errors.add(ValidationError("profitCenter.id", NotFound(dtoList[0].profitCenter!!.id!!)))

            account
               ?: errors.add(ValidationError("account.id", NotFound(dtoList[0].account!!.id!!)))

            distributionTemplate
               ?: errors.add(ValidationError("distributionTemplate.id", NotFound(dto.distributionTemplate!!.id!!)))

            if ((percent != null) && (percent < BigDecimal.ZERO || percent > ONE)) {
               errors.add(ValidationError("percent", MustBeInRangeOf("[0, 1]")))
            }

            if (percentTotal > ONE) {
               errors.add(ValidationError("percent", PercentTotalGreaterThan100(percent!!)))
            }
         }

         updateEntities.add(AccountPayableDistributionDetailEntity(dto, profitCenter!!, account!!, distributionTemplate!!))
      }

      return updateEntities
   }
}
