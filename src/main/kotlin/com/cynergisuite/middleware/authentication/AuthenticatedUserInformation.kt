package com.cynergisuite.middleware.authentication

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@ValueObject
@JsonInclude(NON_NULL)
@Schema(name = "AuthenticationInformation", title = "Claims associated with a user", description = "Describes some useful info about a user's login status.  The loginStatus property will change to describe the user's state.")
data class AuthenticatedUserInformation (

   @field:Schema(name = "employeeNumber", title = "Employee Number", description = "System assigned number for an employee", required = true, nullable = true)
   val employeeNumber: String? = null,

   @field:NotNull
   @field:Schema(name = "loginStatus", title = "Login status of user", description = "Describes if the state of the user", required = true, nullable = false)
   val loginStatus: String,

   @field:Schema(name = "storeNumber", title = "Session's store number", description = "User's store number that their session is associated with", required = true, nullable = true)
   val storeNumber: Int? = null
) {

   constructor(user: AuthenticatedUser, loginStatus: String) :
      this(
         employeeNumber = user.myEmployeeNumber().toString(),
         storeNumber = user.myStoreNumber(),
         loginStatus = loginStatus
      )
}
