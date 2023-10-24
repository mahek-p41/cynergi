package com.cynergisuite.middleware.department

import com.cynergisuite.domain.LegacyIdentifiable
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
@Schema(name = "Department", title = "A Cynergi Department", description = "A department within a company")
data class DepartmentDTO(

   @field:Positive
   @field:Schema(name = "id", description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 2, max = 2)
   @field:Schema(name = "code", description = "2 character code describing a department")
   var code: String? = null,

   @field:Size(min = 2, max = 60)
   @field:Schema(name = "description", description = "Long form of the name of the department")
   var description: String? = null

) : LegacyIdentifiable {

   constructor(entity: DepartmentEntity) :
      this(
         id = entity.id,
         code = entity.code,
         description = entity.description
      )

   override fun myId(): Long? = id
}
