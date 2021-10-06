package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.location.LocationEntity
import com.cynergisuite.middleware.store.Store
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_ONE

@MappedEntity("authenticated_user_vw")
data class AuthenticatedEmployee(

   @field:Id
   @field:GeneratedValue
   val id: Long,
   val type: String, // sysz or eli
   val number: Int, // employee number

   @Relation(ONE_TO_ONE)
   val company: CompanyEntity,

   @Relation(ONE_TO_ONE)
   val department: DepartmentEntity?,

   @Relation(ONE_TO_ONE)
   val assignedLocation: LocationEntity?,

   @Relation(ONE_TO_ONE)
   val chosenLocation: LocationEntity?,

   @Relation(ONE_TO_ONE)
   val fallbackLocation: LocationEntity,

   val passCode: String,
   val cynergiSystemAdmin: Boolean,
   val alternativeStoreIndicator: String,
   val alternativeArea: Long
) : User {

   constructor(user: AuthenticatedEmployee, passCodeOverride: String) :
      this(
         id = user.id,
         type = user.type,
         number = user.number,
         company = user.company,
         department = user.department,
         assignedLocation = user.assignedLocation,
         chosenLocation = user.chosenLocation,
         fallbackLocation = user.fallbackLocation,
         passCode = passCodeOverride,
         cynergiSystemAdmin = user.cynergiSystemAdmin,
         alternativeStoreIndicator = user.alternativeStoreIndicator,
         alternativeArea = user.alternativeArea
      )

   constructor(employeeId: Long, employee: EmployeeEntity, store: Store) :
      this(
         id = employeeId,
         type = employee.type,
         number = employee.number,
         company = employee.company,
         department = employee.department,
         assignedLocation = employee.store?.let { LocationEntity(employee.store) },
         chosenLocation = null, // since we are copying a row from the db for this, we don't have a chosenLocation
         fallbackLocation = LocationEntity(store),
         passCode = employee.passCode,
         cynergiSystemAdmin = employee.cynergiSystemAdmin,
         alternativeStoreIndicator = employee.alternativeStoreIndicator,
         alternativeArea = employee.alternativeArea
      )

   override fun myId(): Long = id
   override fun myCompany(): CompanyEntity = company
   override fun myDepartment(): Department? = department
   override fun myLocation(): Location = chosenLocation ?: assignedLocation ?: fallbackLocation
   override fun myEmployeeType(): String = type
   override fun myEmployeeNumber(): Int = number
   override fun myAlternativeStoreIndicator(): String = alternativeStoreIndicator
   override fun myAlternativeArea(): Long = alternativeArea
   override fun isCynergiAdmin(): Boolean = cynergiSystemAdmin
}
