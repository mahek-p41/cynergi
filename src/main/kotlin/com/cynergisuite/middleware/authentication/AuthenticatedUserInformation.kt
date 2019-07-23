package com.cynergisuite.middleware.authentication

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.middleware.store.Store
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@ValueObject
@JsonInclude(NON_NULL)
@Schema(name = "AuthenticationInformation", description = "Describes some useful info about a user's login status")
data class AuthenticatedUserInformation (
   val number: String? = null,
   val loginStatus: String,
   val store: Int? = null
)
