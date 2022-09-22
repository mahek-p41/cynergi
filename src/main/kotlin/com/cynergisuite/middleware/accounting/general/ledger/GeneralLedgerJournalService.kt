package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.GeneralLedgerJournalFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.infrastructure.RepositoryPage
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailService
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID
import java.util.Locale
import javax.transaction.Transactional

@Singleton
class GeneralLedgerJournalService @Inject constructor(
   private val generalLedgerJournalRepository: GeneralLedgerJournalRepository,
   private val generalLedgerJournalValidator: GeneralLedgerJournalValidator,
   private val generalLedgerDetailService: GeneralLedgerDetailService,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository
) {
   fun fetchOne(id: UUID, company: CompanyEntity): GeneralLedgerJournalDTO? {
      return generalLedgerJournalRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun fetchAll(company: CompanyEntity, filterRequest: GeneralLedgerJournalFilterRequest): Page<GeneralLedgerJournalDTO> {
      val found = generalLedgerJournalRepository.findAll(company, filterRequest)

      return found.toPage { entity: GeneralLedgerJournalEntity ->
         GeneralLedgerJournalDTO(entity)
      }
   }

   fun create(dto: GeneralLedgerJournalDTO, company: CompanyEntity): GeneralLedgerJournalDTO {
      val toCreate = generalLedgerJournalValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerJournalRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: GeneralLedgerJournalDTO, company: CompanyEntity): GeneralLedgerJournalDTO {
      val toUpdate = generalLedgerJournalValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerJournalRepository.update(toUpdate, company))
   }

   fun purge(filterRequest: GeneralLedgerJournalFilterRequest, company: CompanyEntity) {
      val toPurge = generalLedgerJournalRepository.findAll(company, filterRequest)
      toPurge.elements.map {
         generalLedgerJournalRepository.delete(it.id!!, company)
      }
   }

   @Transactional
   fun transfer(user: User, filterRequest: GeneralLedgerJournalFilterRequest, locale: Locale) {
      val company = user.myCompany()
      val glJournals = generalLedgerJournalRepository.findAll(company, filterRequest)
      var glDetailDTO: GeneralLedgerDetailDTO
      val journalEntryNumber = generalLedgerDetailRepository.findNextJENumber(company)

      glJournals.elements.forEach {
         // create GL detail for each distribution
            glDetailDTO = GeneralLedgerDetailDTO(
               null,
               SimpleIdentifiableDTO(it.id),
               it.date,
               SimpleLegacyIdentifiableDTO(it.profitCenter.myId()),
               SimpleIdentifiableDTO(it.source),
               it.amount,
               it.message,
               user.myEmployeeNumber(),
               journalEntryNumber
            )

      generalLedgerDetailService.create(glDetailDTO, company)
         // post accounting entries CYN-930
         val glAccountPostingDTO = GeneralLedgerAccountPostingDTO(glDetailDTO)
      generalLedgerDetailService.postEntry(glAccountPostingDTO, user.myCompany(), locale)
      }
   }

   @Transactional
   fun transfer(user: User, dto: GeneralLedgerJournalDTO, locale: Locale) {
      val company = user.myCompany()

      // create GL detail
      var glDetailDTO: GeneralLedgerDetailDTO = GeneralLedgerDetailDTO(
            null,
            SimpleIdentifiableDTO(dto.id!!),
            dto.date,
            SimpleLegacyIdentifiableDTO(dto.profitCenter!!.myId()),
            SimpleIdentifiableDTO(dto.source!!.id),
            dto.amount,
            dto.message,
            user.myEmployeeNumber(),
            null
         )
      generalLedgerDetailService.create(glDetailDTO, user.myCompany())
      // post accounting entries CYN-930
      val glAccountPostingDTO = GeneralLedgerAccountPostingDTO(glDetailDTO)
      generalLedgerDetailService.postEntry(glAccountPostingDTO, user.myCompany(), locale)
   }

   @Transactional
   fun delete(id: UUID, company: CompanyEntity) {
      generalLedgerJournalRepository.delete(id, company)
   }

   private fun transformEntity(generalLedgerJournal: GeneralLedgerJournalEntity): GeneralLedgerJournalDTO {
      return GeneralLedgerJournalDTO(entity = generalLedgerJournal)
   }
}
