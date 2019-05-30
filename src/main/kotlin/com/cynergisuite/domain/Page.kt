package com.cynergisuite.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS

@ValueObject
@JsonInclude(ALWAYS)
data class Page<VO: IdentifiableValueObject>(
   val elements: List<VO> = emptyList(),
   val requested: PageRequest,
   val totalElements: Long,
   val totalPages: Long = Math.ceil(totalElements.toDouble() / (requested.size ?: 10).toDouble()).toLong(),
   val first: Boolean = requested.page == 0,
   val last: Boolean = requested.page!! < totalPages
)
