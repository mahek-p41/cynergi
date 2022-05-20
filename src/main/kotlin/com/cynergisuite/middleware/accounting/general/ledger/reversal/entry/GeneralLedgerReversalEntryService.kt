package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure.GeneralLedgerReversalEntryRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class GeneralLedgerReversalEntryService @Inject constructor(
   private val generalLedgerDetailService: GeneralLedgerDetailService,
   private val generalLedgerReversalEntryRepository: GeneralLedgerReversalEntryRepository,
   private val generalLedgerReversalEntryValidator: GeneralLedgerReversalEntryValidator,
   private val financialCalendarRepository: FinancialCalendarRepository
) {

   fun fetchById(id: UUID, company: CompanyEntity): GeneralLedgerReversalEntryDTO? =
      generalLedgerReversalEntryRepository.findOne(id, company)?.let { GeneralLedgerReversalEntryDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerReversalEntryDTO> {
      val found = generalLedgerReversalEntryRepository.findAll(company, pageRequest)

      return found.toPage { entity: GeneralLedgerReversalEntryEntity ->
         GeneralLedgerReversalEntryDTO(entity)
      }
   }

   fun create(dto: GeneralLedgerReversalEntryDTO, company: CompanyEntity): GeneralLedgerReversalEntryDTO {
      val toCreate = generalLedgerReversalEntryValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerReversalEntryRepository.insert(company, toCreate))
   }

   fun update(id: UUID, dto: GeneralLedgerReversalEntryDTO, company: CompanyEntity): GeneralLedgerReversalEntryDTO {
      val toUpdate = generalLedgerReversalEntryValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerReversalEntryRepository.update(company, toUpdate))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerReversalEntryRepository.delete(id, company)
   }

   fun checkReversalDate(dto: GeneralLedgerReversalEntryDTO, company: CompanyEntity): Boolean {
      val glOpenDateRange = financialCalendarRepository.findDateRangeWhenGLIsOpen(company)
      val reversalDate = dto.generalLedgerReversal!!.reversalDate

      return !(reversalDate!!.isBefore(glOpenDateRange.first) || reversalDate.isAfter(glOpenDateRange.second))
   }

   fun postReversalEntry(dto: GeneralLedgerReversalEntryDTO, user: User) {
      val entity = generalLedgerReversalEntryRepository.findOne(dto.generalLedgerReversal!!.id!!, user.myCompany())

      // create GL details
      var glDetailDTO: GeneralLedgerDetailDTO
      entity!!.generalLedgerReversalDistributions.forEach { distribution ->
         glDetailDTO = GeneralLedgerDetailDTO(
            null,
            SimpleIdentifiableDTO(distribution.generalLedgerReversalDistributionAccount.id),
            distribution.generalLedgerReversal.reversalDate,
            SimpleLegacyIdentifiableDTO(distribution.generalLedgerReversalDistributionProfitCenter.myId()),
            SimpleIdentifiableDTO(distribution.generalLedgerReversal.source.id),
            distribution.generalLedgerReversalDistributionAmount,
            distribution.generalLedgerReversal.comment,
            user.myEmployeeNumber(),
            null
         )

         glDetailDTO = generalLedgerDetailService.create(glDetailDTO, user.myCompany())

         // post glDetailDTO to GL summary
         // todo: post accounting entries CYN-930
      }

      // delete GL reversal records
      entity.generalLedgerReversal.id?.let { generalLedgerReversalEntryRepository.delete(it, user.myCompany()) }
   }

   private fun transformEntity(entity: GeneralLedgerReversalEntryEntity): GeneralLedgerReversalEntryDTO {
      return GeneralLedgerReversalEntryDTO(entity)
   }
}
