package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.StoreEntity

data class AuditScanAreaEntity(
   val id: Long? = null,
   val name: String? = null,
   val store: StoreEntity? = null,
   val company: Company
) : Identifiable {

   override fun myId(): Long = this.id!!

   constructor(dto: AuditScanAreaDTO, company: Company, store: StoreEntity) :
      this (
         id = dto.id,
         name = dto.name,
         store = store,
         company = company
      )
}
