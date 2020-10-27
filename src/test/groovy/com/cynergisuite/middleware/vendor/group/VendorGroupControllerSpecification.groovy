package com.cynergisuite.middleware.vendor.group

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST

@MicronautTest(transactional = false)
class VendorGroupControllerSpecification extends ControllerSpecificationBase {
   private static final String path = "/vendor/group"
   private jsonOutput = new JsonOutput()
   private jsonSlurper = new JsonSlurper()

   @Inject VendorGroupTestDataLoaderService vendorGroupTestDataLoaderService

   void "fetch one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final vendorGroup = vendorGroupTestDataLoaderService.stream(tstds1).collect()

      when:
      def result = get("$path/${vendorGroup[0].id}")

      then:
      notThrown(Exception)
      result != null
      with(result) {
         id == vendorGroup[0].id
         value == vendorGroup[0].value
         description == vendorGroup[0].description
      }
   }

   void "fetch all" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final vendorGroups = vendorGroupTestDataLoaderService.stream(tstds1).toList()
      vendorGroupTestDataLoaderService.stream(tstds2).toList()
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
         new VendorGroupDTO(elements[0]) == new VendorGroupDTO(vendorGroups[0])
         new VendorGroupDTO(elements[1]) == new VendorGroupDTO(vendorGroups[1])
      }
   }

   void "create one" () {
      given:
      final vendorGroup = vendorGroupTestDataLoaderService.predefined().first()

      when:
      def result = post(path, vendorGroup)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == vendorGroup.value
      result.description == vendorGroup.description
   }

   void "create invalid vendor with null value" () {
      given:
      final vendorGroup = vendorGroupTestDataLoaderService.predefined().first()
      def jsonVendorGroup = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup))
      jsonVendorGroup.remove("value")

      when:
      post(path, jsonVendorGroup)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "value"
      response[0].message == "Is required"
   }

   void "create invalid vendor with null description" () {
      given:
      final vendorGroup = vendorGroupTestDataLoaderService.predefined().first()
      def jsonVendorGroup = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup))
      jsonVendorGroup.remove("description")

      when:
      post(path, jsonVendorGroup)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "description"
      response[0].message == "Is required"
   }

   void "create invalid vendor group with duplicate value from same company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      vendorGroupTestDataLoaderService.stream(tstds1).toList()
      final vendorGroup = vendorGroupTestDataLoaderService.predefined().first()
      def jsonVendorGroup = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup))
      jsonVendorGroup.value = "test2"

      when:
      post(path, jsonVendorGroup)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "value"
      response[0].message == "value already exists"
   }

   void "create valid vendor group with duplicate value from different company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      vendorGroupTestDataLoaderService.stream(tstds1).toList()
      vendorGroupTestDataLoaderService.stream(tstds2).toList()
      final vendorGroup1 = vendorGroupTestDataLoaderService.predefined().find { it.company.myDataset() == tstds1.myDataset() }
      final vendorGroup2 = vendorGroupTestDataLoaderService.predefined().find { it.company.myDataset() == tstds2.myDataset() }
      def jsonVendorGroup = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup1))
      jsonVendorGroup.value = vendorGroup2.value

      when:
      def result = post(path, jsonVendorGroup)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == jsonVendorGroup.value
      result.description == jsonVendorGroup.description
   }

   void "update one" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final vendorGroup = vendorGroupTestDataLoaderService.stream(tstds1).toList()[0]
      def jsonUpdatedVG = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup))
      jsonUpdatedVG.value = "value1"
      jsonUpdatedVG.description = "description1"

      when:
      jsonUpdatedVG.id = vendorGroup.id
      def result = put("$path/${vendorGroup.id}", jsonUpdatedVG)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == "value1"
      result.description == "description1"
   }

   // Front-end should avoid this case if possible
   void "update vendor with no change" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final vendorGroup = new VendorGroupDTO(vendorGroupTestDataLoaderService.stream(tstds1).toList()[0])

      when:
      def result = put("$path/${vendorGroup.id}", vendorGroup)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == vendorGroup.value
      result.description == vendorGroup.description
   }

   void "update vendor with null value" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final vendorGroup = vendorGroupTestDataLoaderService.stream(tstds1).toList()[0]
      def jsonUpdatedVG = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup))
      jsonUpdatedVG.remove("value")

      when:
      jsonUpdatedVG.id = vendorGroup.id
      put("$path/${vendorGroup.id}", jsonUpdatedVG)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "value"
      response[0].message == "Is required"
   }

   void "update vendor with null description" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final vendorGroup = vendorGroupTestDataLoaderService.stream(tstds1).toList()[0]
      def jsonUpdatedVG = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup))
      jsonUpdatedVG.remove("description")

      when:
      jsonUpdatedVG.id = vendorGroup.id
      put("$path/${vendorGroup.id}", jsonUpdatedVG)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status() == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "description"
      response[0].message == "Is required"
   }

   void "update vendor with duplicate value from same company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final vendorGroup = vendorGroupTestDataLoaderService.stream(tstds1).toList()[0]
      def jsonUpdatedVG = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup))
      jsonUpdatedVG.value = "test2"

      when:
      jsonUpdatedVG.id = vendorGroup.id
      put("$path/${vendorGroup.id}", jsonUpdatedVG)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      final response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == "value"
      response[0].message == "value already exists"
   }

   void "update vendor with duplicate value from different company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final vendorGroup1 = vendorGroupTestDataLoaderService.stream(tstds1).toList()[0]
      final vendorGroup2 = vendorGroupTestDataLoaderService.stream(tstds2).toList()[0]
      def jsonUpdatedVG = jsonSlurper.parseText(jsonOutput.toJson(vendorGroup1))
      jsonUpdatedVG.value = vendorGroup2.value

      when:
      jsonUpdatedVG.id = vendorGroup1.id
      def result = put("$path/${vendorGroup1.id}", jsonUpdatedVG)

      then:
      notThrown(Exception)
      result != null
      result.id > 0
      result.value == jsonUpdatedVG.value
      result.description == jsonUpdatedVG.description
   }
}
