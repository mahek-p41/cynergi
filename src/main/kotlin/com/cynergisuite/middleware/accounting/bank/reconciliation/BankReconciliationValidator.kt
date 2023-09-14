package com.cynergisuite.middleware.accounting.bank.reconciliation

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.bank.infrastructure.BankRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.infrastructure.BankReconciliationRepository
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.ClearedDateMustNotBeFutureDate
import com.cynergisuite.middleware.localization.ClearedDateNotPriorTransactionDate
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
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

   fun validateBulkUpdate(dto: List<BankReconciliationDTO>, company: CompanyEntity): List<BankReconciliationEntity> {
      logger.debug("Validating Bulk Update BankReconciliation {}", dto)

      return doBulkValidation(dto, company)
   }

   private fun doValidation(existingBankRecon: BankReconciliationEntity? = null, dto: BankReconciliationDTO, company: CompanyEntity): BankReconciliationEntity {
      val bank = bankRepository.findOne(dto.bank!!.id!!, company)
      val type = bankReconciliationTypeRepository.findOne(dto.type!!.value)

      doValidation { errors ->
         bank
            ?: errors.add(ValidationError("bank.id", NotFound(dto.bank!!.id!!)))

         type
            ?: errors.add(ValidationError("type.value", NotFound(dto.type!!.value)))

         dto.clearedDate?.let {
            if (dto.clearedDate!! > LocalDate.now()) {
               errors.add(ValidationError("clearedDate", ClearedDateMustNotBeFutureDate(dto.clearedDate!!)))
            }

            if(dto.clearedDate!! < dto.date) {
               errors.add(ValidationError("clearedDate", ClearedDateNotPriorTransactionDate(dto.clearedDate!!)))
            }
         }
         if(dto.type!!.value == "V" && dto.clearedDate == null) {
            errors.add(ValidationError("clearedDate", NotNull("clearedDate")))
         }
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

   private fun doBulkValidation(dtoList: List<BankReconciliationDTO>, company: CompanyEntity): List<BankReconciliationEntity> {
      val updateEntities: MutableList<BankReconciliationEntity> = mutableListOf()
      for (dto in dtoList) {
         val bank = bankRepository.findOne(dto.bank!!.id!!, company)
         val type = bankReconciliationTypeRepository.findOne(dto.type!!.value)


         doValidation { errors ->
            bank
               ?: errors.add(ValidationError("bank.id", NotFound(dto.bank!!.id!!)))

            type
               ?: errors.add(ValidationError("type.value", NotFound(dto.type!!.value)))

         }

         updateEntities.add(
            BankReconciliationEntity(
               id = dto.id,
               dto = dto,
               bank = bank!!,
               type = type!!
            )
         )
      }
      return updateEntities
   }
}
