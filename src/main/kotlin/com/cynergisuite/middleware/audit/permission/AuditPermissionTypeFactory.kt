package com.cynergisuite.middleware.audit.permission

object AuditPermissionTypeFactory {

   @JvmStatic
   private val permissionTypes = listOf(
      AuditPermissionType(1, "audit-approver", "Approve audits", "audit.approve"),
      AuditPermissionType(2, "audit-permission-manager", "Audit permission manager", "audit.permission.manager")
   )

   @JvmStatic
   fun findByValue(value: String) =
      permissionTypes.find { it.value == value }

   @JvmStatic
   fun random() =
      permissionTypes.random()

   @JvmStatic
   fun random(excludePermission: AuditPermissionType)
      = permissionTypes.filter { it.id != excludePermission.id }.random()
}

