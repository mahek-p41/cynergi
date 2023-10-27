package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.BankReconClearingFilterRequest
import com.cynergisuite.domain.BankReconFilterRequest
import com.cynergisuite.domain.BankReconciliationTransactionsFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.ReconcileBankAccountFilterRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.bank.BankReconciliationReportDTO
import com.cynergisuite.middleware.accounting.bank.ReconcileBankAccountReportTemplate
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.ReconcileBankAccountRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale
import java.util.UUID

@Singleton
class BankReconciliationService @Inject constructor(
   private val bankReconciliationRepository: BankReconciliationRepository,
   private val reconcileBankAccountRepository: ReconcileBankAccountRepository,
   private val bankReconciliationValidator: BankReconciliationValidator
) {
   fun fetchById(id: UUID, company: CompanyEntity, locale: Locale): BankReconciliationDTO? =
      bankReconciliationRepository.findOne(id, company)?.let { transformEntity(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<BankReconciliationDTO> {
      val found = bankReconciliationRepository.findAll(company, pageRequest)

      return found.toPage { entity: BankReconciliationEntity -> transformEntity(entity) }
   }

   fun create(dto: BankReconciliationDTO, company: CompanyEntity): BankReconciliationDTO {
      val toCreate = bankReconciliationValidator.validateCreate(dto, company)

      return transformEntity(bankReconciliationRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: BankReconciliationDTO, company: CompanyEntity): BankReconciliationDTO {
      val toUpdate = bankReconciliationValidator.validateUpdate(id, dto, company)

      return transformEntity(bankReconciliationRepository.update(toUpdate, company))
   }

   fun fetchReport(filterRequest: BankReconFilterRequest, company: CompanyEntity): BankReconciliationReportDTO {
      return bankReconciliationRepository.findReport(filterRequest, company)

   }

   fun reconcileBankAccount(filterRequest: ReconcileBankAccountFilterRequest, company: CompanyEntity): ReconcileBankAccountReportTemplate {
      return reconcileBankAccountRepository.findReport(filterRequest, company)
   }

   fun bulkDelete(dtoList: List<BankReconciliationDTO>, company: CompanyEntity) {

      dtoList.map {
         bankReconciliationRepository.delete(it.id!!, company)
      }
   }

   fun bulkDelete(filterRequest: BankReconciliationTransactionsFilterRequest, company: CompanyEntity) {
      val found = bankReconciliationRepository.findTransactions(filterRequest, company)

      found.elements.map {
         bankReconciliationRepository.delete(it.id!!, company)
      }
   }

   fun clearing(filterRequest: BankReconClearingFilterRequest, company: CompanyEntity): List<BankReconciliationDTO> {
      val found = bankReconciliationRepository.fetchClear(filterRequest, company)
      return found.map { transformEntity(it) }
   }

   fun bulkUpdate(dtoList: List<BankReconciliationDTO>, company: CompanyEntity): List<BankReconciliationDTO> {
      val toUpdate = bankReconciliationValidator.validateBulkUpdate(dtoList, company)
      val updated = bankReconciliationRepository.bulkUpdate(toUpdate, company)

      return updated.map{ transformEntity(it)}
   }

   fun fetchTransactions(filterRequest: BankReconciliationTransactionsFilterRequest, company: CompanyEntity): Page<BankReconciliationDTO> {
      val found = bankReconciliationRepository.findTransactions(filterRequest, company)

      return found.toPage { entity: BankReconciliationEntity -> transformEntity(entity) }
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
