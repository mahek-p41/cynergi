package com.hightouchinc.cynergi.middleware.data.access

interface IdentityDataAccess<T> {
   fun fetchOne(id: Long): T?
}
