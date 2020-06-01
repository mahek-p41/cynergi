package com.cynergisuite.middleware.location

import com.cynergisuite.middleware.company.Company

interface Location {
   fun myId(): Long
   fun myNumber(): Int
   fun myName(): String
   fun myCompany(): Company?
}
