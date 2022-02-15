package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerSourceCodeService @Inject constructor(
   private val generalLedgerSourceCodeRepository: GeneralLedgerSourceCodeRepository,
   private val generalLedgerSourceCodeValidator: GeneralLedgerSourceCodeValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): GeneralLedgerSourceCodeDTO? =
      generalLedgerSourceCodeRepository.findOne(id, company)?.let { GeneralLedgerSourceCodeDTO(entity = it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerSourceCodeDTO> {
      val found = generalLedgerSourceCodeRepository.findAll(pageRequest, company)

      return found.toPage { GeneralLedgerSourceCode: GeneralLedgerSourceCodeEntity ->
         GeneralLedgerSourceCodeDTO(GeneralLedgerSourceCode)
      }
   }

   fun create(dto: GeneralLedgerSourceCodeDTO, company: CompanyEntity): GeneralLedgerSourceCodeDTO {
      val toCreate = generalLedgerSourceCodeValidator.validateCreate(dto, company)

      return GeneralLedgerSourceCodeDTO(
         entity = generalLedgerSourceCodeRepository.insert(toCreate, company)
      )
   }

   fun update(id: UUID, dto: GeneralLedgerSourceCodeDTO, company: CompanyEntity): GeneralLedgerSourceCodeDTO {
      val toUpdate = generalLedgerSourceCodeValidator.validateUpdate(id, dto, company)

      return GeneralLedgerSourceCodeDTO(
         entity = generalLedgerSourceCodeRepository.update(entity = toUpdate, company = company)
      )
   }

   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerSourceCodeRepository.delete(id, company)
   }
}
