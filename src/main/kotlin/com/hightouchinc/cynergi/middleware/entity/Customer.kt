package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.domain.DataTransferObject
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.NOT_NULL
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.validation.constraints.NotNull

data class Customer(
   val id: Long? = null,

   val account: String,

   val firstName: String,

   val lastName: String,

   val contactName: String,

   val dateOfBirth: LocalDate
): IdentifiableEntity {
   constructor(account: String, firstName: String, lastName: String, contactName: String, dateOfBirth: Date):
      this(id = null, account = account, firstName = firstName, lastName = lastName, contactName = contactName, dateOfBirth = dateOfBirth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())

   constructor(dto: CustomerDto) :
      this(id = dto.id, account = dto.account!!, firstName = dto.firstName!!, lastName = dto.lastName!!, contactName = dto.contactName!!, dateOfBirth = dto.dateOfBirth!!)

   override fun entityId(): Long? = id
}

@DataTransferObject
data class CustomerDto(
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   var account: String? = null,

   @field:NotNull(message = NOT_NULL)
   var firstName: String? = null,

   @field:NotNull(message = NOT_NULL)
   var lastName: String? = null,

   @field:NotNull(message = NOT_NULL)
   var contactName: String? = null,

   @field:NotNull(message = NOT_NULL)
   var dateOfBirth: LocalDate? = null
) {
   constructor(customer: Customer):
      this(id = customer.id, account = customer.account, firstName = customer.firstName, lastName = customer.lastName, contactName = customer.contactName, dateOfBirth = customer.dateOfBirth)
}
