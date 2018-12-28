package com.hightouchinc.cynergi.middleware.data.access

interface DataAccessObject<T>: IdentityDataAccess<T> {
   fun save(t: T): T
}
