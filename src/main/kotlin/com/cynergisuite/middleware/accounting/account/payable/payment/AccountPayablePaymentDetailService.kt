package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.middleware.accounting.account.payable.payment.infrastructure.AccountPayablePaymentDetailRepository
import com.cynergisuite.middleware.company.Company
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayablePaymentDetailService @Inject constructor(
   private val apPaymentDetailRepository: AccountPayablePaymentDetailRepository,
   private val apPaymentDetailValidator: AccountPayablePaymentDetailValidator
) {

   fun fetchById(id: UUID, company: Company): AccountPayablePaymentDetailDTO? =
      apPaymentDetailRepository.findOne(id, company)?.let { AccountPayablePaymentDetailDTO(it) }

   fun create(dto: AccountPayablePaymentDetailDTO, company: Company): AccountPayablePaymentDetailDTO {
      val toCreate = apPaymentDetailValidator.validateCreate(dto, company)

      return transformEntity(apPaymentDetailRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: AccountPayablePaymentDetailDTO, company: Company): AccountPayablePaymentDetailDTO {
      val toUpdate = apPaymentDetailValidator.validateUpdate(id, dto, company)

      return transformEntity(apPaymentDetailRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: Company) {
      apPaymentDetailRepository.delete(id, company)
   }

   private fun transformEntity(AccountPayablePaymentDetailEntity: AccountPayablePaymentDetailEntity): AccountPayablePaymentDetailDTO {
      return AccountPayablePaymentDetailDTO(AccountPayablePaymentDetailEntity)
   }
}
