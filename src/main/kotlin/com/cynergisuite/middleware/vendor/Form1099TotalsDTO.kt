package com.cynergisuite.middleware.vendor

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Schema(name = "Vendor1099TotalsDTO", title = "Vendor 1099 Field Totals", description = "Vendor 1099 Field Totals")
data class Form1099TotalsDTO(

   @field:Schema(description = "1099 Field 1")
   var ten99Field1: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field2: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field3: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field4: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field5: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field6: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field7: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field8: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field9: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field10: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field11: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field12: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field13: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field14: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field15: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field16: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "1099 Field ")
   var ten99Field17: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "Grand Total")
   var grandTotal: BigDecimal = BigDecimal.ZERO
) {
   constructor(entity: Form1099TotalsEntity) :
      this(
         ten99Field1 = entity.ten99Field1,
         ten99Field2 = entity.ten99Field2,
         ten99Field3 = entity.ten99Field3,
         ten99Field4 = entity.ten99Field4,
         ten99Field5 = entity.ten99Field5,
         ten99Field6 = entity.ten99Field6,
         ten99Field7 = entity.ten99Field7,
         ten99Field8 = entity.ten99Field8,
         ten99Field9 = entity.ten99Field9,
         ten99Field10 = entity.ten99Field10,
         ten99Field11 = entity.ten99Field11,
         ten99Field12 = entity.ten99Field12,
         ten99Field13 = entity.ten99Field13,
         ten99Field14 = entity.ten99Field14,
         ten99Field15 = entity.ten99Field15,
         ten99Field16 = entity.ten99Field16,
         ten99Field17 = entity.ten99Field17,
         grandTotal = entity.grandTotal
      )
}
