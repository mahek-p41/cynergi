package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentDetailRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class AccountPayablePaymentDetailService @Inject constructor(
   private val apPaymentDetailRepository: AccountPayablePaymentDetailRepository,
   private val apPaymentDetailValidator: AccountPayablePaymentDetailValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): AccountPayablePaymentDetailDTO? =
      apPaymentDetailRepository.findOne(id, company)?.let { AccountPayablePaymentDetailDTO(it) }

   fun create(dto: AccountPayablePaymentDetailDTO, company: CompanyEntity): AccountPayablePaymentDetailDTO {
      val toCreate = apPaymentDetailValidator.validateCreate(dto, company)

      return transformEntity(apPaymentDetailRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: AccountPayablePaymentDetailDTO, company: CompanyEntity): AccountPayablePaymentDetailDTO {
      val toUpdate = apPaymentDetailValidator.validateUpdate(id, dto, company)

      return transformEntity(apPaymentDetailRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      apPaymentDetailRepository.delete(id, company)
   }

   private fun transformEntity(AccountPayablePaymentDetailEntity: AccountPayablePaymentDetailEntity): AccountPayablePaymentDetailDTO {
      return AccountPayablePaymentDetailDTO(AccountPayablePaymentDetailEntity)
   }
}
