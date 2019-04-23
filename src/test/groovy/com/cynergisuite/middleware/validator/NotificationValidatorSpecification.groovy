package com.cynergisuite.middleware.validator


import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static com.cynergisuite.middleware.config.ExternalBeanFactory.DATE_PATTERN
import static com.cynergisuite.middleware.localization.MessageCodes.Cynergi.END_DATE_BEFORE_START
import static com.cynergisuite.middleware.localization.MessageCodes.Cynergi.NOTIFICATION_RECIPIENTS_ALL
import static com.cynergisuite.middleware.localization.MessageCodes.Cynergi.NOTIFICATION_RECIPIENTS_REQUIRED
import static com.cynergisuite.middleware.localization.MessageCodes.System.NOT_FOUND
import static com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import static java.time.Month.FEBRUARY
import static java.time.Month.JANUARY

class NotificationValidatorSpecification extends Specification {

   void "validate save valid NotificationDto of type All" () {
      given:
      final com.cynergisuite.middleware.service.NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = com.cynergisuite.test.data.loader.NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationDto = com.cynergisuite.test.data.loader.NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll)
         .map { new com.cynergisuite.middleware.entity.NotificationDto(it) }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationDto)

      then:
      notThrown(com.cynergisuite.middleware.exception.ValidationException)
   }

   void "validate save valid NotificationDto of type Employee with single recipient" () {
      given:
      final com.cynergisuite.middleware.service.NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = com.cynergisuite.test.data.loader.NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }

      when:
      final def notification = com.cynergisuite.test.data.loader.NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def notificationRecipients = com.cynergisuite.test.data.loader.NotificationRecipientTestDataLoader.stream(1, notification).collect(Collectors.toList())
      final def notificationDto = new com.cynergisuite.middleware.entity.NotificationDto(notification, notificationRecipients)
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationDto)

      then:
      notThrown(com.cynergisuite.middleware.exception.ValidationException)
   }

   void "validate save invalid NotificationDto of type All with single recipient" () {
      given:
      final com.cynergisuite.middleware.service.NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = com.cynergisuite.test.data.loader.NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notification = com.cynergisuite.test.data.loader.NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def notificationRecipients = com.cynergisuite.test.data.loader.NotificationRecipientTestDataLoader.stream(1, notification).collect(Collectors.toList())
      final def notificationDto = new com.cynergisuite.middleware.entity.NotificationDto(notification, notificationRecipients)
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationDto)

      then:
      final def exception = thrown(com.cynergisuite.middleware.exception.ValidationException)
      exception.errors.size() == 1
      exception.errors[0].messageTemplate == NOTIFICATION_RECIPIENTS_ALL
      exception.errors[0].path == "recipients"
      exception.errors[0].arguments.size() == 1
      exception.errors[0].arguments[0] == "A"
   }

   void "validate save invalid NotificationDto of type Employee with no recipients" () {
      given:
      final com.cynergisuite.middleware.service.NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = com.cynergisuite.test.data.loader.NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }

      when:
      final def notificationDto = com.cynergisuite.test.data.loader.NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll)
         .map { new com.cynergisuite.middleware.entity.NotificationDto(it) }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationDto)


      then:
      final def exception = thrown(com.cynergisuite.middleware.exception.ValidationException)
      exception.errors.size() == 1
      exception.errors[0].messageTemplate == NOTIFICATION_RECIPIENTS_REQUIRED
      exception.errors[0].path == "recipients"
      exception.errors[0].arguments.size() == 1
      exception.errors[0].arguments[0] == "E:Employee"
   }

   void "validate save invalid NotificationDto of type All where startDate is after expirationDate" () {
      given:
      final com.cynergisuite.middleware.service.NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = com.cynergisuite.test.data.loader.NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationDto = com.cynergisuite.test.data.loader.NotificationTestDataLoader.stream(1, "corrto", LocalDate.of(2000, FEBRUARY, 2), LocalDate.of(2000, JANUARY, 2), notificationTypeAll)
         .map { new com.cynergisuite.middleware.entity.NotificationDto(it) }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationDto)

      then:
      final def exception = thrown(com.cynergisuite.middleware.exception.ValidationException)
      exception.errors.size() == 1
      exception.errors[0].messageTemplate == END_DATE_BEFORE_START
      exception.errors[0].path == "expirationDate"
      exception.errors[0].arguments.size() == 2
      exception.errors[0].arguments[0] == "01/02/2000"
      exception.errors[0].arguments[1] == "02/02/2000"
   }

   void "validate update valid NotificationDto of type all" () {
      given:
      final def notificationId = 1
      final com.cynergisuite.middleware.service.NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = com.cynergisuite.test.data.loader.NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationDto = com.cynergisuite.test.data.loader.NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll)
         .map { new com.cynergisuite.middleware.entity.NotificationDto(it) }
         .peek { it.id = notificationId }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateUpdate(notificationDto)

      then:
      notThrown(com.cynergisuite.middleware.exception.ValidationException)
      1 * notificationService.exists(notificationId) >> true
   }

   void "validate update invalid NotificationDto of type all due to missing ID" () {
      given:
      final com.cynergisuite.middleware.service.NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = com.cynergisuite.test.data.loader.NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationDto = com.cynergisuite.test.data.loader.NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll)
         .map { new com.cynergisuite.middleware.entity.NotificationDto(it) }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateUpdate(notificationDto)

      then:
      final exception = thrown(com.cynergisuite.middleware.exception.ValidationException)
      exception.errors.size() == 1
      exception.errors[0].messageTemplate == NOT_NULL
      exception.errors[0].path == "id"
      exception.errors[0].arguments.size() == 1
      exception.errors[0].arguments[0] == "id"
      0 * notificationService.exists(_ as long) >> true
   }

   void "validate update invalid NotificationDto of type all due to exisiting Notification not existing with provided ID" () {
      given:
      final def notificationId = 1
      final com.cynergisuite.middleware.service.NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = com.cynergisuite.test.data.loader.NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationDto = com.cynergisuite.test.data.loader.NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll)
         .map { new com.cynergisuite.middleware.entity.NotificationDto(it) }
         .peek { it.id = notificationId }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateUpdate(notificationDto)

      then:
      final exception = thrown(com.cynergisuite.middleware.exception.ValidationException)
      exception.errors.size() == 1
      exception.errors[0].messageTemplate == NOT_FOUND
      exception.errors[0].path == "id"
      exception.errors[0].arguments.size() == 1
      exception.errors[0].arguments[0] == notificationId
      1 * notificationService.exists(notificationId) >> false
   }
}
