package com.cynergisuite.middleware.area

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "ModuleType", title = "Module type", description = "Module type")
data class ModuleTypeDTO (

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Module value")
   var value: String? = null,

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Module program")
   var program: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for module type")
   var description: String? = null,

   @field:Min(value = 0)
   @field:Max(value = 100)
   @field:Schema(description = "Configured security level of a module", minimum = "0", maximum = "100")
   var level: Int?,

   @field:Schema(description = "Menu type")
   var menuType: SimpleIdentifiableDTO? = null

) {

   constructor(type: ModuleType) :
      this(
         id = type.id,
         value = type.value,
         program = type.program,
         description = type.description,
         level = type.level,
         menuType = type.menuType?.let { SimpleIdentifiableDTO(it.id) }
      )

   constructor(type: ModuleType, localizedDescription: String) :
      this(
         id = type.id,
         value = type.value,
         program = type.program,
         description = localizedDescription,
         level = type.level,
         menuType = type.menuType?.let { SimpleIdentifiableDTO(it.id) }
      )
}
