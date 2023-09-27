package com.cynergisuite.middleware.vendor


data class Form1099ReportEntity(
   val companyName: String,
   val vendors: MutableList<Form1099VendorEntity>?,
   val reportTotals: Form1099TotalsEntity
)
