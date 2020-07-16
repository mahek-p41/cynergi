package com.cynergisuite.middleware.area

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AreaEntity", title = "AreaEntity type", description = "AreaEntity type")
data class AreaTypeDTO (

   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "AreaEntity value")
   var value: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for area type")
   var description: String? = null,

   @field:Schema(description = "Is area type enabled by company")
   var enabled: Boolean = false,

   var menus: MutableList<MenuTypeDTO>

) {

   constructor(type: AreaType) :
      this(
         id = type.id,
         value = type.value,
         description = type.description,
         enabled = type.enabled,
         menus = type.menus.map { MenuTypeDTO(it) } as MutableList<MenuTypeDTO>
      )

   constructor(type: AreaType, localizedDescription: String, menus: List<MenuTypeDTO>? = null) :
      this(
         id = type.id,
         value = type.value,
         description = localizedDescription,
         enabled = type.enabled,
         menus = (menus ?: type.menus.map { MenuTypeDTO(it) }) as MutableList<MenuTypeDTO>
      )
}
