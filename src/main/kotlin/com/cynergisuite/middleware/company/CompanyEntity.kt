package com.cynergisuite.middleware.company

import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

data class CompanyEntity(
   val id: Long? = null,
   val name: String,
   val doingBusinessAs: String? = null,
   val clientCode: String,
   val clientId: Int,
   val datasetCode: String,
   val federalIdNumber: String? = null
) : Company {

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
         .append(this.federalIdNumber)
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
            .append(this.federalIdNumber, other.federalIdNumber)
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
            .append(this.federalIdNumber, other.federalIdNumber)
            .toComparison()
      } else {
         compareToBuilder.toComparison()
      }
   }

   fun toValueObject(): CompanyValueObject {
      return CompanyValueObject(
         id = this.id,
         name = this.name,
         doingBusinessAs = this.doingBusinessAs,
         clientCode = this.clientCode,
         clientId = this.clientId,
         datasetCode = this.datasetCode,
         federalTaxNumber = this.federalIdNumber
      )
   }
}

