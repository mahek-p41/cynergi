package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomainEntity
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_MANY

sealed interface AreaType : TypeDomainEntity<AreaType> {
   val id: Int
   val value: String
   val description: String
   val localizationCode: String
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

   @Relation(ONE_TO_MANY)
   override val menus: MutableList<MenuType> = mutableListOf(),
) : AreaType {
   constructor(areaType: AreaType) :
      this(
         id = areaType.id,
         value = areaType.value,
         description = areaType.description,
         localizationCode = areaType.localizationCode,
         menus = areaType.menus,
      )

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

fun AreaType.convertAreaTypeToEntity(): AreaTypeEntity =
   if (this is AreaTypeEntity) this else AreaTypeEntity(this)

object AccountPayable : AreaTypeEntity(1, "AP", "Account Payable", "account.payable")
object BankReconciliation : AreaTypeEntity(2, "BR", "Bank Reconciliation", "bank.reconciliation")
object GeneralLedger : AreaTypeEntity(3, "GL", "General Ledger", "general.ledger")
object PurchaseOrder : AreaTypeEntity(4, "PO", "Purchase Order", "purchase.order")
object DarwillUpload : AreaTypeEntity(5, "DARWILL", "Darwill Upload", "darwill.upload")

typealias ACCOUNT_PAYABLE = AccountPayable
typealias BANK_REC = BankReconciliation
typealias GENERAL_LEDGER = GeneralLedger
typealias PURCHASE_ORDER = PurchaseOrder
typealias DARWILL_UPLOAD = DarwillUpload
