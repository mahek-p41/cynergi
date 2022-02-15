package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerRecurringService @Inject constructor(
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository,
   private val generalLedgerRecurringValidator: GeneralLedgerRecurringValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): GeneralLedgerRecurringDTO? =
      generalLedgerRecurringRepository.findOne(id, company)?.let { GeneralLedgerRecurringDTO(it) }

   fun create(dto: GeneralLedgerRecurringDTO, company: CompanyEntity): GeneralLedgerRecurringDTO {
      val toCreate = generalLedgerRecurringValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerRecurringRepository.insert(toCreate, company))
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerRecurringDTO> {
      val found = generalLedgerRecurringRepository.findAll(company, pageRequest)

      return found.toPage { generalLedgerRecurringEntity: GeneralLedgerRecurringEntity ->
         GeneralLedgerRecurringDTO(generalLedgerRecurringEntity)
      }
   }

   fun update(id: UUID, dto: GeneralLedgerRecurringDTO, company: CompanyEntity): GeneralLedgerRecurringDTO {
      val toUpdate = generalLedgerRecurringValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerRecurringRepository.update(toUpdate, company))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerRecurringRepository.delete(id, company)
   }

   private fun transformEntity(generalLedgerRecurringEntity: GeneralLedgerRecurringEntity): GeneralLedgerRecurringDTO {
      return GeneralLedgerRecurringDTO(generalLedgerRecurringEntity)
   }
}
