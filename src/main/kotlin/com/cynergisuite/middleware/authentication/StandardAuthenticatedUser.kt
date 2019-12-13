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
   val employeeNumber: Int
): AuthenticatedUser, UserDetails(employeeNumber.toString(), mutableListOf()) {

   constructor(employee: EmployeeEntity, overrideStore: StoreEntity) :
      this(
         id = employee.id!!,
         employeeType = employee.type,
         storeNumber = overrideStore.number,
         employeeNumber = employee.number
      )

   constructor(authentication: Authentication) :
      this(
         id = authentication.attributes.get("id").let { Objects.toString(it).toLong() },
         employeeType = authentication.attributes.get("type").let { Objects.toString(it) },
         storeNumber = authentication.attributes.get("stNum").let { Objects.toString(it).toInt() },
         employeeNumber = authentication.attributes.get("sub").let { Objects.toString(it).toInt() } // sub is a subject which is encoded by the framework
      )

   override fun myId(): Long = id
   override fun myEmployeeType(): String = employeeType
   override fun myStoreNumber(): Int = storeNumber
   override fun myEmployeeNumber(): Int = employeeNumber
}
