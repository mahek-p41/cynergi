package com.cynergisuite.domain

interface TypeDomainService<ENTITY> {
   fun exists(value: String): Boolean

   fun fetchByValue(value: String): ENTITY?

   fun fetchAll(): List<ENTITY>
}
