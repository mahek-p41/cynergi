package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.GeneralLedgerProfitCenterTrialBalanceReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.financial.calendar.infrastructure.FinancialCalendarRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeService
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceReportExportDTO
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.GeneralLedgerProfitCenterTrialBalanceReportTemplate
import com.cynergisuite.middleware.company.CompanyEntity
import com.opencsv.CSVWriter
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.math.BigDecimal
import java.util.UUID

@Singleton
class GeneralLedgerSummaryService @Inject constructor(
   private val financialCalendarRepository: FinancialCalendarRepository,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val generalLedgerSummaryValidator: GeneralLedgerSummaryValidator,
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

   fun fetchOneByBusinessKey(
      company: CompanyEntity,
      accountId: UUID,
      storeNumber: Int,
      overallPeriodValue: String
   ): GeneralLedgerSummaryDTO? {
      return generalLedgerSummaryRepository.findOneByBusinessKey(company, accountId, storeNumber, overallPeriodValue)
         ?.let { transformEntity(it) }
   }

   fun fetchProfitCenterTrialBalanceReportRecords(
      company: CompanyEntity,
      filterRequest: GeneralLedgerProfitCenterTrialBalanceReportFilterRequest
   ): GeneralLedgerProfitCenterTrialBalanceReportTemplate {
      val emptyList = listOf<BigDecimal>()
      val reportNetChange = GeneralLedgerNetChangeDTO(
         BigDecimal.ZERO,
         BigDecimal.ZERO,
         emptyList,
         BigDecimal.ZERO,
         BigDecimal.ZERO,
         BigDecimal.ZERO
      )
      val reportTemplate = GeneralLedgerProfitCenterTrialBalanceReportTemplate(null, reportNetChange, null)
      val pair = financialCalendarRepository.findOverallPeriodIdAndPeriod(company, filterRequest.fromDate!!)

      if (pair != null) {
         val glSummaries =
            generalLedgerSummaryRepository.fetchProfitCenterTrialBalanceReportRecords(company, filterRequest, pair)
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
                  val reportDetails = generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(
                     company,
                     filterRequest,
                     glSummary
                  )
                  val accountNetChange = GeneralLedgerNetChangeDTO(
                     BigDecimal.ZERO,
                     BigDecimal.ZERO,
                     emptyList,
                     BigDecimal.ZERO,
                     BigDecimal.ZERO,
                     BigDecimal.ZERO
                  )
                  accountDetails.add(
                     GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(
                        AccountDTO(glSummary.account),
                        reportDetails,
                        accountNetChange
                     )
                  )
               }
               // different location and same/different account
               else if (glSummary.profitCenter.myNumber() != profitCenterNumber) {
                  val locationNetChange = GeneralLedgerNetChangeDTO(
                     BigDecimal.ZERO,
                     BigDecimal.ZERO,
                     emptyList,
                     BigDecimal.ZERO,
                     BigDecimal.ZERO,
                     BigDecimal.ZERO
                  )
                  locationDetails.add(
                     GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO(
                        profitCenterNumber,
                        accountDetails,
                        locationNetChange
                     )
                  )
                  accountDetails = emptyAccountDetails.map { it.copy() }.toMutableList()

                  accountId = glSummary.account.id
                  profitCenterNumber = glSummary.profitCenter.myNumber()
                  val reportDetails = generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(
                     company,
                     filterRequest,
                     glSummary
                  )
                  val accountNetChange = GeneralLedgerNetChangeDTO(
                     BigDecimal.ZERO,
                     BigDecimal.ZERO,
                     emptyList,
                     BigDecimal.ZERO,
                     BigDecimal.ZERO,
                     BigDecimal.ZERO
                  )
                  accountDetails.add(
                     GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(
                        AccountDTO(glSummary.account),
                        reportDetails,
                        accountNetChange
                     )
                  )
               }
               // same location and different account
               else if (glSummary.account.id != accountId) {
                  accountId = glSummary.account.id
                  val reportDetails = generalLedgerDetailRepository.fetchProfitCenterTrialBalanceReportDetails(
                     company,
                     filterRequest,
                     glSummary
                  )
                  val accountNetChange = GeneralLedgerNetChangeDTO(
                     BigDecimal.ZERO,
                     BigDecimal.ZERO,
                     emptyList,
                     BigDecimal.ZERO,
                     BigDecimal.ZERO,
                     BigDecimal.ZERO
                  )
                  accountDetails.add(
                     GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(
                        AccountDTO(glSummary.account),
                        reportDetails,
                        accountNetChange
                     )
                  )
               }
               // same location and same account - continue to next GL summary
            }
            val locationNetChange = GeneralLedgerNetChangeDTO(
               BigDecimal.ZERO,
               BigDecimal.ZERO,
               emptyList,
               BigDecimal.ZERO,
               BigDecimal.ZERO,
               BigDecimal.ZERO
            )
            locationDetails.add(
               GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO(
                  profitCenterNumber,
                  accountDetails,
                  locationNetChange
               )
            )

            // calculate account, location, and report totals
            locationDetails.forEach { loc ->
               loc.accountDetailList?.forEach { acct ->
                  acct.accountTotals = generalLedgerDetailRepository.findNetChangeProfitCenterTrialBalanceReport(
                     company,
                     filterRequest.fromDate,
                     filterRequest.thruDate,
                     loc.profitCenter,
                     acct.account!!.number,
                     pair.first
                  )

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
            reportTemplate.endOfReportTotals = generalLedgerDetailRepository.fetchTrialBalanceEndOfReportTotals(
               company,
               filterRequest,
               pair.first
            )
         }
      }

      return reportTemplate
   }

   fun transformProfitCenterTrialBalanceReport(
      company: CompanyEntity,
      filterRequest: GeneralLedgerProfitCenterTrialBalanceReportFilterRequest
   ): List<GeneralLedgerProfitCenterTrialBalanceReportExportDTO> {
      val reportTemplate = fetchProfitCenterTrialBalanceReportRecords(company, filterRequest)
      val exportDTOs = mutableListOf<GeneralLedgerProfitCenterTrialBalanceReportExportDTO>()

      reportTemplate.locationDetailList?.forEach { loc ->
         loc.accountDetailList?.forEach { acct ->
            acct.reportDetailList?.forEach { det ->
               val src = if (det.source?.value != null) det.source?.value else ""
               val jeNbr = if (det.journalEntryNumber != null) " ${det.journalEntryNumber}" else ""
               val msg = if (det.message != null) " ${det.message}" else ""

               exportDTOs.add(
                  GeneralLedgerProfitCenterTrialBalanceReportExportDTO(
                     "\"D\"",
                     loc.profitCenter,
                     acct.account?.number,
                     null,
                     det.date,
                     "\"$src$jeNbr$msg\"",
                     det.amount?.setScale(2),
                     null,
                     null,
                     null
                  )
               )
            }

            exportDTOs.add(
               GeneralLedgerProfitCenterTrialBalanceReportExportDTO(
                  "\"T\"",
                  loc.profitCenter,
                  acct.account?.number,
                  "\"${acct.account?.name}\"",
                  null,
                  null,
                  null,
                  acct.accountTotals?.beginBalance?.setScale(2),
                  acct.accountTotals?.endBalance?.setScale(2),
                  acct.accountTotals?.netChange?.setScale(2)
               )
            )
         }
      }

      return exportDTOs
   }

   fun exportProfitCenterTrialBalanceReport(
      company: CompanyEntity,
      filterRequest: GeneralLedgerProfitCenterTrialBalanceReportFilterRequest
   ): ByteArray {
      val found = transformProfitCenterTrialBalanceReport(company, filterRequest)
      val stream = ByteArrayOutputStream()
      val output = OutputStreamWriter(stream)
      val csvWriter =
         CSVWriter(output, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER)

      val headers = arrayOf(
         "Export_Type", "Profit_Center", "Account_Nbr", "Account_Name", "GL_Date",
         "Description", "GL_Amount", "Begin_Balance", "End_Balance", "Net_Change"
      )
      csvWriter.writeNext(headers)

      for (element in found) {
         val date = element.glDate.toString()
         val dateElements = date.split('-')

         val data = arrayOf<String>(
            element.exportType.toString(),
            element.profitCenter.toString(),
            element.accountNbr.toString(),
            if (element.accountName != null) element.accountName.toString() else "",
            if (element.glDate != null) ("${dateElements[1]}/${dateElements[2]}/${dateElements[0].drop(2)}") else "",
            if (element.description != null) element.description.toString() else "",
            if (element.glAmount != null) element.glAmount.toString() else "",
            if (element.beginBalance != null) element.beginBalance.toString() else "",
            if (element.endBalance != null) element.endBalance.toString() else "",
            if (element.netChange != null) element.netChange.toString() else ""
         )
         csvWriter.writeNext(data)
      }
      csvWriter.close()
      output.close()
      return stream.toByteArray()
   }

   fun recalculateGLBalance(company: CompanyEntity) {
      generalLedgerSummaryRepository.resetGLBalance(company)
      generalLedgerSummaryRepository.recalculateGLBalance(company)
      generalLedgerSummaryRepository.setNetActivityPeriods(company)
   }

   private fun transformEntity(generalLedgerSummary: GeneralLedgerSummaryEntity): GeneralLedgerSummaryDTO {
      return GeneralLedgerSummaryDTO(entity = generalLedgerSummary)
   }
}
