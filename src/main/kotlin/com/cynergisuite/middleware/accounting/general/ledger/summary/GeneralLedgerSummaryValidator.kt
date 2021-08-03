package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.accounting.account.infrastructure.AccountRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.accounting.routine.type.infrastructure.OverallPeriodTypeRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerSummaryValidator @Inject constructor(
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val accountRepository: AccountRepository,
   private val storeRepository: StoreRepository,
   private val overallPeriodTypeRepository: OverallPeriodTypeRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSummaryValidator::class.java)

   fun validateCreate(dto: GeneralLedgerSummaryDTO, company: Company): GeneralLedgerSummaryEntity {
      logger.debug("Validating Create GeneralLedgerSummary {}", dto)

      return doSharedValidation(dto, company)
   }

   fun validateUpdate(id: UUID, dto: GeneralLedgerSummaryDTO, company: Company): GeneralLedgerSummaryEntity {
      logger.debug("Validating Update GeneralLedgerSummary {}", dto)

      val generalLedgerSummaryEntity = generalLedgerSummaryRepository.findOne(id, company) ?: throw NotFoundException(id)

      return doSharedValidation(dto, company, generalLedgerSummaryEntity)
   }

   private fun doSharedValidation(dto: GeneralLedgerSummaryDTO, company: Company, existingGLSummary: GeneralLedgerSummaryEntity? = null): GeneralLedgerSummaryEntity {
      val account = accountRepository.findOne(dto.account!!.id!!, company) ?: throw NotFoundException(dto.account!!.id!!)
      val profitCenter = storeRepository.findOne(dto.profitCenter!!.id!!, company) ?: throw NotFoundException(dto.profitCenter!!.id!!)
      val overallPeriod = overallPeriodTypeRepository.findOne(dto.overallPeriod!!.value) ?: throw NotFoundException(dto.overallPeriod!!.value)
      val glSummaryByBusinessKey = generalLedgerSummaryRepository.findOneByBusinessKey(company, account.id!!, profitCenter.id, overallPeriod.id)

      doValidation { errors -> // FIXME the checks below seem suspect.  How is this checking for duplicates?
         if ((existingGLSummary == null && glSummaryByBusinessKey != null) || (existingGLSummary != null && glSummaryByBusinessKey != null && (existingGLSummary.id != dto.id || existingGLSummary.id != glSummaryByBusinessKey.id))) {
            errors.add(ValidationError("id", Duplicate(dto.id)))
         }
      }

      return GeneralLedgerSummaryEntity(dto, account, profitCenter, overallPeriod)
   }
}
