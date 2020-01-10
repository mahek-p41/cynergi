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

   @field:Schema(name = "store", required = false, description = "Store the audit is associated with")
   var store: StoreValueObject? = null,

   @field:Positive
   @field:Schema(name = "number", minimum = "0", required = false, description = "Audit Count")
   var auditNumber: Int = 0,

   @field:Positive
   @field:Schema(name = "totalDetails", description="Total number of items scanned as part of an audit", minimum = "0", readOnly = true, required = false)
   var totalDetails: Int = 0,

   @field:Positive
   @field:Schema(name = "totalExceptions", description="Total number of exceptions associated with an audit", minimum = "0", readOnly = true, required = false)
   var totalExceptions: Int = 0,

   @field:Schema(name = "lastUpdated", description = "Last time an audit detail or audit exception was created", readOnly = true, required = false)
   val lastUpdated: OffsetDateTime? = null,

   @field:Positive
   @field:Schema(name = "inventoryCount", description="Total idle inventory count associated with an audit's store", minimum = "0", readOnly = true, required = false)
   var inventoryCount: Int = 0,

   @field:Schema(name = "actions", required = true, description = "Listing of actions associated with this Audit")
   var actions: MutableSet<AuditActionValueObject> = mutableSetOf()

) : ValueObjectBase<AuditValueObject>() {

   constructor(entity: AuditEntity, locale: Locale, localizationService: LocalizationService) :
      this (
         id = entity.id,
         timeCreated = entity.timeCreated,
         store = StoreValueObject(entity.store),
         auditNumber = entity.number,
         totalDetails = entity.totalDetails,
         totalExceptions = entity.totalExceptions,
         lastUpdated = entity.lastUpdated,
         inventoryCount = entity.inventoryCount,
         actions = entity.actions.asSequence().map { action ->
            AuditActionValueObject(action, AuditStatusValueObject(action.status, action.status.localizeMyDescription(locale, localizationService)))
         }.toMutableSet()
      )

   override fun myId(): Long? = id
   override fun copyMe(): AuditValueObject = copy()

   @Schema(name = "currentStatus", description = "The current AuditStatus of the referenced Audit")
   fun getCurrentStatus(): AuditStatusValueObject? =
      actions.asSequence()
         .sortedBy { it.id }
         .map { it.status }
         .lastOrNull()
}
