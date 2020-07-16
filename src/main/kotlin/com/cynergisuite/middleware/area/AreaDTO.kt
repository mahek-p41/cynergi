package com.cynergisuite.middleware.area

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "AreaEntity", title = "AreaEntity type", description = "AreaEntity type")
data class AreaDTO (

   @field:Positive
   var id: Long? = null,
   var areaType: AreaTypeDTO
) {

   constructor(area: AreaEntity) :
      this(
         id = area.id,
         areaType = AreaTypeDTO(area.areaType)
      )

   constructor(entity: AreaEntity, localizedDescription: String) :
      this(
         id = entity.id,
         areaType = AreaTypeDTO(entity.areaType, localizedDescription)
      )
}
