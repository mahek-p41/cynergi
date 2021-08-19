package com.cynergisuite.middleware.audit.detail.scan.area

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreEntity
import java.util.UUID

data class AuditScanAreaEntity(
   val id: UUID? = null,
   val name: String? = null,
   val store: StoreEntity? = null,
   val company: CompanyEntity
) : Identifiable {

   override fun myId(): UUID? = id!!

   constructor(id: UUID? = null, dto: AuditScanAreaDTO, company: CompanyEntity, store: StoreEntity) :
      this (
         id = id,
         name = dto.name,
         store = store,
         company = company
      )
}
