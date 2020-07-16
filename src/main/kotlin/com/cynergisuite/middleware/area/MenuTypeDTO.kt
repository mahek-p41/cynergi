package com.cynergisuite.middleware.area

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "MenuType", title = "Menu type", description = "Menu type")
data class MenuTypeDTO (

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Menu value")
   var value: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for menu type")
   var description: String? = null,

   @field:Schema(description = "AreaEntity type")
   var modules: MutableList<ModuleTypeDTO>
) {

   constructor(type: MenuType) :
      this(
         id = type.id,
         value = type.value,
         description = type.description,
         modules = type.modules.map { ModuleTypeDTO(it) } as MutableList<ModuleTypeDTO>
      )

   constructor(type: MenuType, localizedDescription: String, modules: List<ModuleTypeDTO>? = null) :
      this(
         id = type.id,
         value = type.value,
         description = localizedDescription,
         modules = (modules ?: type.modules.map { ModuleTypeDTO(it) }) as MutableList<ModuleTypeDTO>
      )
}
