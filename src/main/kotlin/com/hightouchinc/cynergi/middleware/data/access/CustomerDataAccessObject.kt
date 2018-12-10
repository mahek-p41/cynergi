package com.hightouchinc.cynergi.middleware.data.access

import com.hightouchinc.cynergi.middleware.data.domain.Page
import com.hightouchinc.cynergi.middleware.data.transfer.CustomerDataTransferObject
import org.springframework.jdbc.core.JdbcTemplate
import javax.inject.Singleton

@Singleton
class CustomerDataAccessObject(
   private val jdbcTemplate: JdbcTemplate
){
   fun searchForCustomers(customerSearchString: String): Page<CustomerDataTransferObject> {
      val content = jdbcTemplate.
   }
}
