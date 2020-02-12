package com.cynergisuite.middleware.company

import com.cynergisuite.domain.Identifiable
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime
import java.util.UUID

data class CompanyEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val doingBusinessAs: String? = null,
   val clientCode: String,
   val clientId: Int,
   val datasetCode: String,
   val federalTaxNumber: String? = null
) : Company {

   constructor(company: CompanyValueObject) :
      this(
         id = company.id,
         name = company.name!!,
         doingBusinessAs = company.doingBusinessAs,
         clientCode = company.clientCode!!,
         clientId = company.clientId!!,
         datasetCode = company.datasetCode!!,
         federalTaxNumber = company.federalTaxNumber
      )

   override fun myId(): Long? = id
   override fun myClientCode(): String = clientCode
   override fun myClientId(): Int = clientId
   override fun myDataset(): String = datasetCode

   override fun hashCode(): Int =
      HashCodeBuilder()
         .append(this.id)
         .append(this.name)
         .append(this.doingBusinessAs)
         .append(this.clientCode)
         .append(this.clientId)
         .append(this.datasetCode)
         .append(this.federalTaxNumber)
         .toHashCode()

   override fun equals(other: Any?): Boolean =
      if (other is CompanyEntity) {
         EqualsBuilder()
            .append(this.id, other.id)
            .append(this.name, other.name)
            .append(this.doingBusinessAs, other.doingBusinessAs)
            .append(this.clientCode, other.clientCode)
            .append(this.clientId, other.clientId)
            .append(this.datasetCode, other.datasetCode)
            .append(this.federalTaxNumber, other.federalTaxNumber)
            .isEquals
      } else {
         false
      }

   override fun compareTo(other: Company): Int {
      val compareToBuilder = CompareToBuilder()
         .append(this.id, other.myId())
         .append(this.clientCode, other.myClientCode())
         .append(this.clientId, other.myClientId())
         .append(this.datasetCode, other.myDataset())

      return if (other is CompanyEntity) {
         compareToBuilder
            .append(this.name, other.name)
            .append(this.doingBusinessAs, other.doingBusinessAs)
            .append(this.federalTaxNumber, other.federalTaxNumber)
            .toComparison()
      } else {
         compareToBuilder.toComparison()
      }
   }
}

