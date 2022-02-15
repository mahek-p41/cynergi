package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.NotFound
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class BankReconciliationValidator @Inject constructor(
   private val bankReconciliationRepository: BankReconciliationRepository,
   private val bankReconciliationTypeRepository: BankReconciliationTypeRepository,
   private val bankRepository: BankRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(BankReconciliationValidator::class.java)

   fun validateCreate(dto: BankReconciliationDTO, company: CompanyEntity): BankReconciliationEntity {
      logger.trace("Validating Save BankReconciliation {}", dto)

      return doValidation(dto = dto, company = company)
   }

   fun validateUpdate(id: UUID, dto: BankReconciliationDTO, company: CompanyEntity): BankReconciliationEntity {
      logger.trace("Validating Update BankReconciliation {}", dto)

      val existingBankRecon = bankReconciliationRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doValidation(existingBankRecon, dto, company)
   }

   private fun doValidation(existingBankRecon: BankReconciliationEntity? = null, dto: BankReconciliationDTO, company: CompanyEntity): BankReconciliationEntity {
      val bank = bankRepository.findOne(dto.bank!!.id!!, company)
      val type = bankReconciliationTypeRepository.findOne(dto.type!!.value)

      doValidation { errors ->
         bank
            ?: errors.add(ValidationError("bank.id", NotFound(dto.bank!!.id!!)))

         type
            ?: errors.add(ValidationError("type.value", NotFound(dto.type!!.value)))
      }

      return if (existingBankRecon != null) {
         BankReconciliationEntity(
            id = existingBankRecon.id,
            dto = dto,
            bank = bank!!,
            type = type!!
         )
      } else {
         BankReconciliationEntity(
            dto = dto,
            bank = bank!!,
            type = type!!
         )
      }
   }
}
