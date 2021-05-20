package com.cynergisuite.middleware.audit.permission

class AuditPermissionTypeTestDataLoader {

   private static final List<AuditPermissionType> permissionTypes = [
      new AuditPermissionType(1, "audit-approver", "Approve audits", "audit.approve"),
      new AuditPermissionType(2, "audit-permission-manager", "Audit permission manager", "audit.permission.manager")
   ]

   static AuditPermissionType findByValue(String value) {
      permissionTypes.find { it.value == value }
   }

   static AuditPermissionType random() {
      return permissionTypes.random()
   }

   static random(AuditPermissionType excludePermission) {
      permissionTypes.findAll { it.id != excludePermission.id }.random()
   }
}
