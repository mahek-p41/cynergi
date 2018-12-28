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

   val dateOfBirth: LocalDate
) {
   constructor(account: String, firstName: String, lastName: String, contactName: String, dateOfBirth: LocalDate):
      this(id = null, account = account, firstName = firstName, lastName = lastName, contactName = contactName, dateOfBirth = dateOfBirth)
}
