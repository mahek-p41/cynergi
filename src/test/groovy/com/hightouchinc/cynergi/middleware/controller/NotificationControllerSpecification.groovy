package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerSpecificationBase
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.test.data.loader.NotificationDataLoaderService
import io.micronaut.http.HttpRequest

class NotificationControllerSpecification extends ControllerSpecificationBase {
   final def url = "/api/notifications"
   final def notificationsDataLoaderService = applicationContext.getBean(NotificationDataLoaderService)

   void "fetch one notification by id" () {
      given:
      final def savedNotification = notificationsDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create notification") }
      final def notificationDto = new NotificationDto(savedNotification)

      when:
      def result = client.retrieve(HttpRequest.GET("$url/${savedNotification.id}"), NotificationDto)

      then:
      result == notificationDto
   }
}
