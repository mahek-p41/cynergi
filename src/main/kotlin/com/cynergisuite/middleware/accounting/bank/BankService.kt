package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class BankService @Inject constructor(
   private val bankRepository: BankRepository,
   private val bankValidator: BankValidator,
   private val localizationService: LocalizationService
) {
   fun fetchById(id: Long, company: Company, locale: Locale): BankDTO? =
      bankRepository.findOne(id, company)?.let { transformEntity(it, locale) }

   @Validated
   fun fetchAll(company: Company, @Valid pageRequest: PageRequest, locale: Locale): Page<BankDTO> {
      val found = bankRepository.findAll(company, pageRequest)

      return found.toPage { bank: BankEntity ->
         transformEntity(bank, locale)
      }
   }

   @Validated
   fun create(@Valid bankDTO: BankDTO, company: Company, locale: Locale): BankDTO {
      val toCreate = bankValidator.validateCreate(bankDTO, company)

      return transformEntity(bankRepository.insert(toCreate), locale)
   }

   @Validated
   fun update(id: Long, @Valid bankDTO: BankDTO, company: Company, locale: Locale): BankDTO {
      val toUpdate = bankValidator.validateUpdate(id, bankDTO, company)

      return transformEntity(bankRepository.update(toUpdate), locale)
   }

   private fun transformEntity(bankEntity: BankEntity, locale: Locale): BankDTO {
      val localizedDescription = bankEntity.currency.localizeMyDescription(locale, localizationService)

      return BankDTO(bankEntity).copy(currency = BankCurrencyTypeValueObject(bankEntity.currency, localizedDescription))
   }
}

