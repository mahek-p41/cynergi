package com.cynergisuite.middleware.company

import com.cynergisuite.domain.Identifiable

interface Company : Identifiable, Comparable<Company> {
   fun myClientCode(): String
   fun myClientId(): Int
   fun myDataset(): String
}
