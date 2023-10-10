package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class GeneralLedgerSummaryValidator @Inject constructor(
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSummaryValidator::class.java)

   fun validateCreate(dto: GeneralLedgerSummaryDTO, company: CompanyEntity): GeneralLedgerSummaryEntity {
      logger.debug("Validating Create GeneralLedgerSummary {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: GeneralLedgerSummaryDTO, company: CompanyEntity): GeneralLedgerSummaryEntity {
      logger.debug("Validating Update GeneralLedgerSummary {}", dto)

      val generalLedgerSummaryEntity = generalLedgerSummaryRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, generalLedgerSummaryEntity)
   }

   private fun doSharedValidation(dto: GeneralLedgerSummaryDTO, company: CompanyEntity, existingGLSummary: GeneralLedgerSummaryEntity? = null): GeneralLedgerSummaryEntity {
      val account = accountRepository.findOne(dto.account!!.id!!, company) ?: throw NotFoundException(dto.account!!.id!!)
      val profitCenter = storeRepository.findOne(dto.profitCenter!!.id!!, company) ?: throw NotFoundException(dto.profitCenter!!.id!!)
      val overallPeriod = overallPeriodTypeRepository.findOne(dto.overallPeriod!!.value) ?: throw NotFoundException(dto.overallPeriod!!.value)
      val glSummaryByBusinessKey = generalLedgerSummaryRepository.findOneByBusinessKey(company, account.id!!, profitCenter.number, overallPeriod.value)

      doValidation { errors -> // FIXME the checks below seem suspect.  How is this checking for duplicates?
         if ((existingGLSummary == null && glSummaryByBusinessKey != null) || (existingGLSummary != null && glSummaryByBusinessKey != null && (existingGLSummary.id != dto.id || existingGLSummary.id != glSummaryByBusinessKey.id))) {
            errors.add(ValidationError("id", Duplicate(dto.id)))
         }
      }

      return GeneralLedgerSummaryEntity(dto, account, profitCenter, overallPeriod)
   }
}