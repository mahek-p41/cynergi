package com.cynergisuite.middleware.audit.permission

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.ValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.Locale
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@ValueObject
@JsonInclude(NON_NULL)
@Schema(name = "AuditStatusPermissionType", title = "Definition of an allowed Audit Permission ", description = "Definition of an allowed Audit Permission to be associated with a Department for access to assets pertaining to Audits")
data class AuditPermissionTypeValueObject(

   @field:Positive
   @field:Schema(name = "id", description = "This is a database driven primary key value defining the id of the status")
   var id: Long,

   @field:NotNull
   @field:Size(min = 3, max = 15)
   @field:Schema(name = "value", description = "This is a database driven with the original values being CREATED, IN-PROGRESS, COMPLETED, CANCELED and SIGNED-OFF")
   var value: String,

   @field:Size(min = 3, max = 50)
   @field:Schema(name = "description", description = "A localized description suitable for showing the user")
   var description: String? = null
) : Identifiable {

   constructor(type: AuditPermissionType, localizedDescription: String) :
      this(
         id = type.id,
         value = type.value,
         description = localizedDescription
      )

   constructor(type: AuditPermissionType, locale: Locale, localizationService: LocalizationService) :
      this(
         id = type.id,
         value = type.value,
         description = type.localizeMyDescription(locale, localizationService)
      )

   override fun myId(): Long? = id
}
