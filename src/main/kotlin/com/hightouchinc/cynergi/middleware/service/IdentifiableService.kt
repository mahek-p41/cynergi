package com.hightouchinc.cynergi.middleware.service

interface IdentifiableService<DTO> {
   fun fetchById(id: Long): DTO?
}
