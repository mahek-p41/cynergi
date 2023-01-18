package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.InvoiceReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceReportRepository
import com.cynergisuite.middleware.accounting.account.payable.invoice.infrastructure.AccountPayableInvoiceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class AccountPayableInvoiceService @Inject constructor(
   private val accountPayableInvoiceRepository: AccountPayableInvoiceRepository,
   private val accountPayableInvoiceReportRepository: AccountPayableInvoiceReportRepository,
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

   fun fetchReport(company: CompanyEntity, filterRequest: InvoiceReportFilterRequest): AccountPayableInvoiceReportTemplate {
      val found = accountPayableInvoiceReportRepository.fetchReport(company, filterRequest)

      return AccountPayableInvoiceReportTemplate(found)
   }

   fun update(id: UUID, dto: AccountPayableInvoiceDTO, company: CompanyEntity): AccountPayableInvoiceDTO {
      val toUpdate = accountPayableInvoiceValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayableInvoiceRepository.update(toUpdate, company))
   }

   private fun transformEntity(accountPayableInvoiceEntity: AccountPayableInvoiceEntity): AccountPayableInvoiceDTO {
      return AccountPayableInvoiceDTO(accountPayableInvoiceEntity)
   }
}
