package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure.GeneralLedgerControlRepository
import com.cynergisuite.middleware.company.Company
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerControlService @Inject constructor(
   private val generalLedgerControlRepository: GeneralLedgerControlRepository,
   private val generalLedgerControlValidator: GeneralLedgerControlValidator
) {
   fun fetchOne(company: Company): GeneralLedgerControlDTO? {
      return generalLedgerControlRepository.findOne(company)?.let { transformEntity(it) }
   }

   fun create(dto: GeneralLedgerControlDTO, company: Company): GeneralLedgerControlDTO {
      val toCreate = generalLedgerControlValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerControlRepository.insert(toCreate, company))
   }

   fun update(dto: GeneralLedgerControlDTO, company: Company): GeneralLedgerControlDTO {
      val toUpdate = generalLedgerControlValidator.validateUpdate(dto, company)

      return transformEntity(generalLedgerControlRepository.update(toUpdate, company))
   }

   private fun transformEntity(generalLedgerControl: GeneralLedgerControlEntity): GeneralLedgerControlDTO {
      return GeneralLedgerControlDTO(entity = generalLedgerControl)
   }
}
