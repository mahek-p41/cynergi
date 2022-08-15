package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale
import java.util.UUID

@Singleton
class BankService @Inject constructor(
   private val bankRepository: BankRepository,
   private val bankValidator: BankValidator
) {
   fun fetchById(id: UUID, company: CompanyEntity, locale: Locale): BankDTO? =
      bankRepository.findOne(id, company)?.let { BankDTO(it) }

   fun fetchAll(company: CompanyEntity, pageRequest: PageRequest): Page<BankDTO> {
      val found = bankRepository.findAll(company, pageRequest)

      return found.toPage { bank: BankEntity -> BankDTO(bank) }
   }

   fun fetchByGLAccount(id: UUID, company: CompanyEntity): BankDTO? =
      bankRepository.findByGlAccount(id, company)?.let{ BankDTO(it) }

   fun create(dto: BankDTO, company: CompanyEntity): BankDTO {
      val toCreate = bankValidator.validateCreate(dto, company)

      return BankDTO(bankRepository.insert(toCreate))
   }

   fun update(id: UUID, dto: BankDTO, company: CompanyEntity): BankDTO {
      val toUpdate = bankValidator.validateUpdate(id, dto, company)

      return BankDTO(bankRepository.update(toUpdate))
   }

   fun delete(id: UUID, company: CompanyEntity) {
      bankRepository.delete(id, company)
   }
}
