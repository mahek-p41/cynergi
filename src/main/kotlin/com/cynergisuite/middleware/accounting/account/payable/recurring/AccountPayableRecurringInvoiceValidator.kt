package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableRecurringInvoiceStatusTypeRepository
import com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure.ExpenseMonthCreationTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.vendor.infrastructure.VendorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableRecurringInvoiceValidator @Inject constructor(
   private val vendorRepository: VendorRepository,
   private val statusTypeRepository: AccountPayableRecurringInvoiceStatusTypeRepository,
   private val expenseMonthCreationTypeRepository: ExpenseMonthCreationTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableRecurringInvoiceValidator::class.java)

   fun validateCreate(dto: AccountPayableRecurringInvoiceDTO, company: CompanyEntity): AccountPayableRecurringInvoiceEntity {
      logger.trace("Validating Create AccountPayableRecurringInvoice {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: AccountPayableRecurringInvoiceDTO, company: CompanyEntity): AccountPayableRecurringInvoiceEntity {
      logger.debug("Validating Update AccountPayableRecurringInvoice {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: AccountPayableRecurringInvoiceDTO, company: CompanyEntity): AccountPayableRecurringInvoiceEntity {
      val vendor = vendorRepository.findOne(dto.vendor!!.id!!, company)
      val payTo = vendorRepository.findOne(dto.payTo!!.id!!, company)
      val status = statusTypeRepository.findOne(dto.status!!.value)
      val expenseMonthCreationIndicator = expenseMonthCreationTypeRepository.findOne(dto.expenseMonthCreationIndicator!!.value)

      doValidation { errors ->
         vendor ?: errors.add(ValidationError("vendor.id", NotFound(dto.vendor!!.id!!)))
         payTo ?: errors.add(ValidationError("payTo.id", NotFound(dto.payTo!!.id!!)))
         status ?: errors.add(ValidationError("status.value", NotFound(dto.status!!.value)))
         expenseMonthCreationIndicator ?: errors.add(ValidationError("expenseMonthCreationIndicator.value", NotFound(dto.expenseMonthCreationIndicator!!.value)))
      }

      return AccountPayableRecurringInvoiceEntity(
         dto,
         vendor!!,
         payTo!!,
         status!!,
         expenseMonthCreationIndicator,
         null
      )
   }
}
