package com.cynergisuite.middleware.division

import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.company.CompanyEntity
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import static io.micronaut.http.HttpStatus.BAD_REQUEST
import static io.micronaut.http.HttpStatus.FORBIDDEN
import static io.micronaut.http.HttpStatus.METHOD_NOT_ALLOWED
import static io.micronaut.http.HttpStatus.NOT_FOUND
import static io.micronaut.http.HttpStatus.NO_CONTENT

@MicronautTest(transactional = false)
class DivisionControllerSpecification extends ControllerSpecificationBase {
   private static String path = '/division'
   private JsonOutput jsonOutput = new JsonOutput()
   private JsonSlurper jsonSlurper = new JsonSlurper()

   void "fetch one division by id" () {
      given:
      def division1 = divisionFactoryService.single(nineNineEightEmployee.company as CompanyEntity)
      def division2 = divisionFactoryService.single(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)

      when:
      def result1 = get("$path/${division1.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result1) {
         id == division1.id
         number == division1.number
         name == division1.name
         description == division1.description
      }

      when:
      def result2 = get("$path/${division2.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result2) {
         id == division2.id
         number == division2.number
         name == division2.name
         description == division2.description
         divisionalManager.id == division2.divisionalManager.id
      }
   }

   void "fetch one division by id not found" () {
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
      final divisions = []
      divisions.add(this.divisions[0])
      divisions.addAll(divisionFactoryService.stream(11, nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee).toList())

      def pageOne = new StandardPageRequest(1, 5, "id", "ASC")
      def pageTwo = new StandardPageRequest(2, 5, "id", "ASC")
      def pageLast = new StandardPageRequest(3, 5, "id", "ASC")
      def pageFour = new StandardPageRequest(4, 5, "id", "ASC")
      def firstPageDivision = divisions[0..4]
      def secondPageDivision = divisions[5..9]
      def lastPageDivision = divisions[10,11]

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
            id == firstPageDivision[index].id
            number == firstPageDivision[index].number
            name == firstPageDivision[index].name
            description == firstPageDivision[index].description
            divisionalManager.id == firstPageDivision[index].divisionalManager.id
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
            id == secondPageDivision[index].id
            number == secondPageDivision[index].number
            name == secondPageDivision[index].name
            description == secondPageDivision[index].description
            divisionalManager.id == secondPageDivision[index].divisionalManager.id
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
            id == lastPageDivision[index].id
            number == lastPageDivision[index].number
            name == lastPageDivision[index].name
            description == lastPageDivision[index].description
            divisionalManager.id == lastPageDivision[index].divisionalManager.id
         }

      }

      when:
      get("$path/${pageFour}")

      then:
      final notFoundException = thrown(HttpClientResponseException)
      notFoundException.status == NO_CONTENT
   }

   void "create a valid division"() {
      given:
      final division = divisionFactoryService.singleDTO(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      final jsonDivision = jsonOutput.toJson(division)

      when:
      def result = post("$path/", jsonDivision)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id != null
         number > 0
         name == division.name
         description == division.description
         divisionalManager.id == division.divisionalManager.id
      }
   }

   void "create an invalid division without name"() {
      given: 'get json division object and make it invalid'
      final divisionDTO = divisionFactoryService.singleDTO(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      // Make invalid json
      def jsonDivision = jsonSlurper.parseText(jsonOutput.toJson(divisionDTO))
      jsonDivision.remove('name')

      when:
      post("$path/", jsonDivision)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'name'
      response[0].message == 'Is required'
   }

   void "create an invalid division without description"() {
      given: 'get json division object and make it invalid'
      final divisionDTO = divisionFactoryService.singleDTO(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      // Make invalid json
      def jsonDivision = jsonSlurper.parseText(jsonOutput.toJson(divisionDTO))
      jsonDivision.remove('description')

      when:
      post("$path/", jsonDivision)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1
      response[0].path == 'description'
      response[0].message == 'Is required'
   }

   void "create an invalid division with non exist divisionalManager value"() {
      given: 'get json division object and make it invalid'
      final divisionDTO = divisionFactoryService.singleDTO(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      // Make invalid json
      def jsonDivision = jsonSlurper.parseText(jsonOutput.toJson(divisionDTO))
      jsonDivision.divisionalManager.id = 'Invalid'

      when:
      post("$path/", jsonDivision)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.path == 'divisionalManager'
      response.message == 'Failed to convert argument [divisionalManager] for value [Invalid]'
      response.code == "cynergi.validation.conversion.error"
   }

   void "update a valid division"() {
      given: 'Update existingDivision in DB with new data in jsonDivision'
      final existingDivision = divisionFactoryService.single(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      final updatedDivisionDTO = divisionFactoryService.singleDTO(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      def jsonDivision = jsonSlurper.parseText(jsonOutput.toJson(updatedDivisionDTO))
      jsonDivision.id = existingDivision.id

      when:
      def result = put("$path/$existingDivision.id", jsonDivision)

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingDivision.id
         number == existingDivision.number
         name == updatedDivisionDTO.name
         description == updatedDivisionDTO.description
         divisionalManager.id == updatedDivisionDTO.divisionalManager.id
      }
   }

   void "update a division with non-existing division id"() {
      given:
      final nonExistentId = UUID.randomUUID()
      final divisionDTO = divisionFactoryService.single(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      final jsonDivision = jsonSlurper.parseText(jsonOutput.toJson(divisionDTO))

      when:
      put("$path/$nonExistentId", jsonDivision)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()

      response.message == "$nonExistentId was unable to be found"
      response.code == 'system.not.found'
   }

   void "update an invalid division with un-match id in payload"() {
      given:
      final divisionDTO = divisionFactoryService.single(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      final jsonDivision = jsonSlurper.parseText(jsonOutput.toJson(divisionDTO))
      jsonDivision.id = 99

      when:
      put("$path/$divisionDTO.id", jsonDivision)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()

      response.path == 'id'
      response.message == 'Failed to convert argument [id] for value [99]'
   }

   void "update an invalid division without division description"() {
      given:
      final divisionDTO = divisionFactoryService.single(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      final jsonDivision = jsonSlurper.parseText(jsonOutput.toJson(divisionDTO))
      jsonDivision.description = ''

      when:
      put("$path/$divisionDTO.id", jsonDivision)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.size() == 1

      response[0].path == 'description'
      response[0].message == 'Is required'
   }

   void "update an invalid division with non exist divisionalManager value"() {
      given:
      final divisionDTO = divisionFactoryService.single(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      def jsonDivision = jsonSlurper.parseText(jsonOutput.toJson(divisionDTO))
      jsonDivision.divisionalManager.id = 'Z'

      when:
      put("$path/$divisionDTO.id", jsonDivision)

      then:
      def exception = thrown(HttpClientResponseException)
      exception.response.status == BAD_REQUEST
      def response = exception.response.bodyAsJson()
      response.path == 'divisionalManager'
      response.message == 'Failed to convert argument [divisionalManager] for value [Z]'
      response.code == "cynergi.validation.conversion.error"
   }

   void "delete a valid division"() {
      given:
      final existingDivision = divisionFactoryService.single(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)

      when:
      def result = delete("$path/$existingDivision.id")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == existingDivision.id
         number == existingDivision.number
         name == existingDivision.name
         description == existingDivision.description
         divisionalManager.id == existingDivision.divisionalManager.id
      }
   }

   void "delete an invalid division"() {
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

   void "delete a division with region assigned"() {
      given:
      def divisionToDelete = divisions[0]

      when:
      def result = delete("$path/${divisionToDelete.id}")

      then:
      notThrown(HttpClientResponseException)

      with(result) {
         id == divisionToDelete.id
         number == divisionToDelete.number
         name == divisionToDelete.name
         description == divisionToDelete.description
         divisionalManager.id == divisionToDelete.divisionalManager.id
      }
   }

   void "delete a division from other company with region assigned"() {
      when:
      def result = delete("$path/${divisions[1].id}")

      then:
      final exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      def response = exception.response.bodyAsJson()
      response.message == "${divisions[1].id} was unable to be found"
      response.code == 'system.not.found'
   }

   void "create a division with logged in user who is not superuser"() {
      given:
      final division = divisionFactoryService.singleDTO(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)
      final jsonDivision = jsonOutput.toJson(division)

      final companyTstds1 = companyFactoryService.forDatasetCode("tstds1")
      final companyTstds1Store = storeFactoryService.random(companyTstds1)
      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
      final loginAccessToken = loginEmployee(authenticatedEmployee)

      when:
      def result = post("$path/", jsonDivision, loginAccessToken)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "update a division with logged in user who is not superuser"() {
      given:
      final division = this.divisions[0]
      final jsonDivision = jsonOutput.toJson(division)

      final companyTstds1 = companyFactoryService.forDatasetCode("tstds1")
      final companyTstds1Store = storeFactoryService.random(companyTstds1)
      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
      final loginAccessToken = loginEmployee(authenticatedEmployee)

      when:
      def result = put("$path/", jsonDivision, loginAccessToken)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == METHOD_NOT_ALLOWED
   }

   void "delete a division with logged in user who is not superuser"() {
      given:
      final division = this.divisions[0]

      final companyTstds1 = companyFactoryService.forDatasetCode("tstds1")
      final companyTstds1Store = storeFactoryService.random(companyTstds1)
      final companyTstds1Department = departmentFactoryService.random(companyTstds1)
      final authenticatedEmployee = employeeFactoryService.singleAuthenticated(companyTstds1, companyTstds1Store, companyTstds1Department)
      final loginAccessToken = loginEmployee(authenticatedEmployee)

      when:
      def result = delete("$path/${division.id}", loginAccessToken)

      then:
      final exception = thrown(HttpClientResponseException)
      exception.status == FORBIDDEN
   }

   void "recreate deleted division" () {
      given:
      final division = divisionFactoryService.singleDTO(nineNineEightEmployee.company as CompanyEntity, nineNineEightEmployee)

      when: // create a division
      def response1 = post("$path/", division)

      then:
      notThrown(HttpClientResponseException)

      with(response1) {
         id != null
         number > 0
         name == division.name
         description == division.description
         divisionalManager.id == division.divisionalManager.id
      }

      when: // delete division
      delete("$path/$response1.id")

      then: "division of user's company is deleted"
      notThrown(HttpClientResponseException)

      when: // recreate division
      def response2 = post("$path/", division)

      then:
      notThrown(HttpClientResponseException)

      with(response2) {
         id != null
         number > 0
         name == division.name
         description == division.description
         divisionalManager.id == division.divisionalManager.id
      }

      when: // delete division again
      delete("$path/$response2.id")

      then: "division of user's company is deleted"
      notThrown(HttpClientResponseException)
   }
}
