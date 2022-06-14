package com.cynergisuite.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Store", title = "A location where rental inventory is processed", description = "A location within Cynergi where rental items in inventory are managed.", requiredProperties = ["number"])
data class SimpleLegacyNumberDTO(
   @field:Positive
   @field:NotNull
   @field:JsonProperty("number")
   @field:Schema(name = "number", minimum = "1", required = true, nullable = false, description = "number")
   var number: Int? = null,
)
