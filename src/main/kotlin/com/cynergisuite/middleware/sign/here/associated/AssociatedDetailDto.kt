package com.cynergisuite.middleware.sign.here.associated

import io.micronaut.core.annotation.Introspected
import java.time.OffsetDateTime
import java.util.UUID

@Introspected
data class AssociatedDetailDto(
   val id: UUID? = null,
   val type: String? = null,
   var signatories: List<String> = emptyList(),
   val timeCreated: OffsetDateTime? = null,
   val signingDetail: OrgSigRequestedSigningDetail? = null,
   val expirationDate: String? = null,
)

@Introspected
data class OrgSigRequestedSigningDetail(
   val name: String? = null,
   val reason: String? = null,
   val location: String? = null,
   val contactInfo: String? = null,
)
