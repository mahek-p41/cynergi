package com.cynergisuite.middleware.authentication

import com.cynergisuite.domain.Identifiable

interface AuthenticatedUser : Identifiable {
   fun myDataset(): String
   fun myEmployeeType(): String
   fun myStoreNumber(): Int?
   fun myEmployeeNumber(): Int
}
