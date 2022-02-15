package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.SimpleTypeDomainDTO
import com.cynergisuite.middleware.department.DepartmentEntity
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AuditPermissionCreate", description = "Payload for creating an audit permission")
data class AuditPermissionCreateDTO(

   @field:Valid
   @field:NotNull
   val permissionType: SimpleTypeDomainDTO? = null,

   @field:Valid
   @field:NotNull
   val department: SimpleLegacyIdentifiableDTO? = null
) {
   constructor(permission: AuditPermissionType, department: DepartmentEntity) :
      this(
         permissionType = SimpleTypeDomainDTO(permission),
         department = SimpleLegacyIdentifiableDTO(department)
      )
}
