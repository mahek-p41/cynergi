package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.entity.Area
import com.hightouchinc.cynergi.middleware.entity.AreaDto
import com.hightouchinc.cynergi.middleware.repository.AreaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AreaService @Inject constructor(
   private val areaRepository: AreaRepository
) : IdentifiableService<AreaDto> {
   override fun fetchById(id: Long): AreaDto? =
      areaRepository.findOne(id = id)?.let { AreaDto(entity = it) }

   fun exists(id: Long): Boolean =
      areaRepository.exists(id = id)

   fun create(dto: AreaDto): AreaDto =
      AreaDto(
         entity = areaRepository.insert(entity = Area(dto = dto))
      )

   fun update(dto: AreaDto): AreaDto =
      AreaDto(
         entity = areaRepository.update(entity = Area(dto = dto))
      )
}
