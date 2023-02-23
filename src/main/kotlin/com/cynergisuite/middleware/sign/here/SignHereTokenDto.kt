package com.cynergisuite.middleware.sign.here

import com.cynergisuite.middleware.sign.here.token.SignHereTokenEntity
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
data class SignHereTokenDto(

   @field:NotBlank
   @field:Size(min = 30, max = 60)
   val token: String

) {
   constructor(entity: SignHereTokenEntity) :
      this(
         token = entity.token
      )
}
