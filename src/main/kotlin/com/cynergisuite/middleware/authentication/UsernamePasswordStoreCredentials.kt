package com.cynergisuite.middleware.authentication

import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "UsernamePasswordStoreCredentials", title = "Username/Password combo", description = "Username/Password with the ability to set the user's associated store", requiredProperties = ["username", "password", "storeNumber"])
class UsernamePasswordStoreCredentials(
   username: String? = null,
   password: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(name = "storeNumber", required = true, description = "Store Number to be used to override the user's default store")
   var storeNumber: Int? = null

): UsernamePasswordCredentials(
   username,
   password
)
