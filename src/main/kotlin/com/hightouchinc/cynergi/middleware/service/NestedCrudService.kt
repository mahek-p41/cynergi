package com.hightouchinc.cynergi.middleware.service

interface NestedCrudService<DTO, PARENT> {
   fun fetchById(id: Long): DTO?

   fun exists(id: Long): Boolean

   fun create(dto: DTO, parent: PARENT): DTO

   fun update(dto: DTO, parent: PARENT): DTO
}
