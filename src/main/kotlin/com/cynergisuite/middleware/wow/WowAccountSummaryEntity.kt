package com.cynergisuite.middleware.wow

import com.cynergisuite.middleware.company.CompanyEntity
import java.time.LocalDate

class WowAccountSummaryEntity(
   val company: CompanyEntity,
   val storeNumber: Int, // FIXME Convert to using Store when data is in cynergidb and not coming from Fastinfo
   val customerNumber: String,
   val firstName: String?,
   val lastName: String?,
   val email: String?,
   val agreementNumber: String,
   val dateRented: LocalDate?,
   val dueDate: LocalDate?,
   val percentOwnership: String? = null,
   val product: String?,
   val terms: String,
   val nextPaymentAmount: String,
   val address1: String?,
   val address2: String?,
   val city: String?,
   val state: String?,
   val zip: String?,
   val paymentsRemaining: String,
   val projectedPayoutDate: LocalDate?,
   val weeksRemaining: Int,
   val monthsRemaining: Int,
   val pastDue: String?,
   val overdueAmount: String?,
   val clubMember: String?,
   val clubNumber: String?,
   val clubFee: String?,
   val autopay: String?,
   val paymentTerms: String?,
   val cellPhoneNumber: String?,
   val homePhoneNumber: String?
)
