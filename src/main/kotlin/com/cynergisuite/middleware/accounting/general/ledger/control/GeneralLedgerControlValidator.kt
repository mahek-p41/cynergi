package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure.GeneralLedgerControlRepository
import com.cynergisuite.middleware.localization.ConfigAlreadyExist
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.ToDateBeforeFrom
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class GeneralLedgerControlValidator @Inject constructor(
   private val accountRepository: AccountRepository,
   private val generalLedgerControlRepository: GeneralLedgerControlRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerControlValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid dto: GeneralLedgerControlDTO, company: Company): GeneralLedgerControlEntity {
      logger.debug("Validating Create GeneralLedgerControl {}", dto)

      return doSharedValidation(dto, company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(dto: GeneralLedgerControlDTO, company: Company): GeneralLedgerControlEntity {
      logger.debug("Validating Update GeneralLedgerControl {}", dto)

      val generalLedgerControlEntity = generalLedgerControlRepository.findOne(company) ?: throw NotFoundException(company.myId()!!)

      return doSharedValidation(dto, company, generalLedgerControlEntity)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerControlDTO,
      company: Company,
      entity: GeneralLedgerControlEntity? = null
   ): GeneralLedgerControlEntity {

      val periodFrom = dto.periodFrom!!
      val periodTo = dto.periodTo!!
      val defaultAccountPayableAccount = dto.defaultAccountPayableAccount?.id?.let { accountRepository.findOne(it, company) }
      val defaultAccountPayableDiscountAccount = dto.defaultAccountPayableDiscountAccount?.id?.let { accountRepository.findOne(it, company) }
      val defaultAccountReceivableAccount = dto.defaultAccountReceivableAccount?.id?.let { accountRepository.findOne(it, company) }
      val defaultAccountReceivableDiscountAccount = dto.defaultAccountReceivableDiscountAccount?.id?.let { accountRepository.findOne(it, company) }
      val defaultAccountMiscInventoryAccount = dto.defaultAccountMiscInventoryAccount?.id?.let { accountRepository.findOne(it, company) }
      val defaultAccountSerializedInventoryAccount = dto.defaultAccountSerializedInventoryAccount?.id?.let { accountRepository.findOne(it, company) }
      val defaultAccountUnbilledInventoryAccount = dto.defaultAccountUnbilledInventoryAccount?.id?.let { accountRepository.findOne(it, company) }
      val defaultAccountFreightAccount = dto.defaultAccountFreightAccount?.id?.let { accountRepository.findOne(it, company) }

      doValidation { errors ->
         if (generalLedgerControlRepository.exists(company) && entity == null) { // tried to create a General Ledger Control record, but one already existed, basically they did a post when they should have done a put.
            errors.add(ValidationError("company", ConfigAlreadyExist(company.myDataset())))
         }

         if (periodTo.isBefore(periodFrom)) {
            errors.add(ValidationError("periodTo", ToDateBeforeFrom(periodTo, periodFrom)))
         }

         dto.periodFrom ?: errors.add(ValidationError("periodFrom", NotFound(periodFrom)))

         dto.periodTo ?: errors.add(ValidationError("periodTo", NotFound(periodTo)))

         dto.defaultProfitCenter ?: errors.add(ValidationError("defaultProfitCenter", NotFound(dto.defaultProfitCenter!!)))

         if (dto.defaultAccountPayableAccount?.id != null && defaultAccountPayableAccount == null) {
            errors.add(ValidationError("defaultAccountPayableAccount.id", NotFound(dto.defaultAccountPayableAccount!!.id!!)))
         }

         if (dto.defaultAccountPayableDiscountAccount?.id != null && defaultAccountPayableDiscountAccount == null) {
            errors.add(ValidationError("defaultAccountPayableDiscountAccount.id", NotFound(dto.defaultAccountPayableDiscountAccount!!.id!!)))
         }

         if (dto.defaultAccountReceivableAccount?.id != null && defaultAccountReceivableAccount == null) {
            errors.add(ValidationError("defaultAccountReceivableAccount.id", NotFound(dto.defaultAccountReceivableAccount!!.id!!)))
         }

         if (dto.defaultAccountReceivableDiscountAccount?.id != null && defaultAccountReceivableDiscountAccount == null) {
            errors.add(ValidationError("defaultAccountReceivableDiscountAccount.id", NotFound(dto.defaultAccountReceivableDiscountAccount!!.id!!)))
         }

         if (dto.defaultAccountMiscInventoryAccount?.id != null && defaultAccountMiscInventoryAccount == null) {
            errors.add(ValidationError("defaultAccountMiscInventoryAccount.id", NotFound(dto.defaultAccountMiscInventoryAccount!!.id!!)))
         }

         if (dto.defaultAccountSerializedInventoryAccount?.id != null && defaultAccountSerializedInventoryAccount == null) {
            errors.add(ValidationError("defaultAccountSerializedInventoryAccount.id", NotFound(dto.defaultAccountSerializedInventoryAccount!!.id!!)))
         }

         if (dto.defaultAccountUnbilledInventoryAccount?.id != null && defaultAccountUnbilledInventoryAccount == null) {
            errors.add(ValidationError("defaultAccountUnbilledInventoryAccount.id", NotFound(dto.defaultAccountUnbilledInventoryAccount!!.id!!)))
         }

         if (dto.defaultAccountFreightAccount?.id != null && defaultAccountFreightAccount == null) {
            errors.add(ValidationError("defaultAccountFreightAccount.id", NotFound(dto.defaultAccountFreightAccount!!.id!!)))
         }
      }

      return GeneralLedgerControlEntity(
         dto,
         defaultAccountPayableAccount = defaultAccountPayableAccount,
         defaultAccountPayableDiscountAccount = defaultAccountPayableDiscountAccount,
         defaultAccountReceivableAccount = defaultAccountReceivableAccount,
         defaultAccountReceivableDiscountAccount = defaultAccountReceivableDiscountAccount,
         defaultAccountMiscInventoryAccount = defaultAccountMiscInventoryAccount,
         defaultAccountSerializedInventoryAccount = defaultAccountSerializedInventoryAccount,
         defaultAccountUnbilledInventoryAccount = defaultAccountUnbilledInventoryAccount,
         defaultAccountFreightAccount = defaultAccountFreightAccount
      )
   }
}
