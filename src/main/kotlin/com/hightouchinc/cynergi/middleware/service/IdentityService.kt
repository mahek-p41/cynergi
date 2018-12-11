package com.hightouchinc.cynergi.middleware.service

interface IdentityService<T> {
   fun findById(id: Long): T?
}
