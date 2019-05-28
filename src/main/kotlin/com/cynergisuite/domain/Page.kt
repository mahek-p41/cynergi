package com.cynergisuite.domain

@ValueObject
data class Page<T: IdentifiableValueObject>(
   var elements: List<T>,
   var requested: PageRequest,
   var empty: Boolean = elements.isEmpty()
) {
   constructor(elements: List<T>, pageRequest: PageRequest) :
      this (
         elements = elements,
         requested = pageRequest
      )
}


