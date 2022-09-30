package com.cynergisuite.middleware.agreement.signing.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.agreement.signing.AgreementSigningDTO
import com.cynergisuite.middleware.agreement.signing.AgreementSigningService
import com.cynergisuite.middleware.company.CompanyService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(IS_ANONYMOUS)
@Controller("/agreement/signing")
class AgreementSigningController(
   private val companyService: CompanyService,
   private val agreementSigningService: AgreementSigningService
) {
   private val logger: Logger = LoggerFactory.getLogger(AgreementSigningController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}/dataset/{dataset}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AgreementSigningEndpoints"], summary = "Fetch a single Agreement Signing record", description = "Fetch a single Agreement Signing record by it's system generated primary key", operationId = "agreementSigning-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Agreement Signing record was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Agreement Signing record", `in` = ParameterIn.PATH) @QueryValue("id")
      id: UUID,
      @Parameter(name = "dataset", description = "Dataset associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("dataset")
      dataset: String,
      httpRequest: HttpRequest<*>
   ): AgreementSigningDTO {
      logger.info("Fetching Agreement Signing record by {}", id)

      val company = companyService.fetchByDatasetCodeForEntity(dataset)
      val response = agreementSigningService.fetchById(id = id, company = company!!) ?: throw NotFoundException(id)

      logger.debug("Fetching Agreement Signing record by {} resulted in {}", id, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(uri = "/upsertPrep/{dataset}/{customerNumber}/{agreementNumber}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AgreementSigningEndpoints"], summary = "Checking to see if Agreement Signing record already exists", description = "Fetch a single Agreement Signing record by dataset, customer number, and agreement number", operationId = "agreementSigning-alreadyExists")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Agreement Signing record was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchByCustomerAndAgreement(
      @Parameter(name = "dataset", description = "Dataset associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("dataset")
      dataset: String,
      @Parameter(name = "customerNumber", description = "Customer Number associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("customerNumber")
      customerNumber: Int,
      @Parameter(name = "agreementNumber", description = "Agreement Number associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("agreementNumber")
      agreementNumber: Int,
      httpRequest: HttpRequest<*>
   ): AgreementSigningDTO {
      logger.info("Checking to see if Agreement Signing record already exists by {} {} {}", dataset, customerNumber, agreementNumber)

      val company = companyService.fetchByDatasetCodeForEntity(dataset)
      val response = agreementSigningService.fetchByCustomerAndAgreement(company = company!!, customerNumber, agreementNumber) ?: throw NotFoundException(customerNumber)

      logger.debug("Checking to see if Agreement Signing record exists by {} {} {} resulted in {}", dataset, customerNumber, agreementNumber, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get(uri = "/customerAgreements/{dataset}/{customerNumber}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AgreementSigningEndpoints"], summary = "Checking for all agreements for a customer", description = "Fetch all Agreements by dataset and customer number", operationId = "agreementSigning-allCustomerAgreements")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Agreement Signing record was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllSigningAgreementsByCustomerNumber(
      @Parameter(name = "dataset", description = "Dataset associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("dataset")
      dataset: String,
      @Parameter(name = "customerNumber", description = "Customer Number associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("customerNumber")
      customerNumber: Int,
      httpRequest: HttpRequest<*>
   ): List<AgreementSigningDTO> {
      logger.info("Retrieving all signing agreements for a specific customer number {} {}", dataset, customerNumber)

      val company = companyService.fetchByDatasetCodeForEntity(dataset)
      val response = agreementSigningService.findAgreementsByCustomer(company = company!!, customerNumber).map(::AgreementSigningDTO)

      logger.debug("Retrieving all signing agreements for a specific customer number {} {} resulted in {}", dataset, customerNumber, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/paged/dataset/{dataset}{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AgreementSigningEndpoints"], summary = "Fetch a listing of agreements in the signing process", description = "Fetch a paginated listing of Document Signing agreements", operationId = "agreementSigning-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "403", description = "If authentication fails"),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "dataset", description = "Dataset associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("dataset")
      dataset: String,
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") @Valid
      pageRequest: AgreementSigningPageRequest,
      httpRequest: HttpRequest<*>
   ): Page<AgreementSigningDTO> {
      logger.info("Fetch all document signing agreements within parameters given")

      val company = companyService.fetchByDatasetCodeForEntity(dataset)
      val page = agreementSigningService.fetchAll(pageRequest, company!!)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      } else {
         return page
      }
   }

   @Post(uri = "/dataset/{dataset}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AgreementSigningEndpoints"], summary = "Create a single Agreement Signing record", description = "Create a single Agreement Signing record", operationId = "agreementSigning-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Agreement Signing record", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AgreementSigningDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Agreement Signing record was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Parameter(name = "dataset", description = "Dataset associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("dataset")
      dataset: String,
      @Body @Valid
      dto: AgreementSigningDTO,
      httpRequest: HttpRequest<*>
   ): AgreementSigningDTO {
      logger.info("Requested Create Agreement Signing record {}", dto)

      val company = companyService.fetchByDatasetCodeForEntity(dataset) ?: throw NotFoundException(dataset)
      val response = agreementSigningService.create(dto = dto, company = company)

      logger.debug("Requested Create Agreement Signing record {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}/dataset/{dataset}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AgreementSigningEndpoints"], summary = "Update a single Agreement Signing record", description = "Update a single Agreement Signing record", operationId = "agreementSigning-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Agreement Signing record", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AgreementSigningDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Agreement Signing record was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(description = "Primary Key to lookup the Agreement Signing record", `in` = ParameterIn.PATH) @QueryValue("id")
      id: UUID,
      @Parameter(name = "dataset", description = "Dataset associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("dataset")
      dataset: String,
      @Body @Valid
      dto: AgreementSigningDTO,
      httpRequest: HttpRequest<*>
   ): AgreementSigningDTO {
      logger.info("Requested Audit status change or note  {}", dto)

      val company = companyService.fetchByDatasetCodeForEntity(dataset) ?: throw NotFoundException(dataset)
      val response = agreementSigningService.update(dto, company)

      logger.debug("Requested Update Audit {} resulted in {}", dto, response)

      return response
   }
}
