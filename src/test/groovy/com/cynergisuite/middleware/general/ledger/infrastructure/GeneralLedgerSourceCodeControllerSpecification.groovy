package com.cynergisuite.middleware.general.ledger.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.general.ledger.GeneralLedgerSourceCodeDataLoaderService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class GeneralLedgerSourceCodeControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/general/ledger"
   private jsonOutput = new JsonOutput()
   private jsonSlurper = new JsonSlurper()

   @Inject GeneralLedgerSourceCodeDataLoaderService generalLedgerSourceCodeDataLoaderService

   void "fetch one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.stream(tstds1).collect()

      when:
      def result = get("$path/${glSourceCode[0].id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == glSourceCode[0].id
         value == glSourceCode[0].value
         description == glSourceCode[0].description
      }
   }

   void "fetch all" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCodes = generalLedgerSourceCodeDataLoaderService.stream(tstds1).toList()
      generalLedgerSourceCodeDataLoaderService.stream(tstds2).toList()
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")

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
         new GeneralLedgerSourceCodeDTO(elements[0]) == new GeneralLedgerSourceCodeDTO(glSourceCodes[0])
         new GeneralLedgerSourceCodeDTO(elements[1]) == new GeneralLedgerSourceCodeDTO(glSourceCodes[1])
      }
   }

   void "create one" () {
      given:
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.predefined().first()

      when:
      def result = post(path, glSourceCode)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == glSourceCode.value
      result.description == glSourceCode.description
   }

   void "create invalid source code with null value" () {
      given:
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.predefined().first()
      def jsonGLSourceCode = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode))
      jsonGLSourceCode.remove("value")

      when:
      post(path, jsonGLSourceCode)

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
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.predefined().first()
      def jsonGLSourceCode = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode))
      jsonGLSourceCode.remove("description")

      when:
      post(path, jsonGLSourceCode)

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
      generalLedgerSourceCodeDataLoaderService.stream(tstds1).toList()
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.predefined().first()
      def jsonGLSourceCode = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode))
      jsonGLSourceCode.value = "DEF"

      when:
      post(path, jsonGLSourceCode)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "value"
      response[0].message == "value already exists"
   }

   void "create valid source code with duplicate value from different company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      generalLedgerSourceCodeDataLoaderService.stream(tstds1).toList()
      generalLedgerSourceCodeDataLoaderService.stream(tstds2).toList()
      final glSourceCode1 = generalLedgerSourceCodeDataLoaderService.predefined().find { it.company.myDataset() == tstds1.myDataset() }
      final glSourceCode2 = generalLedgerSourceCodeDataLoaderService.predefined().find { it.company.myDataset() == tstds2.myDataset() }
      def jsonGLSourceCode = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode1))
      jsonGLSourceCode.value = glSourceCode2.value

      when:
      def result = post(path, jsonGLSourceCode)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == jsonGLSourceCode.value
      result.description == jsonGLSourceCode.description
   }

   void "update one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.stream(tstds1).toList()[0]
      def jsonUpdatedGLSC = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode))
      jsonUpdatedGLSC.value = "NEW"
      jsonUpdatedGLSC.description = "New description"

      when:
      jsonUpdatedGLSC.id = glSourceCode.id
      def result = put("$path/${glSourceCode.id}", jsonUpdatedGLSC)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == "NEW"
      result.description == "New description"
   }

   void "update source code with null value" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.stream(tstds1).toList()[0]
      def jsonUpdatedGLSC = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode))
      jsonUpdatedGLSC.remove("value")

      when:
      jsonUpdatedGLSC.id = glSourceCode.id
      put("$path/${glSourceCode.id}", jsonUpdatedGLSC)

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
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.stream(tstds1).toList()[0]
      def jsonUpdatedGLSC = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode))
      jsonUpdatedGLSC.remove("description")

      when:
      jsonUpdatedGLSC.id = glSourceCode.id
      put("$path/${glSourceCode.id}", jsonUpdatedGLSC)

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
      final glSourceCode = generalLedgerSourceCodeDataLoaderService.stream(tstds1).toList()[0]
      def jsonUpdatedGLSC = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode))
      jsonUpdatedGLSC.value = "DEF"

      when:
      jsonUpdatedGLSC.id = glSourceCode.id
      put("$path/${glSourceCode.id}", jsonUpdatedGLSC)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "value"
      response[0].message == "value already exists"
   }

   void "update source code with duplicate value from different company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final glSourceCode1 = generalLedgerSourceCodeDataLoaderService.stream(tstds1).toList()[0]
      final glSourceCode2 = generalLedgerSourceCodeDataLoaderService.stream(tstds2).toList()[0]
      def jsonUpdatedGLSC = jsonSlurper.parseText(jsonOutput.toJson(glSourceCode1))
      jsonUpdatedGLSC.value = glSourceCode2.value

      when:
      jsonUpdatedGLSC.id = glSourceCode1.id
      def result = put("$path/${glSourceCode1.id}", jsonUpdatedGLSC)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == jsonUpdatedGLSC.value
      result.description == jsonUpdatedGLSC.description
   }
}
