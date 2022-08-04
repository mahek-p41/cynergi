package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionTemplateRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.Duplicate
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class AccountPayableDistributionTemplateValidator @Inject constructor(
   private val accountPayableDistributionTemplateRepository: AccountPayableDistributionTemplateRepository,
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AccountPayableDistributionTemplateValidator::class.java)

   fun validateCreate(dto: AccountPayableDistributionTemplateDTO, company: CompanyEntity): AccountPayableDistributionTemplateEntity {
      logger.debug("Validating Create AccountPayableDistributionDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: AccountPayableDistributionTemplateDTO, company: CompanyEntity): AccountPayableDistributionTemplateEntity {
      logger.debug("Validating Update AccountPayableDistributionDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   private fun doSharedValidation(dto: AccountPayableDistributionTemplateDTO, company: CompanyEntity): AccountPayableDistributionTemplateEntity {
      val name = accountPayableDistributionTemplateRepository.findByName(dto.name!!, company)

      doValidation { errors ->
         if (name != null) errors.add(ValidationError("name", Duplicate(dto.name)))

      }

      return AccountPayableDistributionTemplateEntity(
         dto
      )
   }
}
