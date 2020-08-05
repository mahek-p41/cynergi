package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.EndDateBeforeStart
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.localization.NotificationRecipientsRequired
import com.cynergisuite.middleware.localization.NotificationRecipientsRequiredAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationValidator @Inject constructor(
   private val notificationService: NotificationService
) {
   private val logger: Logger = LoggerFactory.getLogger(NotificationValidator::class.java)
   private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

   @Throws(ValidationException::class)
   fun validateCreate(vo: NotificationValueObject) {
      logger.debug("Validating Create Notification {}", vo)

      val errors: MutableSet<ValidationError> = doValidate(dto = vo)

      if (errors.isNotEmpty()) {
         logger.info("Validating Create Notification {} had errors", vo)

         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(vo: NotificationValueObject) {
      logger.debug("Validation Update Notification {}", vo)

      val errors = doValidate(dto = vo)
      val id = vo.id

      if (id == null) {
         errors.add(element = ValidationError("id", NotNull("id")))
      } else if (!notificationService.exists(id = id)) {
         errors.add(element = ValidationError("id", NotFound(id)))
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
         errors.add(ValidationError("recipients", NotificationRecipientsRequiredAll("A")))
      } else if (notificationType != "A" && dto.recipients.isEmpty()) {
         errors.add(ValidationError("recipients", NotificationRecipientsRequired(dto.notificationType)))
      }

      if (dto.expirationDate!!.isBefore(dto.startDate!!)) { // using the !! not null operator here is a safe bet as the javax.validation would have been applied to both of these properties and would have failed before doValidate was called
         errors.add(ValidationError("expirationDate", EndDateBeforeStart(dto.expirationDate!!.format(dateFormatter), dto.startDate!!.format(dateFormatter))))
      }

      return errors
   }
}
