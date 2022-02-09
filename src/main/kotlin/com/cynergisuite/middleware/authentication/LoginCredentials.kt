package com.cynergisuite.middleware.authentication

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
@Schema(name = "UsernamePasswordStoreCredentials", title = "Username/Password combo", description = "Username/Password with the ability to set the user's associated store", requiredProperties = ["username", "password", "storeNumber"])
class LoginCredentials(

   @field:NotBlank
   @field:Schema(name = "username", required = true, description = "Username to log into the system")
   var username: String? = null,

   @field:NotBlank
   @field:Schema(name = "password", required = true, description = "Password for the user")
   var password: String? = null,

   @field:Positive
   @field:Schema(name = "storeNumber", required = true, description = "Store Number to be used to override the user's default store")
   var storeNumber: Int? = null,

   @field:NotNull
   @field:Size(min = 6, max = 6)
   @field:Schema(name = "dataset", required = false, description = "Semi required property of which dataset to assign to a user's login.  If a user's dataset can be determined automatically this isn't required.")
   var dataset: String? = null

)
