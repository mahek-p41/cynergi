package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.UUID
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GeneralLedgerDetailService @Inject constructor(
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerDetailValidator: GeneralLedgerDetailValidator
) {
   fun fetchOne(id: UUID, company: CompanyEntity): GeneralLedgerDetailDTO? {
      return generalLedgerDetailRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun create(dto: GeneralLedgerDetailDTO, company: CompanyEntity): GeneralLedgerDetailDTO {
      val toCreate = generalLedgerDetailValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerDetailRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: GeneralLedgerDetailDTO, company: CompanyEntity): GeneralLedgerDetailDTO {
      val toUpdate = generalLedgerDetailValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerDetailRepository.update(toUpdate, company))
   }

   private fun transformEntity(generalLedgerDetail: GeneralLedgerDetailEntity): GeneralLedgerDetailDTO {
      return GeneralLedgerDetailDTO(entity = generalLedgerDetail)
   }
}
