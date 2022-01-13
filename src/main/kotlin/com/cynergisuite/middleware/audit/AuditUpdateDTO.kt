package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AuditUpdate", title = "Requirements for updating an Audit", description = "Defines the requirements for updating and Audit's status")
data class AuditUpdateDTO(

   @field:NotNull
   @field:Schema(name = "id", minimum = "1", required = true, description = "System generated ID")
   var id: UUID? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "status", required = true, description = "Valid status to update the audit to")
   var status: AuditStatusValueObject? = null

) : Identifiable {
   override fun myId(): UUID? = id
}
