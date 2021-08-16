package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.PaymentReportFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentRepository
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayablePaymentService @Inject constructor(
   private val accountPayablePaymentRepository: AccountPayablePaymentRepository,
   private val accountPayablePaymentValidator: AccountPayablePaymentValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): AccountPayablePaymentDTO? =
      accountPayablePaymentRepository.findOne(id, company)?.let { AccountPayablePaymentDTO(it) }

   fun create(dto: AccountPayablePaymentDTO, company: CompanyEntity): AccountPayablePaymentDTO {
      val toCreate = accountPayablePaymentValidator.validateCreate(dto, company)

      return transformEntity(accountPayablePaymentRepository.insert(toCreate, company))
   }

   fun fetchReport(company: CompanyEntity, filterRequest: PaymentReportFilterRequest): AccountPayablePaymentReportTemplate {
      val found = accountPayablePaymentRepository.findAll(company, filterRequest)

      return AccountPayablePaymentReportTemplate(found)
   }

   fun update(id: UUID, dto: AccountPayablePaymentDTO, company: CompanyEntity): AccountPayablePaymentDTO {
      val toUpdate = accountPayablePaymentValidator.validateUpdate(id, dto, company)

      return transformEntity(accountPayablePaymentRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      accountPayablePaymentRepository.delete(id, company)
   }

   private fun transformEntity(accountPayablePaymentEntity: AccountPayablePaymentEntity): AccountPayablePaymentDTO {
      return AccountPayablePaymentDTO(accountPayablePaymentEntity)
   }
}
