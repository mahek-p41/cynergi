package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.GeneralLedgerRecurringEntriesFilterRequest
import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.GeneralLedgerSourceReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.ValidatorBase.Companion.logger
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountService
import com.cynergisuite.middleware.accounting.bank.BankService
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationService
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarService
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerAccountPostingDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerAccountPostingResponseDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerDetailPurgeCountDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSearchReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.GeneralLedgerRecurringEntriesDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure.GeneralLedgerRecurringEntriesRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.GeneralLedgerReversalEntryService
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure.GeneralLedgerReversalEntryRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryService
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.GLNotOpen
import com.cynergisuite.middleware.store.StoreDTO
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.util.Locale
import java.util.UUID
import javax.transaction.Transactional
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

@Singleton
class GeneralLedgerDetailService @Inject constructor(
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerDetailValidator: GeneralLedgerDetailValidator,
   private val generalLedgerRecurringEntriesRepository: GeneralLedgerRecurringEntriesRepository,
   private val generalLedgerReversalEntryRepository: GeneralLedgerReversalEntryRepository,
   private val generalLedgerReversalEntryService: GeneralLedgerReversalEntryService,
   private val financialCalendarService: FinancialCalendarService,
   private val generalLedgerSourceCodeService: GeneralLedgerSourceCodeService,
   private val generalLedgerSummaryService: GeneralLedgerSummaryService,
   private val accountService: AccountService,
   private val bankService: BankService,
   private val bankReconciliationService: BankReconciliationService,
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository,
   private val overallPeriodTypeService: OverallPeriodTypeService
) {
   fun fetchOne(id: UUID, company: CompanyEntity): GeneralLedgerDetailDTO? {
      return generalLedgerDetailRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun fetchAll(company: CompanyEntity, pageRequest: GeneralLedgerDetailPageRequest): Page<GeneralLedgerDetailDTO> {
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

   fun fetchSourceReport(company: CompanyEntity, filterRequest: GeneralLedgerSourceReportFilterRequest): GeneralLedgerSourceReportTemplate {
      val found = generalLedgerDetailRepository.fetchSourceReportSourceDetails(company, filterRequest)

      return GeneralLedgerSourceReportTemplate(found)
   }

   @Transactional
   fun transfer(user: User, filterRequest: GeneralLedgerRecurringEntriesFilterRequest, locale: Locale) {
      val company = user.myCompany()
      val glRecurringEntries = generalLedgerRecurringEntriesRepository.findAll(company, filterRequest)
      var glDetailDTO: GeneralLedgerDetailDTO
      val glDetailList = mutableListOf<GeneralLedgerDetailDTO>()
      val journalEntryNumber = generalLedgerDetailRepository.findNextJENumber(company)

      glRecurringEntries.elements.forEach {
         // create GL detail for each distribution
         it.generalLedgerRecurringDistributions.forEach { distribution ->
            glDetailDTO = GeneralLedgerDetailDTO(
               null,
               AccountDTO(distribution.generalLedgerDistributionAccount),
               filterRequest.entryDate,
               StoreDTO(distribution.generalLedgerDistributionProfitCenter),
               GeneralLedgerSourceCodeDTO(it.generalLedgerRecurring.source),
               distribution.generalLedgerDistributionAmount,
               it.generalLedgerRecurring.message,
               filterRequest.employeeNumber,
               journalEntryNumber
            )

            create(glDetailDTO, company)
            glDetailList.add(glDetailDTO)

            // post accounting entries CYN-930
            val glAccountPostingDTO = GeneralLedgerAccountPostingDTO(glDetailDTO)
            postEntry(glAccountPostingDTO, user.myCompany(), locale)
         }

         // create GL reversal records if Reverse is selected
         if (it.generalLedgerRecurring.reverseIndicator) {
            createGLReversalEntry(glDetailList, company)
         }
         glDetailList.clear()

         // update last transfer date in GL recurring
         it.generalLedgerRecurring.lastTransferDate = filterRequest.entryDate

         generalLedgerRecurringRepository.update(it.generalLedgerRecurring, company)
      }
   }

   @Transactional
   fun transfer(user: User, dto: GeneralLedgerRecurringEntriesDTO, locale: Locale) {
      val company = user.myCompany()
      val glRecurringEntry = generalLedgerRecurringEntriesRepository.findOne(dto.generalLedgerRecurring?.id!!, company)
      var glDetailDTO: GeneralLedgerDetailDTO
      val glDetailList = mutableListOf<GeneralLedgerDetailDTO>()
      val journalEntryNumber = generalLedgerDetailRepository.findNextJENumber(company)

      // create GL detail for each distribution
      glRecurringEntry!!.generalLedgerRecurringDistributions.forEach { distribution ->
         glDetailDTO = GeneralLedgerDetailDTO(
            null,
            AccountDTO(distribution.generalLedgerDistributionAccount),
            dto.entryDate,
            StoreDTO(distribution.generalLedgerDistributionProfitCenter),
            GeneralLedgerSourceCodeDTO(glRecurringEntry.generalLedgerRecurring.source),
            distribution.generalLedgerDistributionAmount,
            dto.generalLedgerRecurring!!.message,
            user.myEmployeeNumber(),
            journalEntryNumber
         )
         create(glDetailDTO, company)
         glDetailList.add(glDetailDTO)

         // post accounting entries CYN-930
         val glAccountPostingDTO = GeneralLedgerAccountPostingDTO(glDetailDTO)
         postEntry(glAccountPostingDTO, user.myCompany(), locale)
      }

      // create GL reversal records if Reverse is selected
      if (dto.generalLedgerRecurring!!.reverseIndicator!!) {
         createGLReversalEntry(glDetailList, company)
      }

      // update last transfer date in GL recurring
      glRecurringEntry.generalLedgerRecurring.lastTransferDate = dto.entryDate
      generalLedgerRecurringRepository.update(glRecurringEntry.generalLedgerRecurring, company)
   }

   private fun createGLReversalEntry(detailList: List<GeneralLedgerDetailDTO>, company: CompanyEntity) {
      val glReversalDTO = GeneralLedgerReversalDTO(
         null,
         detailList[0].source,
         detailList[0].date,
         detailList[0].date!!.plusMonths(1).withDayOfMonth(1),
         detailList[0].message,
         detailList[0].date!!.monthValue,
         detailList[0].date!!.dayOfMonth
      )
      var glReversalDistributionDTO: GeneralLedgerReversalDistributionDTO
      val glReversalDistributionDTOs = mutableListOf<GeneralLedgerReversalDistributionDTO>()
      var balance = BigDecimal.ZERO

      detailList.forEach {
         glReversalDistributionDTO = GeneralLedgerReversalDistributionDTO(
            null,
            SimpleIdentifiableDTO(glReversalDTO),
            SimpleIdentifiableDTO(it.account!!.id),
            SimpleLegacyIdentifiableDTO(it.profitCenter!!.id),
            it.amount!!.times(BigDecimal(-1))
         )
         glReversalDistributionDTOs.add(glReversalDistributionDTO)

         balance += glReversalDistributionDTO.generalLedgerReversalDistributionAmount!!
      }

      generalLedgerReversalEntryService.create(GeneralLedgerReversalEntryDTO(glReversalDTO, glReversalDistributionDTOs, balance), company)
   }

   fun postReversalEntry(id: UUID, user: User, locale: Locale) {
      val entity = generalLedgerReversalEntryRepository.findOne(id, user.myCompany())

      // create GL details
      val journalEntryNumber = generalLedgerDetailRepository.findNextJENumber(user.myCompany())
      var glDetailDTO: GeneralLedgerDetailDTO
      entity!!.generalLedgerReversalDistributions.forEach { distribution ->
         glDetailDTO = GeneralLedgerDetailDTO(
            null,
            AccountDTO(distribution.generalLedgerReversalDistributionAccount.id),
            distribution.generalLedgerReversal.reversalDate,
            StoreDTO(distribution.generalLedgerReversalDistributionProfitCenter),
            GeneralLedgerSourceCodeDTO(distribution.generalLedgerReversal.source),
            distribution.generalLedgerReversalDistributionAmount,
            distribution.generalLedgerReversal.comment,
            user.myEmployeeNumber(),
            journalEntryNumber
         )

         glDetailDTO = create(glDetailDTO, user.myCompany())

         // post glDetailDTO to GL summary
         val glAccountPostingDTO = GeneralLedgerAccountPostingDTO(glDetailDTO)
         postEntry(glAccountPostingDTO, user.myCompany(), locale)
      }

      // delete GL reversal records
      entity.generalLedgerReversal.id?.let { generalLedgerReversalEntryRepository.delete(it, user.myCompany()) }
   }

   private fun transformEntity(generalLedgerDetail: GeneralLedgerDetailEntity): GeneralLedgerDetailDTO {
      return GeneralLedgerDetailDTO(entity = generalLedgerDetail)
   }

   fun postEntry(dto: GeneralLedgerAccountPostingDTO, company: CompanyEntity, locale: Locale): GeneralLedgerAccountPostingResponseDTO {
      val generalLedgerDetail = dto.glDetail!!
      val cal = financialCalendarService.fetchByDate(company, generalLedgerDetail.date!!)!!
      val overallPeriod = cal.overallPeriod!!.value
      val summaryUpdated : UUID?
      val bankRecon : UUID?
      // If summary record not found, create them, then set the appropriate one
      var summary = generalLedgerSummaryService.fetchOneByBusinessKey(company, generalLedgerDetail.account?.id!!, generalLedgerDetail.profitCenter?.storeNumber!!, overallPeriod)
         ?:
         createSummaries(generalLedgerDetail, company, overallPeriod)
      if (cal.generalLedgerOpen == false) {
         val errors: Set<ValidationError> = mutableSetOf(ValidationError("GL not open for this period.", GLNotOpen(cal.periodFrom!!)))
         throw ValidationException(errors)
      } else {
         val glSourceCode = generalLedgerSourceCodeService.fetchById(generalLedgerDetail.source?.id!!, company)
         summaryUpdated = if (glSourceCode?.value == "BAL") {
            summary!!.beginningBalance?.plus(generalLedgerDetail.amount!!)
            generalLedgerSummaryService.update(summary.id!!, summary, company).id
         } else {
            //get netActivityPeriodX where is X is financialCalendar.period and add glDetail.amount to that netActivityPeriod
            val netActivity = "netActivityPeriod" + cal.period
            val prop = GeneralLedgerSummaryDTO::class.memberProperties.find { it -> it.name == netActivity }!!
            if (prop is KMutableProperty1) {
               val netActivityAmount: BigDecimal = (prop as KMutableProperty1<GeneralLedgerSummaryDTO, BigDecimal?>).get(summary!!) ?: BigDecimal.ZERO
               val result = (generalLedgerDetail.amount)?.plus((netActivityAmount))
               (prop as KMutableProperty1<GeneralLedgerSummaryDTO, BigDecimal?>).set(summary, result)
            }

            generalLedgerSummaryService.update(summary?.id!!, summary, company).id
         }
         val glAccount = accountService.fetchById(generalLedgerDetail.account!!.id!!, company, locale)
         val checkCodes = listOf("AP", "SUM", "BAL")
         bankRecon = if (glAccount?.bankId != null && glSourceCode!!.value !in checkCodes) {
            val bank = bankService.fetchByGLAccount(generalLedgerDetail.account!!.id!!, company)
            val bankType = dto.bankType ?: BankReconciliationTypeDTO("M", "Miscellaneous")
            val bankReconciliationDto = BankReconciliationDTO(
               null, SimpleIdentifiableDTO(bank!!.myId()),
               bankType,
               generalLedgerDetail.date,
               null,
               generalLedgerDetail.amount,
               "GL " + generalLedgerDetail.profitCenter!!.id + " " + glSourceCode.value,
               generalLedgerDetail.date.toString().replace("-","")
            )
            bankReconciliationService.create(bankReconciliationDto, company).id
         } else null
         return GeneralLedgerAccountPostingResponseDTO(summaryUpdated, bankRecon)
      }
   }
   fun createSummaries(generalLedgerDetail: GeneralLedgerDetailDTO, company: CompanyEntity, currentOverallPeriod: String): GeneralLedgerSummaryDTO? {
      val overallPeriodTypes = overallPeriodTypeService.fetchAll()
      var createdSummaryDTO : GeneralLedgerSummaryDTO? = null
      var summaryDTO : GeneralLedgerSummaryDTO
      for (type in overallPeriodTypes) {
         summaryDTO = GeneralLedgerSummaryDTO(
            null,
            SimpleIdentifiableDTO(generalLedgerDetail.account?.id!!),
            SimpleLegacyIdentifiableDTO(generalLedgerDetail.profitCenter?.myId()),
            OverallPeriodTypeDTO(type),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
         )
         if (summaryDTO.overallPeriod!!.value == currentOverallPeriod) {
            createdSummaryDTO = generalLedgerSummaryService.create(summaryDTO, company)
         } else
         generalLedgerSummaryService.create(summaryDTO, company)
      }
      return createdSummaryDTO
   }

   fun fetchNetChange(company: CompanyEntity, filterRequest: GeneralLedgerDetailFilterRequest): GeneralLedgerNetChangeDTO? {
      return generalLedgerDetailRepository.findNetChange(company, filterRequest)
   }

   fun purge(purgeDTO: GeneralLedgerDetailPostPurgeDTO, company: CompanyEntity): Int {
      logger.trace("Service - findAllPurgePost using: from {} thru {} source {}", purgeDTO.fromDate, purgeDTO.thruDate, purgeDTO.sourceCode)

      val validPurgeDetails = generalLedgerDetailValidator.validatePurgeDetails(purgeDTO, company)

      val toPurge = generalLedgerDetailRepository.findAllPurgePost(company, validPurgeDetails)

      if (toPurge.isEmpty()) {
         throw NotFoundException("A List of Matching General Ledger Entries")
      } else {
         val purgedCount = generalLedgerDetailRepository.bulkDelete(toPurge, company)
         if (purgedCount != 0) {
            generalLedgerSummaryService.recalculateGLBalance(company)
         }
         return purgedCount
      }
   }

   fun purgeCount(purgeDTO: GeneralLedgerDetailPostPurgeDTO, company: CompanyEntity): GeneralLedgerDetailPurgeCountDTO {
      logger.trace("Service - findPurgePostCounts using: from {} thru {} source {}", purgeDTO.fromDate, purgeDTO.thruDate, purgeDTO.sourceCode)

      val validPurgeDetails = generalLedgerDetailValidator.validatePurgeDetails(purgeDTO, company)

      return generalLedgerDetailRepository.findPurgeCounts(company, validPurgeDetails)
   }
}
