package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "AuditUpdate", title = "Requirements for updating an Audit", description = "Defines the requirements for updating and Audit's status")
data class AuditUpdateValueObject(

   @field:NotNull
   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = true, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(name = "status", required = true, description = "Valid status to update the audit to")
   var status: AuditStatusValueObject? = null

) : ValueObjectBase<AuditUpdateValueObject>() {

   override fun copyMe() = copy()
   override fun valueObjectId(): Long? = id
}
