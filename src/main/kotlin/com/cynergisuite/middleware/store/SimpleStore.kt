package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyValueObject

data class SimpleStore(
   val id: Long,
   val number: Int,
   val name: String,
   val company: Company
): Store {
   constructor(entity: StoreEntity, company: Company) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name,
         company = CompanyValueObject.create(company)!!
      )

   override fun myId(): Long  = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
   override fun myCompany(): Company = company

   // Factory object to create SimpleStore from a Store
   companion object Factory {
      fun create(store: Store): SimpleStore? {
         return when (store) {
            is StoreEntity -> SimpleStore(store.id, store.number, store.name, CompanyValueObject.create(store.myCompany())!!)
            is SimpleStore -> SimpleStore(store.id, store.number, store.name, CompanyValueObject.create(store.company)!!)
            else -> null
         }
      }
   }
}
