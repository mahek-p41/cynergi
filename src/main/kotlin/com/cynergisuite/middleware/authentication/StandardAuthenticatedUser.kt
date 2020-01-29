package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.UserDetails
import java.util.Objects

data class StandardAuthenticatedUser(
   val id: Long,
   val employeeType: String,
   val storeNumber: Int,
   val employeeNumber: Int,
   val dataset: String,
   val department: String?
): AuthenticatedUser, UserDetails(employeeNumber.toString(), mutableListOf()) {

   constructor(employee: EmployeeEntity, overrideStore: StoreEntity) :
      this(
         id = employee.id!!,
         employeeType = employee.type,
         storeNumber = overrideStore.number,
         employeeNumber = employee.number,
         dataset = employee.dataset,
         department = employee.department
      )

   constructor(authentication: Authentication) :
      this(
         id = authentication.attributes["id"].let { Objects.toString(it).toLong() },
         employeeType = authentication.attributes["type"].let { Objects.toString(it) },
         storeNumber = authentication.attributes["stNum"].let { Objects.toString(it).toInt() },
         employeeNumber = authentication.attributes["sub"].let { Objects.toString(it).toInt() }, // sub is a subject which is encoded by the framework
         dataset = authentication.attributes["ds"].let { Objects.toString(it) },
         department = authentication.attributes["dep"].let { Objects.toString(it) }
      )

   override fun myId(): Long = id
   override fun myDataset(): String = dataset
   override fun myEmployeeType(): String = employeeType
   override fun myStoreNumber(): Int = storeNumber
   override fun myEmployeeNumber(): Int = employeeNumber
   override fun myDepartment(): String? = department
}
