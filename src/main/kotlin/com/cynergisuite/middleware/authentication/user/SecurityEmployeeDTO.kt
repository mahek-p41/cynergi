package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.employee.EmployeeValueObject

import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SecurityGroupDTO", title = "A data transfer object containing security group information", description = "An data transfer object containing security group information.")
data class SecurityEmployeeDTO(

   @field:NotNull
   @field:Schema(name = "securityGroup", description = "Security Group")
   var securityGroup: SecurityGroupDTO,

   @field:NotNull
   @field:Schema(name = "employeeList", description = "List of employees")
   var employees: List<EmployeeValueObject>? = null

)
