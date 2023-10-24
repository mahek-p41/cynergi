package com.cynergisuite.middleware.accounting.general.ledger.inquiry

import com.cynergisuite.domain.GeneralLedgerInquiryFilterRequest
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.infrastructure.GeneralLedgerInquiryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GeneralLedgerInquiryService @Inject constructor(
   private val generalLedgerInquiryRepository: GeneralLedgerInquiryRepository,
) {

   fun fetchOne(company: CompanyEntity, filterRequest: GeneralLedgerInquiryFilterRequest): GeneralLedgerInquiryDTO? {
      return generalLedgerInquiryRepository.findOne(company, filterRequest)?.let { transformEntity(it) }
   }

   private fun transformEntity(generalLedgerInquiry: GeneralLedgerInquiryEntity): GeneralLedgerInquiryDTO {
      return GeneralLedgerInquiryDTO(entity = generalLedgerInquiry)
   }
}
