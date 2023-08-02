package com.cynergisuite.middleware.sign.here.agreement

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Introspected
@JsonInclude(NON_NULL)
data class DocumentSignatureRequestDto(
   var id: UUID? = null,
   var timeCreated: OffsetDateTime? = null,
   var signingDetail: DocumentSignatureSigningDetailDto? = null,
   var requestedSignatures: List<String> = emptyList(),
   var expiration: LocalDate? = null,
   var meta: HashMap<*, *> = HashMap<String, Any>(),
)

@Introspected
data class DocumentSignatureSigningDetailDto(
   val name: String,
   val reason: String,
   val location: String,
   val contactInfo: String,
)
