package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.address.AddressRepository
import com.cynergisuite.middleware.address.AddressTestDataLoader
import com.cynergisuite.middleware.address.AddressTestDataLoaderService
import com.cynergisuite.middleware.company.CompanyDTO
import com.cynergisuite.middleware.company.CompanyFactory
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.core.type.Argument
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpRequest.POST
import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.METHOD_NOT_ALLOWED
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class CompanyControllerSpecification extends ControllerSpecificationBase {
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()
   @Inject AddressTestDataLoaderService addressTestDataLoaderService
   @Inject AddressRepository addressRepository

   private static String path = '/company'

   void "fetch one company by id" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')

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
         address == company.address
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
      pageOneResult.elements[0].address == companies[0].address
      pageOneResult.elements[1].id == companies[1].id
      pageOneResult.elements[1].name == companies[1].name
      pageOneResult.elements[1].doingBusinessAs == companies[1].doingBusinessAs
      pageOneResult.elements[1].clientCode == companies[1].clientCode
      pageOneResult.elements[1].clientId == companies[1].clientId
      pageOneResult.elements[1].datasetCode == companies[1].datasetCode
      pageOneResult.elements[1].federalTaxNumber == companies[1].federalIdNumber
      pageOneResult.elements[1].address == companies[1].address

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
      pageOneResult.elements[0].address == companies[0].address
      pageOneResult.elements[1].id == companies[1].id
      pageOneResult.elements[1].name == companies[1].name
      pageOneResult.elements[1].doingBusinessAs == companies[1].doingBusinessAs
      pageOneResult.elements[1].clientCode == companies[1].clientCode
      pageOneResult.elements[1].clientId == companies[1].clientId
      pageOneResult.elements[1].datasetCode == companies[1].datasetCode
      pageOneResult.elements[1].federalTaxNumber == companies[1].federalIdNumber
      pageOneResult.elements[1].address == companies[1].address

      when:
      get("$path$pageTwo")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == NO_CONTENT
   }

   void "create a valid company with address" () {
      given:
      final address = AddressTestDataLoader.single()
      final company = CompanyFactory.stream(1, address).findFirst().orElseThrow { new Exception("Unable to create company") }

      when:
      def result = post("$path", new CompanyDTO(company))

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         name == company.name
         doingBusinessAs == company.doingBusinessAs
         clientCode == company.clientCode
         clientId == company.clientId
         datasetCode == company.datasetCode
         federalTaxNumber == company.federalIdNumber
         it.address.id != null
         it.address.name == address.name
         it.address.address1 == address.address1
      }
   }

   void "create a valid company without address" () {
      given:
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      def clientId = new Random().nextInt(1000)
      def datasetCode = 'tstds3'
      jsonCompany.clientId = clientId
      jsonCompany.datasetCode = datasetCode
      jsonCompany.address = null

      when:
      def result = post("$path", jsonCompany)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         name == 'HTI'
         doingBusinessAs == jsonCompany.doingBusinessAs
         clientCode == jsonCompany.clientCode
         clientId == clientId
         datasetCode == 'tstds3'
         federalTaxNumber == null
         address == null
      }
   }

   void "create an invalid company without clientId" () {
      given:
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      jsonCompany.remove('clientId')

      when:
      post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'companyDTO.clientId'
      response[0].message == 'Is required'
   }

   void "create an invalid company with clientId < 0" () {
      given:
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      jsonCompany.clientId = -100

      when:
      post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'companyDTO.clientId'
      response[0].message == 'companyDTO.clientId must be greater than zero'
   }

   void "create an invalid company with duplicate clientId" () {
      given:
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      jsonCompany.datasetCode = 'tstds3'

      when:
      post("$path", jsonCompany)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'clientId'
      response[0].message == '1234 already exists'
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "create an invalid company with duplicate datasetCode" () {
      given:
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
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
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "create an invalid company with invalid datasetCode" () {
      given:
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
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
      response[0].path == 'companyDTO.datasetCode'
   }

   void "create an invalid company with duplicate clientId & datasetCode" () {
      given:
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
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
      response[0].code == 'cynergi.validation.duplicate'
      response[1].path == 'datasetCode'
      response[1].message == 'tstds1 already exists'
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "create a company without login" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(company))
      jsonCompany.remove('id')
      jsonCompany.name = 'HTI'
      final clientId = new Random().nextInt(1000)
      final datasetCode = 'tstds3'
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
      response.message == 'Required argument [Authentication authentication] not specified'
   }

   void "update a valid company" () {
      given: 'Update existingCompany in DB with all new data in jsonCompany'
      def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientCode = '1234'
         clientId = 1234
         datasetCode = 'tstds3'
         federalTaxNumber = '654321'
         address = null
      }

      when:
      def result = put("$path/$tstds1.id", jsonCompany)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == UUID.fromString(jsonCompany.id)
         name == 'HTI'
         doingBusinessAs == 'Sale'
         clientCode == '1234'
         clientId == 1234
         datasetCode == 'tstds3'
         federalTaxNumber == '654321'
         address == null
      }
   }

   void "update an invalid company with duplicate clientId" () {
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
         address = null
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
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "update an invalid company with duplicate datasetCode" () {
      given: 'Update existingCompany in DB with all new data in jsonCompany'
      def tstds2 = companyFactoryService.forDatasetCode('tstds2')
      def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientCode = '1234'
         datasetCode = tstds2.datasetCode
         federalTaxNumber = '654321'
         address = null
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
      response[0].code == 'cynergi.validation.duplicate'
   }

   void "update a company without login" () {
      given: 'Update existingCompany in DB with all new data in jsonCompany'
      def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientCode = '1234'
         clientId = 1234
         datasetCode = 'tstds3'
         federalTaxNumber = '654321'
         address = null
      }

      when:
      client.exchange(
         POST("/${path}/${tstds1.id}", jsonCompany),
         Argument.of(String),
         Argument.of(String)
      ).bodyAsJson()

      then:
      def exception = thrown(HttpClientResponseException)
      exception.status == METHOD_NOT_ALLOWED
      def response = exception.response.bodyAsJson()
      response.message == "Method [POST] not allowed for URI [/api/company/${tstds1.id}]. Allowed methods: [HEAD, GET, PUT]"
   }

   void "update a valid company by removing address" () {
      given: 'Update existingCompany in DB by removing address'
      def address = addressTestDataLoaderService.single()
      def jsonAddress = jsonSlurper.parseText(jsonOutput.toJson(address))
      def jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonAddress.remove(address)
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientCode = '1234'
         clientId = 1234
         datasetCode = 'tstds3'
         federalTaxNumber = '654321'
         address = jsonAddress
      }

      when:
      def result = put("$path/$tstds1.id", jsonCompany)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == UUID.fromString(jsonCompany.id)
         name == 'HTI'
         doingBusinessAs == 'Sale'
         clientCode == '1234'
         clientId == 1234
         datasetCode == 'tstds3'
         federalTaxNumber == '654321'
         with(address) {
            id == jsonAddress.id
            name == jsonAddress.name
            address1 == jsonAddress.address1
            address2 == jsonAddress.address2
            city == jsonAddress.city
            state == jsonAddress.state
            postalCode == jsonAddress.postalCode
            latitude == jsonAddress.latitude
            longitude == jsonAddress.longitude
            country == jsonAddress.country
            county == jsonAddress.county
            phone == jsonAddress.phone
            fax == jsonAddress.fax
         }
      }
   }

   void "update a valid company by updating address" () {
      given: 'Update existingCompany in DB by updating address'
      def address = addressTestDataLoaderService.single()
      final jsonAddress = jsonSlurper.parseText(jsonOutput.toJson(address))
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonAddress.tap {
         name = 'Test Name'
         address1 = '123 Test St'
         address2 = null
         city = 'Test City'
         state = 'KS'
         postalCode = '12345'
         latitude = '-100.10244'
         longitude = '-34.54896'
         country = 'US'
         county = 'I7'
         phone = '555-555-5555'
         fax = '(555) 555-6789'
      }
      jsonCompany.tap {
         name = 'HTI'
         doingBusinessAs = 'Sale'
         clientCode = '1234'
         clientId = 1234
         datasetCode = 'tstds3'
         federalTaxNumber = '654321'
         address = jsonAddress
      }

      when:
      def result = put("$path/$tstds1.id", jsonCompany)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == UUID.fromString(jsonCompany.id)
         name == 'HTI'
         doingBusinessAs == 'Sale'
         clientCode == '1234'
         clientId == 1234
         datasetCode == 'tstds3'
         federalTaxNumber == '654321'
         with(address) {
            id == jsonAddress.id
            name == jsonAddress.name
            address1 == jsonAddress.address1
            address2 == jsonAddress.address2
            city == jsonAddress.city
            state == jsonAddress.state
            postalCode == jsonAddress.postalCode
            latitude == jsonAddress.latitude
            longitude == jsonAddress.longitude
            country == jsonAddress.country
            county == jsonAddress.county
            phone == jsonAddress.phone
            fax == jsonAddress.fax
         }
      }
   }

   void "update a valid company with an address" () {
      given:
      final address = addressTestDataLoaderService.single()
      final jsonAddress = jsonSlurper.parseText(jsonOutput.toJson(address))
      final jsonCompany = jsonSlurper.parseText(jsonOutput.toJson(tstds1))
      jsonCompany.remove(address)
      jsonCompany.address = jsonAddress

      when:
      def result = put("$path/$tstds1.id", jsonCompany)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == UUID.fromString(jsonCompany.id)
         name == jsonCompany.name
         doingBusinessAs == jsonCompany.doingBusinessAs
         clientCode == jsonCompany.clientCode
         clientId == jsonCompany.clientId
         datasetCode == jsonCompany.datasetCode
         federalTaxNumber == jsonCompany.federalTaxNumber
         with(address) {
            id == UUID.fromString(jsonAddress.id)
            name == jsonAddress.name
            address1 == jsonAddress.address1
            address2 == jsonAddress.address2
            city == jsonAddress.city
            state == jsonAddress.state
            postalCode == jsonAddress.postalCode
            latitude == jsonAddress.latitude
            longitude == jsonAddress.longitude
            country == jsonAddress.country
            county == jsonAddress.county
            phone == jsonAddress.phone
            fax == jsonAddress.fax
         }
      }
   }

   void "update an invalid company with a null address id" () {
      given:
      final address = addressTestDataLoaderService.single()
      final company = new CompanyDTO(companyFactoryService.single(address))
      company.address.id = null

      when:
      put("$path/${company.id}", company)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "address.id"
      response[0].message == "Provide id or update address"
   }

   void "update a company without an address and not adding an address" () {
      given:
      final company = new CompanyDTO(companyFactoryService.single())
      company.name = "Test update name"

      when:
      def result = put("$path/${company.id}", company)

      then:
      notThrown(Exception)
      with(result) {
         id == company.id
         name == "Test update name"
      }
   }

   void "update a company and remove it's address" () {
      given:
      final address = AddressTestDataLoader.single() // create address without id because the companyFactoryService.single bellow will insert it
      final company = new CompanyDTO(companyFactoryService.single(address))
      final addressId = company.address.id
      company.address = null

      when:
      def result = put("$path/${company.id}", company)

      then:
      notThrown(Exception)
      with(result) {
         it.id == company.id
         it.address == null
      }
      addressRepository.findById(addressId).orElse(null) == null
   }

   void "update a company's address name" () {
      given:
      final address = AddressTestDataLoader.single() // create address without id because the companyFactoryService.single bellow will insert it
      final company = new CompanyDTO(companyFactoryService.single(address))
      final addressId = company.address.id
      company.address.name = "Test update name"

      when:
      def result = put("$path/${company.id}", company)

      then:
      notThrown(Exception)
      with(result) {
         it.id == company.id
         it.address.id == addressId
         it.address.name == "Test update name"
      }
      addressRepository.findById(addressId).get().name == "Test update name"
   }
}
