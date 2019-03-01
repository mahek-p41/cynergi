package com.hightouchinc.cynergi.middleware.authentication

import com.hightouchinc.cynergi.middleware.dto.DataTransferObject
import io.micronaut.security.authentication.UserDetails

@DataTransferObject
data class AuthenticatedUserDetails(
   val userName: String,
   val level: Int,
   val userRoles: Set<String>
) : UserDetails(userName, userRoles) {
}
