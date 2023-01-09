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
      val reportNetChange = GeneralLedgerNetChangeDTO(BigDecimal.ZERO, BigDecimal.ZERO, emptyList, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
      val reportTemplate = GeneralLedgerProfitCenterTrialBalanceReportTemplate(null, reportNetChange, null)
      val overallPeriodId = financialCalendarRepository.findOverallPeriodIdAndPeriod(company, filterRequest.fromDate!!).first
      var accountId: UUID? = null
      var profitCenterNumber: Int? = null
      val emptyAccountDetails = mutableListOf<GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO>()
      var accountDetails = mutableListOf<GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO>()
      val locationDetails = mutableListOf<GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO>()
      if (glSummaries.isNotEmpty()) {
         glSummaries.forEach { glSummary ->
            // first glSummary
            if (accountId == null && profitCenterNumber == null) {
               accountId = glSummary.account.id
               profitCenterNumber = glSummary.profitCenter.myNumber()
               val reportDetails = generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(company, filterRequest, glSummary)
               val accountNetChange = GeneralLedgerNetChangeDTO(BigDecimal.ZERO, BigDecimal.ZERO, emptyList, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
               accountDetails.add(GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(AccountDTO(glSummary.account), reportDetails, accountNetChange))
            }
            // different location and same/different account
            else if (glSummary.profitCenter.myNumber() != profitCenterNumber) {
               val locationNetChange = GeneralLedgerNetChangeDTO(BigDecimal.ZERO, BigDecimal.ZERO, emptyList, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
               locationDetails.add(GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO(profitCenterNumber, accountDetails, locationNetChange))
               accountDetails = emptyAccountDetails.map { it.copy() }.toMutableList()

               accountId = glSummary.account.id
               profitCenterNumber = glSummary.profitCenter.myNumber()
               val reportDetails = generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(company, filterRequest, glSummary)
               val accountNetChange = GeneralLedgerNetChangeDTO(BigDecimal.ZERO, BigDecimal.ZERO, emptyList, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
               accountDetails.add(GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(AccountDTO(glSummary.account), reportDetails, accountNetChange))
            }
            // same location and different account
            else if (glSummary.account.id != accountId) {
               accountId = glSummary.account.id
               //profitCenterNumber = it.profitCenter.myNumber()
               val reportDetails = generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(company, filterRequest, glSummary)
               val accountNetChange = GeneralLedgerNetChangeDTO(BigDecimal.ZERO, BigDecimal.ZERO, emptyList, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
               accountDetails.add(GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(AccountDTO(glSummary.account), reportDetails, accountNetChange))
            }
            // same location and same account - continue to next GL summary
         }
         val locationNetChange = GeneralLedgerNetChangeDTO(BigDecimal.ZERO, BigDecimal.ZERO, emptyList, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
         locationDetails.add(GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO(profitCenterNumber, accountDetails, locationNetChange))

         // calculate account, location, and report totals
         locationDetails.forEach { loc ->
            loc.accountDetailList?.forEach { acct ->
               acct.accountTotals = generalLedgerDetailRepository.findNetChangeProfitCenterTrialBalanceReport(company, filterRequest.fromDate, filterRequest.thruDate, loc.profitCenter, acct.account!!.number, overallPeriodId)

               loc.locationTotals!!.debit += acct.accountTotals!!.debit
               loc.locationTotals!!.credit += acct.accountTotals!!.credit
               loc.locationTotals!!.netChange += acct.accountTotals!!.netChange

               reportTemplate.reportTotals!!.beginBalance += acct.accountTotals!!.beginBalance
               reportTemplate.reportTotals!!.debit += acct.accountTotals!!.debit
               reportTemplate.reportTotals!!.credit += acct.accountTotals!!.credit
               reportTemplate.reportTotals!!.netChange += acct.accountTotals!!.netChange
               reportTemplate.reportTotals!!.endBalance += acct.accountTotals!!.endBalance
            }
         }

         reportTemplate.locationDetailList = locationDetails

         // calculate end of report totals
         reportTemplate.endOfReportTotals = generalLedgerDetailRepository.fetchTrialBalanceEndOfReportTotals(company, filterRequest.fromDate, filterRequest.thruDate, filterRequest.startingAccount, filterRequest.endingAccount, overallPeriodId)
      }

      return reportTemplate
   }

   private fun transformEntity(generalLedgerSummary: GeneralLedgerSummaryEntity): GeneralLedgerSummaryDTO {
      return GeneralLedgerSummaryDTO(entity = generalLedgerSummary)
   }
}
