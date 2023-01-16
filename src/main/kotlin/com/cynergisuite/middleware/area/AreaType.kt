package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomain
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.awt.geom.Area

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
object SignatureCapture : AreaType(6, "SIGNATURE_CAPTURE", "Online Signature Capture", "signature.capture")
object WowUpload : AreaType(7, "WOW", "Wow Upload", "wow.upload")

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
      6 -> SignatureCapture
      7 -> WowUpload
      else -> Unknown
   }

fun AreaType.toAreaTypeEntity(): AreaTypeEntity =
   AreaTypeEntity(this)

fun AreaType.toAreaEntity(company: CompanyEntity) =
   AreaEntity(
      areaType = this.toAreaTypeEntity(),
      company = company
   )

fun findAreaType(area: String): AreaType =
   when (area.uppercase().trim()) {
      "AP" -> AccountPayable
      "BR" -> BankReconciliation
      "GL" -> GeneralLedger
      "PO" -> PurchaseOrder
      "DARWILL" -> DarwillUpload
      "SIGNATURE_CAPTURE" -> SignatureCapture
      else -> Unknown
   }
