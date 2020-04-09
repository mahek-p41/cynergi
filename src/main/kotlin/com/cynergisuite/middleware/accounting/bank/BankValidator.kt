package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class BankValidator @Inject constructor(
   private val bankRepository: BankRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(BankValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid bankDTO: BankDTO, company: Company): BankEntity {
      logger.trace("Validating Save Bank {}", bankDTO)

      doValidation {}

      return BankEntity(bankDTO, company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, bankDTO: BankDTO): BankEntity {
      logger.trace("Validating Update Bank {}", bankDTO)

      doValidation { errors ->
         bankRepository.findOne(id, bankDTO.company) ?: errors.add(ValidationError("Bank Id", NotFound(id)))
      }

      return BankEntity(bankDTO)
   }
}
