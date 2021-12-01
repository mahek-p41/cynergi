package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.authentication.user.AuthenticatedUser

sealed interface UserAuthenticationStatus
sealed interface UserAuthenticationSuccess
sealed interface UserAuthenticationFailure

data class UserAuthenticated(
   val user: AuthenticatedUser
) : UserAuthenticationStatus, UserAuthenticationSuccess

data class UserAuthenticatedAsAdmin(
   val user: AuthenticatedUser,
): UserAuthenticationStatus, UserAuthenticationSuccess

data class CredentialsRequireStore(
   val identity: Int,
) : UserAuthenticationStatus, UserAuthenticationFailure

data class CredentialsProvidedDidNotMatch(
   val identity: Int
) : UserAuthenticationStatus, UserAuthenticationFailure
