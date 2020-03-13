package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.location.Location

interface User {
   fun myId(): Long
   fun myCompany(): Company
   fun myDepartment(): Department?
   fun myLocation(): Location
   fun myEmployeeType(): String
   fun myEmployeeNumber(): Int
   fun myAlternativeStoreIndicator(): String
   fun myAlternativeArea(): Int
}
