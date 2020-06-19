package com.cynergisuite.middleware.authentication

import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Validated
@Schema(name = "UsernamePasswordStoreCredentials", title = "Username/Password combo", description = "Username/Password with the ability to set the user's associated store", requiredProperties = ["username", "password", "storeNumber"])
class LoginCredentials(
   username: String? = null,
   password: String? = null,

   @field:Positive
   @field:Schema(name = "storeNumber", required = true, description = "Store Number to be used to override the user's default store")
   var storeNumber: Int? = null,

   @field:NotNull
   @field:Size(min = 6, max = 6)
   @field:Schema(name = "dataset", required = false, description = "Semi required property of which dataset to assign to a user's login.  If a user's dataset can be determined automatically this isn't required.")
   var dataset: String? = null

) : UsernamePasswordCredentials(
   username,
   password
)
