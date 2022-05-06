package com.cynergisuite.middleware.darwill

import com.cynergisuite.middleware.company.CompanyEntity
import java.math.BigInteger

data class DarwillActiveCustomerEntity(
   val company: CompanyEntity,
   val storeId: Int, // FIXME Convert to using Store when data is in cynergidb and not coming from Fastinfo
   val peopleId: String?,
   val uniqueId: BigInteger,
   val firstName: String?,
   val lastName: String?,
   val address1: String?,
   val address2: String?,
   val city: String?,
   val state: String?,
   val zip: String?,
   val cellPhoneNumber: String?,
   val homePhoneNumber: String?,
   val email: String?,

   val agreementId: String,
   val paymentFrequency: String,
   val textOptIn: String,
   val onlineIndicator: String,
   val carePlus: String,
   val projectedPayout: Int,
   val paymentsLeftInWeeks: Int,
   val pastDue: String,
   val daysPastDue: Int
)
