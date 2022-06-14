package com.cynergisuite.middleware.agreement.signing

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@Schema(name = "AgreementSigning", title = "An agreement in the document signing process", description = "An agreement in the document signing process")
data class AgreementSigningDTO(

   @field:Schema(name = "id", description = "System generated ID for the associated agreement signing record")
   var id: UUID? = null,

   @field:NotNull
   @field:NotBlank
   @field:Schema(name = "company", description = "The associated company for this agreement")
   var company: CompanyEntity? = null,

   @field:Schema(name = "store", required = false, description = "Store the agreement is associated with")
   var store: SimpleLegacyNumberDTO? = null,

   @field:Schema(name = "primaryCustomerNumber", description = "Primary Customer Number for the agreement")
   val primaryCustomerNumber: Int? = null,

   @field:Schema(name = "secondaryCustomerNumber", description = "Secondary Customer Number for the agreement")
   val secondaryCustomerNumber: Int? = null,

   @field:Schema(name = "agreementNumber", description = "Agreement Number")
   val agreementNumber: Int? = null,

   @field:NotNull
   @field:Size(min = 1, max = 1)
   @field:Schema(name = "agreementType", description = "Short title to describe the schedule to the user who is setting it up")
   var agreementType: String? = null,

   @field:Schema(name = "statusId", description = "Id for associated status in agreement_signing_status_type_domain")
   val statusId: Int? = null,

   @field:NotNull
   @field:Size(min = 1, max = 200) // TODO What is the actual min and max?
   @field:Schema(name = "externalSignatureId", description = "Link to QR code")
   var externalSignatureId: String? = null

) : Identifiable {

   constructor(entity: AgreementSigningEntity) :
      this (
         id = entity.id,
         company = entity.company,
         store = SimpleLegacyNumberDTO(entity.store.number),
         primaryCustomerNumber = entity.primaryCustomerNumber,
         secondaryCustomerNumber = entity.secondaryCustomerNumber,
         agreementNumber = entity.agreementNumber,
         agreementType = entity.agreementType,
         statusId = entity.statusId,
         externalSignatureId = entity.externalSignatureId,
      )

   override fun myId(): UUID? = id
}
