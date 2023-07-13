package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.AccountFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.vendor.VendorTypeDTO
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit
import java.util.Locale
import java.util.UUID
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileWriter
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File

@Singleton
class AccountService @Inject constructor(
   private val accountRepository: AccountRepository,
   private val accountValidator: AccountValidator,
   private val bankRepository: BankRepository,
   private val localizationService: LocalizationService,
   @Value("\${cynergi.process.update.isam.account}") private val processUpdateIsamAccount: Boolean
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountService::class.java)

   fun fetchById(id: UUID, company: CompanyEntity, locale: Locale): AccountDTO? =
      accountRepository.findOne(id, company)?.let { transformEntity(it, locale) }

   fun fetchAll(company: CompanyEntity, filterRequest: AccountFilterRequest, locale: Locale): Page<AccountDTO> {
      val found = accountRepository.findAll(company, filterRequest)

      return found.toPage { account: AccountEntity ->
         transformEntity(account, locale)
      }
   }

   fun search(company: CompanyEntity, pageRequest: SearchPageRequest, locale: Locale): Page<AccountDTO> {
      val found = accountRepository.search(company, pageRequest)

      return found.toPage { account: AccountEntity ->
         transformEntity(account, locale)
      }
   }

   fun create(dto: AccountDTO, company: CompanyEntity, locale: Locale): AccountDTO {
      val toCreate = accountValidator.validateCreate(dto, company)

      val newAccount = transformEntity(accountRepository.insert(toCreate, company), locale)
      accountToISAM("I", newAccount, company)
      return newAccount

   }

   fun update(id: UUID, dto: AccountDTO, company: CompanyEntity, locale: Locale): AccountDTO {
      val toUpdate = accountValidator.validateUpdate(id, dto, company)

      val updatedAccount = transformEntity(accountRepository.update(toUpdate, company), locale)
      accountToISAM("U", updatedAccount, company)
      return updatedAccount
   }


   private fun transformEntity(accountEntity: AccountEntity, locale: Locale): AccountDTO {
      val localizedTypeDescription = accountEntity.type.localizeMyDescription(locale, localizationService)
      val localizedAccountBalanceTypeDescription = accountEntity.normalAccountBalance.localizeMyDescription(locale, localizationService)
      val localizedStatusDescription = accountEntity.status.localizeMyDescription(locale, localizationService)

      return AccountDTO(
         accountEntity = accountEntity,
         type = AccountTypeDTO(accountEntity.type, localizedTypeDescription),
         normalAccountBalance = NormalAccountBalanceTypeDTO(accountEntity.normalAccountBalance, localizedAccountBalanceTypeDescription),
         status = AccountStatusTypeValueDTO(accountEntity.status, localizedStatusDescription),
         form1099Field = accountEntity.form1099Field?.let { VendorTypeDTO(it) }
      )
   }

   fun delete(id: UUID, company: CompanyEntity, locale: Locale) {
      val deletedAccount = accountRepository.findOne(id, company)?.let {transformEntity(it, locale) }
      accountRepository.delete(id, company)
      accountToISAM("D", deletedAccount!!, company)
   }

   fun accountToISAM(task: String, account: AccountDTO, company: CompanyEntity) {
      var fileWriter: FileWriter? = null
      var csvPrinter: CSVPrinter? = null
      var bankInd: String
      var bankNbr: Int

      val fileName = File.createTempFile("mracct", ".csv")

      try {
         fileWriter = FileWriter(fileName)
         csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT.withDelimiter('|').withHeader("action", "account_number", "type", "status", "normal_balance", "1099_field", "bank_ind", "bank_number", "dummy_field"))

         if (account.bankId != null) {
            var bank = bankRepository.findOne(account.bankId!!, company)
            bankInd = "Y"
            bankNbr = bank!!.number.toInt()
         } else {
            bankInd = "N"
            bankNbr = 0
         }

         var data = listOf("action", "account_number", "type", "status", "normal_balance", "1099_field", "bank_ind", "bank_number", "dummy_field")

         data = listOf(
            task,
            account.number.toString(),
            account.type!!.value!!,
            account.status!!.value!!,
            account.normalAccountBalance!!.value!!,
            account.form1099Field?.value.toString(),
            bankInd,
            bankNbr.toString(),
            "1")
         csvPrinter.printRecord(data)

      } catch (e: Exception) {
         logger.error("Error occurred in creating account csv file!", e)
      } finally {
         try {
            fileWriter!!.flush()
            fileWriter.close()
            csvPrinter!!.close()
            if (processUpdateIsamAccount) {
               val processExecutor: ProcessExecutor = ProcessExecutor()
                  .command("/bin/bash", "/usr/bin/ht.updt_isam_account.sh", fileName.canonicalPath)
                  .exitValueNormal()
                  .timeout(5, TimeUnit.SECONDS)
                  .readOutput(true)
               logger.debug(processExecutor.execute().outputString())
            }
         } catch (e: Throwable) {
            logger.error("Error occurred in creating account csv file!", e)
         }
      }
   }


}
