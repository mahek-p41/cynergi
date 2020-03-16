package com.cynergisuite.middleware.division

import com.cynergisuite.middleware.company.CompanyValueObject
import com.cynergisuite.middleware.employee.Employee
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Positive

@Schema(name = "Division", title = "Division", description = "A division of a company.")
data class DivisionValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = null,

   @field:Positive
   @field:Schema(name = "number", minimum = "1", required = true, nullable = false, description = "Division number")
   var number: Int? = null,

   @field:Schema(name = "name", required = false, nullable = true, description = "Human readable name for a division")
   var name: String? = null,

   @field:Schema(name = "manager", required = false, nullable = true, description = "Division Manager")
   var manager: Employee? = null,

   @field:Schema(name = "description", required = false, nullable = true, description = "Division description")
   var description: String? = null,

   @field:Schema(name = "company", required = false, nullable = true, description = "Company that a division belong to")
   var company: CompanyValueObject? = null

) {

}
