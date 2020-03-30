package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.authentication.user.User
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(NON_NULL)
@Schema(name = "AuthenticationInformation", title = "Claims associated with a user", description = "Describes some useful info about a user's login status.  The loginStatus property will change to describe the user's state.")
data class AuthenticatedUserInformation (

   @field:Schema(name = "employeeNumber", title = "Employee Number", description = "System assigned number for an employee", required = true)
   val employeeNumber: String? = null,

   @field:Schema(name = "loginStatus", title = "Login status of user", description = "Describes if the state of the user", required = true)
   val loginStatus: String,

   @field:Schema(name = "storeNumber", title = "Session's store number", description = "User's store number that their session is associated with", required = true)
   val storeNumber: Int? = null,

   @field:Schema(name = "dataset", title = "Company dataset currently connected to", description = "Company dataset that data is being loaded from", required = true)
   val dataset: String? = null,

   @field:Schema(name = "alternativeStoreIndicator", title = "Alternate Store Indicator", description = "Indicates which other stores an employee may access", required = true, allowableValues = ["A N R D"])
   val alternativeStoreIndicator: String? = null,

   @field:Schema(name = "alternativeArea", title = "alternative area to use for user access", description = "Number of the division or region an employee may access depending on alternativeStoreIndicator")
   val alternativeArea: Int? = null

) {

   constructor(user: User, loginStatus: String) :
      this(
         employeeNumber = user.myEmployeeNumber().toString(),
         storeNumber = user.myLocation().myNumber(),
         loginStatus = loginStatus,
         dataset = user.myCompany().myDataset(),
         alternativeStoreIndicator = user.myAlternativeStoreIndicator(),
         alternativeArea = user.myAlternativeArea()
      )
}
