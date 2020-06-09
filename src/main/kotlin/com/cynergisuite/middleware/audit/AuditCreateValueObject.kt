package com.cynergisuite.middleware.audit

import com.cynergisuite.middleware.store.StoreDTO
import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "AuditCreate", title = "Create an audit for a store", description = "How to create a new audit for a single store")
data class AuditCreateValueObject(

   @field:Schema(name = "store", required = false, description = "Store the audit is associated with")
   var store: StoreDTO? = null

)
