package com.hightouchinc.cynergi.middleware.service

interface CrudService<DTO> {
   fun findById(id: Long): DTO?

   fun save(dto: DTO): DTO

   fun update(dto: DTO): DTO
}
