package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.Company
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerSourceCodeService @Inject constructor(
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val generalLedgerSourceCodeValidator: GeneralLedgerSourceCodeValidator
) {

   fun fetchById(id: Long, company: Company): GeneralLedgerSourceCodeDTO? =
      generalLedgerSourceCodeRepository.findOne(id, company)?.let { GeneralLedgerSourceCodeDTO(entity = it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<GeneralLedgerSourceCodeDTO> {
      val found = generalLedgerSourceCodeRepository.findAll(pageRequest, company)

      return found.toPage { GeneralLedgerSourceCode: GeneralLedgerSourceCodeEntity ->
         GeneralLedgerSourceCodeDTO(GeneralLedgerSourceCode)
      }
   }

   fun create(dto: GeneralLedgerSourceCodeDTO, company: Company): GeneralLedgerSourceCodeDTO {
      val toCreate = generalLedgerSourceCodeValidator.validateCreate(dto, company)

      return GeneralLedgerSourceCodeDTO(
         entity = generalLedgerSourceCodeRepository.insert(toCreate, company)
      )
   }

   fun update(id: Long, dto: GeneralLedgerSourceCodeDTO, company: Company): GeneralLedgerSourceCodeDTO {
      val toUpdate = generalLedgerSourceCodeValidator.validateUpdate(id, dto, company)

      return GeneralLedgerSourceCodeDTO(
         entity = generalLedgerSourceCodeRepository.update(entity = toUpdate)
      )
   }
}
