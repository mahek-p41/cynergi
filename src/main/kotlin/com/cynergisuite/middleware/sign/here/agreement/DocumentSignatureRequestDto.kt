package com.cynergisuite.middleware.sign.here.agreement

import io.micronaut.core.annotation.Introspected
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Introspected
data class DocumentSignatureRequestDto(
   val id: UUID,
   val timeCreated: OffsetDateTime,
   val signingDetail: DocumentSignatureSigningDetailDto,
   val requestedSignatures: List<String>,
   val expiration: LocalDate,
   val meta: HashMap<*, *> = HashMap<String, Any>()
)

@Introspected
data class DocumentSignatureSigningDetailDto(
   val name: String,
   val reason: String,
   val location: String,
   val contactInfo: String,
)
