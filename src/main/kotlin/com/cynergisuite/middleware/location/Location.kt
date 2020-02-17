package com.cynergisuite.middleware.location

import com.cynergisuite.domain.Identifiable

interface Location: Identifiable {
   fun myNumber(): Int
   fun myName(): String
   fun myDataset(): String
}
