package com.cynergisuite.middleware.accounting.account.payable.distribution

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.payable.distribution.infrastructure.AccountPayableDistributionTemplateRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
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

      val existingTemplateByName = accountPayableDistributionTemplateRepository.findByName(dto.name!!, company)
      return doSharedValidation(existingTemplateByName = existingTemplateByName, dto = dto)
   }

   fun validateUpdate(id: UUID, dto: AccountPayableDistributionTemplateDTO, company: CompanyEntity): AccountPayableDistributionTemplateEntity {
      logger.debug("Validating Update AccountPayableDistributionDetail {}", dto)

      val existingTemplate = accountPayableDistributionTemplateRepository.findOne(id, company) ?: throw NotFoundException(id)
      val existingTemplateByName = accountPayableDistributionTemplateRepository.findByName(dto.name!!, company)
      return doSharedValidation(existingTemplate = existingTemplate, existingTemplateByName = existingTemplateByName, dto)
   }

   private fun doSharedValidation(
      existingTemplate: AccountPayableDistributionTemplateEntity? = null,
      existingTemplateByName: AccountPayableDistributionTemplateEntity? = null,
      dto: AccountPayableDistributionTemplateDTO)
   : AccountPayableDistributionTemplateEntity {

      doValidation { errors ->
         if (existingTemplate == null && existingTemplateByName != null) errors.add(ValidationError("name", Duplicate(dto.name)))
         if (existingTemplate != null && existingTemplateByName != null && existingTemplateByName.id != existingTemplate.id) errors.add(ValidationError("name", Duplicate(dto.name)))

      }

      return AccountPayableDistributionTemplateEntity(
         dto
      )
   }
}
