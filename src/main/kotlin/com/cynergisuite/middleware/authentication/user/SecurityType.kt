package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.domain.TypeDomain
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@Introspected
@MappedEntity("security_access_point_type_domain")
data class SecurityType(
   @field:Id
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,
   val areaId: Int?

): TypeDomain() {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
