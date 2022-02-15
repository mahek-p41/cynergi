package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomainEntity
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

sealed interface AreaType : TypeDomainEntity<AreaType> {
   val id: Int
   val value: String
   val description: String
   val localizationCode: String
   val enabled: Boolean
   val menus: MutableList<MenuType>

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

@MappedEntity("area_type_domain")
open class AreaTypeEntity(

   @field:Id
   @field:GeneratedValue
   override val id: Int,
   override val value: String,
   override val description: String,
   override val localizationCode: String,
   override val enabled: Boolean,
   override val menus: MutableList<MenuType> = mutableListOf(),
) : AreaType {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun equals(other: Any?): Boolean {
      return if (other is AreaType) {
         basicEquality(other)
      } else {
         false
      }
   }

   override fun hashCode(): Int {
      return basicHashCode()
   }
}

object AccountPayable : AreaTypeEntity(1, "AP", "Account Payable", "account.payable", false)
object BankReconciliation : AreaTypeEntity(2, "BR", "Bank Reconciliation", "bank.reconciliation", false)
object GeneralLedger : AreaTypeEntity(3, "GL", "General Ledger", "general.ledger", false)
object PurchaseOrder : AreaTypeEntity(4, "PO", "Purchase Order", "purchase.order", false)
object DarwillUpload : AreaTypeEntity(5, "DARWILL", "Darwill Upload", "darwill.upload", false)

typealias ACCOUNT_PAYABLE = AccountPayable
typealias BANK_REC = BankReconciliation
typealias GENERAL_LEDGER = GeneralLedger
typealias PURCHASE_ORDER = PurchaseOrder
typealias DARWILL_UPLOAD = DarwillUpload
