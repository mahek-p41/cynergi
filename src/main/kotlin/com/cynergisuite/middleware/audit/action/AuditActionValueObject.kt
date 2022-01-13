package com.cynergisuite.middleware.audit.action

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AuditAction", title = "Single item of an Audit's history", description = "An action taken against an Audit such as going from CREATED to IN-PROGRESS at a point in time")
data class AuditActionValueObject(

   @field:Schema(name = "id", description = "This is a database generated primary key", required = false)
   var id: UUID? = null,

   @field:Schema(name = "timeCreated", required = false, description = "UTC Timestamp when the Audit was created")
   var timeCreated: OffsetDateTime? = null,

   @field:Schema(name = "timeCreated", required = false, description = "UTC Timestamp when the Audit was last updated")
   var timeUpdated: OffsetDateTime? = null,

   @field:NotNull
   @field:Schema(name = "status", description = "A valid status to be associated with this AuditAction", required = true)
   var status: AuditStatusValueObject? = null,

   @field:Positive
   @field:Schema(name = "changedBy", description = "The Employee number to be associated with this action", required = false)
   var changedBy: EmployeeValueObject? = null

) : Identifiable {

   constructor(entity: AuditActionEntity, auditStatus: AuditStatusValueObject) :
      this(
         id = entity.id,
         timeCreated = entity.timeCreated,
         timeUpdated = entity.timeUpdated,
         status = auditStatus,
         changedBy = EmployeeValueObject(entity.changedBy)
      )

   override fun myId(): UUID? = id
}
