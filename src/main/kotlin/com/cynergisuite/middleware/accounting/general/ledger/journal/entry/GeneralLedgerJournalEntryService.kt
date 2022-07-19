package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailService
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryService
import com.cynergisuite.middleware.authentication.user.User
import jakarta.inject.Singleton
import jakarta.inject.Inject
import java.math.BigDecimal

@Singleton
class GeneralLedgerJournalEntryService @Inject constructor(
  private val generalLedgerDetailService: GeneralLedgerDetailService,
  private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
  private val generalLedgerJournalEntryValidator: GeneralLedgerJournalEntryValidator,
  private val generalLedgerReversalEntryService: GeneralLedgerReversalEntryService
) {
   fun create(dto: GeneralLedgerJournalEntryDTO, user: User): GeneralLedgerJournalEntryDTO {
      generalLedgerJournalEntryValidator.validateCreate(dto, user.myCompany())

      // assign journal entry number
      val journalEntryNumber = generalLedgerDetailRepository.findNextJENumber(user.myCompany())
      dto.journalEntryNumber = journalEntryNumber

      // create GL details
      var glDetailDTO: GeneralLedgerDetailDTO
      dto.journalEntryDetails.forEach { journalEntryDetail ->
         glDetailDTO = GeneralLedgerDetailDTO(
            null,
            SimpleIdentifiableDTO(journalEntryDetail.account!!.id),
            dto.entryDate,
            SimpleLegacyIdentifiableDTO(journalEntryDetail.profitCenter!!.id),
            SimpleIdentifiableDTO(dto.source!!),
            journalEntryDetail.amount,
            dto.message,
            user.myEmployeeNumber(),
            journalEntryNumber
         )

         glDetailDTO = generalLedgerDetailService.create(glDetailDTO, user.myCompany())

         // post glDetailDTO to GL summary
         // todo: post accounting entries CYN-930
      }

      // check reverse
      if (dto.reverse!!) {
         val glReversalEntryDTO = createGLReversalEntry(dto, user)

         // check post reversing entry
         if (dto.postReversingEntry!!) {
            generalLedgerReversalEntryService.postReversalEntry(glReversalEntryDTO, user)
         }
      }

      return dto
   }

   fun createGLReversalEntry(dto: GeneralLedgerJournalEntryDTO, user: User): GeneralLedgerReversalEntryDTO {
      val glReversalDTO = GeneralLedgerReversalDTO(
            null,
            dto.source,
            dto.entryDate,
            dto.entryDate!!.plusMonths(1).withDayOfMonth(1),
            dto.message,
            dto.entryDate!!.monthValue,
            dto.entryDate!!.dayOfMonth
      )

      val glReversalDistributionDTOs = mutableListOf<GeneralLedgerReversalDistributionDTO>()
      var glReversalDistributionDTO: GeneralLedgerReversalDistributionDTO
      var balance = BigDecimal.ZERO
      dto.journalEntryDetails.forEach { journalEntryDetail ->
         glReversalDistributionDTO = GeneralLedgerReversalDistributionDTO(
            null,
            SimpleIdentifiableDTO(glReversalDTO),
            SimpleIdentifiableDTO(journalEntryDetail.account!!.id),
            SimpleLegacyIdentifiableDTO(journalEntryDetail.profitCenter!!.id),
            journalEntryDetail.amount!!.times(BigDecimal(-1))
         )
         glReversalDistributionDTOs.add(glReversalDistributionDTO)

         balance += glReversalDistributionDTO.generalLedgerReversalDistributionAmount!!
      }

      return generalLedgerReversalEntryService.create(GeneralLedgerReversalEntryDTO(glReversalDTO, glReversalDistributionDTOs, balance), user.myCompany())
   }
}
