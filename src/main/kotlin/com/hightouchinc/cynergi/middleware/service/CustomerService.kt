package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.repository.CustomerRepository
import com.hightouchinc.cynergi.middleware.data.domain.Page
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerService @Inject constructor(
   private val customerRepository: CustomerRepository
): IdentityService<Customer> {

   override fun findById(id: Long): Customer? =
      customerRepository.fetchOne(id = id)

   fun save(customer: Customer): Customer =
      customerRepository.save(entity = customer)

   fun save(customers: Collection<Customer>): Collection<Customer> =
      customerRepository.save(customers = customers)

   fun searchForCustomers(customerSearchString: String): Page<Customer> {
      return customerRepository.searchForCustomers(customerSearchTokens = customerSearchString.split(" "))
   }
}
