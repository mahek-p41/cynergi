package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.LegacyIdentifiable

interface Employee : LegacyIdentifiable {
   fun myNumber(): Int
   fun copyMe(): Employee
}
