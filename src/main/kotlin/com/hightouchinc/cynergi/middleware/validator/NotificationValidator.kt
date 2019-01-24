package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.exception.ValidationError
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.NotificationService
import javax.inject.Singleton

@Singleton
class NotificationValidator(
   private val notificationService: NotificationService
) {

   @Throws(ValidationException::class)
   fun validateSave(dto: NotificationDto) {
      val errors: Set<ValidationError> = setOf()

      if (errors.isNotEmpty()) {
         throw ValidationException(errors)
      }
   }

   @Throws(ValidationException::class)
   fun validateUpdate(dto: NotificationDto) {
      val errors = mutableSetOf<ValidationError>()
      val id = dto.id

      if (id == null) {
         errors.add(element = ValidationError("id", ErrorCodes.Validation.NOT_NULL, listOf("id")))
      } else {
         val existingNotification: NotificationDto? = notificationService.fetchById(id = id)

         if (existingNotification == null) {
            errors.add(element = ValidationError("id", ErrorCodes.System.NOT_FOUND, listOf(id)))
         }
      }

      if (errors.isNotEmpty()) {
         throw ValidationException(errors = errors)
      }
   }
}
