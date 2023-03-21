package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.GeneralLedgerJournalExportRequest
import com.cynergisuite.domain.GeneralLedgerJournalFilterRequest
import com.cynergisuite.domain.GeneralLedgerJournalPostPurgeDTO
import com.cynergisuite.domain.GeneralLedgerJournalReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailService
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerJournalRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreDTO
import com.opencsv.CSVWriter
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
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

   fun purge(purgeDTO: GeneralLedgerJournalPostPurgeDTO, company: CompanyEntity) {
      val toPurge = generalLedgerJournalRepository.findAllPurgePost(company, purgeDTO)
      generalLedgerJournalRepository.bulkDelete(toPurge, company)
   }

   fun fetchReport(company: CompanyEntity, filterRequest: GeneralLedgerJournalReportFilterRequest): GeneralLedgerPendingReportTemplate {
      val found = generalLedgerJournalRepository.fetchReport(company, filterRequest)

      return GeneralLedgerPendingReportTemplate(found)
   }

   fun fetchPendingTotals(company: CompanyEntity, filterRequest: GeneralLedgerJournalFilterRequest): GeneralLedgerPendingJournalCountDTO {

      return generalLedgerJournalRepository.fetchPendingTotals(company, filterRequest)
   }

   fun export(filterRequest: GeneralLedgerJournalExportRequest, company: CompanyEntity): ByteArray {
      val found = generalLedgerJournalRepository.export(filterRequest, company)
      //construct new filter request from PostPurgeDTO to calculate totals and check balance
      val countFilter = GeneralLedgerJournalFilterRequest(null, null, null, null,
         filterRequest.beginProfitCenter,
         filterRequest.endProfitCenter,
         filterRequest.beginSourceCode,
         filterRequest.endSourceCode,
         filterRequest.fromDate,
         filterRequest.thruDate
      )
      val totals = fetchPendingTotals(company, countFilter)
      generalLedgerJournalValidator.validateTransfer(totals)

      val stream = ByteArrayOutputStream()
      val output = OutputStreamWriter(stream)
      val csvWriter = CSVWriter(output, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER)

      for(element in found) {
         val data = arrayOf<String>(
            company.id.toString().substring(0,3),
            element.account.number.toString().padStart(8, '0'),
            '"' + element.account.name.padEnd(30, ' ') + '"',
            element.profitCenter.myNumber().toString().padStart(6, '0'),
            '"' + element.date.format(DateTimeFormatter.ofPattern("MM/dd/yy")) + '"',
            '"' + element.source.value + '"',
            if(element.amount > BigDecimal.ZERO) element.amount.toString().padStart(12, '0') else BigDecimal("0.00").toString().padStart(12, '0'),
            if(element.amount < BigDecimal.ZERO) element.amount.abs().toString().padStart(12, '0') else BigDecimal("0.00").toString().padStart(12, '0'),
            '"' + (element.message?.padEnd(70, ' ') ?: ' '.toString().padEnd(70, ' ')) + '"'
         )
         csvWriter.writeNext(data)
      }
      csvWriter.close()
      output.close()
      return stream.toByteArray()
   }

   @Transactional
   fun transfer(user: User, filterRequest: GeneralLedgerJournalPostPurgeDTO, locale: Locale) {
      val company = user.myCompany()

      //construct new filter request from PostPurgeDTO to calculate totals and check balance
      val countFilter = GeneralLedgerJournalFilterRequest(null, null, null, null,
         filterRequest.beginProfitCenter,
         filterRequest.endProfitCenter,
         filterRequest.beginSourceCode,
         filterRequest.endSourceCode,
         filterRequest.fromDate,
         filterRequest.thruDate
      )
      val totals = fetchPendingTotals(company, countFilter)
      generalLedgerJournalValidator.validateTransfer(totals)

      val glJournals = generalLedgerJournalRepository.findAllPurgePost(company, filterRequest)
      var glDetailDTO: GeneralLedgerDetailDTO
      val journalEntryNumber = generalLedgerDetailRepository.findNextJENumber(company)

      glJournals.forEach {
         // create GL detail for each distribution
            glDetailDTO = GeneralLedgerDetailDTO(
               null,
               AccountDTO(it.account),
               it.date,
               StoreDTO(it.profitCenter),
               GeneralLedgerSourceCodeDTO(it.source),
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
      bulkDelete(glJournals, company)
   }

   @Transactional
   fun transfer(user: User, dto: GeneralLedgerJournalDTO, locale: Locale) {

      // create GL detail
      val glDetailDTO = GeneralLedgerDetailDTO(
            null,
            dto.account,
            dto.date,
            dto.profitCenter,
            dto.source,
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

   @Transactional
   fun bulkDelete(glJournals: List<GeneralLedgerJournalEntity>, company: CompanyEntity) {
      generalLedgerJournalRepository.bulkDelete(glJournals, company)
   }

   private fun transformEntity(generalLedgerJournal: GeneralLedgerJournalEntity): GeneralLedgerJournalDTO {
      return GeneralLedgerJournalDTO(entity = generalLedgerJournal)
   }
}
