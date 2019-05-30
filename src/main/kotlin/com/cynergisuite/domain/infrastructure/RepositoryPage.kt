package com.cynergisuite.domain.infrastructure

import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.domain.IdentifiableValueObject
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest

data class RepositoryPage<ENTITY: IdentifiableEntity>(
   val elements: List<ENTITY>,
   val totalElements: Long
) {

   fun <VO: IdentifiableValueObject> toPage(requested: PageRequest, elementTransformer: (e: ENTITY) -> VO): Page<VO> {
      val transformedElements = elements.map { elementTransformer(it) }

      return Page(
         elements = transformedElements,
         totalElements = this.totalElements,
         requested = requested
      )
   }
}
