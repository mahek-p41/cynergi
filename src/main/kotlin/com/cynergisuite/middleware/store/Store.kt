package com.cynergisuite.middleware.store

import com.cynergisuite.domain.Identifiable

interface Store: Identifiable {
   fun myNumber(): Int
   fun myName(): String
   fun myDataset(): String
}
