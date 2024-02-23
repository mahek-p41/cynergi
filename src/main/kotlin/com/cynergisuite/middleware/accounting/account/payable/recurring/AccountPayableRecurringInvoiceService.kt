package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.domain.AccountPayableInvoiceListByVendorFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure.AccountPayableRecurringInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class AccountPayableRecurringInvoiceService @Inject constructor(
   private val accountPayableRecurringInvoiceRepository: AccountPayableRecurringInvoiceRepository,
   private val accountPayableRecurringInvoiceValidator: AccountPayableRecurringInvoiceValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): AccountPayableRecurringInvoiceDTO? =
      accountPayableRecurringInvoiceRepository.findOne(id, company)?.let { AccountPayableRecurringInvoiceDTO(it) }

   fun create(dto: AccountPayableRecurringInvoiceDTO, company: CompanyEntity): AccountPayableRecurringInvoiceDTO {
      val toCreate = accountPayableRecurringInvoiceValidator.validateCreate(dto, company)

      return transformEntity(accountPayableRecurringInvoiceRepository.insert(toCreate, company))
   }

   fun fetchAll(company: CompanyEntity, filterRequest: AccountPayableInvoiceListByVendorFilterRequest): Page<AccountPayableRecurringInvoiceDTO> {
      val found = accountPayableRecurringInvoiceRepository.findAll(company, filterRequest)

      return found.toPage { accountPayableRecurringInvoiceEntity: AccountPayableRecurringInvoiceEntity ->
         AccountPayableRecurringInvoiceDTO(accountPayableRecurringInvoiceEntity)
      }
   }

   fun update(id: UUID, dto: AccountPayableRecurringInvoiceDTO, company: CompanyEntity): AccountPayableRecurringInvoiceDTO {
      val toUpdate = accountPayableRecurringInvoiceValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableRecurringInvoiceRepository.update(toUpdate, company))
   }

   private fun transformEntity(accountPayableRecurringInvoiceEntity: AccountPayableRecurringInvoiceEntity): AccountPayableRecurringInvoiceDTO {
      return AccountPayableRecurringInvoiceDTO(accountPayableRecurringInvoiceEntity)
   }
}
