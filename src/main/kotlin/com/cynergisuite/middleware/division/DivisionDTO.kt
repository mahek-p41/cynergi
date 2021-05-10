package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "Division", title = "Division", description = "A division of a company.")
data class DivisionDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", description = "System generated ID")
   var id: Long? = null,

   @field:Positive
   @field:Schema(name = "id", minimum = "1", description = "System Z external number")
   var number: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Human readable name for a division")
   var name: String? = null,

   @field:NotNull
   @field:Schema(name = "description", description = "Division description")
   var description: String? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "divisionalManager", description = "Divisional manager id")
   var divisionalManager: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(name = "effectiveDate", description = "Date that this Division will become active")
   val effectiveDate: LocalDate? = null,

   @field:Schema(name = "endingDate", description = "Date that this Division will become inactive")
   val endingDate: LocalDate? = null,

   ) : Identifiable {
   override fun myId() = id

   constructor(entity: DivisionEntity) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name,
         description = entity.description,
         divisionalManager = SimpleIdentifiableDTO(entity.divisionalManager?.id),
         effectiveDate = entity.effectiveDate,
         endingDate = entity.endingDate,
      )
}
