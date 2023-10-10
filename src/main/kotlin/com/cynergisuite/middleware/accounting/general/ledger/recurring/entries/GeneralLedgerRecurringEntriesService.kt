package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries

import com.cynergisuite.domain.GeneralLedgerRecurringEntriesFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure.GeneralLedgerRecurringEntriesRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerRecurringEntriesService @Inject constructor(
   private val generalLedgerRecurringEntriesRepository: GeneralLedgerRecurringEntriesRepository,
   private val generalLedgerRecurringEntriesValidator: GeneralLedgerRecurringEntriesValidator
) {

   fun fetchById(id: UUID, company: CompanyEntity): GeneralLedgerRecurringEntriesDTO? =
      generalLedgerRecurringEntriesRepository.findOne(id, company)?.let { GeneralLedgerRecurringEntriesDTO(it) }

   fun fetchAll(company: CompanyEntity, filterRequest: GeneralLedgerRecurringEntriesFilterRequest): Page<GeneralLedgerRecurringEntriesDTO> {
      val found = generalLedgerRecurringEntriesRepository.findAll(company, filterRequest)

      return found.toPage { entity: GeneralLedgerRecurringEntriesEntity ->
         GeneralLedgerRecurringEntriesDTO(entity)
      }
   }

   fun fetchReport(company: CompanyEntity, filterRequest: GeneralLedgerRecurringEntriesFilterRequest): List<GeneralLedgerRecurringEntriesDTO> {
      val found = generalLedgerRecurringEntriesRepository.findAll(company, filterRequest)
      val foundDTOs = mutableListOf<GeneralLedgerRecurringEntriesDTO>()

      for (entity in found.elements) {
         foundDTOs.add(transformEntity(entity))
      }

      return foundDTOs
   }

   fun create(dto: GeneralLedgerRecurringEntriesDTO, company: CompanyEntity): GeneralLedgerRecurringEntriesDTO {
      val toCreate = generalLedgerRecurringEntriesValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerRecurringEntriesRepository.insert(company, toCreate))
   }

   fun update(id: UUID, dto: GeneralLedgerRecurringEntriesDTO, company: CompanyEntity): GeneralLedgerRecurringEntriesDTO {
      val toUpdate = generalLedgerRecurringEntriesValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerRecurringEntriesRepository.update(company, toUpdate))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerRecurringEntriesRepository.delete(id, company)
   }

   private fun transformEntity(entity: GeneralLedgerRecurringEntriesEntity): GeneralLedgerRecurringEntriesDTO {
      return GeneralLedgerRecurringEntriesDTO(entity)
   }
}