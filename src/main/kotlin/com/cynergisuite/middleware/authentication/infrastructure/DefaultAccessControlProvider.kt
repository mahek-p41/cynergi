package com.cynergisuite.middleware.authentication.infrastructure

import com.cynergisuite.middleware.authentication.AuthenticatedUser
import javax.inject.Singleton

/**
 * Provides a default implementation of AccessControlProvider through the Micronaut IOC container.
 */
@Singleton
class DefaultAccessControlProvider: AccessControlProvider // TODO flesh this class out once the Cynergi access system is in place
