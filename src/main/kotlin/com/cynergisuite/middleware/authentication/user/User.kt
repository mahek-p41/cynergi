package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.location.Location

interface User : IdentifiableUser {
   fun myCompany(): Company
   fun myDepartment(): Department?
   fun myLocation(): Location
}
