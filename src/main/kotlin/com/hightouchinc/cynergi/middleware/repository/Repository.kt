package com.hightouchinc.cynergi.middleware.repository

interface Repository<ENTITY> {
   fun fetchOne(id: Long): ENTITY?
   fun save(entity: ENTITY): ENTITY
   // TODO fun fetchAll(parent: PARENT, start: Long = 0, pageSize: Long = 10): Page
}
