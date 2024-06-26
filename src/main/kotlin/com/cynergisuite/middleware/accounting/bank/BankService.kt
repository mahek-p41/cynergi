package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.account.AccountService
import com.cynergisuite.middleware.accounting.account.status.AccountStatusTypeValueDTO
import com.cynergisuite.middleware.accounting.account.type.AccountTypeDTO
import com.cynergisuite.middleware.accounting.account.normalAccountBalance.NormalAccountBalanceTypeDTO
import com.cynergisuite.middleware.accounting.account.AccountRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.vendor.VendorTypeDTO
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale
import java.util.UUID

@Singleton
class BankService @Inject constructor(
   private val accountRepository: AccountRepository,
   private val accountService: AccountService,
   private val bankRepository: BankRepository,
   private val bankValidator: BankValidator,
   private val localizationService: LocalizationService,
   @Value("\${cynergi.process.update.isam.account}") private val processUpdateIsamAccount: Boolean
) {
   fun fetchById(id: UUID, company: CompanyEntity, locale: Locale): BankDTO? =
      bankRepository.findOne(id, company)?.let { BankDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<BankDTO> {
      val found = bankRepository.findAll(company, pageRequest)

      return found.toPage { bank: BankEntity -> BankDTO(bank) }
   }

   fun fetchByGLAccount(id: UUID, company: CompanyEntity): BankDTO? =
      bankRepository.findByGlAccount(id, company)?.let{ BankDTO(it) }


   fun create(dto: BankDTO, company: CompanyEntity, locale: Locale): BankDTO {
      val toCreate = bankValidator.validateCreate(dto, company)

      val bankAdded = BankDTO(bankRepository.insert(toCreate))
      val associatedAccount = accountRepository.findOne(bankAdded.generalLedgerAccount!!.id!!, company)?.let {transformAccountEntity(it, locale) }
      if (processUpdateIsamAccount) {
         accountService.accountToISAM("U", associatedAccount!!, company)
      }
      return bankAdded
   }

   fun update(id: UUID, dto: BankDTO, company: CompanyEntity, locale: Locale): BankDTO {
      val toUpdate = bankValidator.validateUpdate(id, dto, company)

      val startingBank = bankRepository.findOne(id, company)
      val startingGLAccountId = startingBank!!.generalLedgerAccount.id

      val bankUpdated = BankDTO(bankRepository.update(toUpdate))
      val endingGLAccount = accountRepository.findOne(bankUpdated.generalLedgerAccount!!.id!!, company)?.let {transformAccountEntity(it, locale) }
      val endingGLAccountId = endingGLAccount!!.id
      if (processUpdateIsamAccount) {
         accountService.accountToISAM("U", endingGLAccount, company)
      }
      if (processUpdateIsamAccount && (startingGLAccountId != endingGLAccountId)) {
         val changedAccount = accountRepository.findOne(startingGLAccountId!!, company)?.let {transformAccountEntity(it, locale) }
         accountService.accountToISAM("U", changedAccount!!, company)
      }

      return bankUpdated
   }

   fun delete(id: UUID, company: CompanyEntity, locale: Locale) {
      val bankBeingDeleted = bankRepository.findOne(id, company)?.let { BankDTO(it) }

      bankRepository.delete(id, company)
      val associatedAccount = accountRepository.findOne(bankBeingDeleted!!.generalLedgerAccount!!.id!!, company)?.let {transformAccountEntity(it, locale) }
      if (processUpdateIsamAccount) {
         accountService.accountToISAM("U", associatedAccount!!, company)
      }
   }

   private fun transformAccountEntity(accountEntity: AccountEntity, locale: Locale): AccountDTO {
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

   fun fetchBalance(id: UUID, company: CompanyEntity): Float? {
      val balance = bankRepository.fetchBalance(id, company)
      return balance
   }
}
