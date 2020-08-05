package com.cynergisuite.middleware.area

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Menu", title = "MenuDTO", description = "A data transfer object containing a menu information")
data class MenuDTO(

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Menu value")
   var value: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for menu")
   var description: String? = null,

   @field:Schema(description = "List of modules under a menu")
   var modules: MutableList<ModuleDTO>
) {

   constructor(type: MenuType) :
      this(
         id = type.id,
         value = type.value,
         description = type.description,
         modules = type.modules.map { ModuleDTO(it) } as MutableList<ModuleDTO>
      )

   constructor(type: MenuType, localizedDescription: String, modules: List<ModuleDTO>? = null) :
      this(
         id = type.id,
         value = type.value,
         description = localizedDescription,
         modules = (modules ?: type.modules.map { ModuleDTO(it) }) as MutableList<ModuleDTO>
      )
}
