package com.hightouchinc.cynergi.middleware.data.access

import io.reactiverse.reactivex.pgclient.PgPool

class CustomerDataAccessObject(
   private val pgPool: PgPool
){
   fun searchForCustomers(customerSearchString: String) {

   }
}
