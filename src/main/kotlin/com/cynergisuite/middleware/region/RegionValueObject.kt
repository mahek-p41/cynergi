package com.cynergisuite.middleware.region

import com.cynergisuite.middleware.division.DivisionValueObject
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Positive

@Schema(name = "Region", title = "Region", description = "A region of a division.")
data class RegionValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = null,

   @field:Positive
   @field:Schema(name = "number", minimum = "1", required = true, nullable = false, description = "Region number")
   var number: Int? = null,

   @field:Schema(name = "name", required = false, nullable = true, description = "Human readable name for a region")
   var name: String? = null,

   @field:Schema(name = "managerNumber", minimum = "1", required = true, nullable = false, description = "Manager number")
   var managerNumber: Int? = null,

   @field:Schema(name = "description", required = false, nullable = true, description = "Region description")
   var description: String? = null,

   @field:Schema(name = "division", required = false, nullable = true, description = "Division that a region belong to")
   var division: DivisionValueObject? = null
) {

}
