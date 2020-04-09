package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(NON_NULL)
@Schema(name = "AuthenticationInformation", title = "Claims associated with a user", description = "Describes some useful info about a user's login status.  The loginStatus property will change to describe the user's state.")
data class AuthenticatedUserInformation(

   @field:Schema(name = "employeeNumber", title = "Employee Number", description = "System assigned number for an employee", required = true)
   val employeeNumber: String? = null,

   @field:Schema(name = "storeNumber", title = "Session's store number", description = "User's store number that their session is associated with", required = true)
   val storeNumber: Int? = null,

   @field:Schema(name = "company", title = "Company currently connected to", description = "Company that data is being loaded from", required = true)
   val company: CompanyValueObject? = null,

   @field:Schema(name = "alternativeStoreIndicator", title = "Alternate Store Indicator", description = "Indicates which other stores an employee may access", required = true, allowableValues = ["A N R D"])
   val alternativeStoreIndicator: String? = null,

   @field:Schema(name = "alternativeArea", title = "Alternative area to use for user access", description = "Number of the division or region an employee may access depending on alternativeStoreIndicator")
   val alternativeArea: Int? = null,

   @field:Schema(name = "permissions", title = "Permissions that user has access to", description = "Listing of permissions of an employee")
   val permissions: Set<String>? = null

) {
   constructor(user: User, permissions: Set<String>, companyOverride: CompanyValueObject) :
      this(
         employeeNumber = user.myEmployeeNumber().toString(),
         storeNumber = user.myLocation().myNumber(),
         company = companyOverride,
         alternativeStoreIndicator = user.myAlternativeStoreIndicator(),
         alternativeArea = user.myAlternativeArea(),
         permissions = permissions
      )

}
