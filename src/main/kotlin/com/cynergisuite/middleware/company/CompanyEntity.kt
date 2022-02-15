package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.address.AddressEntity
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_ONE
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.util.UUID

@Introspected
@MappedEntity("company")
data class CompanyEntity(

   @field:Id
   @field:GeneratedValue
   val id: UUID? = null,
   val name: String,
   val doingBusinessAs: String? = null,
   val clientCode: String,
   val clientId: Int,
   val datasetCode: String,
   val federalIdNumber: String? = null,

   @Relation(ONE_TO_ONE)
   val address: AddressEntity? = null

) : Comparable<CompanyEntity> {

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

   fun copyMeWithNewDatasetCode(datasetCode: String) = copy(datasetCode = datasetCode)
   fun copyMeWithNewAddress(address: AddressEntity) = copy(address = address)

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

   override fun compareTo(other: CompanyEntity): Int =
      CompareToBuilder()
         .append(this.id, other.id)
         .append(this.clientCode, other.clientCode)
         .append(this.clientId, other.clientId)
         .append(this.datasetCode, other.datasetCode)
         .append(this.name, other.name)
         .append(this.doingBusinessAs, other.doingBusinessAs)
         .append(this.federalIdNumber, other.federalIdNumber)
         .append(this.address, other.address)
         .toComparison()
}
