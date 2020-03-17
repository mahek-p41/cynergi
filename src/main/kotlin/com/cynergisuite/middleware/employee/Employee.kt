package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.Identifiable

interface Employee: Identifiable {
   fun myNumber(): Int
   fun copyMe(): Employee
}
