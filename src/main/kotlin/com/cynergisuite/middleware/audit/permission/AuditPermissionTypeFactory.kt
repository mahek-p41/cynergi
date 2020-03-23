package com.cynergisuite.middleware.audit.permission

object AuditPermissionTypeFactory {

   @JvmStatic
   private val permissionTypes = listOf(
      AuditPermissionType(1, "audit-fetchOne", "Find audit by ID", "audit.fetch.one"),
      AuditPermissionType(2, "audit-fetchAll", "List audits", "audit.fetch.all"),
      AuditPermissionType(3, "audit-fetchAllStatusCounts", "List stats for audits", "audit.fetch.all.status.counts"),
      AuditPermissionType(4, "audit-create", "Create an audit", "audit.create"),
      AuditPermissionType(5, "audit-completeOrCancel", "Complete or Cancel an audit", "audit.complete.or.cancel"),
      AuditPermissionType(6, "audit-updateApproved", "Update an audit's status", "audit.update.approved"),
      AuditPermissionType(7, "audit-updateApprovedAllExceptions", "Update an audit's status", "audit.update.approve.all.exceptions"),
      AuditPermissionType(8, "auditDetail-fetchOne", "Find an audit inventory item by ID", "audit.detail.fetch.one"),
      AuditPermissionType(9, "auditDetail-fetchAll", "List audit inventory items", "audit.detail.fetch.all"),
      AuditPermissionType(10, "auditDetail-save", "Create a found inventory item", "audit.detail.save"),
      AuditPermissionType(11, "auditException-fetchOne", "Find an audit exception by ID", "audit.exception.fetch.one"),
      AuditPermissionType(12, "auditException-fetchAll", "List audit exceptions", "audit.exception.fetch.all"),
      AuditPermissionType(13, "auditException-create", "Create an audit exception", "audit.exception.create"),
      AuditPermissionType(14, "auditException-update", "Update an audit exception note or status", "audit.exception.update"),
      AuditPermissionType(15, "auditException-approved", "Allow user to approve an audit", "audit.exception.approved"),
      AuditPermissionType(16, "auditSchedule-fetchOne", "Allow user to fetch a single audit schedule", "audit.schedule.fetch.one"),
      AuditPermissionType(17, "auditSchedule-fetchAll", "Allow user to fetch all audit schedules", "audit.schedule.fetch.all"),
      AuditPermissionType(18, "auditSchedule-create", "Allow user to create an audit schedule", "audit.schedule.create"),
      AuditPermissionType(19, "auditSchedule-update", "Allow user to update an audit schedule", "audit.schedule.update"),
      AuditPermissionType(20, "auditPermission-fetchOne", "Allow user to fetch a single audit permission", "audit.permission.fetch.one"),
      AuditPermissionType(21, "auditPermission-fetchAll", "Allow user to fetch a a listing of audit permissions", "audit.permission.fetch.all"),
      AuditPermissionType(22, "auditPermissionType-fetchAll", "Allow user to fetch a a listing of audit permission types", "audit.permission.type.fetch.all"),
      AuditPermissionType(23, "auditPermission-create", "Allow user to create an audit permission", "audit.permission.create"),
      AuditPermissionType(24, "auditPermission-delete", "Allow user to delete an audit permission", "audit.permission.delete")
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

