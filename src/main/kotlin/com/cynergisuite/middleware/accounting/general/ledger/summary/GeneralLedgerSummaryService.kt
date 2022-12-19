package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.GeneralLedgerProfitCenterTrialBalanceReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceReportDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceReportTemplate
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.math.BigDecimal
import java.util.UUID

@Singleton
class GeneralLedgerSummaryService @Inject constructor(
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val generalLedgerSummaryValidator: GeneralLedgerSummaryValidator
) {
   fun fetchOne(id: UUID, company: CompanyEntity): GeneralLedgerSummaryDTO? {
      return generalLedgerSummaryRepository.findOne(id, company)?.let { transformEntity(it) }
   }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<GeneralLedgerSummaryDTO> {
      val found = generalLedgerSummaryRepository.findAll(company, pageRequest)

      return found.toPage { generalLedgerSummary: GeneralLedgerSummaryEntity ->
         GeneralLedgerSummaryDTO(generalLedgerSummary)
      }
   }

   fun create(dto: GeneralLedgerSummaryDTO, company: CompanyEntity): GeneralLedgerSummaryDTO {
      val toCreate = generalLedgerSummaryValidator.validateCreate(dto, company)

      return transformEntity(generalLedgerSummaryRepository.insert(toCreate, company))
   }

   fun update(id: UUID, dto: GeneralLedgerSummaryDTO, company: CompanyEntity): GeneralLedgerSummaryDTO {
      val toUpdate = generalLedgerSummaryValidator.validateUpdate(id, dto, company)

      return transformEntity(generalLedgerSummaryRepository.update(toUpdate, company))
   }

   fun fetchOneByBusinessKey(company: CompanyEntity, accountId: UUID, storeNumber: Int, overallPeriodValue: String): GeneralLedgerSummaryDTO? {
      return generalLedgerSummaryRepository.findOneByBusinessKey(company, accountId, storeNumber, overallPeriodValue)?.let { transformEntity(it)}
   }

   fun fetchProfitCenterTrialBalanceReportRecords(company: CompanyEntity, filterRequest: GeneralLedgerProfitCenterTrialBalanceReportFilterRequest): GeneralLedgerProfitCenterTrialBalanceReportTemplate {
      val glSummaries = generalLedgerSummaryRepository.fetchProfitCenterTrialBalanceReportRecords(company, filterRequest)
      val emptyList = listOf<BigDecimal>()
      val emptyNetChange = GeneralLedgerNetChangeDTO(BigDecimal.ZERO, BigDecimal.ZERO, emptyList, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
      val reportTemplate = GeneralLedgerProfitCenterTrialBalanceReportTemplate(null, emptyNetChange, null)
      val overallPeriodId = financialCalendarRepository.findOverallPeriodIdAndPeriod(company, filterRequest.startingDate!!).first
      if (glSummaries.isNotEmpty()) {
         // create first account detail from first GL summary
         var accountId = glSummaries[0].account.id
         var profitCenterNumber = glSummaries[0].profitCenter.myNumber()
         val reportDetails = mutableListOf<GeneralLedgerProfitCenterTrialBalanceReportDetailDTO>()
         val accountDetails = mutableListOf<GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO>()
         val locationDetails = mutableListOf<GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO>()
         reportDetails.addAll(generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(company, filterRequest, glSummaries[0]))
         accountDetails.add(GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(AccountDTO(glSummaries[0].account), reportDetails))
         reportDetails.clear()

         // create the rest of the account details and the location details from the rest of the GL summaries
         glSummaries.forEach {
            // different location and same/different account
            if (it.profitCenter.myNumber() != profitCenterNumber) {
               locationDetails.add(GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO(profitCenterNumber, accountDetails, emptyNetChange))
               accountDetails.clear()

               accountId = it.account.id
               profitCenterNumber = it.profitCenter.myNumber()
               reportDetails.addAll(generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(company, filterRequest, it))
               accountDetails.add(GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(AccountDTO(it.account), reportDetails))
               reportDetails.clear()
            }
            // same location and different account
            else if (it.account.id != accountId) {
               accountId = it.account.id
               profitCenterNumber = it.profitCenter.myNumber()
               reportDetails.addAll(generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(company, filterRequest, it))
               accountDetails.add(GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(AccountDTO(it.account), reportDetails))
               reportDetails.clear()
            }
            // same location and same account - continue to next GL summary
         }
         locationDetails.add(GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO(profitCenterNumber, accountDetails))
         accountDetails.clear()

         // calculate account, location, and report totals
         locationDetails.forEach { loc ->
            loc.accountDetailList?.forEach { acct ->
               acct.accountTotals = generalLedgerDetailRepository.findNetChangeProfitCenterTrialBalanceReport(company, filterRequest.startingDate, filterRequest.endingDate, loc.profitCenter, acct.account!!.number, overallPeriodId)!!

               loc.locationTotals!!.debit += acct.accountTotals!!.debit
               loc.locationTotals!!.credit += acct.accountTotals!!.credit

               reportTemplate.reportTotals!!.beginBalance += acct.accountTotals!!.beginBalance
               reportTemplate.reportTotals!!.endBalance += acct.accountTotals!!.endBalance
            }
            loc.locationTotals!!.netChange = loc.locationTotals!!.debit - loc.locationTotals!!.credit

            reportTemplate.reportTotals!!.debit += loc.locationTotals!!.debit
            reportTemplate.reportTotals!!.credit += loc.locationTotals!!.credit
            reportTemplate.reportTotals!!.netChange += loc.locationTotals!!.netChange
         }

         reportTemplate.locationDetailList = locationDetails

         // calculate end of report totals
         reportTemplate.endOfReportTotals = generalLedgerDetailRepository.fetchTrialBalanceEndOfReportTotals(company, filterRequest.startingDate, filterRequest.endingDate, filterRequest.startingAccount, filterRequest.endingAccount, overallPeriodId)
      }

      return reportTemplate
   }

   private fun transformEntity(generalLedgerSummary: GeneralLedgerSummaryEntity): GeneralLedgerSummaryDTO {
      return GeneralLedgerSummaryDTO(entity = generalLedgerSummary)
   }
}
