package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.Approved
import com.cynergisuite.middleware.audit.status.AuditStatusService
import com.cynergisuite.middleware.audit.status.Created
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.AuditOpenAtStore
import com.cynergisuite.middleware.localization.AuditStatusNotFound
import com.cynergisuite.middleware.localization.AuditUnableToChangeStatusFromTo
import com.cynergisuite.middleware.localization.InvalidCompany
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.ThruDateIsBeforeFrom
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.Locale

@Singleton
class AuditValidator @Inject constructor(
   private val auditRepository: AuditRepository,
   private val auditStatusService: AuditStatusService,
   private val companyRepository: CompanyRepository,
   private val employeeRepository: EmployeeRepository,
   private val localizationService: LocalizationService,
   private val storeRepository: StoreRepository
) : ValidatorBase() {
   private val logger: Logger = LoggerFactory.getLogger(AuditValidator::class.java)

   @Throws(ValidationException::class)
   fun validationFetchAll(pageRequest: AuditPageRequest, company: CompanyEntity): AuditPageRequest {
      doValidation { errors ->
         val from = pageRequest.from
         val thru = pageRequest.thru

         if (thru != null && from != null && thru.isBefore(from)) {
            errors.add(ValidationError("from", ThruDateIsBeforeFrom(from, thru)))
         }

         if (!companyRepository.exists(company.id)) {
            errors.add(ValidationError("dataset", InvalidCompany(company)))
         }
      }

      return pageRequest
   }

   @Throws(ValidationException::class)
   fun validateFindAuditStatusCounts(pageRequest: AuditPageRequest, company: CompanyEntity) =
      validationFetchAll(pageRequest, company)

   @Throws(ValidationException::class)
   fun validateCreate(audit: AuditCreateValueObject, user: User): AuditEntity {
      logger.debug("Validating Create Audit {}", audit)

      doValidation { errors ->
         val storeNumber = audit.store?.storeNumber

         if (storeNumber != null && !storeRepository.exists(number = storeNumber, company = user.myCompany())) {
            errors.add(ValidationError("storeNumber", NotFound(storeNumber)))
         }

         if (storeNumber != null && auditRepository.countAuditsNotCompletedOrCanceled(storeNumber = storeNumber, company = user.myCompany()) > 0) {
            errors.add(ValidationError("storeNumber", AuditOpenAtStore(storeNumber)))
         }

         if (employeeRepository.doesNotExist(user)) {
            errors.add(ValidationError("user", NotFound(user.myEmployeeNumber())))
         }
      }

      return AuditEntity(
         store = storeRepository.findOne(number = audit.store!!.storeNumber!!, company = user.myCompany())!!,
         number = 0,
         totalDetails = 0,
         totalExceptions = 0,
         hasExceptionNotes = false,
         lastUpdated = OffsetDateTime.now(),
         inventoryCount = 0,
         actions = mutableSetOf(
            AuditActionEntity(
               status = Created,
               changedBy = employeeRepository.findOne(user)!!
            )
         )
      )
   }

   @Throws(ValidationException::class)
   fun validateUpdate(audit: AuditUpdateDTO, user: User, locale: Locale): Pair<AuditActionEntity, AuditEntity> {
      logger.debug("Validating Update Audit {}", audit)

      doValidation { errors ->
         val id = audit.id!!
         val requestedStatus = auditStatusService.fetchByValue(audit.status!!.value!!)
         val existingAudit = auditRepository.findOne(id, user.myCompany())

         if (existingAudit == null) {
            errors.add(ValidationError("id", NotFound(id)))
         } else if (requestedStatus != null) {
            val currentStatus = existingAudit.currentStatus()

            if (!auditStatusService.requestedStatusIsValid(currentStatus, requestedStatus) || requestedStatus == Approved) {
               errors.add(
                  ValidationError(
                     "status",
                     AuditUnableToChangeStatusFromTo(
                        id,
                        currentStatus.localizeMyDescription(locale, localizationService),
                        requestedStatus.localizeMyDescription(locale, localizationService)
                     )
                  )
               )
            }
         } else {
            errors.add(
               ValidationError("status", AuditStatusNotFound(audit.status!!.value!!))
            )
         }

         if (employeeRepository.doesNotExist(user)) {
            errors.add(ValidationError("user", NotFound(user.myEmployeeNumber())))
         }
      }

      return Pair(
         AuditActionEntity(
            status = auditStatusService.fetchByValue(audit.status!!.value!!)!!,
            changedBy = employeeRepository.findOne(user)!!
         ),
         auditRepository.findOne(audit.id!!, user.myCompany())!!
      )
   }

   @Throws(ValidationException::class)
   fun validateApproved(audit: SimpleIdentifiableDTO, company: CompanyEntity, user: User, locale: Locale): AuditEntity {
      val existingAudit = auditRepository.findOne(audit.myId()!!, company) ?: throw NotFoundException(audit.myId()!!)

      doValidation { errors ->
         val currentStatus = existingAudit.currentStatus()

         if (!auditStatusService.requestedStatusIsValid(currentStatus, Approved)) {
            errors.add(
               ValidationError(
                  "status",
                  AuditUnableToChangeStatusFromTo(
                     existingAudit.id!!,
                     currentStatus.localizeMyDescription(locale, localizationService),
                     Approved.localizeMyDescription(locale, localizationService)
                  )
               )
            )
         }
      }

      return existingAudit
   }

   @Throws(NotFoundException::class)
   fun validateApproveAll(audit: SimpleIdentifiableDTO, company: CompanyEntity): AuditEntity {
      return auditRepository.findOne(audit.myId()!!, company) ?: throw NotFoundException(audit.myId()!!)
   }
}
