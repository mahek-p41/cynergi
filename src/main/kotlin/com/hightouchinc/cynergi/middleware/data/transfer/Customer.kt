package com.hightouchinc.cynergi.middleware.data.transfer

import com.hightouchinc.cynergi.middleware.data.domain.DataTransferObject
import java.time.LocalDate

@DataTransferObject
data class Customer(

   val id: Long? = null,

   val account: String,

   val firstName: String,

   val lastName: String,

   val contactName: String,

   val dateOfBirth: LocalDate,

   val taxNumber: String,

   val allowOlp: Boolean = false,

   val allowRecur: Boolean = false,

   val cellOptIn: Boolean = false,

   val cellPin: String? = null
) {
   constructor(account: String, firstName: String, lastName: String, contactName: String, dateOfBirth: LocalDate, taxNumber: String, allowOlp: Boolean, allowRecur: Boolean, cellOptIn: Boolean):
      this(id = null, account = account, firstName = firstName, lastName = lastName, contactName = contactName, dateOfBirth = dateOfBirth, taxNumber = taxNumber, allowOlp = allowOlp, allowRecur = allowRecur, cellOptIn = cellOptIn)
}
