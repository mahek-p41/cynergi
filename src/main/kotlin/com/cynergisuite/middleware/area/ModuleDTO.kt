package com.cynergisuite.middleware.area

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Module", title = "ModuleDTO", description = "A data transfer object containing a module information")
data class ModuleDTO(

   @field:NotNull
   @field:Positive
   var id: Int? = null,

   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Module value")
   var value: String? = null,

   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Module program")
   var program: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for module")
   var description: String? = null,

) {

   constructor(type: ModuleTypeEntity) :
      this(
         id = type.id,
         value = type.value,
         program = type.program,
         description = type.description,
      )

   constructor(type: ModuleTypeEntity, localizedDescription: String) :
      this(
         id = type.id,
         value = type.value,
         program = type.program,
         description = localizedDescription,
      )
}
