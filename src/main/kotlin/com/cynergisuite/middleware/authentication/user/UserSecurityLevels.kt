package com.cynergisuite.middleware.authentication.user

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "UserSecurityLevels", description = "Defines the security levels a user has for the various modules")
data class UserSecurityLevels(

   @field:Schema(name = "accountPayableLevel", description = "Employee's account payable security level")
   var accountPayableLevel: Int? = null,

   @field:Schema(name = "purchaseOrderLevel", description = "Employee's purchase order security level")
   var purchaseOrderLevel: Int? = null,

   @field:Schema(name = "generalLedgerLevel", description = "Employee's general ledger security level")
   var generalLedgerLevel: Int? = null,

   @field:Schema(name = "systemAdministrationLevel", description = "Employee's system administration security level")
   var systemAdministrationLevel: Int? = null,

   @field:Schema(name = "fileMaintenanceLevel", description = "Employee's file maintenance security level")
   var fileMaintenanceLevel: Int? = null,

   @field:Schema(name = "bankReconciliationLevel", description = "Employee's bank reconciliation security level")
   var bankReconciliationLevel: Int? = null
) {
   constructor(isCynergiAdmin: Boolean) : this(
      accountPayableLevel = if (isCynergiAdmin) 99 else 0,
      purchaseOrderLevel = if (isCynergiAdmin) 99 else 0,
      generalLedgerLevel = if (isCynergiAdmin) 99 else 0,
      systemAdministrationLevel = if (isCynergiAdmin) 99 else 0,
      fileMaintenanceLevel = if (isCynergiAdmin) 99 else 0,
      bankReconciliationLevel = if (isCynergiAdmin) 99 else 0
   )
}
