package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankReconciliationValidator @Inject constructor(
   private val bankReconciliationRepository: BankReconciliationRepository,
   private val bankReconciliationTypeRepository: BankReconciliationTypeRepository,
   private val bankRepository: BankRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(BankReconciliationValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(dto: BankReconciliationDTO, company: Company): BankReconciliationEntity {
      logger.trace("Validating Save BankReconciliation {}", dto)

      return doValidation(dto = dto, company = company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, dto: BankReconciliationDTO, company: Company): BankReconciliationEntity {
      logger.trace("Validating Update BankReconciliation {}", dto)

      val existingBankRecon = bankReconciliationRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doValidation(existingBankRecon, dto, company)
   }

   private fun doValidation(existingBankRecon: BankReconciliationEntity? = null, dto: BankReconciliationDTO, company: Company): BankReconciliationEntity {
      val bank = bankRepository.findOne(dto.bank!!.id!!, company)
      val type = bankReconciliationTypeRepository.findOne(dto.type!!.id!!)

      doValidation { errors ->
         bank ?: errors.add(ValidationError("bank.id", NotFound(dto.bank!!.id!!)))
         type ?: errors.add(ValidationError("type.id", NotFound(dto.type!!.id!!)))
      }

      return if (existingBankRecon != null) {
         BankReconciliationEntity(
            id = existingBankRecon.id,
            company = company,
            dto = dto,
            bank = bank!!,
            type = type!!
         )
      } else {
         BankReconciliationEntity(
            company = company,
            dto = dto,
            bank = bank!!,
            type = type!!
         )
      }
   }
}
