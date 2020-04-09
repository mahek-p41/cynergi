package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.company.Company
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class BankService @Inject constructor(
   private val bankRepository: BankRepository,
   private val bankValidator: BankValidator
) {
   fun fetchById(id: Long, company: Company): BankValueObject? =
      bankRepository.findOne(id, company)?.let { BankValueObject(it) }

   @Validated
   fun fetchAll(company: Company, @Valid pageRequest: PageRequest): Page<BankValueObject> {
      val found = bankRepository.findAll(company, pageRequest)

      return found.toPage { bank: BankEntity ->
         BankValueObject(bank)
      }
   }

   @Validated
   fun create(@Valid bankDTO: BankDTO, company: Company): BankValueObject {
      val toCreate = bankValidator.validateCreate(bankDTO, company)

      return BankValueObject(bankRepository.insert(toCreate))
   }

   @Validated
   fun update(id: Long, @Valid bankVO: BankDTO): BankValueObject {
      val toUpdate = bankValidator.validateUpdate(id, bankVO)

      return BankValueObject(bankRepository.update(toUpdate))
   }
}
