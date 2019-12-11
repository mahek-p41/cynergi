package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.TypeDomainEntity
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "NotificationType", description = "The type describing a Notification")
sealed class NotificationType(
   open val id: Long,
   open val value: String,
   open val description: String,
   open val localizationCode: String
) : TypeDomainEntity<NotificationType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   fun copy(): NotificationType =
      NotificationTypeEntity(
         id = this.id,
         value = this.value,
         description = this.description,
         localizationCode = this.localizationCode
      )
}

data class NotificationTypeEntity(
   override val id: Long,
   override val value: String,
   override val description: String,
   override val localizationCode: String
) : NotificationType(id, value, description, localizationCode)

object StoreNotificationType: NotificationType(1, "S", "Store", "notification.store")
object EmployeeNotificationType: NotificationType(2, "E", "Employee", "notification.employee")
object DepartmentNotificationType: NotificationType(3, "D", "Department", "notification.department")
object AllNotificationType: NotificationType(4, "A", "All", "notification.all")

typealias STORE = StoreNotificationType
typealias EMPLOYEE = EmployeeNotificationType
typealias DEPARTMENT = DepartmentNotificationType
typealias ALL = AllNotificationType
