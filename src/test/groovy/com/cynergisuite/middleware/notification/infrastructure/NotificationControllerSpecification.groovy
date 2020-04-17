package com.cynergisuite.middleware.notification.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.error.ErrorDataTransferObject
import com.cynergisuite.middleware.notification.NotificationDataLoaderService
import com.cynergisuite.middleware.notification.NotificationRecipientDataLoaderService
import com.cynergisuite.middleware.notification.NotificationRecipientTestDataLoader
import com.cynergisuite.middleware.notification.NotificationRecipientValueObject
import com.cynergisuite.middleware.notification.NotificationRequestValueObject
import com.cynergisuite.middleware.notification.NotificationResponseValueObject
import com.cynergisuite.middleware.notification.NotificationTestDataLoader
import com.cynergisuite.middleware.notification.NotificationTypeDomainTestDataLoader
import com.cynergisuite.middleware.notification.NotificationTypeValueObject
import com.cynergisuite.middleware.notification.NotificationValueObject
import com.cynergisuite.middleware.notification.NotificationsResponseValueObject
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static io.micronaut.http.HttpRequest.*
import static io.micronaut.http.HttpStatus.*

@MicronautTest(transactional = false)
class NotificationControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/notifications"
   private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

   @Inject NotificationRepository notificationRepository
   @Inject NotificationDataLoaderService notificationsDataLoaderService
   @Inject NotificationRecipientDataLoaderService notificationRecipientDataLoaderService


   void "fetch one notification by id with no recipients" () {
      given:
      final def savedNotification = notificationsDataLoaderService.single()
      final def notificationValueObject = new NotificationResponseValueObject(new NotificationValueObject(savedNotification))

      when:
      def result = get("$path/${savedNotification.id}")

      then:
      result.notification.id == notificationValueObject.notification.id
      result.notification.dateCreated == notificationValueObject.notification.dateCreated.with { dateFormatter.format(it) }
      result.notification.startDate == notificationValueObject.notification.startDate.with { dateFormatter.format(it) }
      result.notification.companyId == notificationValueObject.notification.company
      result.notification.message == notificationValueObject.notification.message
      result.notification.sendingEmployee == notificationValueObject.notification.sendingEmployee
      result.notification.notificationType == notificationValueObject.notification.notificationType.toString()
      result.notification.recipients != null
      result.notification.recipients.size() == 0
   }

   void "fetch one notification by id with recipients" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipients = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      def result = client.retrieve(GET("$path/${notification.id}"), NotificationResponseValueObject)

      then:
      result.notification.recipients.size() == 2
      result.notification.recipients.sort({ o1, o2 -> o1.id <=> o2.id}) == recipients.sort({ o1, o2 -> o1.id <=> o2.id}).collect { new NotificationRecipientValueObject(it) }
   }

   void "fetch one notification by id not found" () {
      when:
      client.exchange(GET("$path/0"), Argument.of(NotificationResponseValueObject), Argument.of(ErrorDataTransferObject))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(ErrorDataTransferObject).orElse(null)?.message == "0 was unable to be found"
   }

   void "fetch all by sending employee and company through the admin path" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def sendingEmployee = "bob"
      final def fiveNotifications = notificationsDataLoaderService.stream(5, companyId, LocalDate.now(), null, notificationType, sendingEmployee).collect(Collectors.toList())
      fiveNotifications.each { notification -> notificationRecipientDataLoaderService.stream(2, notification).forEach { notification.recipients.add(it) } }

      when:
      def result = client.retrieve(GET("$path/admin").headers(["X-Auth-Company": companyId, "X-Auth-User": sendingEmployee]), NotificationsResponseValueObject)

      then:
      result.notifications.each { it.recipients.sort { o1, o2 -> o1.id <=> o2.id } }.sort { o1, o2 -> o1.id <=> o2.id }
      result.notifications.size() == 5
      result.notifications.collect { it.sendingEmployee }.findAll { it == sendingEmployee }.size() == 5
      result.notifications == fiveNotifications.collect { new NotificationValueObject(it) }
      result.notifications.collect { it.recipients.size() }.findAll { it == 2 }.size() == 5
   }

   @Deprecated
   void "fetch all by company with type All deprecated" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def fiveNotifications = notificationsDataLoaderService.stream(5, companyId, LocalDate.now(), null, notificationType, null).collect(Collectors.toList())

      when:
      def result = client.retrieve(GET("$path?type=${notificationType.value}").headers(["X-Auth-Company": companyId]), NotificationsResponseValueObject)

      then:
      result == new NotificationsResponseValueObject(fiveNotifications.collect { new NotificationValueObject(it)} )
      result.notifications.size() == 5
   }

   void "fetch all by company with type All" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def fiveNotifications = notificationsDataLoaderService.stream(5, companyId, LocalDate.now(), null, notificationType, null).collect(Collectors.toList())

      when:
      def result = client.retrieve(GET("$path/company/${companyId}?type=${notificationType.value}"), NotificationValueObject[])

      then:
      result.size() == 5
      result == fiveNotifications.collect { new NotificationValueObject(it) }.toArray()
   }

   @Deprecated
   void "fetch all by company with type Employee deprecated" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      def recipient = recipientNotifications[0].recipient
      def result = client.retrieve(GET("$path?type=${notificationType.value}").headers(["X-Auth-Company": companyId, "X-Auth-User": recipient]), NotificationsResponseValueObject)

      then:
      notThrown(HttpClientResponseException)
      result.notifications.size() == 1
      result.notifications[0].id == notification.id
      result.notifications[0].recipients[0] == new NotificationRecipientValueObject(recipientNotifications[0])
   }

   void "fetch all by company with type Employee" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      def recipient = recipientNotifications[0].recipient
      def result = client.retrieve(GET("$path/company/${companyId}/${recipient}?type=${notificationType.value}"), NotificationValueObject[])

      then:
      result.size() == 1
      result[0].id == notification.id
      result[0].recipients[0] == new NotificationRecipientValueObject(recipientNotifications[0])
   }

   @Deprecated
   void "fetch all by company without the required X-Auth-Company header deprecated" () {
      when:
      client.retrieve(GET(path), Argument.of(NotificationsResponseValueObject), Argument.of(ErrorDataTransferObject))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      exception.response.getBody(ErrorDataTransferObject).orElse(null)?.message == "Required argument companyId not specified"
   }

   @Deprecated
   void "fetch all permissions should only be hard coded department" () {
      when:
      final result = client.retrieve(GET("$path/permissions"), Map)

      then:
      result.size() == 2
      result["id"] == 1
      result["depts_allowed"].size() == 1
      result["depts_allowed"][0] == "ALL"
   }

   void "fetch all types" () {
      when:
      final def types = client.retrieve(GET("${path}/types"), NotificationTypeValueObject[])

      then:
      types.size() == 4
      types[0].value == 'A'
      types[0].description == 'All'
      types[1].value == 'D'
      types[1].description == 'Department'
      types[2].value == 'E'
      types[2].description == 'Employee'
      types[3].value == 'S'
      types[3].description == 'Store'
   }

   void "attempt to fetch all types with typo results in bad request status" () {
      when:
      client.retrieve(GET("${path}/type"), Argument.of(NotificationTypeValueObject[]), Argument.of(ErrorDataTransferObject))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      exception.response.getBody(ErrorDataTransferObject).orElse(null)?.message == "Failed to convert argument [id] for value [type]"
   }

   void "post valid notification of type All" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def notification = NotificationTestDataLoader.stream(1, "testco", null, null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create Notification") }

      when:
      final def savedNotification = client.retrieve(POST(path, new NotificationRequestValueObject(new NotificationValueObject(notification))), NotificationResponseValueObject).notification

      then:
      savedNotification.id != null
      savedNotification.id > 0
      savedNotification.company == "testco"
      savedNotification.sendingEmployee == notification.sendingEmployee
      savedNotification.recipients.size() == 0
      notificationRepository.exists(savedNotification.id)
   }

   void "post valid notification of type All with only the 'A'" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def notification = NotificationTestDataLoader.stream(1, "testco", null, null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def notificationPayload = new NotificationValueObject(notification, "A")

      when:
      final def savedNotification = client.retrieve(POST(path, new NotificationRequestValueObject(notificationPayload)), NotificationResponseValueObject).notification

      then:
      savedNotification.id != null
      savedNotification.id > 0
      savedNotification.company == "testco"
      savedNotification.sendingEmployee == notification.sendingEmployee
      savedNotification.recipients.size() == 0
      notificationRepository.exists(savedNotification.id)
   }

   void "post valid notification of type Employee with 1 recipient" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = NotificationTestDataLoader.stream(1, "testco", null, null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def notificationRecipients = NotificationRecipientTestDataLoader.stream(1, notification).collect(Collectors.toList())

      when:
      final def savedNotification = client.retrieve(POST(path, new NotificationRequestValueObject(new NotificationValueObject(notification, notificationRecipients))), NotificationResponseValueObject).notification

      then:
      savedNotification.id != null
      savedNotification.id > 0
      savedNotification.recipients.size() == 1
      savedNotification.recipients[0].id > 0
      savedNotification.recipients[0].description == notificationRecipients[0].description
      savedNotification.recipients[0].recipient == notificationRecipients[0].recipient
      notificationRepository.exists(savedNotification.id)
   }

   void "post invalid notification of type employee with no recipients" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = NotificationTestDataLoader.stream(1, "testco", null, null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create Notification") }

      when:
      client.retrieve(POST(path, new NotificationRequestValueObject(new NotificationValueObject(notification))), Argument.of(NotificationResponseValueObject), Argument.of(ErrorDataTransferObject[]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final errors = exception.response.getBody(ErrorDataTransferObject[]).get()
      errors.size() == 1
      errors[0].message == "Recipients required for notification type E:Employee"
      errors[0].path == "recipients"
   }

   void "post invalid notification of type all with nulls" () {
      given:
      final def notification = new NotificationValueObject(null, null, null, null, null, null, null, null, [])

      when:
      client.retrieve(POST(path, new NotificationRequestValueObject(notification)), Argument.of(String), Argument.of(String))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final json = exception.response.bodyAsJson()
      json.size() == 6
      json.collect { new ErrorDataTransferObject(it.message, it.path) }.sort {o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Is required", "notification.company"),
         new ErrorDataTransferObject("Is required", "notification.expirationDate"),
         new ErrorDataTransferObject("Is required", "notification.message"),
         new ErrorDataTransferObject("Is required", "notification.notificationType"),
         new ErrorDataTransferObject("Is required", "notification.sendingEmployee"),
         new ErrorDataTransferObject("Is required", "notification.startDate")
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "put valid notification of type all" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def savedNotification = notificationsDataLoaderService.stream(1, "testco", null, null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }

      when:
      final updatedNotification = new NotificationValueObject(null, "Updated message", savedNotification)
      final result = client.retrieve(PUT("$path/${savedNotification.id}", new NotificationRequestValueObject(updatedNotification)), NotificationResponseValueObject).notification

      then:
      result.message == "Updated message"
      result.id == savedNotification.id
   }

   void "put valid notification of type employee add recipient" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())
      notification.recipients.addAll(recipientNotifications)

      when:
      final updatedNotification = new NotificationValueObject(null, notification.message, notification)
      final newRecipient = NotificationRecipientTestDataLoader.stream(1, notification).findFirst().orElseThrow { new Exception("Unable to create NotificationRecipient")}
      updatedNotification.recipients.add(new NotificationRecipientValueObject(newRecipient))
      final result = client.retrieve(PUT("$path/${notification.id}", new NotificationRequestValueObject(updatedNotification)), NotificationResponseValueObject).notification

      then:
      result.id == notification.id
      result.recipients.size() == 3
   }

   void "put valid notification of type employee remove recipient with recipient ID's from client" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())
      notification.recipients.add(recipientNotifications[0])

      when:
      final updatedNotification = new NotificationValueObject(null, notification.message, notification)
      final result = client.retrieve(PUT("$path/${notification.id}", new NotificationRequestValueObject(updatedNotification)), NotificationResponseValueObject).notification

      then:
      result.id == notification.id
      result.recipients.size() == 1
      result.recipients[0].id == recipientNotifications[0].id
   }

   void "put valid notification of type employee remove recipient without recipient ID's from client" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())
      notification.recipients.add(recipientNotifications[0])

      when:
      final updatedNotification = new NotificationValueObject(null, notification.message, notification)
      updatedNotification.recipients.each { it.id = null }
      final result = client.retrieve(PUT("$path/${notification.id}", new NotificationRequestValueObject(updatedNotification)), NotificationResponseValueObject).notification

      then:
      result.id == notification.id
      result.recipients.size() == 1
      result.recipients[0].id == recipientNotifications[0].id
   }

   void "put invalid notification with all nulls" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def savedNotification = notificationsDataLoaderService.stream(1, "testco", null, null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }

      when:
      final updatedNotification = new NotificationValueObject(savedNotification)
      updatedNotification.startDate = null
      updatedNotification.expirationDate = null
      updatedNotification.company = null
      updatedNotification.message = null
      updatedNotification.sendingEmployee = null
      updatedNotification.notificationType = null
      client.exchange(
         PUT("$path/${savedNotification.id}", new NotificationRequestValueObject(updatedNotification)),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(HttpClientResponseException)
      final errors = exception.response.bodyAsJson()
      errors.size() == 6

      errors.collect { new ErrorDataTransferObject(it.message, it.path) }.sort {o1, o2 -> o1 <=> o2 } == [
         new ErrorDataTransferObject("Is required", "notification.company"),
         new ErrorDataTransferObject("Is required", "notification.expirationDate"),
         new ErrorDataTransferObject("Is required", "notification.message"),
         new ErrorDataTransferObject("Is required", "notification.notificationType"),
         new ErrorDataTransferObject("Is required", "notification.sendingEmployee"),
         new ErrorDataTransferObject("Is required", "notification.startDate"),
      ].sort { o1, o2 -> o1 <=> o2 }
   }

   void "put invalid notification of type all without an ID" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def notification = NotificationTestDataLoader.stream(1, companyId, null, null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create Notification") }

      when:
      client.retrieve(PUT("$path/${notification.id}", new NotificationRequestValueObject(new NotificationValueObject(notification))), Argument.of(NotificationResponseValueObject), Argument.of(ErrorDataTransferObject[]))

      then:
      final exception = thrown(HttpClientResponseException)
      final errors = exception.response.getBody(ErrorDataTransferObject[]).get().sort {o1, o2 -> (o1.message <=> o2.message) }
      errors.size() == 1
      errors[0].message == "Failed to convert argument [id] for value [null]"
      errors[0].path == "id"
   }

   void "delete notification of type all" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def notification = notificationsDataLoaderService.stream(1, "testco", null, null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }

      when:
      final def response = client.exchange(DELETE("$path/${notification.id}"))

      then:
      response.status == NO_CONTENT
   }

   void 'delete notification of type employee with recipients' () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType, null).findFirst().orElseThrow { new Exception("Unable to create notification") }
      notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      final def response = client.exchange(DELETE("$path/${notification.id}"))

      then:
      response.status == NO_CONTENT
   }
}
