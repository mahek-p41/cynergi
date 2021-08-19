package com.cynergisuite.middleware.company

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.util.UUID

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
) : Comparable<CompanyEntity> {

   fun copyMeWithNewDatasetCode(datasetCode: String) = copy(datasetCode = datasetCode)

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

   override fun compareTo(other: CompanyEntity): Int =
      CompareToBuilder()
         .append(this.id, other.id)
         .append(this.clientCode, other.clientCode)
         .append(this.clientId, other.clientId)
         .append(this.datasetCode, other.datasetCode)
         .append(this.name, other.name)
         .append(this.doingBusinessAs, other.doingBusinessAs)
         .append(this.federalIdNumber, other.federalIdNumber)
         .toComparison()
}
