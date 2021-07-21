package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@Deprecated("This should only be used on entities coming from fastinfo.  Once that is no longer required this interface should be deleted")
@Schema(name = "NumericIdentifiable", title = "Provides a reference to another model", description = "Describes a simple object that has an ID.  This is usually used as a stand-in when a dependent has a larger more complex 'parent' object when just the ID is required to identify the dependency")
data class SimpleLegacyIdentifiableDTO(

   @field:NotNull
   @field:Schema(name = "id", description = "System managed ID that points to a valid instance")
   var id: Long? = null

) : LegacyIdentifiable {
   constructor(entity: LegacyIdentifiable) :
      this(
         id = entity.myId()
      )

   override fun myId(): Long? = id
}
