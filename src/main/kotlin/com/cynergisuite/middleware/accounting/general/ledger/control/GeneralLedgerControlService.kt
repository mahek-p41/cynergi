package com.cynergisuite.middleware.accounting.general.ledger.control

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure.GeneralLedgerControlRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class GeneralLedgerControlService @Inject constructor(
   private val generalLedgerControlRepository: GeneralLedgerControlRepository,
   private val generalLedgerControlValidator: GeneralLedgerControlValidator
) {
   fun fetchOne(company: Company): GeneralLedgerControlDTO? {
      return generalLedgerControlRepository.findOne(company)?.let { transformEntity(it) }
   }

   @Validated
   fun create(@Valid dto: GeneralLedgerControlDTO, company: Company): GeneralLedgerControlDTO {
      val toCreate = generalLedgerControlValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerControlRepository.insert(toCreate, company))
   }

   @Validated
   fun update(@Valid dto: GeneralLedgerControlDTO, company: Company): GeneralLedgerControlDTO {
      val toUpdate = generalLedgerControlValidator.validateUpdate(dto, company)

      return transformEntity(generalLedgerControlRepository.update(toUpdate, company))
   }

   private fun transformEntity(generalLedgerControl: GeneralLedgerControlEntity): GeneralLedgerControlDTO {
      return GeneralLedgerControlDTO(entity = generalLedgerControl)
   }
}
