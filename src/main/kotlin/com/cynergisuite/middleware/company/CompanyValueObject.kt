package com.cynergisuite.middleware.company

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.CompareToBuilder
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
@Schema(name = "Company", title = "An entity containing a rental company", description = "An entity containing a rental company.", requiredProperties = ["clientId", "name", "clientId", "datasetCode"])
data class CompanyValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", required = false, nullable = false, description = "Human readable name for a company")
   var name: String? = null,

   @field:Schema(name = "doingBusinessAs", required = false, nullable = true, description = "Doing business as")
   var doingBusinessAs: String? = null,

   @field:NotNull
   @field:Schema(name = "clientCode", required = true, nullable = false, description = "Three digit company code")
   var clientCode: String? = null,

   @field:Positive
   @field:NotNull
   @field:JsonProperty("clientId")
   @field:Schema(name = "clientId", minimum = "1000", required = true, nullable = false, description = "Unique host company number, HT wide")
   var clientId: Int? = null,

   @field:NotNull
   @field:Schema(name = "datasetCode", required = false, nullable = false, description = "Dataset that this company belongs to")
   var datasetCode: String? = null,

   @field:Schema(name = "federalTaxNumber", required = true, nullable = false, description = "Federal taxpayer identification number")
   var federalTaxNumber: String? = null

) : Company {
//   TODO something work like this would be nice
//   constructor(company: Company) {
//      if (company is CompanyValueObject) {
//         CompanyValueObject(company)
//      } else if (company is CompanyEntity) {
//         CompanyValueObject(company)
//      }
//   }

   // Factory object to create CompanyValueObject from a Company
   companion object Factory {
      fun create(company: Company): CompanyValueObject? {
         return when (company) {
             is CompanyValueObject -> CompanyValueObject(company)
             is CompanyEntity -> CompanyValueObject(company)
             else -> null
         }
      }
   }

   constructor(company: CompanyValueObject) :
      this(
         id = company.id,
         name = company.name,
         doingBusinessAs = company.doingBusinessAs,
         clientCode = company.clientCode,
         clientId = company.clientId,
         datasetCode = company.datasetCode,
         federalTaxNumber = company.federalTaxNumber
      )

   constructor(company: CompanyEntity) :
      this(
         id = company.id,
         name = company.name,
         doingBusinessAs = company.doingBusinessAs,
         clientCode = company.clientCode,
         clientId = company.clientId,
         datasetCode = company.datasetCode,
         federalTaxNumber = company.federalIdNumber
      )

   constructor(company: Company, federalTaxNumberOverride: String? = null) :
      this(
         id = company.myId(),
         name = if (company is CompanyEntity) company.name else null,
         doingBusinessAs = if (company is CompanyEntity) company.doingBusinessAs else null,
         clientCode = company.myClientCode(),
         clientId = company.myClientId(),
         datasetCode = company.myDataset(),
         federalTaxNumber = federalTaxNumberOverride
      )

   override fun myClientCode(): String = clientCode!!

   override fun myClientId(): Int = clientId!!

   override fun myDataset(): String = datasetCode!!

   override fun myId(): Long? = id

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
            .append(this.federalTaxNumber, other.federalIdNumber)
            .toComparison()
      } else {
         compareToBuilder.toComparison()
      }
   }

}

