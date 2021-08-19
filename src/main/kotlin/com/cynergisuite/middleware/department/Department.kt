package com.cynergisuite.middleware.department

import com.cynergisuite.domain.LegacyIdentifiable
import com.cynergisuite.middleware.company.CompanyEntity

interface Department : LegacyIdentifiable {
   fun myCode(): String
   fun myCompany(): CompanyEntity
}
