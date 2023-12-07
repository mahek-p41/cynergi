package com.cynergisuite.middleware.wow

import com.cynergisuite.middleware.company.CompanyEntity

class WowSingleAgreementEntity(
   val company: CompanyEntity,
   val storeNumber: Int, // FIXME Convert to using Store when data is in cynergidb and not coming from Fastinfo
   val customerNumber: String,
   val firstName: String?,
   val lastName: String?,
   val email: String?,
   val agreementNumber: String,
   val product: String?,
   val description: String?,
   val paymentsRemaining: String,
   val cellPhoneNumber: String?,
   val homePhoneNumber: String?
)
