package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.department.DepartmentValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.schedule.ScheduleValueObject
import com.cynergisuite.middleware.store.StoreValueObject
import java.util.Locale

@ValueObject
data class AuditScheduleValueObject(
   val id: Long? = null,
   val store: StoreValueObject,
   val departmentAccess: DepartmentValueObject,
   val schedule: ScheduleValueObject
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
