package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.domain.Page
import com.hightouchinc.cynergi.middleware.entity.Customer
import com.hightouchinc.cynergi.middleware.entity.CustomerDto
import com.hightouchinc.cynergi.middleware.repository.CustomerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerCrudService @Inject constructor(
   private val customerRepository: CustomerRepository
): CrudService<CustomerDto> {
   override fun findById(id: Long): CustomerDto? =
      customerRepository.fetchOne(id = id)?.let { CustomerDto(customer = it) }

   override fun save(dto: CustomerDto): CustomerDto =
      CustomerDto(customerRepository.save(entity = Customer(dto = dto)))

   override fun update(dto: CustomerDto): CustomerDto {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   fun save(customers: Collection<Customer>): Collection<Customer> =
      customerRepository.save(customers = customers)

   fun searchForCustomers(customerSearchString: String): Page<Customer> {
      return customerRepository.searchForCustomers(customerSearchTokens = customerSearchString.split(" "))
   }
}
