package com.hightouchinc.cynergi.middleware.controller

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.controller.spi.ControllerSpecificationBase
import com.hightouchinc.cynergi.middleware.entity.ChecklistDto
import com.hightouchinc.cynergi.test.data.loader.ChecklistDataLoaderService
import com.hightouchinc.cynergi.test.data.loader.ChecklistTestDataLoader
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.hateos.JsonError

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

class ChecklistControllerSpecification extends ControllerSpecificationBase {
   final def url = "/api/company/corrto/verification"
   final def checklistDataLoaderService = applicationContext.getBean(ChecklistDataLoaderService)

   void "fetch one checklist by id where everything is filled out" () {
      given:
      final def savedChecklist = checklistDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Checklist") }
      final def checklistDto = new ChecklistDto(savedChecklist)

      when:
      def result = client.retrieve(GET("$url/${savedChecklist.id}"), ChecklistDto)

      then:
      result == checklistDto
      result.properties.findAll { it.value == null }.size() == 0 //check that none of the properties on the result are null
   }

   void "fetch one checklist by id not found" () {
      when:
      client.exchange(GET("$url/0"), ChecklistDto)

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(JsonError).orElse(null)?.message == "Resource 0 was unable to be found"
   }

   void "fetch one checklist by customer account" () {
      given:
      final def savedChecklist = checklistDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Checklist") }
      final def checklistDto = new ChecklistDto(savedChecklist)

      when:
      def result = client.retrieve(GET("$url/account/${savedChecklist.customerAccount}"), ChecklistDto)

      then:
      result == checklistDto
      result.properties.findAll { it.value == null }.size() == 0 //check that none of the properties on the result are null
   }

   void "fetch one checklist by customer account not found" () {
      when:
      client.exchange(GET("$url/account/-1"), ChecklistDto)

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(JsonError).orElse(null)?.message == "Resource -1 was unable to be found"
   }

   void "save checklist successfully" () {
      given:
      final def checklist = ChecklistTestDataLoader.stream(1).map { new ChecklistDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Checklist") }

      when:
      final def savedChecklist = client.retrieve(POST(url, checklist), ChecklistDto)

      then:
      savedChecklist.id != null
      savedChecklist.id > 0
      savedChecklist.customerAccount == checklist.customerAccount
      savedChecklist.customerComments == checklist.customerComments
      savedChecklist.verifiedBy == checklist.verifiedBy
      savedChecklist.verifiedTime != null
   }

   void "save checklist without auto, employment or landlord" () {
      given:
      final def checklist = ChecklistTestDataLoader.stream(1, false, false, false).map { new ChecklistDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Checklist") }

      when:
      final def savedChecklist = client.retrieve(POST(url, checklist), ChecklistDto)

      then:
      savedChecklist.id != null
      savedChecklist.id > 0
      savedChecklist.customerAccount == checklist.customerAccount
      savedChecklist.customerComments == checklist.customerComments
      savedChecklist.verifiedBy == checklist.verifiedBy
      savedChecklist.verifiedTime != null
      savedChecklist.properties.findAll { it.value == null }.size() == 3 // helps cover the case when new nullable composite properties are added that are like auto, employment or landlord
      savedChecklist.properties.findAll { it.value == null }.collect { it.key }.containsAll(['auto', 'employment', 'landlord'])
   }

   void "save completely empty checklist should fail" () {
      given:
      final def checklist = new ChecklistDto(
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         null
      )

      when:
      client.retrieve(POST(url, checklist), ChecklistDto)

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final def errors = exception.response.getBody(JsonError[]).orElse(null)
      errors != null
      errors.size() == 3

      def sortedErrors = errors.sort { o1, o2 -> (o1.message <=> o2.message)}
      sortedErrors[0].message == "customerAccount is required"
      sortedErrors[1].message == "verifiedBy is required"
      sortedErrors[2].message == "verifiedTime is required"
   }

   void "save checklist with longer than allowed customer comments should result in a failure" () {
      given:
      final def stringFaker = new Faker().lorem()
      final def checklist = ChecklistTestDataLoader.stream(1).map { new ChecklistDto(it) }.peek { it.customerComments = stringFaker.fixedString(260) }.findFirst().orElseThrow { new Exception("Unable to create Checklist") }

      when:
      client.exchange(POST(url, checklist))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final def errors = exception.response.getBody(JsonError[]).orElse(null)
      errors != null
      errors.size() == 1
      errors[0].message == "provided value ${checklist.customerComments} is too large for customerComments"
   }

   void "update checklist successfully" () {
      given:
      final def savedChecklist = checklistDataLoaderService.stream(1).map { new ChecklistDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Checklist") }
      final def toUpdateCheckList = savedChecklist.copyMe()

      when:
      toUpdateCheckList.customerComments = "Updated comments"
      final def updatedChecklist = client.retrieve(PUT(url, toUpdateCheckList), ChecklistDto)

      then:
      updatedChecklist == toUpdateCheckList
   }
}
