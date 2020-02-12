package com.cynergisuite.middleware.department

import com.cynergisuite.domain.Identifiable

interface Department: Identifiable {
   fun myCode(): String
}
