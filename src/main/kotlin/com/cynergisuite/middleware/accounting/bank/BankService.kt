package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.Company
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankService @Inject constructor(
   private val bankRepository: BankRepository,
   private val bankValidator: BankValidator
) {
   fun fetchById(id: Long, company: Company, locale: Locale): BankDTO? =
      bankRepository.findOne(id, company)?.let { BankDTO(it) }

   fun fetchAll(company: Company, pageRequest: PageRequest): Page<BankDTO> {
      val found = bankRepository.findAll(company, pageRequest)

      return found.toPage { bank: BankEntity -> BankDTO(bank) }
   }

   fun create(dto: BankDTO, company: Company): BankDTO {
      val toCreate = bankValidator.validateCreate(dto, company)

      return BankDTO(bankRepository.insert(toCreate))
   }

   fun update(id: Long, dto: BankDTO, company: Company): BankDTO {
      val toUpdate = bankValidator.validateUpdate(id, dto, company)

      return BankDTO(bankRepository.update(toUpdate))
   }
}
