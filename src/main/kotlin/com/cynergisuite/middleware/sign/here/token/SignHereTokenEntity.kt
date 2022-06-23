package com.cynergisuite.middleware.sign.here.token

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.core.annotation.Introspected
import java.util.*

@Introspected
data class SignHereTokenEntity(
   val id: UUID? = null,
   val company: CompanyEntity,
   val store: StoreEntity,
   val token: String,
) : Identifiable {

   override fun myId(): UUID? = id
}
