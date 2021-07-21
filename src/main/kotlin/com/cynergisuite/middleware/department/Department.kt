package com.cynergisuite.middleware.department

import com.cynergisuite.domain.LegacyIdentifiable
import com.cynergisuite.middleware.company.Company

interface Department : LegacyIdentifiable {
   fun myCode(): String
   fun myCompany(): Company
}
