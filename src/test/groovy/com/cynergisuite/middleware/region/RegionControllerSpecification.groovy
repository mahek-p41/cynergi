package com.cynergisuite.middleware.region

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject


import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.FORBIDDEN
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class RegionControllerSpecification extends ControllerSpecificationBase {
   @Inject EmployeeFactoryService userSetupEmployeeFactoryService

   private static String path = '/region'
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()
   private DivisionEntity tstds1Division

   @Override
   void setup() {
      tstds1Division = divisions[0]
   }

   void "fetch one region by id" () {
      given:
      def region1 = regionFactoryService.single(tstds1Division)
      def region2 = regionFactoryService.single(tstds1Division, nineNineEightEmployee)

      when:
      def result1 = get("$path/${region1.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result1) {
         id == region1.id
         number == region1.number
         name == region1.name
         description == region1.description
      }

      when:
      def result2 = get("$path/${region2.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result2) {
         id == region2.id
         number == region2.number
         name == region2.name
         description == region2.description
         regionalManager.id == region2.regionalManager.id
      }
   }

   void "fetch one region by id not found" () {
      when:
      get("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == '0 was unable to be found'
   }

   void "fetch all" () {
      given:
      final def regions = []
      regions.add(this.regions[0])
      regions.addAll(regionFactoryService.stream(11, tstds1Division, nineNineEightEmployee).toList())

      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPageRegion = regions[0..4]
      def secondPageRegion = regions[5..9]
      def lastPageRegion = regions[10,11]

      when:
      def pageOneResult = get("$path${pageOne}")

      then:
      pageOneResult.requested.with { new StandardPageRequest(it) } == pageOne
      pageOneResult.totalElements == 12
      pageOneResult.totalPages == 3
      pageOneResult.first == true
      pageOneResult.last == false
      pageOneResult.elements.size() == 5
      pageOneResult.elements.eachWithIndex { result, index ->
         with(result) {
            id == firstPageRegion[index].id
            number == firstPageRegion[index].number
            name == firstPageRegion[index].name
            description == firstPageRegion[index].description
         }
      }

      when:
      def pageTwoResult = get("$path${pageTwo}")

      then:
      pageTwoResult.requested.with { new StandardPageRequest(it) } == pageTwo
      pageTwoResult.totalElements == 12
      pageTwoResult.totalPages == 3
      pageTwoResult.first == false
      pageTwoResult.last == false
      pageTwoResult.elements.size() == 5
      pageTwoResult.elements.eachWithIndex { result, index ->
         with(result) {
            id == secondPageRegion[index].id
            number == secondPageRegion[index].number
            name == secondPageRegion[index].name
            description == secondPageRegion[index].description
            regionalManager.id == secondPageRegion[index].regionalManager.id
         }

      }
      when:
      def pageLastResult = get("$path${pageLast}")

      then:
      pageLastResult.requested.with { new StandardPageRequest(it) } == pageLast
      pageLastResult.totalElements == 12
      pageLastResult.totalPages == 3
      pageLastResult.first == false
      pageLastResult.last == true
      pageLastResult.elements.size() == 2
      pageLastResult.elements.eachWithIndex { result, index ->
         with(result) {
            id == lastPageRegion[index].id
            number == lastPageRegion[index].number
            name == lastPageRegion[index].name
            description == lastPageRegion[index].description
            regionalManager.id == lastPageRegion[index].regionalManager.id
         }

      }

      when:
      get("$path/${pageFour}")

      then:
      final def notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create a valid region"() {
      given:
      final def region = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
      final def jsonRegion = jsonOutput.toJson(region)

      when:
      def result = post("$path/", jsonRegion)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id > 0
         name == region.name
         number > 0
         number == id
         description == region.description
         regionalManager.id == region.regionalManager.id
      }
   }

   void "create an invalid region without name"() {
      given: 'get json region object and make it invalid'
      final def regionDTO = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
      // Make invalid json
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.remove('name')

      when:
      def result = post("$path/", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'regionDTO.name'
      response[0].message == 'Is required'
   }

   void "create an invalid region without description"() {
      given: 'get json region object and make it invalid'
      final def regionDTO = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
      // Make invalid json
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.remove('description')

      when:
      post("$path/", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'regionDTO.description'
      response[0].message == 'Is required'
   }

   void "create an invalid region with non exist regionalManager value"() {
      given: 'get json region object and make it invalid'
      final def regionDTO = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
      // Make invalid json
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.regionalManager.id = 'Invalid'

      when:
      post("$path/", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response.path == 'regionalManager'
      response.message == 'Failed to convert argument [regionalManager] for value [Invalid]'
   }

   void "update a valid region"() {
      given: 'Update existingRegion in DB with new data in jsonRegion'
      final def existingRegion = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      final def updatedRegionDTO = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(updatedRegionDTO))
      jsonRegion.id = existingRegion.id

      when:
      def result = put("$path/$existingRegion.id", jsonRegion)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingRegion.id
         number == existingRegion.number
         name == updatedRegionDTO.name
         description == updatedRegionDTO.description
         regionalManager.id == updatedRegionDTO.regionalManager.id
      }
   }

   void "update a invalid region without region description"() {
      given:
      final def regionDTO = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.description = ''

      when:
      put("$path/$regionDTO.id", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1

      response[0].path == 'regionDTO.description'
      response[0].message == 'Is required'
   }

   void "update a invalid region with non exist regionalManager value"() {
      given:
      final def regionDTO = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.regionalManager.id = 'Z'

      when:
      put("$path/$regionDTO.id", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 2
      response.path == 'regionalManager'
      response.message == 'Failed to convert argument [regionalManager] for value [Z]'
   }

   void "delete a valid region"() {
      given:
      final def existingRegion = regionFactoryService.single(tstds1Division, nineNineEightEmployee)

      when:
      def result = delete("$path/$existingRegion.id")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingRegion.id
         number == existingRegion.number
         name == existingRegion.name
         description == existingRegion.description
         regionalManager.id == existingRegion.regionalManager.id
      }
   }

   void "delete an invalid region"() {
      when:
      delete("$path/0")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == '0 was unable to be found'
   }

   void "delete a region with region assigned"() {
      given:
      def regionToDelete = regions[0]

      when:
      def result = delete("$path/${regionToDelete.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == regionToDelete.id
         number == regionToDelete.number
         name == regionToDelete.name
         description == regionToDelete.description
      }
   }

   void "delete a region from other company with region assigned"() {
      when:
      def result = delete("$path/${regions[1].id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response.message == "${regions[1].id} was unable to be found"
   }


   void "create a region with logged in user who is not superuser"() {
      given:
      final region = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
      final jsonRegion = jsonOutput.toJson(region)

      final companyTstds1 = companyFactoryService.forDatasetCode("tstds1")
      final companyTstds1Store = storeFactoryService.random(companyTstds1)
      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
      final loginAccessToken = loginEmployee(authenticatedEmployee)

      when:
      post("$path/", jsonRegion, loginAccessToken)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "update a region with logged in user who is not superuser"() {
      given:
      final region = this.regions[0]
      final jsonRegion = jsonOutput.toJson(region)

      final companyTstds1 = companyFactoryService.forDatasetCode("tstds1")
      final companyTstds1Store = storeFactoryService.random(companyTstds1)
      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
      final loginAccessToken = loginEmployee(authenticatedEmployee)

      when:
      put("$path/", jsonRegion, loginAccessToken)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "delete a region with logged in user who is not superuser"() {
      given:
      final region = this.regions[0]

      final companyTstds1 = companyFactoryService.forDatasetCode("tstds1")
      final companyTstds1Store = storeFactoryService.random(companyTstds1)
      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
      final loginAccessToken = loginEmployee(authenticatedEmployee)

      when:
      delete("$path/${region.id}", loginAccessToken)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "associate store with region for tstds2" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final store = storeFactoryService.store(2, tstds2)
      final tstds2SuperUser = userSetupEmployeeFactoryService.singleSuperUser(998, tstds2, 'man', 'super', 'pass')
      final tstds2SuperUserAuthenticated = userService.fetchUserByAuthentication(tstds2SuperUser.myNumber(), 'pass', tstds2.datasetCode, null).blockingGet().with { new AuthenticatedEmployee(it, 'pass') }
      final tstds2SuperUserLogin = loginEmployee(tstds2SuperUserAuthenticated)
      final division = divisionFactoryService.single(tstds2)
      final region = regionFactoryService.single(division)

      when:
      def result = post("$path/${region.id}/store", new SimpleIdentifiableDTO(store.myId()), tstds2SuperUserLogin)

      then:
      notThrown(HttpClientResponseException)
      result == null
   }

   void "associate store with region for tstds1 against tstds2" () {
      given: "division/region/store from tstds1"
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final tstds1Store3 = storeFactoryService.store(3, tstds1)
      final tstds2SuperUser = userSetupEmployeeFactoryService.singleSuperUser(998, tstds2, 'man', 'super', 'pass')
      final tstds2SuperUserAuthenticated = userService.fetchUserByAuthentication(tstds2SuperUser.myNumber(), 'pass', tstds2.datasetCode, null).blockingGet().with { new AuthenticatedEmployee(it, 'pass') }
      final tstds2SuperUserLogin = loginEmployee(tstds2SuperUserAuthenticated)
      final division = divisionFactoryService.single(tstds1)
      final region = regionFactoryService.single(division)

      when: "attempting to associate division/region/store from a company separate from the one the user is logged in under"
      post("$path/${region.id}/store", new SimpleIdentifiableDTO(tstds1Store3.myId()), tstds2SuperUserLogin)

      then: "a not found should result"
      final ex = thrown(HttpClientResponseException)
      ex.status == NOT_FOUND
      final response = ex.response.bodyAsJson()
      response.message == "${region.id} was unable to be found"
   }

   void "disassociate store from region for tstds2" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final store = storeFactoryService.store(2, tstds2)
      final tstds2SuperUser = userSetupEmployeeFactoryService.singleSuperUser(998, tstds2, 'man', 'super', 'pass')
      final tstds2SuperUserAuthenticated = userService.fetchUserByAuthentication(tstds2SuperUser.myNumber(), 'pass', tstds2.datasetCode, null).blockingGet().with { new AuthenticatedEmployee(it, 'pass') }
      final tstds2SuperUserLogin = loginEmployee(tstds2SuperUserAuthenticated)
      final division = divisionFactoryService.single(tstds2)
      final region = regionFactoryService.single(division)
      final regionStore = storeFactoryService.companyStoresToRegion(region, store)

      when:
      def result = delete("$path/${region.id}/store/${store.myId()}", tstds2SuperUserLogin)

      then:
      notThrown(HttpClientResponseException)
      result == null
   }

   void "disassociate store from region for tstds1 using tstds2 user" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('tstds1')
      final tstds2 = companyFactoryService.forDatasetCode('tstds2')
      final store = storeFactoryService.store(3, tstds1)
      final tstds2SuperUser = userSetupEmployeeFactoryService.singleSuperUser(998, tstds2, 'man', 'super', 'pass')
      final tstds2SuperUserAuthenticated = userService.fetchUserByAuthentication(tstds2SuperUser.myNumber(), 'pass', tstds2.datasetCode, null).blockingGet().with { new AuthenticatedEmployee(it, 'pass') }
      final tstds2SuperUserLogin = loginEmployee(tstds2SuperUserAuthenticated)
      final division = divisionFactoryService.single(tstds1)
      final region = regionFactoryService.single(division)
      final regionStore = storeFactoryService.companyStoresToRegion(region, store)

      when:
      delete("$path/${region.id}/store/${store.myId()}", tstds2SuperUserLogin)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == NOT_FOUND
      final response = ex.response.bodyAsJson()
      response.message == "${region.id} was unable to be found"
   }
}
