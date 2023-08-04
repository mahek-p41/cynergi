package com.cynergisuite.middleware.sign.here.agreement

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.UUID

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "SignHereAgreement", title = "Sign Here Agreement", description = "Sign here agreement", requiredProperties = ["id", "agreementNumber", "customerNumber", "timeCreated"])
data class SignHereAgreementDto(

   @field:Schema(name = "id", required = true, nullable = false, description = "Sign Here agreement unique identifier")
   val id: UUID,

   @field:Schema(name = "agreementNumber", minimum = "1", required = true, nullable = false, description = "Agreement number being signed")
   val agreementNumber: String?,

   @field:Schema(name = "customerNumber", minimum = "1", required = true, nullable = false, description = "Customer number associated with agreement")
   val customerNumber: String?,

   @field:Schema(name = "agreementType", minimum = "1", required = true, nullable = false, description = "Agreement Type associated with agreement")
   val agreementType: String?,

   @field:Schema(name = "timeCreated", required = true, nullable = false, description = "Timestamp of when the request was made to the Sign Here service")
   val timeCreated: OffsetDateTime,

   @field:Schema(name = "customerName", required = false, nullable = true, description = "Customer name")
   val customerName: String? = null

)
