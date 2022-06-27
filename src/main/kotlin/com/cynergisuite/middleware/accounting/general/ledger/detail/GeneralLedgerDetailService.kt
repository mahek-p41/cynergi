package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.GeneralLedgerRecurringEntriesFilterRequest
import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSearchReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeService
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.GeneralLedgerRecurringEntriesDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure.GeneralLedgerRecurringEntriesRepository
import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringRepository
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.GeneralLedgerSummaryService
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.area.GeneralLedger
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.util.UUID
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

@Singleton
class GeneralLedgerDetailService @Inject constructor(
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerDetailValidator: GeneralLedgerDetailValidator,
   private val generalLedgerRecurringEntriesRepository: GeneralLedgerRecurringEntriesRepository,
   private val financialCalendarService: FinancialCalendarService,
   private val generalLedgerSourceCodeService: GeneralLedgerSourceCodeService,
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val generalLedgerSummaryService: GeneralLedgerSummaryService
   private val generalLedgerRecurringRepository: GeneralLedgerRecurringRepository,
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

   fun transfer(user: User, filterRequest: GeneralLedgerRecurringEntriesFilterRequest) {
      val company = user.myCompany()
      val glRecurringEntries = generalLedgerRecurringEntriesRepository.findAll(company, filterRequest)
      var glDetailDTO: GeneralLedgerDetailDTO
      val journalEntryNumber = generalLedgerDetailRepository.findNextJENumber(company)

      glRecurringEntries.forEach {
         // create GL detail for each distribution
         it.generalLedgerRecurringDistributions.forEach { distribution ->
            glDetailDTO = GeneralLedgerDetailDTO(
               null,
               SimpleIdentifiableDTO(distribution.generalLedgerDistributionAccount.id),
               filterRequest.entryDate,
               SimpleLegacyIdentifiableDTO(distribution.generalLedgerDistributionProfitCenter.myId()),
               SimpleIdentifiableDTO(it.generalLedgerRecurring.source),
               distribution.generalLedgerDistributionAmount,
               it.generalLedgerRecurring.message,
               filterRequest.employeeNumber,
               journalEntryNumber
            )

            create(glDetailDTO, company)
         }

         // update last transfer date in GL recurring
         it.generalLedgerRecurring.lastTransferDate = filterRequest.entryDate

         generalLedgerRecurringRepository.update(it.generalLedgerRecurring, company)
      }
   }

   fun transfer(user: User, dto: GeneralLedgerRecurringEntriesDTO) {
      val company = user.myCompany()
      val glRecurringEntry = generalLedgerRecurringEntriesRepository.findOne(dto.generalLedgerRecurring?.id!!, company)
      var glDetailDTO: GeneralLedgerDetailDTO

      // create GL detail for each distribution
      glRecurringEntry!!.generalLedgerRecurringDistributions.forEach { distribution ->
         glDetailDTO = GeneralLedgerDetailDTO(
            null,
            SimpleIdentifiableDTO(distribution.generalLedgerDistributionAccount.id),
            dto.entryDate,
            SimpleLegacyIdentifiableDTO(distribution.generalLedgerDistributionProfitCenter.myId()),
            SimpleIdentifiableDTO(glRecurringEntry.generalLedgerRecurring.source),
            distribution.generalLedgerDistributionAmount,
            dto.generalLedgerRecurring!!.message,
            user.myEmployeeNumber(),
            null
         )
         create(glDetailDTO, company)
      }

      // update last transfer date in GL recurring
      glRecurringEntry.generalLedgerRecurring.lastTransferDate = dto.entryDate
      generalLedgerRecurringRepository.update(glRecurringEntry.generalLedgerRecurring, company)
   }

   private fun transformEntity(generalLedgerDetail: GeneralLedgerDetailEntity): GeneralLedgerDetailDTO {
      return GeneralLedgerDetailDTO(entity = generalLedgerDetail)
   }

   fun postEntry(generalLedgerDetail: GeneralLedgerDetailDTO, company: CompanyEntity): GeneralLedgerDetailDTO {
      val periodOpen = financialCalendarService.fetchDateRangeWhenGLIsOpen(company)
      val cal = financialCalendarService.fetchByDate(company, generalLedgerDetail.date!!)!!
      val overallPeriod =
         when (cal.overallPeriod?.value) {
            "R" -> 1
            "P" -> 2
            "C" -> 3
            else -> 4
         }

      var summary = generalLedgerSummaryService.fetchOneByBusinessKey(company, generalLedgerDetail.account?.id!!, generalLedgerDetail.profitCenter?.myId()!!, overallPeriod)

      if(cal.generalLedgerOpen == true) {
      } else {
         // gl not open for this period. Posting aborted
      }
      val glSourceCode = generalLedgerSourceCodeService.fetchById(generalLedgerDetail.source?.id!!, company)
      if(glSourceCode?.value == "BAL") {
         summary!!.beginningBalance?.plus(generalLedgerDetail.amount!!)
         generalLedgerSummaryService.update(summary.id!!, summary, company)
      } else {
         val netActivity  = "netActivityPeriod" + cal.period.toString()
         var prop = GeneralLedgerSummaryDTO::class.memberProperties.find { it -> it.name == netActivity}!!
         if(prop is KMutableProperty1) {
            var test: BigDecimal? = (prop as KMutableProperty1<GeneralLedgerSummaryDTO, BigDecimal>).get(summary!!)
            var result = (generalLedgerDetail.amount?: BigDecimal.ZERO) + (test?: BigDecimal.ZERO)
            (prop as KMutableProperty1<GeneralLedgerSummaryDTO, Any>).set(summary!!,  result)
         }

         generalLedgerSummaryService.update(summary?.id!!, summary!!, company)
      }
      return generalLedgerDetail
      }
}
