package com.cynergisuite.middleware.department

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company

interface Department : Identifiable {
   fun myCode(): String
   fun myCompany(): Company
}
