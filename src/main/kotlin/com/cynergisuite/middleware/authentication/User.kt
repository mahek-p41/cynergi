package com.cynergisuite.middleware.authentication

import com.cynergisuite.middleware.location.Location

interface User : AuthenticatedUser {
   fun myFirstNameMi(): String?
   fun myLastName(): String
   fun myPassCode(): String
   fun myLocation(): Location?
   fun myDepartment(): String?
   fun amIActive(): Boolean
   fun doesAllowAutoStoreAssign(): Boolean
}
