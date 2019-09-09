package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.audit.action.AuditActionValueObject
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.store.StoreValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.Locale
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Audit", title = "Single Audit associated with a single Store", description = "A single audit for a store on a specified date along with it's current state")
data class AuditValueObject (

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:Schema(name = "timeCreated", required = false, description = "UTC Timestamp when the Audit was created")
   val timeCreated: OffsetDateTime? = null,

   @field:Schema(name = "timeCreated", required = false, description = "UTC Timestamp when the Audit was last updated")
   val timeUpdated: OffsetDateTime? = null,

   @field:Schema(name = "store", required = false, description = "Store the audit is associated with")
   var store: StoreValueObject? = null,

   @field:Schema(name = "actions", required = true, description = "Listing of actions associated with this Audit")
   var actions: MutableSet<AuditActionValueObject> = mutableSetOf()

) : ValueObjectBase<AuditValueObject>() {

   constructor(store: StoreValueObject) :
      this(
         id = null,
         store = store,
         actions = mutableSetOf()
      )

   constructor(entity: Audit, locale: Locale, localizationService: LocalizationService) :
      this (
         id = entity.id,
         timeCreated = entity.timeCreated,
         timeUpdated = entity.timeUpdated,
         store = StoreValueObject(entity.store),
         actions = entity.actions.asSequence().map { action ->
            AuditActionValueObject(action, AuditStatusValueObject(action.status, action.status.localizeMyDescription(locale, localizationService)))
         }.toMutableSet()
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): AuditValueObject = copy()

   fun getCurrentStatus(): AuditStatusValueObject? = actions.asSequence().sortedBy { it.id }.map { it.status }.lastOrNull()
}
