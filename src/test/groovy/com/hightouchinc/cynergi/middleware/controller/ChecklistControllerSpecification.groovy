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

   void "fetch one checklist by id" () {
      given:
      final def savedChecklist = checklistDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Checklist") }

      expect:
      client.retrieve(GET("$url/${savedChecklist.id}"), ChecklistDto) == new ChecklistDto(savedChecklist)
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

      expect:
      client.retrieve(GET("$url/account/${savedChecklist.customerAccount}"), ChecklistDto) == new ChecklistDto(savedChecklist)
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

   void "save checklist with longer than allowed customer comments" () {
      given:
      final def stringFaker = new Faker().lorem()
      final def checklist = ChecklistTestDataLoader.stream(1).map { new ChecklistDto(it) }.peek { it.customerComments = stringFaker.fixedString(260) }.findFirst().orElseThrow { new Exception("Unable to create Checklist") }

      when:
      client.exchange(POST(url, checklist))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
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
