package com.cynergisuite.middleware.sign.here.token

import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.middleware.company.CompanyDTO
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@Schema(name = "SignHereToken", title = "A store-associated token granting access for document signing", description = "A store-associated token granting access for document signing")
data class SignHereTokenDTO(

   @field:Schema(name = "id", description = "System generated ID for the associated token")
   var id: String? = null,

   @field:NotNull
   @field:NotBlank
   @field:Schema(name = "company", description = "The associated company for this store token")
   var company: CompanyDTO? = null,

   @field:Schema(name = "store", required = false, description = "Store the token is associated with")
   var store: SimpleLegacyNumberDTO? = null,

   @field:Size(min = 30, max = 60)
   @field:Schema(name = "token", description = "AWS access token")
   var token: String? = null
) {

   constructor(entity: SignHereTokenEntity) :
      this (
         id = entity.id.toString(),
         company = CompanyDTO(entity.company),
         store = SimpleLegacyNumberDTO(entity.store.number),
         token = entity.token,
      )
}
