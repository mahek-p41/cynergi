package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.department.DepartmentEntity
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AuditPermissionCreate", description = "Payload for creating an audit permission")
data class AuditPermissionCreateDataTransferObject(

   @field:Valid
   @field:NotNull
   val permissionType: SimpleIdentifiableDTO? = null,

   @field:Valid
   @field:NotNull
   val department: SimpleIdentifiableDTO? = null
) {
   constructor(permission: AuditPermissionType, department: DepartmentEntity) :
      this(
         permissionType = SimpleIdentifiableDTO(permission),
         department = SimpleIdentifiableDTO(department)
      )
}
