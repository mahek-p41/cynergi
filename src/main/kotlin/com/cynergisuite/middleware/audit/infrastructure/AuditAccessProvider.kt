package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.authentication.AuthenticatedUser
import com.cynergisuite.middleware.authentication.infrastructure.AccessControlProvider
import io.micronaut.core.type.Argument
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditAccessProvider @Inject constructor(
   private val auditPermissionRepository: AuditPermissionRepository
): AccessControlProvider {

   override fun canUserAccess(user: AuthenticatedUser, asset: String, arguments: Array<Argument<Any>>): Boolean {
      val auditPermission = auditPermissionRepository.findOneByAsset(asset)
      val userDepartment = user.myDepartment()

      return if (auditPermission == null) { // TODO handle the case where the user doesn't have a department assigned AKA 998
         true
      } else {
         auditPermission.department.code == userDepartment
      }
   }
}
