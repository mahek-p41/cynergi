package com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StagingDepositFilterRequest
import com.cynergisuite.domain.StagingStatusFilterRequest
import com.cynergisuite.middleware.accounting.general.ledger.deposit.AccountingDetailWrapper
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositDTO
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositService
import com.cynergisuite.middleware.accounting.general.ledger.deposit.StagingDepositStatusDTO
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@AreaControl("GL")
@Controller("/api/general-ledger/deposit")
class StagingDepositController @Inject constructor(
   private val userService: UserService,
   private val stagingDepositService: StagingDepositService
) {
   private val logger: Logger = LoggerFactory.getLogger(StagingDepositController::class.java)

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["StagingDepositEndpoints"], summary = "Fetch a listing of Staging Deposits", description = "Fetch a paginated listing of Staging Deposits", operationId = "StagingDeposits-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Staging Deposits was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StagingDepositPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<StagingDepositDTO> {
      logger.info("Fetching staging deposits {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = stagingDepositService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/status{?filterRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(
      tags = ["StagingDepositEndpoints"],
      summary = "Fetch staging status",
      description = "Fetch staging status",
      operationId = "StagingDeposits-fetchStatus"
   )
   @ApiResponses(
      value = [
         ApiResponse(
            responseCode = "200",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, array = ArraySchema(schema = Schema(implementation = StagingDepositStatusDTO::class)))]
         ),
         ApiResponse(
            responseCode = "204",
            description = "The requested Staging Status was unable to be found, or the result is empty"
         ),
         ApiResponse(
            responseCode = "401",
            description = "If the user calling this endpoint does not have permission to operate it"
         ),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchStagingStatus(
      @Parameter(name = "filterRequest", `in` = ParameterIn.QUERY, required = true)
      @Valid @QueryValue("filterRequest")
      filterRequest: StagingStatusFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<StagingDepositStatusDTO> {
      logger.info("Fetching staging deposits {}", filterRequest)

      val user = userService.fetchUser(authentication)

      return stagingDepositService.fetchStatus(user.myCompany(), filterRequest)
   }

   @Get(uri = "/detail/{id:[0-9a-fA-F\\-]+}", produces = [MediaType.APPLICATION_JSON])
   @Operation(
      tags = ["StagingDepositEndpoints"],
      summary = "Fetch a listing of accounting entry details",
      description = "Fetch a listing of accounting entry details",
      operationId = "StagingDeposits-fetchAccountingDetails"
   )
   @ApiResponses(
      value = [
         ApiResponse(
            responseCode = "200",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AccountingDetailWrapper::class))]
         ),
         ApiResponse(
            responseCode = "204",
            description = "The requested Staging Deposits was unable to be found, or the result is empty"
         ),
         ApiResponse(
            responseCode = "401",
            description = "If the user calling this endpoint does not have permission to operate it"
         ),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAccountingDetails(
      @QueryValue("id")
      verifyId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AccountingDetailWrapper {
      logger.info("Fetching staging accounting entry details for verifyId {}", verifyId)

      val user = userService.fetchUser(authentication)

      return stagingDepositService.fetchAccountingDetails(user.myCompany(), verifyId)
   }

   @Throws(PageOutOfBoundsException::class)
   @Post(uri = "day", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["StagingDepositEndpoints"], summary = "Move Accounting Details Staging to Pending Journal Entries by day", description = "Move Accounting Details Staging to Pending Journal Entries by day", operationId = "StagingDeposits-daySelected")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Accounting Details was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun day(
      @Body
      dto: List<UUID>,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ){
      logger.info("Move Accounting Details Staging to Pending Journal Entries by day {}", dto)

      val user = userService.fetchUser(authentication)
      stagingDepositService.postByDate(user.myCompany(), dto, user.isCynergiAdmin())
   }

   @Throws(PageOutOfBoundsException::class)
   @Post(uri = "day-criteria{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["StagingDepositEndpoints"], summary = "Move Accounting Details Staging to Pending Journal Entries by day with criteria", description = "Move Accounting Details Staging to Pending Journal Entries by day with criteria", operationId = "StagingDeposits-dayCriteria")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Staging Deposits was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun dayCriteria(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      filterRequest: StagingDepositFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Move Accounting Details Staging to Pending Journal Entries by day with criteria {}", filterRequest)

      val user = userService.fetchUser(authentication)

      val filteredList = stagingDepositService.fetchAll(user.myCompany(), filterRequest)
      val idList = filteredList.map { it.id }.toList()
      if (filteredList.isNotEmpty()) {
         stagingDepositService.postByDate(user.myCompany(), idList, user.isCynergiAdmin())
      } else throw NotFoundException("No elements found to post to GL")
   }

   @Throws(PageOutOfBoundsException::class)
   @Post(uri = "month", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["StagingDepositEndpoints"], summary = "Move Accounting Details Staging to Pending Journal Entries by month", description = "Move Accounting Details Staging to Pending Journal Entries by month", operationId = "StagingDeposits-monthSelected")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Staging Deposits was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun month(
      @Body
      dto: List<UUID>,
      @QueryValue
      lastDayOfMonth: String,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Move Accounting Details Staging to Pending Journal Entries by month {}", dto)

      val user = userService.fetchUser(authentication)
      val date = LocalDate.parse(lastDayOfMonth, DateTimeFormatter.ISO_LOCAL_DATE)
      stagingDepositService.postByMonth(user.myCompany(), dto, date, user.isCynergiAdmin())
   }

   @Throws(PageOutOfBoundsException::class)
   @Post(uri = "month-criteria{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["StagingDepositEndpoints"], summary = "Move Accounting Details Staging to Pending Journal Entries by month with criteria", description = "Move Accounting Details Staging to Pending Journal Entries by month with criteria", operationId = "StagingDeposits-monthCriteria")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Staging Deposits was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun monthCriteria(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      filterRequest: StagingDepositFilterRequest,
      @QueryValue
      lastDayOfMonth: String,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Move Accounting Details Staging to Pending Journal Entries by month with criteria {}", filterRequest)
      val date = LocalDate.parse(lastDayOfMonth, DateTimeFormatter.ISO_LOCAL_DATE)
      val user = userService.fetchUser(authentication)
      val filteredList = stagingDepositService.fetchAll(user.myCompany(), filterRequest)
      val idList = filteredList.map { it.id }.toList()

      if(filteredList.isNotEmpty()) {
         stagingDepositService.postByMonth(user.myCompany(), idList, date, user.isCynergiAdmin())
      } else throw NotFoundException("No elements found to post to GL")
   }
}
