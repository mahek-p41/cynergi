package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class AccountService @Inject constructor(
   private val accountRepository: AccountRepository,
   private val accountValidator: AccountValidator,
   private val localizationService: LocalizationService
) {
   fun fetchById(id: Long, company: Company, locale: Locale): AccountDTO? =
      accountRepository.findOne(id, company)?.let { transformEntity(it, locale) }

   @Validated
   fun fetchAll(company: Company, @Valid pageRequest: PageRequest, locale: Locale): Page<AccountDTO> {
      val found = accountRepository.findAll(company, pageRequest)

      return found.toPage { account: AccountEntity ->
         transformEntity(account, locale)
      }
   }

   @Validated
   fun create(@Valid accountDTO: AccountDTO, company: Company, locale: Locale): AccountDTO {
      val toCreate = accountValidator.validateCreate(accountDTO, company)

      return transformEntity(accountRepository.insert(toCreate), locale)
   }

   @Validated
   fun update(id: Long, @Valid accountVO: AccountDTO, company: Company, locale: Locale): AccountDTO {
      val toUpdate = accountValidator.validateUpdate(id, accountVO, company)

      return transformEntity(accountRepository.update(toUpdate), locale)
   }

   private fun transformEntity(accountEntity: AccountEntity, locale: Locale): AccountDTO {
      val localizedTypeDescription = accountEntity.type.localizeMyDescription(locale, localizationService)
      val localizedAccountBalanceTypeDescription = accountEntity.normalAccountBalance.localizeMyDescription(locale, localizationService)
      val localizedStatusDescription = accountEntity.status.localizeMyDescription(locale, localizationService)

      return AccountDTO(
         accountEntity = accountEntity,
         type = AccountTypeValueObject(accountEntity.type, localizedTypeDescription),
         normalAccountBalance = NormalAccountBalanceTypeValueObject(accountEntity.normalAccountBalance, localizedAccountBalanceTypeDescription),
         status = AccountStatusTypeValueObject(accountEntity.status, localizedStatusDescription)
      )
   }
}
