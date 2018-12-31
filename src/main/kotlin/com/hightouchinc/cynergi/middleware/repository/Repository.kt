package com.hightouchinc.cynergi.middleware.repository

interface Repository<ENTITY> {
   fun fetchOne(id: Long): ENTITY?
   fun save(entity: ENTITY): ENTITY
}
