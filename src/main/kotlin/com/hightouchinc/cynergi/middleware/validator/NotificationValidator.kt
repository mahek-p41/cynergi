package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.NotificationService
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.END_DATE_BEFORE_START
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.NOTIFICATION_RECIPIENTS_ALL
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.NOTIFICATION_RECIPIENTS_REQUIRED
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.System.NOT_FOUND
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.NOT_NULL
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationValidator @Inject constructor(
   private val notificationService: NotificationService,
   private val dateFormatter: DateTimeFormatter
) {

   @Throws(ValidationException::class)
   fun validateSave(dto: NotificationDto) {
      val errors: MutableSet<ValidationError> = doValidate(dto = dto)

      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(dto: NotificationDto) {
      val errors = doValidate(dto = dto)
      val id = dto.id

      if (id == null) {
         errors.add(element = ValidationError("id", NOT_NULL, listOf("id")))
      } else {
         val existingNotification: NotificationDto? = notificationService.fetchById(id = id)

         if (existingNotification == null) {
            errors.add(element = ValidationError("id", NOT_FOUND, listOf(id)))
         }
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors = errors)
      }
   }

   private fun doValidate(dto: NotificationDto): MutableSet<ValidationError> {
      val errors: MutableSet<ValidationError> = mutableSetOf()

      if (dto.notificationType == "A" && dto.recipients.isNotEmpty()) {
         errors.add(ValidationError("recipients", NOTIFICATION_RECIPIENTS_ALL, listOf("A")))
      } else if (dto.notificationType != "A" && dto.recipients.isEmpty()) {
         errors.add(ValidationError("recipients", NOTIFICATION_RECIPIENTS_REQUIRED, listOf(dto.notificationType)))
      }

      if (dto.expirationDate!!.isBefore(dto.startDate!!)) { // using the !! not null operator here is a safe bet as the javax.validation would have been applied to both of these properties and would have failed before doValidate was called
         errors.add(ValidationError("expirationDate", END_DATE_BEFORE_START, listOf(dto.expirationDate!!.format(dateFormatter), dto.startDate!!.format(dateFormatter))))
      }

      return errors
   }
}
