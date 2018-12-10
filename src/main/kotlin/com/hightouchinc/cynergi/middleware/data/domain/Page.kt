package com.hightouchinc.cynergi.middleware.data.domain

data class Page<T>(
   val content: List<T>,
   val totalElements: Int,
   val pageNumber: Int,
   val first: Boolean,
   val last: Boolean
)
