package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.FORBIDDEN
import static io.micronaut.http.HttpStatus.NO_CONTENT
import static io.micronaut.http.HttpStatus.UNAUTHORIZED

@MicronautTest(transactional = false)
class CompanyControllerSpecification extends ControllerSpecificationBase {
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()

   private static String path = '/company'

   void "fetch one company by id" () {
      given:
      final def company = companyFactoryService.forDatasetCode('tstds1')

      when:
      def result = get("$path/$company.id")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == company.id
         name == company.name
         doingBusinessAs == company.doingBusinessAs
         clientCode == company.clientCode
         clientId == company.clientId
         datasetCode == company.datasetCode
         federalTaxNumber == company.federalIdNumber
      }
   }

   void "fetch all predefined companies without login" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def pageOneResult = httpClient.toBlocking().exchange(GET("$path$pageOne"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      notThrown(Exception)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 2
      pageOneResult.totalElements == 2
      pageOneResult.elements[0].id == companies[0].id
      pageOneResult.elements[0].name == companies[0].name
      pageOneResult.elements[0].doingBusinessAs == companies[0].doingBusinessAs
      pageOneResult.elements[0].clientCode == companies[0].clientCode
      pageOneResult.elements[0].clientId == companies[0].clientId
      pageOneResult.elements[0].datasetCode == companies[0].datasetCode
      pageOneResult.elements[0].federalTaxNumber == companies[0].federalIdNumber
      pageOneResult.elements[1].id == companies[1].id
      pageOneResult.elements[1].name == companies[1].name
      pageOneResult.elements[1].doingBusinessAs == companies[1].doingBusinessAs
      pageOneResult.elements[1].clientCode == companies[1].clientCode
      pageOneResult.elements[1].clientId == companies[1].clientId
      pageOneResult.elements[1].datasetCode == companies[1].datasetCode
      pageOneResult.elements[1].federalTaxNumber == companies[1].federalIdNumber

      when:
      httpClient.toBlocking().exchange(GET("$path$pageTwo"),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NO_CONTENT
   }

   void "fetch all predefined companies with logged in user" () {
      given:
      final pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      final pageTwo = new StandardPageRequest(2, 5, "id", "ASC")

      when:
      def pageOneResult = get("$path$pageOne")

      then:
      notThrown(Exception)
      new StandardPageRequest(pageOneResult.requested) == pageOne
      pageOneResult.elements != null
      pageOneResult.elements.size() == 2
      pageOneResult.totalElements == 2
      pageOneResult.elements[0].id == companies[0].id
      pageOneResult.elements[0].name == companies[0].name
      pageOneResult.elements[0].doingBusinessAs == companies[0].doingBusinessAs
      pageOneResult.elements[0].clientCode == companies[0].clientCode
      pageOneResult.elements[0].clientId == companies[0].clientId
      pageOneResult.elements[0].datasetCode == companies[0].datasetCode
      pageOneResult.elements[0].federalTaxNumber == companies[0].federalIdNumber
      pageOneResult.elements[1].id == companies[1].id
      pageOneResult.elements[1].name == companies[1].name
      pageOneResult.elements[1].doingBusinessAs == companies[1].doingBusinessAs
      pageOneResult.elements[1].clientCode == companies[1].clientCode
      pageOneResult.elements[1].clientId == companies[1].clientId
      pageOneResult.elements[1].datasetCode == companies[1].datasetCode
      pageOneResult.elements[1].federalTaxNumber == companies[1].federalIdNumber

      when:
      get("$path$pageTwo")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NO_CONTENT
   }

   void "create a valid company"() {
      given:
      final def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      def clientId = new Random().nextInt(1000)
      def datasetCode = 'tstds3'
      jsonCompany.clientId = clientId
      jsonCompany.datasetCode = datasetCode

      when:
      def result = post("$path", jsonCompany)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == 'HTI'
         doingBusinessAs == jsonCompany.doingBusinessAs
         clientCode == jsonCompany.clientCode
         clientId == clientId
         datasetCode == 'tstds3'
         federalTaxNumber == null
      }
   }

   void "create an invalid company without clientId"() {
      given:
      final def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      jsonCompany.remove('clientId')

      when:
      def result = post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'companyVO.clientId'
      response[0].message == 'Is required'
   }

   void "create an invalid company with clientId < 0"() {
      given:
      final def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      jsonCompany.clientId = -100

      when:
      def result = post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'companyVO.clientId'
      response[0].message == 'companyVO.clientId must be greater than zero'
   }

   void "create an invalid company with duplicate clientId"() {
      given:
      final def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      jsonCompany.datasetCode = 'tstds3'

      when:
      def result = post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'clientId'
      response[0].message == '1234 already exists'
   }

   void "create an invalid company with duplicate datasetCode"() {
      given:
      final def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      jsonCompany.clientId = '9999'
      jsonCompany.datasetCode = 'tstds2'

      when:
      post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'datasetCode'
      response[0].message == 'tstds2 already exists'
   }

   void "create an invalid company with invalid datasetCode"() {
      given:
      final def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      jsonCompany.clientId = '9999'
      jsonCompany.datasetCode = 'InvalidCode'

      when:
      post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].message == 'Size of provided value InvalidCode is invalid'
      response[0].path == 'companyVO.datasetCode'
   }

   void "create an invalid company with duplicate clientId & datasetCode"() {
      given:
      final def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'

      when:
      post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response[0].path == 'clientId'
      response[0].message == '1234 already exists'
      response[1].path == 'datasetCode'
      response[1].message == 'tstds1 already exists'
   }


   void "create a company without login"() {
      given:
      final def company = companyFactoryService.forDatasetCode('tstds1')
      final def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(company))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      def clientId = new Random().nextInt(1000)
      def datasetCode = 'tstds3'
      jsonCompany.clientId = clientId
      jsonCompany.datasetCode = datasetCode

      when:
      client.exchange(
         POST("/${path}", jsonCompany),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.message == 'Required argument authentication not specified'
   }

   void "update a valid company"() {
      given: 'Update existingCompany in DB with all new data in jsonCompany'
      def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientCode = '1234'
         clientId = 1234
         datasetCode = 'tstds3'
         federalTaxNumber = '654321'
      }

      when:
      def result = put("$path/$tstds1.id", jsonCompany)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == jsonCompany.id
         name == 'HTI'
         doingBusinessAs == 'Sale'
         clientCode == '1234'
         clientId == 1234
         datasetCode == 'tstds3'
         federalTaxNumber == '654321'
      }
   }

   void "update an invalid company with duplicate clientId"() {
      given: 'Update existingCompany in DB with all new data in jsonCompany'
      def tstds2 = companyFactoryService.forDatasetCode('tstds2')
      def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientId = tstds2.clientId
         clientCode = '1234'
         datasetCode = 'tstds3'
         federalTaxNumber = '654321'
      }

      when:
      put("$path/$tstds1.id", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'clientId'
      response[0].message == '4321 already exists'
   }

   void "update an invalid company with duplicate datasetCode"() {
      given: 'Update existingCompany in DB with all new data in jsonCompany'
      def tstds2 = companyFactoryService.forDatasetCode('tstds2')
      def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientCode = '1234'
         datasetCode = tstds2.datasetCode
         federalTaxNumber = '654321'
      }

      when:
      put("$path/$tstds1.id", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'datasetCode'
      response[0].message == 'tstds2 already exists'
   }

   void "update a company without login"() {
      given: 'Update existingCompany in DB with all new data in jsonCompany'
      def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientCode = '1234'
         clientId = 1234
         datasetCode = 'tstds3'
         federalTaxNumber = '654321'
      }

      when:
      client.exchange(
         POST("/${path}/$tstds1.id", jsonCompany),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      def exception = thrown(HttpClientResponseException)
      exception.status == UNAUTHORIZED
      def response = exception.response.bodyAsJson()
      response.message == 'You are not logged in'
   }

}
