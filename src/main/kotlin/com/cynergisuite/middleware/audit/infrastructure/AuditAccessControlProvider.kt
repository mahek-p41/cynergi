package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.middleware.audit.permission.infrastructure.AuditPermissionRepository
import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.infrastructure.AccessControlProvider
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.localization.AccessDenied
import io.micronaut.core.type.MutableArgumentValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditAccessControlProvider @Inject constructor(
   private val auditPermissionRepository: AuditPermissionRepository
) : AccessControlProvider {

   override fun canUserAccess(user: User, asset: String, parameters: MutableMap<String, MutableArgumentValue<*>>): Boolean {
      val auditPermission = auditPermissionRepository.permissionDepartmentByAsset(asset, user.myCompany())

      return user.isCynergiAdmin() || // if user is high touch admin then allow no questions asked
         auditPermission.isEmpty() || // if no permissions have been setup for this asset then allow
         auditPermission.contains(user.myDepartment()) // if permissions have been setup and the user's department is contained in that permission then allow
   }

   override fun generateException(user: User, asset: String?, parameters: MutableMap<String, MutableArgumentValue<*>>): Exception {
      return AccessException(AccessDenied(), user.myEmployeeNumber().toString())
   }
}
