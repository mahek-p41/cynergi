package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.address.AddressEntity
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.util.UUID

data class CompanyEntity(
   val id: UUID? = null,
   val name: String,
   val doingBusinessAs: String? = null,
   val clientCode: String,
   val clientId: Int,
   val datasetCode: String,
   val federalIdNumber: String? = null,
   val address: AddressEntity? = null
) : Company {

   // Factory object to create CompanyEntity from a Company
   companion object Factory {
      fun create(company: Company): CompanyEntity? {
         return when (company) {
            is CompanyDTO -> CompanyEntity(company)
            is CompanyEntity -> CompanyEntity(company)
            else -> null
         }
      }
   }

   constructor(companyDTO: CompanyEntity) :
      this (
         id = companyDTO.id,
         name = companyDTO.name,
         doingBusinessAs = companyDTO.doingBusinessAs,
         clientCode = companyDTO.clientCode,
         clientId = companyDTO.clientId,
         datasetCode = companyDTO.datasetCode,
         federalIdNumber = companyDTO.federalIdNumber,
         address = companyDTO.address
      )

   constructor(companyDTO: CompanyDTO) :
      this (
         id = companyDTO.id,
         name = companyDTO.name!!,
         doingBusinessAs = companyDTO.doingBusinessAs,
         clientCode = companyDTO.clientCode!!,
         clientId = companyDTO.clientId!!,
         datasetCode = companyDTO.datasetCode!!,
         federalIdNumber = companyDTO.federalTaxNumber,
         address = companyDTO.address?.let { AddressEntity(it) }
      )

   override fun myId(): UUID? = id
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
         .append(this.address)
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
            .append(this.address, other.address)
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
            .append(this.address, other.address)
            .toComparison()
      } else {
         compareToBuilder.toComparison()
      }
   }
}
