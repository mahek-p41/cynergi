package com.cynergisuite.middleware.area

import com.cynergisuite.domain.SimpleTypeDomainDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Max
import javax.validation.constraints.Min
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

   @field:NotNull
   @field:Min(value = 0)
   @field:Max(value = 99)
   @field:Schema(description = "Configured security level of a module", minimum = "0", maximum = "99")
   var level: Int? = null,

   @field:Schema(description = "Menu in which module belongs to")
   var menuType: SimpleTypeDomainDTO? = null

) {

   constructor(type: ModuleTypeEntity) :
      this(
         id = type.id,
         value = type.value,
         program = type.program,
         description = type.description,
         level = type.level,
         menuType = type.menuType?.let { SimpleTypeDomainDTO(it.id) }
      )

   constructor(type: ModuleTypeEntity, localizedDescription: String) :
      this(
         id = type.id,
         value = type.value,
         program = type.program,
         description = localizedDescription,
         level = type.level,
         menuType = type.menuType?.let { SimpleTypeDomainDTO(it.id) }
      )
}
