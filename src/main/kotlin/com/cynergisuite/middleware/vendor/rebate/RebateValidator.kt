package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountStatusTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.MustBeInRangeOf
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.SelectPercentOrPerUnit
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateTypeRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RebateValidator @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val accountStatusTypeRepository: AccountStatusTypeRepository,
   private val rebateTypeRepository: RebateTypeRepository,
   private val accountRepository: AccountRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(RebateValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: RebateDTO, company: Company): RebateEntity {
      logger.trace("Validating Create Rebate {}", dto)

      return doSharedValidation(dto, company)
   }

   @Throws(ValidationException::class, NotFoundException::class)
   fun validateUpdate(id: Long, dto: RebateDTO, company: Company): RebateEntity {
      logger.debug("Validating Update Rebate {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: RebateDTO, company: Company): RebateEntity {
      val generalLedgerDebitAccountId = dto.generalLedgerDebitAccount?.id
      val generalLedgerCreditAccountId = dto.generalLedgerCreditAccount?.id

      val vendor = vendorRepository.findOne(dto.vendor!!.id!!, company)
      val status = accountStatusTypeRepository.findOne(dto.status!!.value!!)
      val description = dto.description
      val rebate = rebateTypeRepository.findOne(dto.rebate!!.value)
      val percent = dto.percent
      val amountPerUnit = dto.amountPerUnit
      val accrualIndicator = dto.accrualIndicator
      val generalLedgerDebitAccount = dto.generalLedgerDebitAccount?.id?.let { accountRepository.findOne(it, company) }
      val generalLedgerCreditAccount = dto.generalLedgerCreditAccount?.id?.let { accountRepository.findOne(it, company) }

      doValidation { errors ->
         vendor
            ?: errors.add(ValidationError("vendor.id", NotFound(dto.vendor!!.id!!)))

         status
            ?: errors.add(ValidationError("status.value", NotFound(dto.status!!.value!!)))

         description
            ?: errors.add(ValidationError("description", NotFound(dto.description!!)))

         rebate
            ?: errors.add(ValidationError("rebate.value", NotFound(dto.rebate!!.value)))

         if ((percent != null) && (percent > BigDecimal.ONE)) {
            errors.add(ValidationError("percent", MustBeInRangeOf("(0, 1]")))
         }

         if ((percent == null && amountPerUnit == null) || (percent != null && amountPerUnit != null)) {
            errors.add(ValidationError("percent, amountPerUnit", SelectPercentOrPerUnit(percent, amountPerUnit)))
         }

         accrualIndicator
            ?: errors.add(ValidationError("accrualIndicator", NotFound(dto.accrualIndicator!!)))

         if (generalLedgerDebitAccountId != null && generalLedgerDebitAccount == null) {
            errors.add(ValidationError("generalLedgerDebitAccount.id", NotFound(generalLedgerDebitAccountId)))
         }

         if (generalLedgerCreditAccountId != null && generalLedgerCreditAccount == null) {
            errors.add(ValidationError("generalLedgerCreditAccount.id", NotFound(generalLedgerCreditAccountId)))
         }
      }

      return RebateEntity(
         dto = dto,
         vendor = vendor!!,
         status = status!!,
         rebate = rebate!!,
         generalLedgerDebitAccount = generalLedgerDebitAccount,
         generalLedgerCreditAccount = generalLedgerCreditAccount
      )
   }
}
