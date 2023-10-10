package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountStatusTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.AccountIsRequired
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.SelectPercentOrPerUnit
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateRepository
import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class RebateValidator @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val accountStatusTypeRepository: AccountStatusTypeRepository,
   private val rebateTypeRepository: RebateTypeRepository,
   private val rebateRepository: RebateRepository,
   private val accountRepository: AccountRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(RebateValidator::class.java)

   fun validateCreate(dto: RebateDTO, company: CompanyEntity): RebateEntity {
      logger.trace("Validating Create Rebate {}", dto)
      val existingDescription = rebateRepository.findByDescription(dto.description!!, company)
      return doSharedValidation(existingDescription = existingDescription, dto = dto, company = company)
   }

   fun validateUpdate(id: UUID, dto: RebateDTO, company: CompanyEntity): RebateEntity {
      logger.debug("Validating Update Rebate {}", dto)
      val existingDescription = rebateRepository.findByDescription(dto.description!!, company)
      val existingRebate = rebateRepository.findOne(id, company)
      return doSharedValidation(existingDescription = existingDescription, existingRebate = existingRebate, dto, company)
   }

   private fun doSharedValidation(
      existingDescription: RebateEntity? = null,
      existingRebate: RebateEntity? = null,
      dto: RebateDTO, company: CompanyEntity): RebateEntity {
      val generalLedgerDebitAccountId = dto.generalLedgerDebitAccount?.id

      val status = accountStatusTypeRepository.findOne(dto.status!!.value!!)
      val description = dto.description
      val rebate = rebateTypeRepository.findOne(dto.type!!.value)
      val percent = dto.percent
      val amountPerUnit = dto.amountPerUnit
      val accrualIndicator = dto.accrualIndicator
      val generalLedgerDebitAccount = dto.generalLedgerDebitAccount?.id?.let { accountRepository.findOne(it, company) }
      val generalLedgerCreditAccount = dto.generalLedgerCreditAccount!!.id.let { accountRepository.findOne(it!!, company) }
      doValidation { errors ->
         if (dto.vendors != null) {
            for ((i, vendor) in dto.vendors!!.withIndex()) {
               if (vendorRepository.doesNotExist(vendor.id!!)) {
                  errors.add(ValidationError("vendors[$i].id", NotFound(vendor.id!!)))
               }
            }
         }

         status
            ?: errors.add(ValidationError("status.value", NotFound(dto.status!!.value!!)))

         description
            ?: errors.add(ValidationError("description", NotFound(dto.description!!)))

         rebate
            ?: errors.add(ValidationError("rebate.value", NotFound(dto.type!!.value)))

         if ((percent == null && amountPerUnit == null) || (percent != null && amountPerUnit != null)) {
            errors.add(ValidationError("percent, amountPerUnit", SelectPercentOrPerUnit(percent, amountPerUnit)))
         }

         if (generalLedgerDebitAccountId != null && generalLedgerDebitAccount == null) {
            errors.add(ValidationError("generalLedgerDebitAccount.id", NotFound(generalLedgerDebitAccountId)))
         }
         if (accrualIndicator == true && generalLedgerDebitAccount == null) {
            errors.add(ValidationError("generalLedgerDebitAccount", AccountIsRequired(generalLedgerDebitAccount)))
         }

         if (existingRebate == null && existingDescription != null ) {
            errors.add(ValidationError("description", Duplicate(dto.description)))
         }

         if (existingRebate != null && existingDescription != null && existingDescription.id != existingRebate.id)
            errors.add(ValidationError("description", Duplicate(dto.description)))



         generalLedgerCreditAccount
            ?: errors.add(ValidationError("generalLedgerCreditAccount.id", NotFound(dto.generalLedgerCreditAccount!!.id!!)))
      }

      return RebateEntity(
         dto = dto,
         vendors = mutableListOf(),
         status = status!!,
         rebate = rebate!!,
         generalLedgerDebitAccount = generalLedgerDebitAccount,
         generalLedgerCreditAccount = generalLedgerCreditAccount!!
      )
   }
}