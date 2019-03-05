package com.hightouchinc.cynergi.middleware.authentication

import io.micronaut.security.authentication.UserDetails

class AuthenticatedUserDetails(
   userName: String,
   userRoles: Set<String>,
   val level: Int
) : UserDetails(userName, userRoles)
