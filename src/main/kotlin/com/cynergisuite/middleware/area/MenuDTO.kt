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
@Schema(title = "MenuDTO", description = "A data transfer object containing a menu information")
data class MenuDTO(

   @field:Positive
   var id: Int? = null,

   @field:Schema(description = "List of sub-menus")
   var menus: MutableList<MenuDTO>? = null,

   @field:NotNull
   @field:Size(min = 1, max = 10)
   @field:Schema(description = "Menu value")
   var value: String? = null,

   @field:NotNull
   @field:Size(min = 1, max = 100)
   @field:Schema(description = "A localized description for menu")
   var description: String? = null,

   @field:NotNull
   @field:Positive
   @field:Schema(description = "Display order relative to the other Menus in the same Area")
   val orderNumber: Int? = null,

   @field:Schema(description = "List of modules under a menu")
   var modules: MutableList<ModuleDTO>
) {

   constructor(type: MenuTypeEntity) :
      this(
         id = type.id,
         value = type.value,
         description = type.description,
         orderNumber = type.orderNumber,
         modules = mutableListOf() // type.modules.map { ModuleDTO(it) } as MutableList<ModuleDTO>
      )

   constructor(type: MenuTypeEntity, localizedDescription: String, menus: List<MenuDTO>? = null, modules: List<ModuleDTO>? = null) :
      this(
         id = type.id,
         menus = mutableListOf(), // (menus ?: type.menus.map { MenuDTO(it) }) as MutableList<MenuDTO>,
         value = type.value,
         description = localizedDescription,
         orderNumber = type.orderNumber,
         modules = mutableListOf(), // (modules ?: type.modules.map { ModuleDTO(it) }) as MutableList<ModuleDTO>
      )
}
