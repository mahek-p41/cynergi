package com.cynergisuite.middleware.department

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase

@ValueObject
data class DepartmentValueObject(
   var id: Long,
   var code: String,
   var description: String,
   var securityProfile: Int,
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
