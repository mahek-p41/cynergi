package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.VendorStatisticsFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceInquiryDTO
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.vendor.VendorService
import com.cynergisuite.middleware.vendor.VendorStatisticsDTO
import com.cynergisuite.middleware.vendor.VendorStatisticsService
import com.cynergisuite.middleware.vendor.rebate.RebateDTO
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/vendor-statistics")
class VendorStatisticsController @Inject constructor(
   private val vendorService: VendorService,
   private val vendorStatisticsService: VendorStatisticsService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorStatisticsEndpoints"], summary = "Fetch Vendor Statistics", description = "Fetch Vendor Statistics by vendor id", operationId = "vendorStatistics-fetchStatistics")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorStatisticsDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Vendor was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchStatistics(
      @QueryValue("id") vendorId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorStatisticsDTO {
      logger.info("Fetching Vendor Statistics by {}", vendorId)

      val user = userService.fetchUser(authentication)
      val vendorDTO = vendorService.fetchById(vendorId, user.myCompany()) ?: throw NotFoundException(vendorId)
      val response = vendorStatisticsService.fetchStatistics(vendorDTO, user.myCompany())

      logger.debug("Fetching Vendor Statistics by {} resulted in {}", vendorId, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/rebates{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorStatisticsEndpoints"], summary = "Fetch Rebates", description = "Fetch Rebates for Vendor Statistics", operationId = "vendorStatistics-fetchRebates")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Rebates were unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchRebates(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: VendorStatisticsFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<RebateDTO> {
      logger.info("Fetching Rebates for Vendor Statistics by {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val response = vendorStatisticsService.fetchRebates(user.myCompany(), filterRequest)

      logger.debug("Fetching Rebates for Vendor Statistics by {} resulted in {}", filterRequest.vendorId, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/invoices{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorStatisticsEndpoints"], summary = "Fetch Account Payable Invoices", description = "Fetch Account Payable Invoices for Vendor Statistics", operationId = "vendorStatistics-fetchInvoices")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Account Payable Invoices were unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchInvoices(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: VendorStatisticsFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AccountPayableInvoiceInquiryDTO> {
      logger.info("Fetching Invoices for Vendor Statistics by {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val response = vendorStatisticsService.fetchInvoices(user.myCompany(), filterRequest)

      logger.debug("Fetching Invoices for Vendor Statistics by {} resulted in {}", filterRequest.vendorId, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/purchase-orders{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorStatisticsEndpoints"], summary = "Fetch Purchase Orders", description = "Fetch Purchase Orders for Vendor Statistics", operationId = "vendorStatistics-fetchPurchaseOrders")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Purchase Orders were unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchPurchaseOrders(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: VendorStatisticsFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<PurchaseOrderDTO> {
      logger.info("Fetching Purchase Orders for Vendor Statistics by {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val response = vendorStatisticsService.fetchPurchaseOrders(user.myCompany(), filterRequest)

      logger.debug("Fetching Purchase Orders for Vendor Statistics by {} resulted in {}", filterRequest.vendorId, response)

      return response
   }
}
