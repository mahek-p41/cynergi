package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.DataTransferObject
import com.cynergisuite.domain.SimpleIdentifiableDataTransferObject
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.store.StoreEntity
import io.swagger.v3.oas.annotations.media.Schema
import java.time.DayOfWeek
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@DataTransferObject
@Schema(name = "AuditScheduleCreateUpdate", title = "Requirements for creating an audit schedule", description = "Payload for creating a schedule for an audit associated with what stores and which department is supposed to do the audit")
data class AuditScheduleCreateUpdateDataTransferObject(

   @field:Positive
   @field:Schema(name = "id", description = "System generated ID for the associated schedule")
   val id: Long? = null,

   @field:NotNull
   @field:Size(min = 3, max = 64)
   @field:Schema(name = "title", description = "Short title to describe the schedule to the user who is setting it up")
   val title: String? = null, // equates to Schedule.title

   @field:NotNull
   @field:Size(min = 3, max = 256)
   @field:Schema(name = "description", description = "Long description of the schedule the user entered")
   val description: String? = null, // equates to Schedule.description

   @field:NotNull
   @field:Schema(name = "schedule", description = "Day of week the audit needs to be scheduled for", allowableValues = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"])
   val schedule: DayOfWeek? = null, // equates to Schedule.schedule

   @field:Valid
   @field:NotEmpty
   @field:Schema(name = "stores", description = "Set of stores the audit schedule is supposed to run against")
   val stores: Set<SimpleIdentifiableDataTransferObject> = mutableSetOf(), // is from a schedule argument that is collected together

   @field:Valid
   @field:NotNull
   @field:Schema(name = "department", description = "Department that is supposed to do the audit at the desired stores")
   val department: SimpleIdentifiableDataTransferObject? = null,

   @field:NotNull
   @field:Schema(name = "enabled", description = "Whether the audit is enabled or not")
   val enabled: Boolean? = true

) {
   constructor(title: String, description: String, schedule: DayOfWeek, stores: Set<StoreEntity>, department: DepartmentEntity) :
      this(
         title = title,
         description = description,
         schedule = schedule,
         stores = stores.asSequence().map { SimpleIdentifiableDataTransferObject(it.id) }.toSet(),
         department = SimpleIdentifiableDataTransferObject(department.id)
      )

   constructor(id: Long, title: String, description: String, schedule: DayOfWeek, stores: Set<StoreEntity>, department: DepartmentEntity) :
      this(
         id = id,
         title = title,
         description = description,
         schedule = schedule,
         stores = stores.asSequence().map { SimpleIdentifiableDataTransferObject(it.id) }.toSet(),
         department = SimpleIdentifiableDataTransferObject(department.id)
      )

   constructor(title: String, description: String, schedule: DayOfWeek, stores: Set<Long>, department: Long) :
      this(
         title = title,
         description = description,
         schedule = schedule,
         stores = stores.asSequence().map { SimpleIdentifiableDataTransferObject(it) }.toSet(),
         department = SimpleIdentifiableDataTransferObject(department)
      )
}
