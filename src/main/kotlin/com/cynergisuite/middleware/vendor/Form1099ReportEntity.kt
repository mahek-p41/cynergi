package com.cynergisuite.middleware.vendor


data class Form1099ReportEntity(
   val companyName: String,
   val companyAddress1: String,
   val companyAddress2: String?,
   val companyCity: String,
   val companyState: String,
   val companyPostalCode: String,
   val companyFederalIdNumber: String? = null,
   val vendors: MutableList<Form1099VendorEntity>?,
   val reportTotals: Form1099TotalsEntity
)
