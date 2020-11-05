package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.company.Company
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerDetailService @Inject constructor(
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerDetailValidator: GeneralLedgerDetailValidator
) {
   fun fetchOne(id: Long, company: Company): GeneralLedgerDetailDTO? {
      return generalLedgerDetailRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun create(dto: GeneralLedgerDetailDTO, company: Company): GeneralLedgerDetailDTO {
      val toCreate = generalLedgerDetailValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerDetailRepository.insert(toCreate, company))
   }

   fun update(id: Long, dto: GeneralLedgerDetailDTO, company: Company): GeneralLedgerDetailDTO {
      val toUpdate = generalLedgerDetailValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerDetailRepository.update(toUpdate, company))
   }

   private fun transformEntity(generalLedgerDetail: GeneralLedgerDetailEntity): GeneralLedgerDetailDTO {
      return GeneralLedgerDetailDTO(entity = generalLedgerDetail)
   }
}
