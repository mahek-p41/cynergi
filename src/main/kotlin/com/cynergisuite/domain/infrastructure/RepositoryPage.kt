package com.cynergisuite.domain.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest

data class RepositoryPage<ENTITY, REQUESTED : PageRequest>(
   val elements: List<ENTITY>,
   val totalElements: Long,
   val requested: REQUESTED
) {

   fun <VO> toPage(elementTransformer: (e: ENTITY) -> VO): Page<VO> {
      val transformedElements = elements.map { elementTransformer(it) }

      return Page(
         elements = transformedElements,
         totalElements = this.totalElements,
         requested = requested.copyFillInDefaults()
      )
   }
}
