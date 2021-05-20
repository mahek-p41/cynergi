package com.cynergisuite.middleware.area

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Area", title = "AreaDTO", description = "A data transfer object containing a area information")
data class AreaDTO(

   @field:Positive
   var id: Int? = null,

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Area value")
   var value: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for area")
   var description: String? = null,

   @field:Schema(description = "Is area enabled by company")
   var enabled: Boolean = false,

   @field:Schema(description = "List of menus under an area")
   var menus: MutableList<MenuDTO>

) {
   constructor(type: AreaType, localizedDescription: String, menus: List<MenuDTO>? = null) :
      this(
         id = type.id,
         value = type.value,
         description = localizedDescription,
         enabled = type.enabled,
         menus = (menus ?: type.menus.map { MenuDTO(it) }) as MutableList<MenuDTO>
      )
}
