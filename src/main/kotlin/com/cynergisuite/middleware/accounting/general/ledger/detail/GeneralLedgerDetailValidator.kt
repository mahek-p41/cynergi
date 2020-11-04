package com.cynergisuite.middleware.accounting.general.ledger.detail

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.infrastructure.GeneralLedgerSourceCodeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.ConfigAlreadyExist
import com.cynergisuite.middleware.localization.MustMatchPathVariable
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class GeneralLedgerDetailValidator @Inject constructor(
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val sourceCodeRepository: GeneralLedgerSourceCodeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerDetailValidator::class.java)

   @Throws(ValidationException::class)
   fun validateCreate(@Valid dto: GeneralLedgerDetailDTO, company: Company): GeneralLedgerDetailEntity {
      logger.debug("Validating Create GeneralLedgerDetail {}", dto)

      return doSharedValidation(dto, company)
   }

   @Throws(ValidationException::class)
   fun validateUpdate(id: Long, @Valid dto: GeneralLedgerDetailDTO, company: Company): GeneralLedgerDetailEntity {
      logger.debug("Validating Update GeneralLedgerDetail {}", dto)

      val generalLedgerDetailEntity = generalLedgerDetailRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, generalLedgerDetailEntity)
   }

   private fun doSharedValidation(
      dto: GeneralLedgerDetailDTO,
      company: Company,
      entity: GeneralLedgerDetailEntity? = null
   ): GeneralLedgerDetailEntity {
      val account = dto.account?.id?.let { accountRepository.findOne(it, company) }
      val profitCenter = dto.profitCenter?.id?.let { storeRepository.findOne(it, company) }
      val source = dto.source?.id?.let { sourceCodeRepository.findOne(it, company) }

      doValidation { errors ->
         if (generalLedgerDetailRepository.exists(company) && entity == null) {
            errors.add(ValidationError("company", ConfigAlreadyExist(company.myDataset())))
         }

         if (entity?.myId() != dto.myId()) errors.add(ValidationError("id", MustMatchPathVariable("Id")))

         // account is not nullable
         account ?: errors.add(ValidationError("account.id", NotFound(dto.account!!.id!!)))

         // profitCenter is not nullable
         profitCenter ?: errors.add(ValidationError("profitCenter.id", NotFound(dto.profitCenter!!.id!!)))

         // source is not nullable
         source ?: errors.add(ValidationError("source.id", NotFound(dto.source!!.id!!)))
      }

      return GeneralLedgerDetailEntity(dto, account!!, profitCenter!!, source!!)
   }
}
