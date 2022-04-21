package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomain
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

sealed class AreaType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,
) : TypeDomain() {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

object Unknown : AreaType(-1, "UNK", "Unknown Area", "unknown.area")
object AccountPayable : AreaType(1, "AP", "Account Payable", "account.payable")
object BankReconciliation : AreaType(2, "BR", "Bank Reconciliation", "bank.reconciliation")
object GeneralLedger : AreaType(3, "GL", "General Ledger", "general.ledger")
object PurchaseOrder : AreaType(4, "PO", "Purchase Order", "purchase.order")
object DarwillUpload : AreaType(5, "DARWILL", "Darwill Upload", "darwill.upload")

@MappedEntity("area_type_domain")
class AreaTypeEntity(

   @field:Id
   @field:GeneratedValue
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,

) : TypeDomain() {

   constructor(areaType: AreaType) :
      this(
         id = areaType.id,
         value = areaType.value,
         description = areaType.description,
         localizationCode = areaType.localizationCode,
      )

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}

fun AreaTypeEntity.toAreaType(): AreaType =
   when (this.id) {
      1 -> AccountPayable
      2 -> BankReconciliation
      3 -> GeneralLedger
      4 -> PurchaseOrder
      5 -> DarwillUpload
      else -> Unknown
   }

fun AreaType.toAreaTypeEntity(): AreaTypeEntity =
   AreaTypeEntity(this)
