package com.cynergisuite.middleware.agreement.signing

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class AgreementSigningEntity(
   val id: UUID? = null,
   val company: CompanyEntity,
   val store: StoreEntity,
   val primaryCustomerNumber: Int,
   val secondaryCustomerNumber: Int,
   val agreementNumber: Int,
   val agreementType: String,
   val statusId: Int,
   val externalSignatureId: UUID,
) : Identifiable {

   override fun myId(): UUID? = id
}
