package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.department.DepartmentValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.schedule.ScheduleValueObject
import com.cynergisuite.middleware.store.StoreValueObject
import io.swagger.v3.oas.annotations.media.Schema
import java.util.Locale
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@ValueObject
@Schema(name = "AuditSchedule", description = "Schedule audit that associates a store, what department is supposed to do the audit and the schedule that the audit is to be done on")
data class AuditScheduleValueObject(

   @field:Positive
   var id: Long? = null,

   @field:Valid
   @field:NotNull
   var store: StoreValueObject? = null,

   @field:Valid
   @field:NotNull
   var departmentAccess: DepartmentValueObject? = null,

   @field:Valid
   @field:NotNull
   var schedule: ScheduleValueObject? = null

) : ValueObjectBase<AuditScheduleValueObject>() {

   constructor(auditScheduleEntity: AuditScheduleEntity, locale: Locale, localizationService: LocalizationService) :
      this(
         id = auditScheduleEntity.id,
         store = StoreValueObject(auditScheduleEntity.store),
         departmentAccess = DepartmentValueObject(auditScheduleEntity.departmentAccess),
         schedule = ScheduleValueObject(auditScheduleEntity.schedule, locale, localizationService)
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): AuditScheduleValueObject = copy()
}
