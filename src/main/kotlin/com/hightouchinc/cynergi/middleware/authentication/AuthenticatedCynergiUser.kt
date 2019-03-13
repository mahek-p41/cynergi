package com.hightouchinc.cynergi.middleware.authentication

import io.micronaut.security.authentication.UserDetails

class AuthenticatedCynergiUser(
   username: String,
   roles: MutableCollection<String>,
   val level: Int,
   val companyId: Long,
   val userId: Long
): UserDetails(username, roles)
