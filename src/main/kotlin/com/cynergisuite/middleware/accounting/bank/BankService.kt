package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
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
   private val bankValidator: BankValidator
) {
   fun fetchById(id: Long, company: Company, locale: Locale): BankDTO? =
      bankRepository.findOne(id, company)?.let { BankDTO(it) }

   @Validated
   fun fetchAll(company: Company, @Valid pageRequest: PageRequest): Page<BankDTO> {
      val found = bankRepository.findAll(company, pageRequest)

      return found.toPage { bank: BankEntity -> BankDTO(bank) }
   }

   @Validated
   fun create(@Valid bankDTO: BankDTO, company: Company): BankDTO {
      val toCreate = bankValidator.validateCreate(bankDTO, company)

      return BankDTO(bankRepository.insert(toCreate))
   }

   @Validated
   fun update(id: Long, @Valid bankDTO: BankDTO, company: Company): BankDTO {
      val toUpdate = bankValidator.validateUpdate(id, bankDTO, company)

      return BankDTO(bankRepository.update(toUpdate))
   }
}

