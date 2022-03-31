package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.vendor.VendorTypeDTO
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale
import java.util.UUID

@Singleton
class AccountService @Inject constructor(
   private val accountRepository: AccountRepository,
   private val accountValidator: AccountValidator,
   private val localizationService: LocalizationService
) {
   fun fetchById(id: UUID, company: CompanyEntity, locale: Locale): AccountDTO? =
      accountRepository.findOne(id, company)?.let { transformEntity(it, locale) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest, locale: Locale): Page<AccountDTO> {
      val found = accountRepository.findAll(company, pageRequest)

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

      return transformEntity(accountRepository.insert(toCreate, company), locale)
   }

   fun update(id: UUID, dto: AccountDTO, company: CompanyEntity, locale: Locale): AccountDTO {
      val toUpdate = accountValidator.validateUpdate(id, dto, company)

      return transformEntity(accountRepository.update(toUpdate, company), locale)
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
         form1099Field = VendorTypeDTO(accountEntity.form1099Field)
      )
   }

   fun delete(id: UUID, company: CompanyEntity) {
      accountRepository.delete(id, company)
   }
}
