package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.data.domain.Page
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import org.springframework.jdbc.core.JdbcTemplate
import javax.inject.Singleton

@Singleton
class CustomerDataAccessObject(
   private val jdbcTemplate: JdbcTemplate
){
   fun searchForCustomers(customerSearchString: String): Page<Customer> {
      /*val content = jdbcTemplate.query("") { rs: ResultSet ->

      }*/

      return Page(listOf(), 0, 1, false, false)
   }
}
