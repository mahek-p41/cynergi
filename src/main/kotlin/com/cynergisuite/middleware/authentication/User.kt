package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.store.Store

interface User : AuthenticatedUser {
   fun myFirstNameMi(): String?
   fun myLastName(): String
   fun myPassCode(): String
   fun myStore(): Store?
   fun myDepartment(): String?
   fun amIActive(): Boolean
   fun doesAllowAutoStoreAssign(): Boolean
}
