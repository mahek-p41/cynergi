package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.ValidatorBase
import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.status.APPROVED
import com.cynergisuite.middleware.audit.status.AuditStatusService
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

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
   fun validationFetchAll(pageRequest: AuditPageRequest, company: Company): AuditPageRequest {
      doValidation { errors ->
         val from = pageRequest.from
         val thru = pageRequest.thru

         if (thru != null && from != null && thru.isBefore(from)) {
            errors.add(ValidationError("from", ThruDateIsBeforeFrom(from, thru)))
         }

         if (companyRepository.doesNotExist(company)) {
            errors.add(ValidationError("dataset", InvalidCompany(company)))
         }
      }

      return pageRequest
   }

   @Throws(ValidationException::class)
   fun validateFindAuditStatusCounts(pageRequest: AuditPageRequest, company: Company) =
      validationFetchAll(pageRequest, company)

   @Throws(ValidationException::class)
   fun validateCreate(audit: AuditCreateValueObject, user: User): AuditEntity {
      logger.debug("Validating Create Audit {}", audit)

      doValidation { errors ->
         val storeNumber = audit.store?.number

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
         store = storeRepository.findOne(number = audit.store!!.number!!, company = user.myCompany())!!,
         number = 0,
         totalDetails = 0,
         totalExceptions = 0,
         hasExceptionNotes = false,
         lastUpdated = OffsetDateTime.now(),
         inventoryCount = 0,
         actions = mutableSetOf(
            AuditActionEntity(
               status = CREATED,
               changedBy = employeeRepository.findOne(user)!!
            )
         )
      )
   }

   @Throws(ValidationException::class)
   fun validateCompleteOrCancel(audit: AuditUpdateValueObject, user: User, locale: Locale): Pair<AuditActionEntity, AuditEntity> {
      logger.debug("Validating Update Audit {}", audit)

      doValidation { errors ->
         val id = audit.id!!
         val requestedStatus = auditStatusService.fetchByValue(audit.status!!.value!!)
         val existingAudit = auditRepository.findOne(id, user.myCompany())

         if (existingAudit == null) {
            errors.add(ValidationError("id", NotFound(id)))
         } else if (requestedStatus != null) {
            val currentStatus = existingAudit.currentStatus()

            if (!auditStatusService.requestedStatusIsValid(currentStatus, requestedStatus) || requestedStatus == APPROVED) {
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
   fun validateApproved(audit: SimpleIdentifiableDataTransferObject, company: Company, user: User, locale: Locale): AuditEntity {
      val existingAudit = auditRepository.findOne(audit.myId()!!, company) ?: throw NotFoundException(audit.myId()!!)

      doValidation { errors ->
         val currentStatus = existingAudit.currentStatus()

         if (!auditStatusService.requestedStatusIsValid(currentStatus, APPROVED)) {
            errors.add(
               ValidationError(
                  "status",
                  AuditUnableToChangeStatusFromTo(
                     existingAudit.id!!,
                     currentStatus.localizeMyDescription(locale, localizationService),
                     APPROVED.localizeMyDescription(locale, localizationService)
                  )
               )
            )
         }
      }

      return existingAudit
   }

   @Throws(NotFoundException::class)
   fun validateApproveAll(audit: SimpleIdentifiableDataTransferObject, company: Company): AuditEntity {
      return auditRepository.findOne(audit.myId()!!, company) ?: throw NotFoundException(audit.myId()!!)
   }
}
