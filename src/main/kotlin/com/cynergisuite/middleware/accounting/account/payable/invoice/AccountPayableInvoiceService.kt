package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableInvoiceService @Inject constructor(
   private val accountPayableInvoiceRepository: AccountPayableInvoiceRepository,
   private val accountPayableInvoiceValidator: AccountPayableInvoiceValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): AccountPayableInvoiceDTO? =
      accountPayableInvoiceRepository.findOne(id, company)?.let { AccountPayableInvoiceDTO(it) }

   fun create(dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceDTO {
      val toCreate = accountPayableInvoiceValidator.validateCreate(dto, company)

      return transformEntity(accountPayableInvoiceRepository.insert(toCreate, company))
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<AccountPayableInvoiceDTO> {
      val found = accountPayableInvoiceRepository.findAll(company, pageRequest)

      return found.toPage { accountPayableInvoiceEntity: AccountPayableInvoiceEntity ->
         AccountPayableInvoiceDTO(accountPayableInvoiceEntity)
      }
   }

   fun update(id: UUID, dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceDTO {
      val toUpdate = accountPayableInvoiceValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableInvoiceRepository.update(toUpdate, company))
   }

   private fun transformEntity(accountPayableInvoiceEntity: AccountPayableInvoiceEntity): AccountPayableInvoiceDTO {
      return AccountPayableInvoiceDTO(accountPayableInvoiceEntity)
   }
}
