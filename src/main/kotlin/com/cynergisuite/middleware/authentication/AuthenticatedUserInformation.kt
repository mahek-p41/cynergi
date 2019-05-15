package com.cynergisuite.middleware.authentication

import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@ValueObject
@JsonInclude(NON_NULL)
data class AuthenticatedUserInformation (
   val number: String? = null,
   val loginStatus: String
)
