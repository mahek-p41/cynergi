package com.cynergisuite.middleware.sign.here.associated

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "SignHereAssociated", title = "Sign Here Associated", description = "Sign here associated", requiredProperties = ["id", "agreementNumber", "customerNumber", "timeCreated"])
data class SignHereAssociatedDto(

   @field:Schema(name = "id", required = true, nullable = false, description = "Sign Here agreement unique identifier")
   val id: UUID? = null,

   @field:Schema(name = "type", minimum = "1", required = true, nullable = false, description = "Status type")
   val type: String? = null,

   @field:Schema(name = "signatories", minimum = "1", required = true, nullable = false, description = "Customer(s)/Store Personnel needing to sign the agreement")
   var signatories: List<String> = emptyList(),

   @field:Schema(name = "name", required = false, nullable = true, description = "Customer name")
   val name: String? = null,

   @field:Schema(name = "timeCreated", required = true, nullable = false, description = "Timestamp of when the request was made to the Sign Here service")
   val timeCreated: OffsetDateTime? = null,

   @field:Schema(name = "signatureUrl", required = false, nullable = true, description = "URL that a signature can be signed at if available")
   val signatureUrl: String? = null,
)
