package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.company.Company
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankReconciliationService @Inject constructor(
   private val bankReconciliationRepository: BankReconciliationRepository,
   private val bankReconciliationValidator: BankReconciliationValidator
) {
   fun fetchById(id: Long, company: Company, locale: Locale): BankReconciliationDTO? =
      bankReconciliationRepository.findOne(id, company)?.let { transformEntity(it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<BankReconciliationDTO> {
      val found = bankReconciliationRepository.findAll(company, pageRequest)

      return found.toPage { entity: BankReconciliationEntity -> transformEntity(entity) }
   }

   fun create(dto: BankReconciliationDTO, company: Company): BankReconciliationDTO {
      val toCreate = bankReconciliationValidator.validateCreate(dto, company)

      return transformEntity(bankReconciliationRepository.insert(toCreate, company))
   }

   fun update(id: Long, dto: BankReconciliationDTO, company: Company): BankReconciliationDTO {
      val toUpdate = bankReconciliationValidator.validateUpdate(id, dto, company)

      return transformEntity(bankReconciliationRepository.update(toUpdate, company))
   }
}

private fun transformEntity(entity: BankReconciliationEntity): BankReconciliationDTO {
   return BankReconciliationDTO(
      id = entity.id,
      bank = SimpleIdentifiableDTO(entity.bank.id),
      type = BankReconciliationTypeDTO(entity.type),
      date = entity.date,
      clearedDate = entity.clearedDate,
      amount = entity.amount,
      description = entity.description,
      document = entity.document
   )
}
