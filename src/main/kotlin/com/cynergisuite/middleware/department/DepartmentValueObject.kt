package com.cynergisuite.middleware.department

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase

@ValueObject
data class DepartmentValueObject(
   val id: Long,
   val code: String,
   val description: String,
   val securityProfile: Int,
   val defaultMenu: String
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
