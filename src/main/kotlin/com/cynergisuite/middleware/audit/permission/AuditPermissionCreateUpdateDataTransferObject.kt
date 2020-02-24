package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.middleware.department.DepartmentEntity
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "AuditPermissionCreateUpdate", description = "Payload for creating or updating an audit permission")
data class AuditPermissionCreateUpdateDataTransferObject(

   @field:Positive
   val id: Long? = null,

   @field:Valid
   @field:NotNull
   val permissionType: SimpleIdentifiableDataTransferObject? = null,

   @field:Valid
   @field:NotNull
   val department: SimpleIdentifiableDataTransferObject? = null
) {
   constructor(id: Long, permission: AuditPermissionType, department: DepartmentEntity) :
      this(
         id = id,
         permissionType = SimpleIdentifiableDataTransferObject(permission),
         department = SimpleIdentifiableDataTransferObject(department)
      )

   constructor(permission: AuditPermissionType, department: DepartmentEntity) :
      this(
         permissionType = SimpleIdentifiableDataTransferObject(permission),
         department = SimpleIdentifiableDataTransferObject(department)
      )
}
