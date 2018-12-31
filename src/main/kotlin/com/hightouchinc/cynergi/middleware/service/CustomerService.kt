package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.data.access.CustomerDataAccessObject
import com.hightouchinc.cynergi.middleware.data.domain.Page
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerService @Inject constructor(
   private val customerDataAccessObject: CustomerDataAccessObject
): IdentityService<Customer> {

   override fun findById(id: Long): Customer? =
      customerDataAccessObject.fetchOne(id = id)

   fun save(customer: Customer): Customer =
      customerDataAccessObject.save(t = customer)

   fun save(customers: Collection<Customer>): Collection<Customer> =
      customerDataAccessObject.save(customers = customers)

   fun searchForCustomers(customerSearchString: String): Page<Customer> {
      return customerDataAccessObject.searchForCustomers(customerSearchTokens = customerSearchString.split(" "))
   }
}
