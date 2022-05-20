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
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerDetailService @Inject constructor(
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerDetailValidator: GeneralLedgerDetailValidator,
   private val generalLedgerRecurringEntriesRepository: GeneralLedgerRecurringEntriesRepository
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

      glRecurringEntries.forEach {
         // create GL detail for each distribution
         it.generalLedgerRecurringDistributions.forEach { distribution ->
            glDetailDTO = GeneralLedgerDetailDTO(
               null,
               SimpleIdentifiableDTO(distribution.generalLedgerDistributionAccount.id),
               filterRequest.entryDate!!.toLocalDate(),
               SimpleLegacyIdentifiableDTO(distribution.generalLedgerDistributionProfitCenter.myId()),
               SimpleIdentifiableDTO(distribution.generalLedgerRecurring.source.id),
               distribution.generalLedgerDistributionAmount,
               distribution.generalLedgerRecurring.message,
               filterRequest.employeeNumber,
               null
            )

            create(glDetailDTO, company)
         }

         // update last transfer date in GL recurring
         it.generalLedgerRecurring.lastTransferDate = filterRequest.entryDate!!.toLocalDate()
         generalLedgerRecurringEntriesRepository.update(company, it)
      }
   }

   private fun transformEntity(generalLedgerDetail: GeneralLedgerDetailEntity): GeneralLedgerDetailDTO {
      return GeneralLedgerDetailDTO(entity = generalLedgerDetail)
   }
}
