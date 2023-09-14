package com.cynergisuite.middleware.inload

import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.time.LocalDate
import java.util.UUID

@Singleton
class InloadINVEOMService @Inject constructor(
   private val companyRepository: CompanyRepository,
   private val accountRepository: AccountRepository,

) : CsvInloaderBase(FileSystems.getDefault().getPathMatcher("glob:INVEOM_*")) {

   private val logger: Logger = LoggerFactory.getLogger(InloadINVEOMService::class.java)

   override fun inloadCsv(record: CSVRecord, batchId: UUID) {
      logger.debug("Loading inventory end of month data {}", record)

      // Todo: make some improvements on next task
      val company = companyRepository.findByDataset(record["Data_Set_ID"].trim())!!
      val account = accountRepository.findByNumber(record["Account_Number"].toLong(), company)!!

      val map = mapOf(
         "company_id" to company.id,
         "store_number_sfk" to record["Store_Number"].toInt(),
         "year" to record["Year"].toInt(),
         "month" to record["Month"].toInt(),
         "serial_number" to record["Serial_Number"].toString(),
         "cost" to record["Cost"].toBigDecimal(),
         "net_book_value" to record["Net_Book_Value"].toBigDecimal(),
         "book_depreciation" to record["Book_Depreciation"].toBigDecimal(),
         "asset_account_id" to account.id,
         "contra_asset_account_id" to account.id,
         "model" to record["Model"].toString(),
         "alternate_id" to record["Alternate_Id"].toString(),
         "current_inv_indr" to record["Current_Inv_Indr"].toString(),
         "macrs_previous_fiscal_year_end_cost" to record["Macrs_Previous_Fiscal_Year_End_Cost"].toBigDecimal(),
         "macrs_previous_fiscal_year_end_depr" to record["Macrs_Previous_Fiscal_Year_End_Depr"].toBigDecimal(),
         "macrs_previous_fiscal_year_end_amt_depr" to record["Macrs_Previous_Fiscal_Year_End_Amt_Depr"].toBigDecimal(),
         "macrs_previous_fiscal_year_end_date" to LocalDate.parse(record["Macrs_Previous_Fiscal_Year_End_Date"]),
         "macrs_latest_fiscal_year_end_cost" to record["Macrs_Latest_Fiscal_Year_End_Cost"].toBigDecimal(),
         "macrs_latest_fiscal_year_end_depr" to record["Macrs_Latest_Fiscal_Year_End_Depr"].toBigDecimal(),
         "macrs_latest_fiscal_year_end_amt_depr" to record["Macrs_Latest_Fiscal_Year_End_Amt_Depr"].toBigDecimal(),
         "macrs_previous_fiscal_year_bonus" to record["Macrs_Previous_Fiscal_Year_Bonus"].toBigDecimal(),
         "macrs_latest_fiscal_year_bonus" to record["Macrs_Latest_Fiscal_Year_Bonus"].toBigDecimal(),
         "deleted" to record["Deleted"].toBoolean()
      )
   }
}
