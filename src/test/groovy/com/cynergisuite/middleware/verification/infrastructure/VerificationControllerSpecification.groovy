package com.cynergisuite.middleware.verification.infrastructure

import com.cynergisuite.middleware.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.verfication.Verification
import com.cynergisuite.middleware.verfication.VerificationDto
import com.cynergisuite.middleware.verfication.VerificationReference
import com.cynergisuite.middleware.verfication.infrastructure.VerificationReferenceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javafaker.Faker
import com.cynergisuite.middleware.error.ErrorValueObject
import groovy.json.JsonSlurper
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException

import java.util.stream.Collectors

import static com.cynergisuite.test.helper.SpecificationHelpers.allPropertiesFullAndNotEmpty
import static com.cynergisuite.test.helper.SpecificationHelpers.allPropertiesFullAndNotEmptyExcept
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

class VerificationControllerSpecification extends ControllerSpecificationBase {
   final def url = "/api/verifications/corrto"
   final def verificationDataLoaderService = applicationContext.getBean(com.cynergisuite.test.data.loader.VerificationDataLoaderService)
   final def verificationReferenceDataLoaderService = applicationContext.getBean(com.cynergisuite.test.data.loader.VerificationReferenceDataLoaderService)
   final def verificationReferenceRepository = applicationContext.getBean(VerificationReferenceRepository)
   final ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper)

   void "fetch one verification by id where everything is filled out" () {
      given:
      final def savedVerification = verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationDto = new VerificationDto(savedVerification)

      when:
      def result = client.retrieve(GET("$url/${savedVerification.id}"), VerificationDto)

      then:
      notThrown(HttpClientResponseException)
      result == verificationDto
      result.references.size() == 6
      allPropertiesFullAndNotEmpty(result)
   }

   void "fetch one verification by id not found" () {
      when:
      client.exchange(GET("$url/0"), Argument.of(VerificationDto), Argument.of(ErrorValueObject))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(ErrorValueObject).orElse(null)?.message == "Resource 0 was unable to be found"
   }

   void "fetch one verification by customer account" () {
      given:
      final def savedVerification = verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationDto = new VerificationDto(savedVerification)

      when:
      def result = client.retrieve(GET("$url/account/${savedVerification.customerAccount}"), VerificationDto)

      then:
      notThrown(HttpClientResponseException)
      result == verificationDto
      allPropertiesFullAndNotEmpty(result)
   }

   void "fetch one verification by customer account not found" () {
      when:
      client.exchange(GET("$url/account/-1"), Argument.of(VerificationDto), Argument.of(ErrorValueObject))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(ErrorValueObject).orElse(null)?.message == "Resource -1 was unable to be found"
   }

   void "post verification successfully" () {
      given:
      final def verification = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }

      when:
      final def savedVerification = client.retrieve(POST(url, verification), VerificationDto)

      then:
      savedVerification.id != null
      savedVerification.id > 0
      savedVerification.customerAccount == verification.customerAccount
      savedVerification.customerComments == verification.customerComments
      savedVerification.verifiedBy == verification.verifiedBy
      savedVerification.verifiedTime != null
   }

   void "post verification without auto, employment or landlord" () {
      given:
      final def verification = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1, false, false, false).map { new VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }

      when:
      final def savedVerification = client.retrieve(POST(url, verification), VerificationDto)

      then:
      savedVerification.id != null
      savedVerification.id > 0
      savedVerification.customerAccount == verification.customerAccount
      savedVerification.customerComments == verification.customerComments
      savedVerification.verifiedBy == verification.verifiedBy
      savedVerification.verifiedTime != null
      savedVerification.properties.findAll { it.value == null }.size() == 3 // helps cover the case when new nullable composite properties are added that are like auto, employment or landlord
      savedVerification.properties.findAll { it.value == null }.collect { it.key }.containsAll(['auto', 'employment', 'landlord'])
   }

   void "post completely empty verification should fail" () {
      given:
      final def verification = new VerificationDto(
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         []
      )

      when:
      client.retrieve(POST(url, verification), Argument.of(VerificationDto), Argument.of(ErrorValueObject[]))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final def errors = exception.response.getBody(ErrorValueObject[]).orElse(null)
      errors != null
      errors.size() == 3

      def sortedErrors = errors.sort { o1, o2 -> (o1.message <=> o2.message)}
      sortedErrors[0].message == "customerAccount is required"
      sortedErrors[1].message == "verifiedBy is required"
      sortedErrors[2].message == "verifiedTime is required"
   }

   void "post verification with longer than allowed customer comments should result in a failure" () {
      given:
      final def stringFaker = new Faker().lorem()
      final def verification = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new VerificationDto(it) }.peek { it.customerComments = stringFaker.fixedString(260) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }

      when:
      client.exchange(POST(url, verification), Argument.of(VerificationDto), Argument.of(ErrorValueObject[]))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final def errors = exception.response.getBody(ErrorValueObject[]).orElse(null)
      errors != null
      errors.size() == 1
      errors[0].message == "size of provided value ${verification.customerComments} is invalid for property customerComments"
   }

   void "post verification with no references" () {
      given:
      final def verification = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1, true, true, true, false).map { new VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }

      when:
      final def savedVerification = client.retrieve(POST(url, verification), VerificationDto)

      then:
      savedVerification.id != null
      savedVerification.id > 0
      savedVerification.customerAccount == verification.customerAccount
      savedVerification.customerComments == verification.customerComments
      savedVerification.verifiedBy == verification.verifiedBy
      savedVerification.verifiedTime != null
      savedVerification.references.size() ==  0
      allPropertiesFullAndNotEmptyExcept(savedVerification, "references")
   }

   void "post verification unsuccessfully due to bad date" () {
      given:
      final def verification = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationJson = new JsonSlurper().parseText(objectMapper.writeValueAsString(verification))

      when:
      verificationJson["cust_verified_date"] = "2019-02-30"
      client.exchange(POST(url, verificationJson), Argument.of(VerificationDto), Argument.of(ErrorValueObject[]))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final def errors = exception.response.getBody(ErrorValueObject[]).orElse(null)
      errors != null
      errors.size() == 1
      errors[0].message == "Failed to convert argument [cust_verified_date] for value [2019-02-30]"
   }

   void "put verification successfully" () {
      given:
      final def savedVerification = verificationDataLoaderService.stream(1).map { new VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def toUpdateVerification = savedVerification.copyMe()

      when:
      toUpdateVerification.customerComments = "Updated comments"
      final def updatedVerification = client.retrieve(PUT(url, toUpdateVerification), VerificationDto)

      then:
      updatedVerification == toUpdateVerification
   }

   void "put verification references by adding a third reference" () {
      given:
      final Verification verification = verificationDataLoaderService.stream(1, true, true, true, false).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final List<VerificationReference> references = verificationReferenceDataLoaderService.stream(verification, 2).collect(Collectors.toList())
      final def toUpdate = verification.copyMe()
      toUpdate.references.addAll(references)
      toUpdate.references.add(com.cynergisuite.test.data.loader.VerificationReferenceTestDataLoader.stream(toUpdate, 1).findFirst().orElseThrow { new Exception("Unable to create VerificationReference") })

      when:
      final def updatedVerification = client.retrieve(PUT(url, new VerificationDto(toUpdate)), VerificationDto)

      then:
      updatedVerification.id != null
      updatedVerification.id > 0
      updatedVerification.customerAccount == verification.customerAccount
      updatedVerification.customerComments == verification.customerComments
      updatedVerification.verifiedBy == verification.verifiedBy
      updatedVerification.verifiedTime != null
      updatedVerification.references.size() ==  3
      updatedVerification.references.collect { it.id != null && it.id > 0 }.size() == 3 // check that all 3 items in the references list have an ID assigned
      allPropertiesFullAndNotEmpty(updatedVerification)
   }

   void "delete verification reference via update with one missing" () {
      given:
      final def verification = verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def savedVerification = new VerificationDto(verification)
      savedVerification.references.remove(5) // remove the last one

      when:
      final def updatedVerification = client.retrieve(PUT(url, savedVerification), VerificationDto)
      final def dbReferences = verificationReferenceRepository.findAll(verification)

      then:
      updatedVerification.id != null
      updatedVerification.id > 0
      updatedVerification.customerAccount == savedVerification.customerAccount
      updatedVerification.customerComments == savedVerification.customerComments
      updatedVerification.verifiedBy == savedVerification.verifiedBy
      updatedVerification.verifiedTime != null
      updatedVerification.references.size() ==  5
      updatedVerification.references.collect { it.id != null && it.id > 0 }.size() == 5 // check that all 5 items in the references list have an ID assigned
      allPropertiesFullAndNotEmpty(updatedVerification)

      dbReferences.size() == 5
      dbReferences.collect { it.id } == savedVerification.references.collect { it.id } // check that the db only contains the 5 items passed through the PUT call
   }

   void "delete two previously created verification reference via update with one missing" () {
      given:
      final def verification = verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def savedVerification = new VerificationDto(verification)
      savedVerification.references.remove(1)
      savedVerification.references.remove(1)

      when:
      final def updatedVerification = client.retrieve(PUT(url, savedVerification), VerificationDto)
      final def dbReferences = verificationReferenceRepository.findAll(verification) // query the db for what it actually has

      then:
      updatedVerification.id != null
      updatedVerification.id > 0
      updatedVerification.customerAccount == savedVerification.customerAccount
      updatedVerification.customerComments == savedVerification.customerComments
      updatedVerification.verifiedBy == savedVerification.verifiedBy
      updatedVerification.verifiedTime != null
      updatedVerification.references.size() ==  4
      updatedVerification.references.collect { it.id != null && it.id > 0 }.size() == 4 // check that all 5 items in the references list have an ID assigned
      allPropertiesFullAndNotEmpty(updatedVerification)

      dbReferences.size() == 4
      dbReferences.collect { it.id } == savedVerification.references.collect { it.id } // check that the db only contains the 5 items passed through the PUT call
   }
}
