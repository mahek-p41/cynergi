package com.cynergisuite.middleware.department

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@ValueObject
@Schema(name = "Department", description = "A department within a company")
data class DepartmentValueObject(

   @field:Positive
   @field:Schema(name = "id", description = "System generated ID")
   var id: Long,

   @field:Size(min = 2, max = 2)
   @field:Schema(name = "code", description = "2 character code describing a department")
   var code: String,

   @field:Size(min = 2, max = 60)
   @field:Schema(name = "description", description = "Long form of the name of the department")
   var description: String,

   @field:Positive
   @field:Schema(name = "securityProfile", description = "Access level the department has within the company")
   var securityProfile: Int,

   @field:Size(min = 2, max = 60)
   @field:Schema(name = "defaultMenu", description = "Default menu users associated with this department see")
   var defaultMenu: String

) : ValueObjectBase<DepartmentValueObject>() {

   constructor(entity: DepartmentEntity) :
      this(
         id = entity.id,
         code = entity.code,
         description = entity.description,
         securityProfile = entity.securityProfile,
         defaultMenu = entity.defaultMenu
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): DepartmentValueObject = copy()
}
