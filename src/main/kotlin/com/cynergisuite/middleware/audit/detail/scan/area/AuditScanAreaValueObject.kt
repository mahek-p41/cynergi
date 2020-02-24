package com.cynergisuite.middleware.audit.detail.scan.area

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AuditScanArea", title = "Area where an item was scanned", description = "Possible location within a store where an item was scanned as part of an audit")
data class AuditScanAreaValueObject (

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(description = "This is a database driven with the original values being SHOWROOM, STOREROOM and WAREHOUSE")
   var value: String? = null,

   @field:Nullable
   @field:Size(min = 3, max = 50)
   @field:Schema(description = "A localized description suitable for showing the user")
   var description: String? = null

) {

   constructor(entity: AuditScanArea) :
      this(
         entity = entity,
         localizedDescription = entity.description
      )

   constructor(entity: AuditScanArea, localizedDescription: String) :
      this(
         value = entity.value,
         description = localizedDescription
      )
}
