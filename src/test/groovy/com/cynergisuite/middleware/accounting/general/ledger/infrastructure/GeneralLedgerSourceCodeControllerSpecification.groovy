package com.cynergisuite.middleware.accounting.general.ledger.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.accounting.account.AccountTestDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.CONFLICT
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static io.micronaut.http.HttpStatus.NOT_FOUND

@MicronautTest(transactional = false)
class GeneralLedgerSourceCodeControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general-ledger/source-code"

   @Inject AccountTestDataLoaderService accountDataLoaderService
   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService
   @Inject GeneralLedgerDetailDataLoaderService generalLedgerDetailDataLoaderService

   void "fetch one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)

      when:
      def result = get("$path/${glSourceCode.id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glSourceCode.id
         value == glSourceCode.value
         description == glSourceCode.description
      }
   }

   void "fetch all" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCodes1 = generalLedgerSourceCodeDataLoaderService.stream(2, tstds1).toList()
      generalLedgerSourceCodeDataLoaderService.stream(1, tstds2).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def result = get("$path$pageOne")

      then:
      notThrown(Exception)
      with(result) {
         requested.with { new StandardPageRequest(it) } == pageOne
         totalElements == 2
         totalPages == 1
         first == true
         last == true
         new GeneralLedgerSourceCodeDTO(elements[0]) == new GeneralLedgerSourceCodeDTO(glSourceCodes1[0])
         new GeneralLedgerSourceCodeDTO(elements[1]) == new GeneralLedgerSourceCodeDTO(glSourceCodes1[1])
      }

      when:
      get("$path$pageTwo")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create one" () {
      given:
      final glSourceCode = GeneralLedgerSourceCodeDataLoader.singleDTO()

      when:
      def result = post(path, glSourceCode)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.value == glSourceCode.value
      result.description == glSourceCode.description
   }

   void "create invalid source code with null value" () {
      given:
      final glSourceCode = GeneralLedgerSourceCodeDataLoader.singleDTO()
      glSourceCode.value = null

      when:
      post(path, glSourceCode)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "value"
      response[0].message == "Is required"
   }

   void "create invalid source code with null description" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = GeneralLedgerSourceCodeDataLoader.singleDTO()
      glSourceCode.description = null

      when:
      post(path, glSourceCode)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "description"
      response[0].message == "Is required"
   }

   void "create invalid source code with duplicate value from same company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode1 = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glSourceCode2 = GeneralLedgerSourceCodeDataLoader.singleDTO()
      glSourceCode2.value = glSourceCode1.value

      when:
      post(path, glSourceCode2)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'value'
      response[0].message == 'value already exists'
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "create valid source code with duplicate value from different company" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCode1 = generalLedgerSourceCodeDataLoaderService.single(tstds2)
      final glSourceCode2 = GeneralLedgerSourceCodeDataLoader.singleDTO()
      glSourceCode2.value = glSourceCode1.value

      when:
      def result = post(path, glSourceCode2)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.value == glSourceCode2.value
      result.description == glSourceCode2.description
   }

   void "update one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final existingGLSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final updatedGLSourceCode = GeneralLedgerSourceCodeDataLoader.singleDTO()
      updatedGLSourceCode.id = existingGLSourceCode.id

      when:
      def result = put("$path/${existingGLSourceCode.id}", updatedGLSourceCode)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.value == updatedGLSourceCode.value
      result.description == updatedGLSourceCode.description
   }

   void "update source code with null value" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final existingGLSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final updatedGLSourceCode = GeneralLedgerSourceCodeDataLoader.singleDTO()
      updatedGLSourceCode.id = existingGLSourceCode.id
      updatedGLSourceCode.value = null

      when:
      updatedGLSourceCode.id = existingGLSourceCode.id
      put("$path/${existingGLSourceCode.id}", updatedGLSourceCode)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "value"
      response[0].message == "Is required"
   }

   void "update source code with null description" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final existingGLSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final updatedGLSourceCode = GeneralLedgerSourceCodeDataLoader.singleDTO()
      updatedGLSourceCode.id = existingGLSourceCode.id
      updatedGLSourceCode.description = null

      when:
      updatedGLSourceCode.id = existingGLSourceCode.id
      put("$path/${existingGLSourceCode.id}", updatedGLSourceCode)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "description"
      response[0].message == "Is required"
   }

   void "update source code with duplicate value from same company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode1 = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glSourceCode2 = GeneralLedgerSourceCodeDataLoader.singleDTO()
      glSourceCode2.value = glSourceCode1.value

      when:
      glSourceCode2.id = glSourceCode1.id
      def result = put("$path/${glSourceCode1.id}", glSourceCode2)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.value == glSourceCode2.value
      result.description == glSourceCode2.description
   }

   void "update source code with duplicate value from different company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCode1 = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glSourceCode2 = generalLedgerSourceCodeDataLoaderService.single(tstds2)
      final updatedGLSourceCode = GeneralLedgerSourceCodeDataLoader.singleDTO()
      updatedGLSourceCode.id = glSourceCode1.id
      updatedGLSourceCode.value = glSourceCode2.value

      when:
      def result = put("$path/${glSourceCode1.id}", updatedGLSourceCode)

      then:
      notThrown(Exception)
      result != null
      result.id != null
      result.value == updatedGLSourceCode.value
      result.description == updatedGLSourceCode.description
   }

   void "delete one source code" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)

      when:
      delete("$path/${glSourceCode.id}")

      then:
      notThrown(Exception)

      when:
      get("$path/${glSourceCode.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$glSourceCode.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "delete source code still has references" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds1)
      final glAccount = accountDataLoaderService.single(tstds1)
      final profitCenter = storeFactoryService.store(3, nineNineEightEmployee.company)
      generalLedgerDetailDataLoaderService.single(tstds1, glAccount, profitCenter, glSourceCode)

      when:
      delete("$path/${glSourceCode.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == CONFLICT
      def response = exception.response.bodyAsJson()
      response.message == "Requested operation violates data integrity"
      response.code == "cynergi.data.constraint.violated"
   }

   void "delete source code from other company is not allowed" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.single(tstds2)

      when:
      delete("$path/${glSourceCode.id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$glSourceCode.id was unable to be found"
      response.code == 'system.not.found'
   }

   void "recreate deleted source code" () {
      given:
      final glSourceCode = GeneralLedgerSourceCodeDataLoader.singleDTO()

      when: // create a source code
      def response1 = post(path, glSourceCode)

      then:
      notThrown(Exception)
      response1 != null
      response1.id != null
      response1.value == glSourceCode.value
      response1.description == glSourceCode.description

      when: // delete source code
      delete("$path/$response1.id")

      then: "source code of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate source code
      def response2 = post(path, glSourceCode)

      then:
      notThrown(Exception)
      response2 != null
      response2.id != null
      response2.value == glSourceCode.value
      response2.description == glSourceCode.description

      when: // delete source code again
      delete("$path/$response2.id")

      then: "source code of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
