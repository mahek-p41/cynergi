package com.cynergisuite.middleware.vendor

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "VendorStatisticsInquiry", title = "Vendor Statistics Inquiry", description = "AP Vendor Statistics Inquiry")
data class VendorStatisticsDTO(

   @field:NotNull
   @field:Schema(name = "vendor", description = "Vendor DTO")
   var vendor: VendorDTO,

   @field:Schema(name = "ytdPaid", description = "Total AP payment detail amount YTD")
   var ytdPaid: BigDecimal? = null,

   @field:Schema(name = "ptdPaid", description = "Total AP payment detail amount PTD")
   var ptdPaid: BigDecimal? = null,

   @field:Schema(name = "unpaidAmounts", description = "Total unpaid amount and unpaid amount by AP Invoice due date")
   var unpaidAmounts: VendorStatisticsUnpaidAmountsEntity? = null,

)
