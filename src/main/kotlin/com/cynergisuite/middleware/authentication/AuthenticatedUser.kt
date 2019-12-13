package com.cynergisuite.middleware.authentication

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.UserDetails
import java.util.Objects

interface AuthenticatedUser : Identifiable {
   fun myEmployeeType(): String
   fun myStoreNumber(): Int?
   fun myEmployeeNumber(): Int
}
