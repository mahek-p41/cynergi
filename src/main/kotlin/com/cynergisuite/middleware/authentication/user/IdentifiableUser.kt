package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.domain.Identifiable

interface IdentifiableUser: Identifiable {
   fun myEmployeeType(): String
   fun myEmployeeNumber(): Int
}
