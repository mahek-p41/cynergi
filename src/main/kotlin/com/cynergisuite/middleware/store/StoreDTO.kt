package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.CompanyValueObject

data class StoreDTO(
   val id: Long,
   val number: Int,
   val name: String,
   val company: CompanyValueObject
): Store {
   override fun myId(): Long  = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
   override fun myCompany(): CompanyValueObject = company

   // Factory object to create StoreDTO from a Store
   companion object Factory {
      fun create(store: Store): StoreDTO? {
         return when (store) {
            is StoreDTO -> StoreDTO(store.id, store.number, store.name, store.company)
            is SimpleStore -> StoreDTO(store.id, store.number, store.name, CompanyValueObject.create(store.company)!!)
            else -> null
         }
      }
   }
}
