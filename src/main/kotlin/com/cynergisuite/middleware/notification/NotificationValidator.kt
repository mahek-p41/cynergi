package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Cynergi.EndDateBeforeStart
import com.cynergisuite.middleware.localization.Cynergi.NotificationRecipientsRequired
import com.cynergisuite.middleware.localization.Cynergi.NotificationRecipientsRequiredAll
import com.cynergisuite.middleware.localization.SystemCode.NotFound
import com.cynergisuite.middleware.localization.Validation.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationValidator @Inject constructor(
   private val notificationService: NotificationService,
   private val dateFormatter: DateTimeFormatter
) {
   private val logger: Logger = LoggerFactory.getLogger(NotificationValidator::class.java)

   @Throws(ValidationException::class)
   fun validateSave(vo: NotificationValueObject) {
      logger.debug("Validating Save Notification {}", vo)

      val errors: MutableSet<ValidationError> = doValidate(dto = vo)

      if (errors.isNotEmpty()) {
         logger.info("Validating Save Notification {} had errors", vo)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: NotificationValueObject) {
      logger.debug("Validation Update Notification {}", vo)

      val errors = doValidate(dto = vo)
      val id = vo.id

      if (id == null) {
         errors.add(element = ValidationError("id", NotNull, listOf("id")))
      } else if ( !notificationService.exists(id = id) ) {
         errors.add(element = ValidationError("id", NotFound, listOf(id)))
      }

      if (errors.isNotEmpty()) {
         logger.debug("Validating Update Notification {} had errors", vo)

         throw ValidationException(errors = errors)
      }
   }

   private fun doValidate(dto: NotificationValueObject): MutableSet<ValidationError> {
      val errors: MutableSet<ValidationError> = mutableSetOf()
      val notificationType = dto.notificationType?.substringBefore(":")

      if (notificationType == "A" && dto.recipients.isNotEmpty()) {
         errors.add(ValidationError("recipients", NotificationRecipientsRequiredAll, listOf("A")))
      } else if (notificationType != "A" && dto.recipients.isEmpty()) {
         errors.add(ValidationError("recipients", NotificationRecipientsRequired, listOf(dto.notificationType)))
      }

      if (dto.expirationDate!!.isBefore(dto.startDate!!)) { // using the !! not null operator here is a safe bet as the javax.validation would have been applied to both of these properties and would have failed before doValidate was called
         errors.add(ValidationError("expirationDate", EndDateBeforeStart, listOf(dto.expirationDate!!.format(dateFormatter), dto.startDate!!.format(dateFormatter))))
      }

      return errors
   }
}
