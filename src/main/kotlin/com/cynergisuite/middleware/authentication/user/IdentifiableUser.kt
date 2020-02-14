package com.cynergisuite.middleware.authentication.user

interface IdentifiableUser {
   fun myId(): Long
   fun myEmployeeType(): String
   fun myEmployeeNumber(): Int
}
