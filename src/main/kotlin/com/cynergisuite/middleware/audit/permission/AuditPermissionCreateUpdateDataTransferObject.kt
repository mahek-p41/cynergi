package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.domain.ValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@DataTransferObject
@JsonInclude(NON_NULL)
@Schema(name = "AuditPermissionCreateUpdate", description = "Payload for creating or updating an audit permission")
data class AuditPermissionCreateUpdateDataTransferObject(

   @field:Positive
   val id: Long? = null,

   @field:NotNull
   val permission: SimpleIdentifiableDataTransferObject? = null,

   @field:NotNull
   val department: SimpleIdentifiableDataTransferObject? = null
)
