package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerSpecificationBase
import com.hightouchinc.cynergi.middleware.dto.NotificationResponseDto
import com.hightouchinc.cynergi.middleware.dto.NotificationsResponseDto
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.test.data.loader.NotificationDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.NotificationRecipientDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.NotificationTypeDomainTestDataLoader
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.hateos.JsonError

import java.time.LocalDate
import java.util.stream.Collectors

import static com.hightouchinc.cynergi.test.helper.SpecificationHelpers.allPropertiesFullAndNotEmptyExcept
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

class NotificationControllerSpecification extends ControllerSpecificationBase {
   final def url = "/api/notifications"
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

   void "fetch one notification by id not found" () {
      when:
      client.exchange(GET("$url/0"), NotificationResponseDto)

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(JsonError).orElse(null)?.message == "Resource 0 was unable to be found"
   }

   void "fetch all by company with type All" () {
      given:
      final def companyId = "corrto"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }
      final def fiveNotifications = notificationsDataLoaderService.stream(5, companyId, LocalDate.now(), null, notificationType).collect(Collectors.toList())

      when:
      def result = client.retrieve(GET("$url?type=${notificationType.value}").headers(["X-Auth-Company": companyId]), NotificationsResponseDto)

      then:
      notThrown(HttpClientResponseException)
      result == new NotificationsResponseDto(fiveNotifications.collect { new NotificationDto(it)} )
      result.notifications.size() == 5
   }

   void "fetch all by company with type Employee" () {
      given:
      final def companyId = "corrto"
      final def notificationType = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }
      final def notification = notificationsDataLoaderService.stream(1, companyId, LocalDate.now(), null, notificationType).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def recipientNotifications = notificationRecipientDataLoaderService.stream(2, notification).collect(Collectors.toList())

      when:
      def recipient = recipientNotifications[0].recipient
      def result = client.retrieve(GET("$url?type=${notificationType.value}").headers(["X-Auth-Company": companyId, "X-Auth-User": recipient]), NotificationsResponseDto)

      then:
      notThrown(HttpClientResponseException)
      result == new NotificationsResponseDto([new NotificationDto(notification)])
      result.notifications.size() == 1
   }

   void "fetch all by company without the required X-Auth-Company header" () {
      when:
      client.retrieve(GET(url), NotificationsResponseDto)

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      exception.response.getBody(JsonError).orElse(null)?.message == "Required argument companyId not specified"
   }
}
