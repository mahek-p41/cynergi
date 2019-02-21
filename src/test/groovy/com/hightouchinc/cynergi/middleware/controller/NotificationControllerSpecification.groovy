package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerSpecificationBase
import com.hightouchinc.cynergi.middleware.dto.ErrorDto
import com.hightouchinc.cynergi.middleware.dto.NotificationRequestDto
import com.hightouchinc.cynergi.middleware.dto.NotificationResponseDto
import com.hightouchinc.cynergi.middleware.dto.NotificationsResponseDto
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.entity.NotificationRecipientDto
import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomainDto
import com.hightouchinc.cynergi.middleware.repository.NotificationRepository
import com.hightouchinc.cynergi.test.data.loader.NotificationDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.NotificationRecipientDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.NotificationRecipientTestDataLoader
import com.hightouchinc.cynergi.test.data.loader.NotificationTestDataLoader
import com.hightouchinc.cynergi.test.data.loader.NotificationTypeDomainTestDataLoader
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException

import java.time.LocalDate
import java.util.stream.Collectors

import static com.hightouchinc.cynergi.test.helper.SpecificationHelpers.allPropertiesFullAndNotEmptyExcept
import static io.micronaut.http.HttpRequest.DELETE
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

class NotificationControllerSpecification extends ControllerSpecificationBase {
   final def url = "/api/notifications"
   final def notificationRepository = applicationContext.getBean(NotificationRepository)
   final def notificationsDataLoaderService = applicationContext.getBean(NotificationDataLoaderService)
   final def notificationRecipientDataLoaderService = applicationContext.getBean(NotificationRecipientDataLoaderService)

   void "fetch one notification by id with no recipients" () {
      given:
      final def savedNotification = notificationsDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def notificationDto = new NotificationResponseDto(new NotificationDto(savedNotification))

      when:
      def result = client.retrieve(GET("$url/${savedNotification.id}"), NotificationResponseDto)

      then:
      result == notificationDto
      allPropertiesFullAndNotEmptyExcept(result.notification, "recipients")
   }

   void "fetch one notification by id with recipients" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipients = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      def result = client.retrieve(GET("$url/${notification.id}"), NotificationResponseDto)

      then:
      result.notification.recipients.size() == 2
      result.notification.recipients.sort({ o1, o2 -> o1.id <=> o2.id}) == recipients.sort({ o1, o2 -> o1.id <=> o2.id}).collect { new NotificationRecipientDto(it) }
   }

   void "fetch one notification by id not found" () {
      when:
      client.exchange(GET("$url/0"), Argument.of(NotificationResponseDto), Argument.of(ErrorDto))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(ErrorDto).orElse(null)?.message == "Resource 0 was unable to be found"
   }

   void "fetch all by sending employee and company through the admin path" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def sendingEmployee = "bob"
      final def fiveNotifications = notificationsDataLoaderService.stream(5, companyId, LocalDate.now(), null, notificationType, sendingEmployee).collect(Collectors.toList())
      fiveNotifications.each { notification -> notificationRecipientDataLoaderService.stream(2, notification).forEach { notification.recipients.add(it) } }

      when:
      def result = client.retrieve(GET("$url/admin").headers(["X-Auth-Company": companyId, "X-Auth-User": sendingEmployee]), NotificationsResponseDto)

      then:
      result.notifications.each { it.recipients.sort { o1, o2 -> o1.id <=> o2.id } }.sort { o1, o2 -> o1.id <=> o2.id }
      result.notifications.size() == 5
      result.notifications.collect { it.sendingEmployee }.findAll { it == sendingEmployee }.size() == 5
      result.notifications == fiveNotifications.collect { new NotificationDto(it) }
      result.notifications.collect { it.recipients.size() }.findAll { it == 2 }.size() == 5
   }

   @Deprecated
   void "fetch all by company with type All deprecated" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def fiveNotifications = notificationsDataLoaderService.stream(5, companyId, LocalDate.now(), null, notificationType).collect(Collectors.toList())

      when:
      def result = client.retrieve(GET("$url?type=${notificationType.value}").headers(["X-Auth-Company": companyId]), NotificationsResponseDto)

      then:
      result == new NotificationsResponseDto(fiveNotifications.collect { new NotificationDto(it)} )
      result.notifications.size() == 5
   }

   void "fetch all by company with type All" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def fiveNotifications = notificationsDataLoaderService.stream(5, companyId, LocalDate.now(), null, notificationType).collect(Collectors.toList())

      when:
      def result = client.retrieve(GET("$url/company/${companyId}?type=${notificationType.value}"), NotificationDto[])

      then:
      result.size() == 5
      result == fiveNotifications.collect { new NotificationDto(it) }.toArray()
   }

   @Deprecated
   void "fetch all by company with type Employee deprecated" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      def recipient = recipientNotifications[0].recipient
      def result = client.retrieve(GET("$url?type=${notificationType.value}").headers(["X-Auth-Company": companyId, "X-Auth-User": recipient]), NotificationsResponseDto)

      then:
      notThrown(HttpClientResponseException)
      result.notifications.size() == 1
      result.notifications[0].id == notification.id
      result.notifications[0].recipients[0] == new NotificationRecipientDto(recipientNotifications[0])
   }

   void "fetch all by company with type Employee" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      def recipient = recipientNotifications[0].recipient
      def result = client.retrieve(GET("$url/company/${companyId}/${recipient}?type=${notificationType.value}"), NotificationDto[])

      then:
      result.size() == 1
      result[0].id == notification.id
      result[0].recipients[0] == new NotificationRecipientDto(recipientNotifications[0])
   }

   @Deprecated
   void "fetch all by company without the required X-Auth-Company header deprecated" () {
      when:
      client.retrieve(GET(url), Argument.of(NotificationsResponseDto), Argument.of(ErrorDto))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      exception.response.getBody(ErrorDto).orElse(null)?.message == "Required argument companyId not specified"
   }

   @Deprecated
   void "fetch all permissions should only be hard coded department" () {
      when:
      final result = client.retrieve(GET("$url/permissions"), Map)

      then:
      result.size() == 2
      result["id"] == 1
      result["depts_allowed"].size() == 1
      result["depts_allowed"][0] == "ALL"
   }

   void "fetch all types" () {
      when:
      final def types = client.retrieve(GET("${url}/types"), NotificationTypeDomainDto[])

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
      client.retrieve(GET("${url}/type"), Argument.of(NotificationTypeDomainDto[]), Argument.of(ErrorDto))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      exception.response.getBody(ErrorDto).orElse(null)?.message == "Failed to convert argument [id] for value [type]"
   }

   void "post valid notification of type All" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def notification = NotificationTestDataLoader.stream(1, "testco", null, null, notificationType).findFirst().orElseThrow { new Exception("Unable to create Notification") }

      when:
      final def savedNotification = client.retrieve(POST(url, new NotificationRequestDto(new NotificationDto(notification))), NotificationResponseDto).notification

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
      final def notification = NotificationTestDataLoader.stream(1, "testco", null, null, notificationType).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def notificationPayload = new NotificationDto(notification, "A")

      when:
      final def savedNotification = client.retrieve(POST(url, new NotificationRequestDto(notificationPayload)), NotificationResponseDto).notification

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
      final def notification = NotificationTestDataLoader.stream(1, "testco", null, null, notificationType).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def notificationRecipients = NotificationRecipientTestDataLoader.stream(1, notification).collect(Collectors.toList())

      when:
      final def savedNotification = client.retrieve(POST(url, new NotificationRequestDto(new NotificationDto(notification, notificationRecipients))), NotificationResponseDto).notification

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
      final def notification = NotificationTestDataLoader.stream(1, "testco", null, null, notificationType).findFirst().orElseThrow { new Exception("Unable to create Notification") }

      when:
      client.retrieve(POST(url, new NotificationRequestDto(new NotificationDto(notification))), Argument.of(NotificationResponseDto), Argument.of(ErrorDto[]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final errors = exception.response.getBody(ErrorDto[]).get()
      errors.size() == 1
      errors[0].message == "Recipients required for notification type E:Employee"
      errors[0].path == "recipients"
   }

   void "post invalid notification of type all with nulls" () {
      given:
      final def notification = new NotificationDto(null, null, null, null, null, null, null, [])

      when:
      client.retrieve(POST(url, new NotificationRequestDto(notification)), Argument.of(NotificationResponseDto), Argument.of(ErrorDto[]))

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final errors = exception.response.getBody(ErrorDto[]).get().sort { o1, o2 -> (o1.message <=> o2.message) }
      errors.size() == 6
      errors[0].message == "notification.company is required"
      errors[0].path == "notification.company"
      errors[1].message == "notification.expirationDate is required"
      errors[1].path == "notification.expirationDate"
      errors[2].message == "notification.message is required"
      errors[2].path == "notification.message"
      errors[3].message == "notification.notificationType is required"
      errors[3].path == "notification.notificationType"
      errors[4].message == "notification.sendingEmployee is required"
      errors[4].path == "notification.sendingEmployee"
      errors[5].message == "notification.startDate is required"
      errors[5].path == "notification.startDate"
   }

   void "put valid notification of type all" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def savedNotification = notificationsDataLoaderService.stream(1, "testco", null, null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }

      when:
      final updatedNotification = new NotificationDto(null, "Updated message", savedNotification)
      final result = client.retrieve(PUT("$url/${savedNotification.id}", new NotificationRequestDto(updatedNotification)), NotificationResponseDto).notification

      then:
      result.message == "Updated message"
      result.id == savedNotification.id
   }

   void "put valid notification of type employee add recipient" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())
      notification.recipients.addAll(recipientNotifications)

      when:
      final updatedNotification = new NotificationDto(null, notification.message, notification)
      final newRecipient = NotificationRecipientTestDataLoader.stream(1, notification).findFirst().orElseThrow { new Exception("Unable to create NotificationRecipient")}
      updatedNotification.recipients.add(new NotificationRecipientDto(newRecipient))
      final result = client.retrieve(PUT("$url/${notification.id}", new NotificationRequestDto(updatedNotification)), NotificationResponseDto).notification

      then:
      result.id == notification.id
      result.recipients.size() == 3
   }

   void "put valid notification of type employee remove recipient" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())
      notification.recipients.add(recipientNotifications[0])

      when:
      final updatedNotification = new NotificationDto(null, notification.message, notification)
      final result = client.retrieve(PUT("$url/${notification.id}", new NotificationRequestDto(updatedNotification)), NotificationResponseDto).notification

      then:
      result.id == notification.id
      result.recipients.size() == 1
      result.recipients[0].id == recipientNotifications[0].id
   }

   void "put invalid notification with all nulls" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def savedNotification = notificationsDataLoaderService.stream(1, "testco", null, null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }

      when:
      final updatedNotification = new NotificationDto(savedNotification)
      updatedNotification.startDate = null
      updatedNotification.expirationDate = null
      updatedNotification.company = null
      updatedNotification.message = null
      updatedNotification.sendingEmployee = null
      updatedNotification.notificationType = null
      client.retrieve(PUT("$url/${savedNotification.id}", new NotificationRequestDto(updatedNotification)), Argument.of(NotificationResponseDto), Argument.of(ErrorDto[]))

      then:
      final exception = thrown(HttpClientResponseException)
      final errors = exception.response.getBody(ErrorDto[]).get().sort { o1, o2 -> (o1.message <=> o2.message) }
      errors.size() == 6
      errors[0].message == "notification.company is required"
      errors[0].path == "notification.company"
      errors[1].message == "notification.expirationDate is required"
      errors[1].path == "notification.expirationDate"
      errors[2].message == "notification.message is required"
      errors[2].path == "notification.message"
      errors[3].message == "notification.notificationType is required"
      errors[3].path == "notification.notificationType"
      errors[4].message == "notification.sendingEmployee is required"
      errors[4].path == "notification.sendingEmployee"
      errors[5].message == "notification.startDate is required"
      errors[5].path == "notification.startDate"
   }

   void "put invalid notification of type all without an ID" () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def notification = NotificationTestDataLoader.stream(1, companyId, null, null, notificationType).findFirst().orElseThrow { new Exception("Unable to create Notification") }

      when:
      client.retrieve(PUT("$url/${notification.id}", new NotificationRequestDto(new NotificationDto(notification))), Argument.of(NotificationResponseDto), Argument.of(ErrorDto[]))

      then:
      final exception = thrown(HttpClientResponseException)
      final errors = exception.response.getBody(ErrorDto[]).get().sort { o1, o2 -> (o1.message <=> o2.message) }
      errors.size() == 1
      errors[0].message == "Failed to convert argument [id] for value [null]"
      errors[0].path == "id"
   }

   void "delete notification of type all" () {
      given:
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def notification = notificationsDataLoaderService.stream(1, "testco", null, null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }

      when:
      final def response = client.exchange(DELETE("$url/${notification.id}"))

      then:
      response.status == NO_CONTENT
   }

   void 'delete notification of type employee with recipients' () {
      given:
      final def companyId = "testco"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }
      notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      final def response = client.exchange(DELETE("$url/${notification.id}"))

      then:
      response.status == NO_CONTENT
   }
}
