package com.cynergisuite.middleware.notification

import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.EndDateBeforeStart
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.localization.NotNull
import com.cynergisuite.middleware.localization.NotificationRecipientsRequired
import com.cynergisuite.middleware.localization.NotificationRecipientsRequiredAll
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

import static com.cynergisuite.middleware.ExternalBeanFactory.DATE_PATTERN
import static java.time.Month.FEBRUARY
import static java.time.Month.JANUARY

class NotificationValidatorSpecification extends Specification {

   void "validate save valid NotificationValueObject of type All" () {
      given:
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationValueObject = NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll, null)
         .map { new NotificationValueObject(it) }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationValueObject)

      then:
      notThrown(ValidationException)
   }

   void "validate save valid NotificationValueObject of type Employee with single recipient" () {
      given:
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }

      when:
      final def notification = NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll, null).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def notificationRecipients = NotificationRecipientTestDataLoader.stream(1, notification).collect(Collectors.toList())
      final def notificationValueObject = new NotificationValueObject(notification, notificationRecipients)
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationValueObject)

      then:
      notThrown(ValidationException)
   }

   void "validate save invalid NotificationValueObject of type All with single recipient" () {
      given:
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notification = NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll, null).findFirst().orElseThrow { new Exception("Unable to create Notification") }
      final def notificationRecipients = NotificationRecipientTestDataLoader.stream(1, notification).collect(Collectors.toList())
      final def notificationValueObject = new NotificationValueObject(notification, notificationRecipients)
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationValueObject)

      then:
      final def exception = thrown(ValidationException)
      exception.errors.size() == 1
      exception.errors[0].localizationCode instanceof NotificationRecipientsRequiredAll
      exception.errors[0].path == "recipients"
      exception.errors[0].localizationCode.arguments.size() == 1
      exception.errors[0].localizationCode.arguments[0] == "A"
   }

   void "validate save invalid NotificationValueObject of type Employee with no recipients" () {
      given:
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "E" }

      when:
      final def notificationValueObject = NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll, null)
         .map { new NotificationValueObject(it) }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationValueObject)


      then:
      final def exception = thrown(ValidationException)
      exception.errors.size() == 1
      exception.errors[0].localizationCode instanceof NotificationRecipientsRequired
      exception.errors[0].path == "recipients"
      exception.errors[0].localizationCode.arguments.size() == 1
      exception.errors[0].localizationCode.arguments[0] == "E:Employee"
   }

   void "validate save invalid NotificationValueObject of type All where startDate is after expirationDate" () {
      given:
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationValueObject = NotificationTestDataLoader.stream(1, "corrto", LocalDate.of(2000, FEBRUARY, 2), LocalDate.of(2000, JANUARY, 2), notificationTypeAll, null)
         .map { new NotificationValueObject(it) }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateSave(notificationValueObject)

      then:
      final def exception = thrown(ValidationException)
      exception.errors.size() == 1
      exception.errors[0].localizationCode instanceof EndDateBeforeStart
      exception.errors[0].path == "expirationDate"
      exception.errors[0].localizationCode.arguments.size() == 2
      exception.errors[0].localizationCode.arguments[0] == "01/02/2000"
      exception.errors[0].localizationCode.arguments[1] == "02/02/2000"
   }

   void "validate update valid NotificationValueObject of type all" () {
      given:
      final def notificationId = 1
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationValueObject = NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll, null)
         .map { new NotificationValueObject(it) }
         .peek { it.id = notificationId }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateUpdate(notificationValueObject)

      then:
      notThrown(ValidationException)
      1 * notificationService.exists(notificationId) >> true
   }

   void "validate update invalid NotificationValueObject of type all due to missing ID" () {
      given:
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationValueObject = NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll, null)
         .map { new NotificationValueObject(it) }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateUpdate(notificationValueObject)

      then:
      final exception = thrown(ValidationException)
      exception.errors.size() == 1
      exception.errors[0].localizationCode instanceof NotNull
      exception.errors[0].path == "id"
      exception.errors[0].localizationCode.arguments.size() == 1
      exception.errors[0].localizationCode.arguments[0] == "id"
      0 * notificationService.exists(_ as long) >> true
   }

   void "validate update invalid NotificationValueObject of type all due to exisiting Notification not existing with provided ID" () {
      given:
      final def notificationId = 1
      final NotificationService notificationService = Mock()
      final def dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
      final def notificationTypeAll = NotificationTypeDomainTestDataLoader.values().find { it.value == "A" }

      when:
      final def notificationValueObject = NotificationTestDataLoader.stream(1, "corrto", null, null, notificationTypeAll, null)
         .map { new NotificationValueObject(it) }
         .peek { it.id = notificationId }
         .findFirst().orElseThrow { new Exception("Unable to create Notification") }
      new NotificationValidator(notificationService, dateFormatter).validateUpdate(notificationValueObject)

      then:
      final exception = thrown(ValidationException)
      exception.errors.size() == 1
      exception.errors[0].localizationCode instanceof NotFound
      exception.errors[0].path == "id"
      exception.errors[0].localizationCode.arguments.size() == 1
      exception.errors[0].localizationCode.arguments[0] == notificationId
      1 * notificationService.exists(notificationId) >> false
   }
}
