package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.data.access.CustomerDataAccessObject
import com.hightouchinc.cynergi.middleware.data.transfer.CustomerDataTransferObject
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.reactivex.Observable

@Controller("/api/v1/customers")
class CustomerController(
   private val customerDao: CustomerDataAccessObject
) {

   @Get("/search/{searchString}")
   fun search(@Parameter("searchString") searchString: String): Observable<CustomerDataTransferObject> {
      customerDao.searchForCustomers(searchString)
      TODO("Query the db")
   }
}
