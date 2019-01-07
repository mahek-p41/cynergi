package com.hightouchinc.cynergi.middleware.service

interface NestedCrudService<DTO, PARENT> {
   fun findById(id: Long): DTO?

   fun exists(id: Long): Boolean

   fun save(dto: DTO, parent: PARENT): DTO

   fun update(dto: DTO, parent: PARENT): DTO
}
