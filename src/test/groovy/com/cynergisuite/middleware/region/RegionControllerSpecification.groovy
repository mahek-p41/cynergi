package com.cynergisuite.middleware.region

import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.EmployeeTestDataLoaderService
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.METHOD_NOT_ALLOWED
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class RegionControllerSpecification extends ControllerSpecificationBase {
   @Inject EmployeeTestDataLoaderService userSetupEmployeeTestDataLoaderService

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
         with(division) {
            id == this.tstds1Division.id
            number == this.tstds1Division.number
            name == this.tstds1Division.name
            description == this.tstds1Division.description
         }
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
         with(division) {
            id == this.tstds1Division.id
            number == this.tstds1Division.number
            name == this.tstds1Division.name
            description == this.tstds1Division.description
         }
      }
   }

   void "fetch one region by id not found" () {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      get("$path/$nonExistentId")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "fetch all" () {
      given:
      final regions = []
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
            with(division) {
               id == this.tstds1Division.id
               number == this.tstds1Division.number
               name == this.tstds1Division.name
               description == this.tstds1Division.description
            }
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
            with(division) {
               id == this.tstds1Division.id
               number == this.tstds1Division.number
               name == this.tstds1Division.name
               description == this.tstds1Division.description
            }
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
            with(division) {
               id == this.tstds1Division.id
               number == this.tstds1Division.number
               name == this.tstds1Division.name
               description == this.tstds1Division.description
            }
         }

      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create a valid region"() {
      given:
      final region = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)

      when:
      def result = post("$path/", region)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         name == region.name
         number > 0
         description == region.description
         regionalManager.id == region.regionalManager.id
         with(division) {
            id == this.tstds1Division.id
            number == this.tstds1Division.number
            name == this.tstds1Division.name
            description == this.tstds1Division.description
         }
      }
   }

   void "create an invalid region without name"() {
      given: 'get json region object and make it invalid'
      final regionDTO = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
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
      response[0].path == 'name'
      response[0].message == 'Is required'
   }

   void "create an invalid region without description"() {
      given: 'get json region object and make it invalid'
      final regionDTO = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
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
      response[0].path == 'description'
      response[0].message == 'Is required'
   }

   void "create an invalid region with non exist regionalManager value"() {
      given: 'get json region object and make it invalid'
      final regionDTO = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
      // Make invalid json
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.regionalManager.id = 'Invalid'

      when:
      post("$path/", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.path == 'regionalManager'
      response.message == 'Failed to convert argument [regionalManager] for value [Invalid]'
      response.code == "cynergi.validation.conversion.error"
   }

   void "update a valid region"() {
      given: 'Update existingRegion in DB with new data in jsonRegion'
      final existingRegion = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      final updatedRegionDTO = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
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
         with(division) {
            id == this.tstds1Division.id
            number == this.tstds1Division.number
            name == this.tstds1Division.name
            description == this.tstds1Division.description
         }
      }
   }

   void "update a invalid region with non-existing region id"() {
      given:
      final nonExistentId = UUID.randomUUID()
      final regionDTO = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      final jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))

      when:
      put("$path/$nonExistentId", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()

      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "update a invalid region without region id"() {
      given:
      final regionDTO = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      final jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.remove('id')

      when:
      put("$path/$regionDTO.id", jsonRegion)

      then:
      notThrown(HttpClientResponseException)
   }

   void "update a invalid region with un-match id in payload"() {
      given:
      final regionDTO = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      final jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.id = 99

      when:
      put("$path/$regionDTO.id", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.path == 'id'
      response.message == 'Failed to convert argument [id] for value [99]'
   }

   void "update a invalid region without region description"() {
      given:
      final regionDTO = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.description = ''

      when:
      put("$path/$regionDTO.id", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1

      response[0].path == 'description'
      response[0].message == 'Is required'
   }

   void "update a invalid region with non exist regionalManager value"() {
      given:
      final regionDTO = regionFactoryService.single(tstds1Division, nineNineEightEmployee)
      def jsonRegion = jsonSlurper.parseText(jsonOutput.toJson(regionDTO))
      jsonRegion.regionalManager.id = 'Z'

      when:
      put("$path/$regionDTO.id", jsonRegion)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.path == 'regionalManager'
      response.message == 'Failed to convert argument [regionalManager] for value [Z]'
      response.code == "cynergi.validation.conversion.error"
   }

   void "delete a valid region"() {
      given:
      final existingRegion = regionFactoryService.single(tstds1Division, nineNineEightEmployee)

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
         with(division) {
            id == this.tstds1Division.id
            number == this.tstds1Division.number
            name == this.tstds1Division.name
            description == this.tstds1Division.description
         }
      }
   }

   void "delete an invalid region"() {
      given:
      final nonExistentId = UUID.randomUUID()

      when:
      delete("$path/$nonExistentId")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
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
         with(division) {
            id == this.tstds1Division.id
            number == this.tstds1Division.number
            name == this.tstds1Division.name
            description == this.tstds1Division.description
         }
      }
   }

   void "delete a region from other company with region assigned"() {
      when:
      def result = delete("$path/${regions[1].id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${regions[1].id} was unable to be found"
      response.code == 'system.not.found'
   }

//   void "create a region with logged in user who is not superuser"() {
//      given:
//      final region = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)
//      final jsonRegion = jsonOutput.toJson(region)
//
//      final companyTstds1 = companyFactoryService.forDatasetCode("coravt")
//      final companyTstds1Store = storeFactoryService.random(companyTstds1)
//      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
//      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
//      final loginAccessToken = loginEmployee(authenticatedEmployee)
//
//      when:
//      post("$path/", jsonRegion, loginAccessToken)
//
//      then:
//      final exception = thrown(HttpClientResponseException)
//      exception.status == FORBIDDEN
//   }

   void "update a region with logged in user who is not superuser"() {
      given:
      final region = this.regions[0]
      final jsonRegion = jsonOutput.toJson(region)

      final companyTstds1 = companyFactoryService.forDatasetCode("coravt")
      final companyTstds1Store = storeFactoryService.random(companyTstds1)
      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
      final loginAccessToken = loginEmployee(authenticatedEmployee)

      when:
      put("$path/", jsonRegion, loginAccessToken)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == METHOD_NOT_ALLOWED
   }

//   void "delete a region with logged in user who is not superuser"() {
//      given:
//      final region = this.regions[0]
//
//      final companyTstds1 = companyFactoryService.forDatasetCode("coravt")
//      final companyTstds1Store = storeFactoryService.random(companyTstds1)
//      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
//      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
//      final loginAccessToken = loginEmployee(authenticatedEmployee)
//
//      when:
//      delete("$path/${region.id}", loginAccessToken)
//
//      then:
//      final exception = thrown(HttpClientResponseException)
//      exception.status == FORBIDDEN
//   }

   void "recreate deleted region" () {
      given:
      final region = regionFactoryService.singleDTO(tstds1Division, nineNineEightEmployee)

      when: // create a region
      def response1 = post("$path/", region)

      then:
      notThrown(HttpClientResponseException)

      with(response1) {
         id != null
         name == region.name
         number > 0
         description == region.description
         regionalManager.id == region.regionalManager.id
         with(division) {
            id == this.tstds1Division.id
            number == this.tstds1Division.number
            name == this.tstds1Division.name
            description == this.tstds1Division.description
         }
      }

      when: // delete region
      delete("$path/$response1.id")

      then: "region of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate region
      def response2 = post("$path/", region)

      then:
      notThrown(HttpClientResponseException)

      with(response2) {
         id != null
         name == region.name
         number > 0
         description == region.description
         regionalManager.id == region.regionalManager.id
         with(division) {
            id == this.tstds1Division.id
            number == this.tstds1Division.number
            name == this.tstds1Division.name
            description == this.tstds1Division.description
         }
      }

      when: // delete region again
      delete("$path/$response2.id")

      then: "region of user's company is deleted"
      notThrown(HttpClientResponseException)
   }

   void "associate store with region for tstds2" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      final store = storeFactoryService.store(6, tstds2)
      final tstds2SuperUser = userSetupEmployeeTestDataLoaderService.singleSuperUser(998, tstds2, 'man', 'super', 'pass')
      final tstds2SuperUserAuthenticated = userService.fetchUserByAuthentication(tstds2SuperUser.myNumber(), 'pass', tstds2.datasetCode, null).with { new AuthenticatedEmployee(it, 'pass') }
      final tstds2SuperUserLogin = loginEmployee(tstds2SuperUserAuthenticated)
      final division = divisionFactoryService.single(tstds2)
      final region = regionFactoryService.single(division)

      when:
      def result = post("$path/${region.id}/store", new SimpleLegacyIdentifiableDTO(store.myId()), tstds2SuperUserLogin)

      then:
      notThrown(HttpClientResponseException)
      result == null
   }

   void "associate store with region for tstds1 against tstds2" () {
      given: "division/region/store from tstds1"
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      final tstds1Store3 = storeFactoryService.store(3, tstds1)
      final tstds2SuperUser = userSetupEmployeeTestDataLoaderService.singleSuperUser(998, tstds2, 'man', 'super', 'pass')
      final tstds2SuperUserAuthenticated = userService.fetchUserByAuthentication(tstds2SuperUser.myNumber(), 'pass', tstds2.datasetCode, null).with { new AuthenticatedEmployee(it, 'pass') }
      final tstds2SuperUserLogin = loginEmployee(tstds2SuperUserAuthenticated)
      final division = divisionFactoryService.single(tstds1)
      final region = regionFactoryService.single(division)

      when: "attempting to associate division/region/store from a company separate from the one the user is logged in under"
      post("$path/${region.id}/store", new SimpleLegacyIdentifiableDTO(tstds1Store3.myId()), tstds2SuperUserLogin)

      then: "a not found should result"
      final ex = thrown(HttpClientResponseException)
      ex.status == NOT_FOUND
      final response = ex.response.bodyAsJson()
      response.message == "${region.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "disassociate store from region for tstds2" () {
      given:
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      final store = storeFactoryService.store(6, tstds2)
      final tstds2SuperUser = userSetupEmployeeTestDataLoaderService.singleSuperUser(998, tstds2, 'man', 'super', 'pass')
      final tstds2SuperUserAuthenticated = userService.fetchUserByAuthentication(tstds2SuperUser.myNumber(), 'pass', tstds2.datasetCode, null).with { new AuthenticatedEmployee(it, 'pass') }
      final tstds2SuperUserLogin = loginEmployee(tstds2SuperUserAuthenticated)
      final division = divisionFactoryService.single(tstds2)
      final region = regionFactoryService.single(division)
      storeFactoryService.companyStoresToRegion(region, store)

      when:
      def result = delete("$path/${region.id}/store/${store.myId()}", tstds2SuperUserLogin)

      then:
      notThrown(HttpClientResponseException)
      result == null
   }

   void "disassociate store from region for tstds1 using tstds2 user" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final tstds2 = companyFactoryService.forDatasetCode('corrto')
      final store = storeFactoryService.store(3, tstds1)
      final tstds2SuperUser = userSetupEmployeeTestDataLoaderService.singleSuperUser(998, tstds2, 'man', 'super', 'pass')
      final tstds2SuperUserAuthenticated = userService.fetchUserByAuthentication(tstds2SuperUser.myNumber(), 'pass', tstds2.datasetCode, null).with { new AuthenticatedEmployee(it, 'pass') }
      final tstds2SuperUserLogin = loginEmployee(tstds2SuperUserAuthenticated)
      final division = divisionFactoryService.single(tstds1)
      final region = regionFactoryService.single(division)
      storeFactoryService.companyStoresToRegion(region, store)

      when:
      delete("$path/${region.id}/store/${store.myId()}", tstds2SuperUserLogin)

      then:
      final ex = thrown(HttpClientResponseException)
      ex.status == NOT_FOUND
      final response = ex.response.bodyAsJson()
      response.message == "${region.id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "re-associate store with another region of the same company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final division = divisionFactoryService.single(tstds1)
      final newRegion = regionFactoryService.single(division)
      final store1 = storeFactoryService.store(1, tstds1)

      when:
      def result = get("/store")

      then: 'state of stores before re-associate'
      notThrown(HttpClientResponseException)

      with(result) {
         elements != null
         totalElements == 7
         elements[1].id == 2
         elements[1].storeNumber == store1.myNumber()
         elements[1].name == store1.myName()
         elements[1].region.id == this.regions[0].id
         elements[1].region.name == this.regions[0].name
      }

      when: 're-associate store with a new region'
      post("$path/${newRegion.id}/store", new SimpleLegacyIdentifiableDTO(store1.myId()))

      then:
      notThrown(HttpClientResponseException)

      when:
      def result2 = get("/store")

      then: 'state after store re-associate with a new region'
      notThrown(HttpClientResponseException)
      with(result2) {
         elements != null
         totalElements == 7
         elements[1].id == 2
         elements[1].storeNumber == store1.myNumber()
         elements[1].name == store1.myName()
         elements[1].region.id == newRegion.id
         elements[1].region.name == newRegion.name
      }
   }

   void "re-associate store with another region of the other company" () {
      given:
      final tstds1 = companyFactoryService.forDatasetCode('coravt')
      final store1 = storeFactoryService.store(1, tstds1)

      when: 're-associate store with a region of other company'
      post("$path/${regions[1].id}/store", new SimpleLegacyIdentifiableDTO(store1.myId()))

      then: "a not found should result"
      final ex = thrown(HttpClientResponseException)
      ex.status == NOT_FOUND
      final response = ex.response.bodyAsJson()
      response.message == "${regions[1].id} was unable to be found"
      response.code == 'system.not.found'
   }
}