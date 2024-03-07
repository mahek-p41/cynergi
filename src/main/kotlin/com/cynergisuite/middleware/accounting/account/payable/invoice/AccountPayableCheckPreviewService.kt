package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.AccountPayableCheckPreviewFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.check.preview.infrastructure.AccountPayableCheckPreviewRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.CheckInUse
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableCheckPreviewService @Inject constructor(
   private val accountPayableCheckPreviewRepository: AccountPayableCheckPreviewRepository,
) {

   fun checkPreview(company: CompanyEntity, filterRequest: AccountPayableCheckPreviewFilterRequest): AccountPayableCheckPreviewDTO {
      val checkPreviews = accountPayableCheckPreviewRepository.fetchCheckPreview(company, filterRequest)
      if (checkPreviews.vendorList.isNotEmpty()) {
         val checksInUse = accountPayableCheckPreviewRepository.validateCheckNums(filterRequest.checkNumber.toBigInteger(), filterRequest.bank, checkPreviews.vendorList)
         if (checksInUse){
            val errors: Set<ValidationError> = mutableSetOf(ValidationError("Check previously used", CheckInUse()))
            throw ValidationException(errors)
         } else {
           return transformEntity(checkPreviews)
         }
      } else throw NotFoundException("")
   }

   private fun transformEntity(checkPreviewEntity: AccountPayableCheckPreviewEntity): AccountPayableCheckPreviewDTO {
      return AccountPayableCheckPreviewDTO(checkPreviewEntity)
   }
}
