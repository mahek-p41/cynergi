package com.hightouchinc.cynergi.middleware.validator


import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.NotificationService
import com.hightouchinc.cynergi.test.data.loader.NotificationTestDataLoader
import com.hightouchinc.cynergi.test.data.loader.NotificationTypeDomainTestDataLoader
import spock.lang.Specification

import java.time.format.DateTimeFormatter

import static com.hightouchinc.cynergi.middleware.config.ExternalBeanFactory.DATE_PATTERN

class NotificationValidatorSpecification extends Specification {

   void "validate save valid NotificationDto of type All" () {
      given:
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationDto = NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll).map { new NotificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Notification")}
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationDto)

      then:
      notThrown(ValidationException)
   }
}
