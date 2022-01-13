package com.cynergisuite.middleware.darwill

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class DarwillManagementDto(

   @field:NotEmpty
   @field:Size(min = 6, max = 6)
   val dataset: String? = null,

   @field:NotEmpty
   @field:Size(min = 3, max = 100)
   val username: String? = null,

   @field:NotEmpty
   @field:Size(min = 3, max = 100)
   val password: String? = null,

   @field:NotEmpty
   @field:Size(min = 4)
   val host: String? = null,

   @field:NotNull
   @field:Min(1)
   @field:Max(65000)
   val port: Int? = null,
)
