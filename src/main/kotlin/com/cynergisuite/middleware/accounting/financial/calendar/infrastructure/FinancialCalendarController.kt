package com.cynergisuite.middleware.accounting.financial.calendar.infrastructure

import com.cynergisuite.domain.FinancialCalendarValidateDatesFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarCompleteDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarGLAPDateRangeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.FinancialCalendarService
import com.cynergisuite.middleware.accounting.financial.calendar.FiscalYearDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure.GeneralLedgerDetailRepository
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NoContentException
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/accounting/financial-calendar")
class FinancialCalendarController @Inject constructor(
   private val financialCalendarService: FinancialCalendarService,
   private val generalLedgerDetailRepository: GeneralLedgerDetailRepository,
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(FinancialCalendarController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Fetch a single Financial Calendar", description = "Fetch a single Financial Calendar by it's system generated primary key", operationId = "financialCalendar-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = FinancialCalendarDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Financial Calendar was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): FinancialCalendarDTO {
      logger.info("Fetching Financial Calendar by {}", id)

      val user = userService.fetchUser(authentication)
      val response = financialCalendarService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Financial Calendar by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Fetch a listing of Financial Calendars", description = "Fetch a paginated listing of Financial Calendar", operationId = "financialCalendar-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Financial Calendar was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<FinancialCalendarDTO> {
      logger.info("Fetching all Financial Calendars {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = financialCalendarService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Get(uri = "/fiscal-year", produces = [APPLICATION_JSON])
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Fetch a listing of Fiscal Years", description = "Fetch a listing of Fiscal Years", operationId = "financialCalendar-fetchFiscalYears")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchFiscalYear(
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<FiscalYearDTO> {
      logger.info("Fetching Fiscal Years")

      val user = userService.fetchUser(authentication)
      return financialCalendarService.fetchFiscalYears(user.myCompany())
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Create a single Financial Calendar", description = "Create a single Financial Calendar", operationId = "financialCalendar-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = FinancialCalendarDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Financial Calendar was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: FinancialCalendarDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): FinancialCalendarDTO {
      logger.debug("Requested Create Financial Calendar {}", dto)

      val user = userService.fetchUser(authentication)
      val response = financialCalendarService.create(dto, user.myCompany())

      logger.debug("Requested Create Financial Calendar {} resulted in {}", dto, response)

      return response
   }

   @Post(uri = "/year", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Create a single Financial Calendar", description = "Create a single Financial Calendar", operationId = "financialCalendar-create-financial-year")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = List::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Financial Calendar was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun createFinancialYear(
      @Body @Valid
      financialCalendarList: List<FinancialCalendarDTO>,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<FinancialCalendarDTO> {
      logger.debug("Requested Create Financial Calendar {}", financialCalendarList)

      val user = userService.fetchUser(authentication)
      val response = financialCalendarList.map { financialCalendarService.create(it, user.myCompany()) }.toList()

      logger.debug("Requested Create Financial Calendar {} resulted in {}", financialCalendarList, response)

      return response
   }

   @Post(uri = "/complete", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Create a complete Financial Calendar", description = "Create a complete Financial Calendar", operationId = "financialCalendar-create-financial-complete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = List::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The Financial Calendar was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )

   fun createCompleteCalendar(
      @Body @Valid
      createDate: FinancialCalendarCompleteDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<FinancialCalendarDTO> {
      logger.debug("Requested Create Financial Calendar with beginning date {}", createDate.periodFrom)

      val user = userService.fetchUser(authentication)
      val date = createDate.periodFrom!!
      val response = financialCalendarService.create(date, user.myCompany())

      logger.debug("Requested Create Financial Calendar {} resulted in {}", date, response)

      return response
   }

   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Update a single Financial Calendar", description = "Update a single Financial Calendar", operationId = "financialCalendar-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = FinancialCalendarDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Financial Calendar was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the Financial Calendar being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: FinancialCalendarDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): FinancialCalendarDTO {
      logger.info("Requested Update Financial Calendar {}", dto)

      val user = userService.fetchUser(authentication)
      val response = financialCalendarService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Financial Calendar {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/open-gl", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class)
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Set GLAccounts Open for a period", description = "Set GLAccounts to false then set to true for the desired period(s)", operationId = "financialCalendar-open-gl")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = FinancialCalendarDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun openGL(
      @Body @Valid
      dateRangeDTO: FinancialCalendarDateRangeDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested set GLAccounts Open for periods in date range {}", dateRangeDTO)

      val user = userService.fetchUser(authentication)
      val response = financialCalendarService.openGLAccountsForPeriods(dateRangeDTO, user.myCompany())

      logger.debug("Requested set GLAccounts Open for periods in date range {} resulted in {}", dateRangeDTO, response)
   }

   @Put(value = "/open-ap", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class)
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Set AP Accounts Open for a period", description = "Set APAccounts to false then set to true for the desired period(s)", operationId = "financialCalendar-open-ap")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = FinancialCalendarDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun openAP(
      @Body @Valid
      dateRangeDTO: FinancialCalendarDateRangeDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested set APAccounts Open for periods in date range {}", dateRangeDTO)

      val user = userService.fetchUser(authentication)
      val response = financialCalendarService.openAPAccountsForPeriods(dateRangeDTO, user.myCompany())

      logger.debug("Requested set APAccounts Open for periods in date range {} resulted in {}", dateRangeDTO, response)
   }

   @Put(value = "/open-gl-ap", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class)
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Set GL/AP Accounts Open for a period", description = "Set GL/AP Accounts to false then set to true for the desired period(s)", operationId = "financialCalendar-open-gl-ap")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = FinancialCalendarDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun openGLAndAP(
      @Body @Valid
      dateRangeDTO: FinancialCalendarGLAPDateRangeDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested set GL/AP Accounts Open for periods in date range {}", dateRangeDTO)

      val user = userService.fetchUser(authentication)
      val response = financialCalendarService.openGLAPAccountsForPeriods(dateRangeDTO, user.myCompany())

      logger.debug("Requested set GLAccounts Open for periods in date range {} resulted in {}", dateRangeDTO, response)
   }

   @Get(value = "/gl-dates-open", produces = [APPLICATION_JSON])
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Fetch the date range when GL is open", description = "Fetch the Financial Calendar date range when the General Ledger is open", operationId = "financialCalendar-fetchDateRangeWhenGLIsOpen")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Pair::class))]),
         ApiResponse(responseCode = "204", description = "Open GL range was unable to be found"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchDateRangeWhenGLIsOpen(
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Pair<LocalDate, LocalDate> {
      logger.info("Fetching Financial Calendar date range when General Ledger is open")

      val user = userService.fetchUser(authentication)
      val response = financialCalendarService.fetchDateRangeWhenGLIsOpen(user.myCompany())

      logger.debug("Fetching Financial Calendar date range when General Ledger is open resulted in {}", response)

      return response ?: throw NoContentException()
   }

   @Get(value = "/ap-dates-open", produces = [APPLICATION_JSON])
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Fetch the date range when AP is open", description = "Fetch the Financial Calendar date range when the Accounts Payable is open", operationId = "financialCalendar-fetchDateRangeWhenAPIsOpen")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Pair::class))]),
         ApiResponse(responseCode = "204", description = "Open AP range was unable to be found"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchDateRangeWhenAPIsOpen(
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Pair<LocalDate, LocalDate> {
      logger.info("Fetching Financial Calendar date range when Accounts Payable is open")

      val user = userService.fetchUser(authentication)
      val response = financialCalendarService.fetchDateRangeWhenAPIsOpen(user.myCompany())

      logger.debug("Fetching Financial Calendar date range when Accounts Payable is open resulted in {}", response)

      return response ?: throw NoContentException()
   }

   @Throws(NotFoundException::class)
   @Get(value = "/validate-dates{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Validate start and end dates for GL Trial Balance reports", description = "Validate start and end dates for GL Trial Balance reports", operationId = "financialCalendar-validateDates")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Boolean::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Financial Calendar was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun validateDates(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @QueryValue("filterRequest")
      filterRequest: FinancialCalendarValidateDatesFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Boolean {
      logger.info("Validating Financial Calendar dates {}", filterRequest)

      val user = userService.fetchUser(authentication)

      // validate dates are found
      val isFromDateFound = financialCalendarService.dateFoundInFinancialCalendar(user.myCompany(), filterRequest.fromDate!!)
      val isThruDateFound = financialCalendarService.dateFoundInFinancialCalendar(user.myCompany(), filterRequest.thruDate!!)

      // validate from and thru dates are in same fiscal year
      val sameFiscalYear = financialCalendarService.sameFiscalYear(user.myCompany(), filterRequest)

      val response = isFromDateFound || isThruDateFound || sameFiscalYear

      logger.debug("Validating Financial Calendar for given date resulted in", response)

      return response
   }

   //Used to confirm whether or not both general_ledger_detail and general_ledger_summary are empty prior to creating a financial calendar
   @Throws(NotFoundException::class)
   @Get(value = "/gl-exist", produces = [APPLICATION_JSON])
   @Operation(tags = ["FinancialCalendarEndpoints"], summary = "Returns True if GL Detail and Summary are empty", description = "Returns True if GL Detail and Summary are empty", operationId = "financialCalendar-generalLedgerDetailAndSummaryExist")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Boolean::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Financial Calendar was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun generalLedgerDetailAndSummaryExist(
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Boolean {
      logger.info("Checking for existing general_ledger_detail or general_ledger_summary records")

      val user = userService.fetchUser(authentication)

      val isGLDFound = generalLedgerDetailRepository.exists(user.myCompany())
      val isGLSFound = generalLedgerSummaryRepository.existsByCompanyOnly(user.myCompany())

      val response = isGLDFound || isGLSFound

      logger.debug("Checking for existing general_ledger_detail or general_ledger_summary records resulted in {}", response)

      return response
   }
}