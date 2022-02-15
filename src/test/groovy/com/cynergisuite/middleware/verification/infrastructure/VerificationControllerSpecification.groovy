package com.cynergisuite.middleware.verification.infrastructure

import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.error.ErrorDTO
import com.cynergisuite.middleware.verfication.Verification
import com.cynergisuite.middleware.verification.VerificationTestDataLoaderService
import com.cynergisuite.middleware.verfication.VerificationReference
import com.cynergisuite.middleware.verification.VerificationReferenceTestDataLoaderService
import com.cynergisuite.middleware.verification.VerificationReferenceTestDataLoader
import com.cynergisuite.middleware.verification.VerificationTestDataLoader
import com.cynergisuite.middleware.verfication.VerificationValueObject
import com.cynergisuite.middleware.verfication.infrastructure.VerificationReferenceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javafaker.Faker
import groovy.json.JsonSlurper
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject
import java.util.stream.Collectors

import static com.cynergisuite.domain.infrastructure.SpecificationHelpers.allPropertiesFullAndNotEmpty
import static com.cynergisuite.domain.infrastructure.SpecificationHelpers.allPropertiesFullAndNotEmptyExcept
import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpRequest.PUT
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class VerificationControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/verifications/corrto"

   @Inject VerificationTestDataLoaderService verificationTestDataLoaderService
   @Inject VerificationReferenceTestDataLoaderService verificationReferenceDataLoaderService
   @Inject VerificationReferenceRepository verificationReferenceRepository
   @Inject ObjectMapper objectMapper

   void "fetch one verification by id where everything is filled out" () {
      given:
      final savedVerification = verificationTestDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final verificationValueObject = new VerificationValueObject(savedVerification)

      when:
      def result = client.retrieve(GET("$path/${savedVerification.id}"), VerificationValueObject)

      then:
      notThrown(HttpClientResponseException)
      result == verificationValueObject
      result.references.size() == 6
      allPropertiesFullAndNotEmpty(result)
   }

   void "fetch one verification by id not found" () {
      when:
      client.exchange(GET("$path/0"), Argument.of(VerificationValueObject), Argument.of(ErrorDTO))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(ErrorDTO).orElse(null)?.message == "0 was unable to be found"
      exception.response.getBody(ErrorDTO).orElse(null)?.code == 'system.not.found'
   }

   void "fetch one verification by customer account" () {
      given:
      final savedVerification = verificationTestDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final verificationValueObject = new VerificationValueObject(savedVerification)

      when:
      def result = client.retrieve(GET("$path/account/${savedVerification.customerAccount}"), VerificationValueObject)

      then:
      notThrown(HttpClientResponseException)
      result == verificationValueObject
      allPropertiesFullAndNotEmpty(result)
   }

   void "fetch one verification by customer account not found" () {
      when:
      client.exchange(GET("$path/account/-1"), Argument.of(VerificationValueObject), Argument.of(ErrorDTO))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(ErrorDTO).orElse(null)?.message == "-1 was unable to be found"
      exception.response.getBody(ErrorDTO).orElse(null)?.code == 'system.not.found'
   }

   void "post verification successfully" () {
      given:
      final verification = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }

      when:
      def savedVerification = client.retrieve(POST(path, verification), VerificationValueObject)

      then:
      notThrown(HttpClientResponseException)
      savedVerification.id != null
      savedVerification.id > 0
      savedVerification.customerAccount == verification.customerAccount
      savedVerification.customerComments == verification.customerComments
      savedVerification.verifiedBy == verification.verifiedBy
      savedVerification.verifiedTime != null
   }

   void "post verification without auto, employment or landlord" () {
      given:
      final verification = VerificationTestDataLoader.stream(1, false, false, false, false).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }

      when:
      def savedVerification = client.retrieve(POST(path, verification), VerificationValueObject)

      then:
      notThrown(HttpClientResponseException)
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
      final verification = new VerificationValueObject(
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
      client.retrieve(POST(path, verification), Argument.of(VerificationValueObject), Argument.of(ErrorDTO[]))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final errors = exception.response.getBody(ErrorDTO[]).orElse(null)
      errors != null
      errors.size() == 3

      def sortedErrors = errors.sort { o1, o2 -> (o1.message <=> o2.message)}
      sortedErrors[0].message == "Is required"
      sortedErrors[1].message == "Is required"
      sortedErrors[2].message == "Is required"
   }

   void "post verification with longer than allowed customer comments should result in a failure" () {
      given:
      final stringFaker = new Faker().lorem()
      final verification = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.peek { it.customerComments = stringFaker.fixedString(260) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }

      when:
      client.exchange(POST(path, verification), Argument.of(VerificationValueObject), Argument.of(ErrorDTO[]))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final errors = exception.response.getBody(ErrorDTO[]).orElse(null)
      errors != null
      errors.size() == 1
      errors[0].message == "Size of provided value ${verification.customerComments} is invalid"
      errors[0].path == "customerComments"
   }

   void "post verification with no references" () {
      given:
      final verification = VerificationTestDataLoader.stream(1, true, true, true, false).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }

      when:
      def savedVerification = client.retrieve(POST(path, verification), VerificationValueObject)

      then:
      notThrown(HttpClientResponseException)
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
      final verification = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final verificationJson = new JsonSlurper().parseText(objectMapper.writeValueAsString(verification))

      when:
      verificationJson["cust_verified_date"] = "2019-02-30"
      client.exchange(POST(path, verificationJson), Argument.of(VerificationValueObject), Argument.of(ErrorDTO[]))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST

      final errors = exception.response.getBody(ErrorDTO[]).orElse(null)
      errors != null
      errors.size() == 1
      errors[0].message == "Failed to convert argument [cust_verified_date] for value [2019-02-30]"
   }

   void "put verification successfully" () {
      given:
      final savedVerification = verificationTestDataLoaderService.stream(1).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final toUpdateVerification = savedVerification.copyMe()

      when:
      toUpdateVerification.customerComments = "Updated comments"
      def updatedVerification = client.retrieve(PUT(path, toUpdateVerification), VerificationValueObject)
      updatedVerification.verifiedTime = toUpdateVerification.verifiedTime

      then:
      notThrown(HttpClientResponseException)
      updatedVerification == toUpdateVerification
   }

   void "put verification references by adding a third reference" () {
      given:
      final Verification verification = verificationTestDataLoaderService.stream(1, true, true, true, false).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final List<VerificationReference> references = verificationReferenceDataLoaderService.stream(2, verification).collect(Collectors.toList())
      final toUpdate = verification.copyMe()
      toUpdate.references.addAll(references)
      toUpdate.references.add(VerificationReferenceTestDataLoader.stream(1, toUpdate).findFirst().orElseThrow { new Exception("Unable to create VerificationReference") })

      when:
      def updatedVerification = client.retrieve(PUT(path, new VerificationValueObject(toUpdate)), VerificationValueObject)

      then:
      notThrown(HttpClientResponseException)
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
      final verification = verificationTestDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final savedVerification = new VerificationValueObject(verification)
      savedVerification.references.remove(5) // remove the last one

      when:
      def updatedVerification = client.retrieve(PUT(path, savedVerification), VerificationValueObject)
      def dbReferences = verificationReferenceRepository.findAll(verification)

      then:
      notThrown(HttpClientResponseException)
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
      dbReferences.collect { it.id }.sort { o1, o2 -> o1 <=> o2 } == savedVerification.references.collect { it.id }.sort { o1, o2 -> o1 <=> o2} // check that the db only contains the 5 items passed through the PUT call
   }

   void "delete two previously created verification reference via update with one missing" () {
      given:
      final verification = verificationTestDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final savedVerification = new VerificationValueObject(verification)
      savedVerification.references.remove(1)
      savedVerification.references.remove(1)

      when:
      def updatedVerification = client.retrieve(PUT(path, savedVerification), VerificationValueObject)
      def dbReferences = verificationReferenceRepository.findAll(verification) // query the db for what it actually has

      then:
      notThrown(HttpClientResponseException)
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
      dbReferences.collect { it.id }.sort { o1, o2 -> o1 <=> o2 } == savedVerification.references.collect { it.id }.sort { o1, o2 -> o1 <=> o2 } // check that the db only contains the 5 items passed through the PUT call
   }
}
