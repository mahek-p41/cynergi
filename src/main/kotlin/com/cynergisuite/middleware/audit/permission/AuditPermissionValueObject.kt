package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.department.DepartmentDTO
import com.cynergisuite.middleware.localization.LocalizationService
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.Locale
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AuditPermission", title = "Single Audit Permission", description = "A single audit permission defining access to endpoints of the auditing system")
data class AuditPermissionValueObject(

   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: UUID? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "type", description = "Audit permission type associated with this audit permission")
   var type: AuditPermissionTypeValueObject,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "department", description = "Department assigned to this audit permission")
   var department: DepartmentDTO

) : Identifiable {

   constructor(entity: AuditPermissionEntity, locale: Locale, localizationService: LocalizationService) :
      this(
         id = entity.id,
         type = AuditPermissionTypeValueObject(entity.type, locale, localizationService),
         department = DepartmentDTO(entity.department)
      )

   override fun myId(): UUID? = id
}
