package com.cynergisuite.middleware.area

import com.cynergisuite.extensions.orElseNull
import com.cynergisuite.middleware.area.infrastructure.AreaTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AreaTypeService @Inject constructor(
   private val areaTypeRepository: AreaTypeRepository,
) {
   fun findById(id: Int): AreaTypeEntity? {
      return areaTypeRepository.findById(id).orElseNull()
   }
}
