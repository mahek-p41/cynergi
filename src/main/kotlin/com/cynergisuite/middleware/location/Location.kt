package com.cynergisuite.middleware.location

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company

interface Location: Identifiable {
   fun myNumber(): Int
   fun myName(): String
   fun myCompany(): Company
}
