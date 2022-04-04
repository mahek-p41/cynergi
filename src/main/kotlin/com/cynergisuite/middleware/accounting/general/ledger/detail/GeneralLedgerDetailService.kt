package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.GeneralLedgerRecurringEntriesFilterRequest
import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSearchReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure.GeneralLedgerRecurringEntriesRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure.GeneralLedgerReversalEntryRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerDetailService @Inject constructor(
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerDetailValidator: GeneralLedgerDetailValidator,
   private val generalLedgerRecurringEntriesRepository: GeneralLedgerRecurringEntriesRepository,
   private val generalLedgerReversalEntryRepository: GeneralLedgerReversalEntryRepository
) {
   fun fetchOne(id: UUID, company: CompanyEntity): GeneralLedgerDetailDTO? {
      return generalLedgerDetailRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerDetailDTO> {
      val found = generalLedgerDetailRepository.findAll(company, pageRequest)

      return found.toPage { entity: GeneralLedgerDetailEntity ->
         GeneralLedgerDetailDTO(entity)
      }
   }

   fun create(dto: GeneralLedgerDetailDTO, company: CompanyEntity): GeneralLedgerDetailDTO {
      val toCreate = generalLedgerDetailValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerDetailRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: GeneralLedgerDetailDTO, company: CompanyEntity): GeneralLedgerDetailDTO {
      val toUpdate = generalLedgerDetailValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerDetailRepository.update(toUpdate, company))
   }

   fun fetchReport(company: CompanyEntity, filterRequest: GeneralLedgerSearchReportFilterRequest): GeneralLedgerSearchReportTemplate {
      val found = generalLedgerDetailRepository.fetchReports(company, filterRequest)

      return GeneralLedgerSearchReportTemplate(found)
   }

   fun transfer(company: CompanyEntity, filterRequest: GeneralLedgerRecurringEntriesFilterRequest) {
      val glRecurringEntries = generalLedgerRecurringEntriesRepository.findAll(company, filterRequest)
      var glDetailDTO: GeneralLedgerDetailDTO
      // todo: journalEntryNumber = next available number

      glRecurringEntries.forEach {
         // create GL detail for each distribution
         it.generalLedgerRecurringDistributions.forEach { distribution ->
            glDetailDTO = GeneralLedgerDetailDTO(
               null,
               SimpleIdentifiableDTO(distribution.generalLedgerDistributionAccount),
               filterRequest.entryDate!!.toLocalDate(),
               SimpleLegacyIdentifiableDTO(distribution.generalLedgerDistributionProfitCenter.myId()),
               SimpleIdentifiableDTO(distribution.generalLedgerRecurring.source),
               distribution.generalLedgerDistributionAmount,
               distribution.generalLedgerRecurring.message,
               filterRequest.employeeNumber,
               // todo: journalEntryNumber
            )

            create(glDetailDTO, company)
         }

         // update last transfer date in GL recurring
         it.generalLedgerRecurring.lastTransferDate = filterRequest.entryDate!!.toLocalDate()
         generalLedgerRecurringEntriesRepository.update(company, it)
      }
   }

   fun transfer(user: User, pageRequest: PageRequest) {
      val glReversalEntries = generalLedgerReversalEntryRepository.findAll(user.myCompany(), pageRequest).elements
      var glDetailDTO: GeneralLedgerDetailDTO
      // todo: check if GL period is open
      // todo: journalEntryNumber = next available number

      glReversalEntries.forEach {
         // create GL detail for each distribution
         it.generalLedgerReversalDistributions.forEach { distribution ->
            glDetailDTO = GeneralLedgerDetailDTO(
               null,
               SimpleIdentifiableDTO(distribution.generalLedgerReversalDistributionAccount),
               distribution.generalLedgerReversal.reversalDate,
               SimpleLegacyIdentifiableDTO(distribution.generalLedgerReversalDistributionProfitCenter.myId()),
               SimpleIdentifiableDTO(distribution.generalLedgerReversal.source),
               distribution.generalLedgerReversalDistributionAmount,
               distribution.generalLedgerReversal.comment,
               user.myEmployeeNumber(),
               // todo: journalEntryNumber
            )

            create(glDetailDTO, user.myCompany())
         }
      }
   }

   private fun transformEntity(generalLedgerDetail: GeneralLedgerDetailEntity): GeneralLedgerDetailDTO {
      return GeneralLedgerDetailDTO(entity = generalLedgerDetail)
   }
}
