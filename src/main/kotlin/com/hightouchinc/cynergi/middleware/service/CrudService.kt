package com.hightouchinc.cynergi.middleware.service

interface CrudService<DTO> {
   fun findById(id: Long): DTO?

   fun exists(id: Long): Boolean

   fun save(dto: DTO): DTO

   fun update(dto: DTO): DTO
}
