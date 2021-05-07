package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.company.Company
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerRecurringService @Inject constructor(
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository,
   private val generalLedgerRecurringValidator: GeneralLedgerRecurringValidator
) {

   fun fetchById(id: Long, company: Company): GeneralLedgerRecurringDTO? =
      generalLedgerRecurringRepository.findOne(id, company)?.let { GeneralLedgerRecurringDTO(it) }

   fun create(dto: GeneralLedgerRecurringDTO, company: Company): GeneralLedgerRecurringDTO {
      val toCreate = generalLedgerRecurringValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerRecurringRepository.insert(toCreate, company))
   }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<GeneralLedgerRecurringDTO> {
      val found = generalLedgerRecurringRepository.findAll(company, pageRequest)

      return found.toPage { generalLedgerRecurringEntity: GeneralLedgerRecurringEntity ->
         GeneralLedgerRecurringDTO(generalLedgerRecurringEntity)
      }
   }

   fun update(id: Long, dto: GeneralLedgerRecurringDTO, company: Company): GeneralLedgerRecurringDTO {
      val toUpdate = generalLedgerRecurringValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerRecurringRepository.update(toUpdate, company))
   }

   fun delete(id: Long, company: Company) {
      generalLedgerRecurringRepository.delete(id, company)
   }

   private fun transformEntity(generalLedgerRecurringEntity: GeneralLedgerRecurringEntity): GeneralLedgerRecurringDTO {
      return GeneralLedgerRecurringDTO(generalLedgerRecurringEntity)
   }
}
