package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentTypeTypeRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class AccountPayablePaymentValidator @Inject constructor(
   private val bankRepository: BankRepository,
   private val statusRepository: AccountPayablePaymentStatusTypeRepository,
   private val typeRepository: AccountPayablePaymentTypeTypeRepository,
   private val vendorRepository: VendorRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayablePaymentValidator::class.java)

   fun validateCreate(dto: AccountPayablePaymentDTO, company: CompanyEntity): AccountPayablePaymentEntity {
      logger.trace("Validating Create AccountPayablePayment {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: AccountPayablePaymentDTO, company: CompanyEntity): AccountPayablePaymentEntity {
      logger.debug("Validating Update AccountPayablePayment {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: AccountPayablePaymentDTO, company: CompanyEntity): AccountPayablePaymentEntity {
      val bank = bankRepository.findOne(dto.bank!!.id!!, company)
      val vendor = vendorRepository.findOne(dto.vendor!!.id!!, company)
      val type = typeRepository.findOne(dto.type!!.value)
      val status = statusRepository.findOne(dto.status!!.value)

      doValidation { errors ->
         // non-nullable validations
         bank ?: errors.add(ValidationError("bank.id", NotFound(dto.bank!!.id!!)))
         vendor ?: errors.add(ValidationError("vendor.id", NotFound(dto.vendor!!.id!!)))
         type ?: errors.add(ValidationError("type.value", NotFound(dto.type!!.value)))
         status ?: errors.add(ValidationError("status.value", NotFound(dto.status!!.value)))
      }

      return AccountPayablePaymentEntity(
         dto,
         bank!!,
         vendor!!,
         status!!,
         type!!
      )
   }
}
