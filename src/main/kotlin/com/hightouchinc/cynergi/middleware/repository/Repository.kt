package com.hightouchinc.cynergi.middleware.repository

interface Repository<ENTITY> {
   fun fetchOne(id: Long): ENTITY?
   // TODO fun fetchAll(parent: PARENT, start: Long = 0, pageSize: Long = 10): Page

   fun exists(id: Long): Boolean

   fun save(entity: ENTITY): ENTITY

   fun update(entity: ENTITY): ENTITY
}
