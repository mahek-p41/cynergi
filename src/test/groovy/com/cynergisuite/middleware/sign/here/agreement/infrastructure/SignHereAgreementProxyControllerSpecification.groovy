package com.cynergisuite.middleware.sign.here.agreement.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.domain.infrastructure.ControllerSpecificationBase
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.sign.here.DocumentPageRequest
import com.cynergisuite.middleware.sign.here.agreement.DocumentSignatureStatus
import com.cynergisuite.middleware.sign.here.associated.AssociatedDetailDto
import com.cynergisuite.middleware.sign.here.agreement.DocumentSignatureRequestDto
import com.cynergisuite.middleware.sign.here.agreement.DocumentSignatureSigningDetailDto
import com.cynergisuite.middleware.sign.here.associated.OrgSigRequestedSigningDetail
import com.cynergisuite.middleware.sign.here.token.SignHereTokenTestDataLoaderService
import com.cynergisuite.middleware.store.StoreEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import groovy.json.JsonBuilder
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpStatus
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.OffsetDateTime

@MicronautTest(transactional = false)
class SignHereAgreementProxyControllerSpecification extends ControllerSpecificationBase {
   @Inject @Value("\${sign.here.please.port}") Integer signHerePleasePort
   @Inject SignHereTokenTestDataLoaderService signHereTokenTestDataLoaderService
   @Inject ObjectMapper objectMapper

   WireMockServer wireMockServer

   void setup() {
      final options = new WireMockConfiguration()
      options.port(signHerePleasePort)
      options.bindAddress("localhost")
      this.wireMockServer = new WireMockServer(options)
      this.wireMockServer.start()
   }

   void cleanup() {
      this.wireMockServer.stop()
   }

   void "list agreements" () {
      setup:
      final store = storeFactoryService.store(1, tstds1) as StoreEntity // load a store
      final tstds1StoreManagerDepartment = departmentFactoryService.department('SM', tstds1) // grab a store manager department
      final store1Tstds1Employee = employeeFactoryService.single(store1Tstds1, tstds1StoreManagerDepartment) // create us a store user with store manager as their department
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(store1Tstds1Employee.number, store1Tstds1Employee.passCode, tstds1.datasetCode, store1Tstds1Employee.store.myNumber()).with { new AuthenticatedEmployee(it, store1Tstds1Employee.passCode) } // grab store manager's authentication
      final store1Tstds1UserAccessToken = loginEmployee(store1Tstds1AuthenticatedEmployee) // login to server as the store manager
      final signHereToken = signHereTokenTestDataLoaderService.single(tstds1, store, "1234568792502154981dkfjoijfkdidkfdjokakdifjeuandkdjfiehgnx") // mash the keyboard to get some characters to use as a standin for a token
      final loginResponse = objectMapper.writeValueAsString(new BearerAccessRefreshToken(null, ["ROLE_SIG_REQUEST"], 3600, "jsonwebtokenhere", "jsonrefreshtokenhere", "Bearer")) // create a fake login response
      final documentRequestResponse = objectMapper.writeValueAsString(
         new Page([
            new DocumentSignatureRequestDto(
               UUID.randomUUID(),
               OffsetDateTime.now(),
               new DocumentSignatureSigningDetailDto(
                  "test name",
                  "test reason",
                  "test location",
                  "test contact"
               ),
               ["test@email.com"],
               LocalDate.now().plusYears(7),
               [
                  'Agreement-No': '987654321',
                  'Customer-No': '123456789',
                  'Agreement-Type': 'R'
               ],
               new DocumentSignatureStatus("CERTIFIED", "Document Certified")
            ),
         ], new DocumentPageRequest(), 1, 1, true, true)
      )
      wireMockServer.stubFor(WireMock.post("/api/login/token").withRequestBody(WireMock.equalTo(new JsonBuilder([token: signHereToken.token]).toString())).willReturn(WireMock.okJson(loginResponse))) // mock out a login call to the e-sig service
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/api/document/requested.*")).willReturn(WireMock.okJson(documentRequestResponse))) // mock out a call to the e-sig DocumentController.fetchOrganizationSignatureRequestsByToken call

      when:
      def result = get("/sign/here/agreement", store1Tstds1UserAccessToken)

      then:
      notThrown(Exception)
      wireMockServer.checkForUnmatchedRequests() // check to see if any of the routes above weren't matched
      result.elements.size() == 1
      with(result.elements[0]) {
         id != null
         agreementNumber == "987654321"
         customerNumber == "123456789"
         agreementType == "R"
         timeCreated != null
         customerName == "test name"
      }
   }

   void "list associated detail" () {
      setup:
      final store = storeFactoryService.store(1, tstds1) as StoreEntity // load a store
      final tstds1StoreManagerDepartment = departmentFactoryService.department('SM', tstds1) // grab a store manager department
      final store1Tstds1Employee = employeeFactoryService.single(store1Tstds1, tstds1StoreManagerDepartment) // create us a store user with store manager as their department
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(store1Tstds1Employee.number, store1Tstds1Employee.passCode, tstds1.datasetCode, store1Tstds1Employee.store.myNumber()).with { new AuthenticatedEmployee(it, store1Tstds1Employee.passCode) } // grab store manager's authentication
      final store1Tstds1UserAccessToken = loginEmployee(store1Tstds1AuthenticatedEmployee) // login to server as the store manager
      final signHereToken = signHereTokenTestDataLoaderService.single(tstds1, store, "1234568792502154981dkfjoijfkdidkfdjokakdifjeuandkdjfiehgnx") // mash the keyboard to get some characters to use as a standin for a token
      final loginResponse = objectMapper.writeValueAsString(new BearerAccessRefreshToken(null, ["ROLE_SIG_REQUEST"], 3600, "jsonwebtokenhere", "jsonrefreshtokenhere", "Bearer")) // create a fake login response
      final randomUUID = UUID.randomUUID()
      final pageRequested = new StandardPageRequest()
      final associatedRequestResponse = objectMapper.writeValueAsString(
         new Page([
            new AssociatedDetailDto(
               randomUUID,
               'SIG_REQUESTED',
               ["patricks@hightouchinc.com", "patricks@hightouchinc.com"],
               OffsetDateTime.now(),
               new OrgSigRequestedSigningDetail(
                  "test name",
                  "test reason",
                  "test location",
                  "test contact"
               ),
               null,
               null,
            ),
         ], new StandardPageRequest(), 1, 1, true, true)
      )
      wireMockServer.stubFor(WireMock.post("/api/login/token").withRequestBody(WireMock.equalTo(new JsonBuilder([token: signHereToken.token]).toString())).willReturn(WireMock.okJson(loginResponse))) // mock out a login call to the e-sig service
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/api/document/requested/${randomUUID}*")).willReturn(WireMock.okJson(associatedRequestResponse))) // mock out a call to the e-sig DocumentController.fetchOrganizationSignatureRequestedAssociated call

      when:
      def result = get("/sign/here/agreement/requested/associated/${randomUUID}${pageRequested}", store1Tstds1UserAccessToken)

      then:
      notThrown(Exception)
      wireMockServer.checkForUnmatchedRequests() // check to see if any of the routes above weren't matched
      result.elements.size() == 1
      with(result.elements[0]) {
         id != null
         type == 'SIG_REQUESTED'
         name == 'test name'
         timeCreated != null
         signatories[0] == 'patricks@hightouchinc.com'
         signatories[1] == 'patricks@hightouchinc.com'
      }
   }

   void "load archived document" () {
      setup:
      final store = storeFactoryService.store(1, tstds1) as StoreEntity // load a store
      final tstds1StoreManagerDepartment = departmentFactoryService.department('SM', tstds1) // grab a store manager department
      final store1Tstds1Employee = employeeFactoryService.single(store1Tstds1, tstds1StoreManagerDepartment) // create us a store user with store manager as their department
      final store1Tstds1AuthenticatedEmployee = userService.fetchUserByAuthentication(store1Tstds1Employee.number, store1Tstds1Employee.passCode, tstds1.datasetCode, store1Tstds1Employee.store.myNumber()).with { new AuthenticatedEmployee(it, store1Tstds1Employee.passCode) } // grab store manager's authentication
      final store1Tstds1UserAccessToken = loginEmployee(store1Tstds1AuthenticatedEmployee) // login to server as the store manager
      final signHereToken = signHereTokenTestDataLoaderService.single(tstds1, store, "1234568792502154981dkfjoijfkdidkfdjokakdifjeuandkdjfiehgnx") // mash the keyboard to get some characters to use as a standin for a token
      final loginResponse = objectMapper.writeValueAsString(new BearerAccessRefreshToken(null, ["ROLE_SIG_REQUEST"], 3600, "jsonwebtokenhere", "jsonrefreshtokenhere", "Bearer")) // create a fake login response
      final documentId = UUID.randomUUID()
      wireMockServer.stubFor(WireMock.post("/api/login/token").withRequestBody(WireMock.equalTo(new JsonBuilder([token: signHereToken.token]).toString())).willReturn(WireMock.okJson(loginResponse))) // mock out a login call to the e-sig service
      wireMockServer.stubFor(
         WireMock.get("/api/document/detail/archive/${documentId}")
            .withHeader("Authorization", WireMock.equalTo("Bearer jsonwebtokenhere"))
            .willReturn(
               WireMock.aResponse()
                  .withHeader("Content-Type", "application/pdf")
                  .withBodyFile("multi-sign.pdf")
            )
      )

      when:
      def result = getForResponse("/sign/here/agreement/document/detail/archive/${documentId}", store1Tstds1UserAccessToken)

      then:
      notThrown(Exception)
      result.status == HttpStatus.OK
      result.body().startsWith("%PDF-1.4")
   }
}
