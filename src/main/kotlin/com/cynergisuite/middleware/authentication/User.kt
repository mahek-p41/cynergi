package com.cynergisuite.middleware.authentication

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity

interface User : AuthenticatedUser {
   fun myFirstNameMi(): String?
   fun myLastName(): String
   fun myPassCode(): String
   fun myStore(): Store?
   fun myDepartment(): String?
   fun amIActive(): Boolean
   fun doesAllowAutoStoreAssign(): Boolean
}
