package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@Singleton
class AccountPayablePaymentDetailValidator @Inject constructor(
   private val apInvoiceRepository: AccountPayableInvoiceRepository,
   private val apPaymentRepository: AccountPayablePaymentRepository,
   private val vendorRepository: VendorRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayablePaymentDetailValidator::class.java)

   fun validateCreate(dto: AccountPayablePaymentDetailDTO, company: CompanyEntity): AccountPayablePaymentDetailEntity {
      logger.trace("Validating Create AccountPayablePaymentDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: AccountPayablePaymentDetailDTO, company: CompanyEntity): AccountPayablePaymentDetailEntity {
      logger.debug("Validating Update AccountPayablePaymentDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: AccountPayablePaymentDetailDTO, company: CompanyEntity): AccountPayablePaymentDetailEntity {
      val invoice = apInvoiceRepository.findOne(dto.invoice!!.id!!, company)
      val payment = apPaymentRepository.findOne(dto.payment!!.id!!, company)
      val vendor = dto.vendor?.id?.let { vendorRepository.findOne(it, company) }

      doValidation { errors ->
         // non-nullable validations
         invoice ?: errors.add(ValidationError("invoice.id", NotFound(dto.invoice!!.id!!)))
         payment ?: errors.add(ValidationError("payment.id", NotFound(dto.payment!!.id!!)))

         // nullable validations
         if (dto.vendor?.id != null && vendor == null) {
            errors.add(ValidationError("vendor.id", NotFound(dto.vendor!!.id!!)))
         }
      }

      return AccountPayablePaymentDetailEntity(
         dto,
         vendor,
         invoice!!,
         payment!!
      )
   }
}
