package com.cynergisuite.middleware.wow

import com.cynergisuite.middleware.company.CompanyEntity
import java.time.LocalDate

class WowFinalPaymentEntity(
   val company: CompanyEntity,
   val storeNumber: Int, // FIXME Convert to using Store when data is in cynergidb and not coming from Fastinfo
   val customerNumber: String,
   val firstName: String?,
   val lastName: String?,
   val email: String?,
   val agreementNumber: String,
   val product: String?,
   val payoutDate: LocalDate?
)
