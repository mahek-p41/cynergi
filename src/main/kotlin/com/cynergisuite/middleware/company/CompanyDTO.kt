package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.address.AddressDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.CompareToBuilder
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Company", title = "An entity containing a rental company", description = "An entity containing a rental company.", requiredProperties = ["clientId", "name", "clientId", "datasetCode"])
data class CompanyDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", required = false, nullable = false, description = "Human readable name for a company")
   var name: String? = null,

   @field:Size(min = 1, max = 100)
   @field:Schema(name = "doingBusinessAs", required = false, nullable = true, description = "Doing business as")
   var doingBusinessAs: String? = null,

   @field:NotNull
   @field:Size(min = 1, max = 6)
   @field:Schema(name = "clientCode", required = true, nullable = false, description = "Six digit company code")
   var clientCode: String? = null,

   @field:Positive
   @field:NotNull
   @field:Schema(name = "clientId", minimum = "1000", required = true, nullable = false, description = "Unique host company number, HT wide")
   var clientId: Int? = null,

   @field:NotNull
   @field:Size(min = 1, max = 6)
   @field:Schema(name = "datasetCode", required = false, nullable = false, description = "Dataset that this company belongs to")
   var datasetCode: String? = null,

   @field:Schema(name = "federalTaxNumber", required = true, nullable = false, description = "Federal taxpayer identification number")
   var federalTaxNumber: String? = null,

   @field:Schema(name = "address", required = false, nullable = true, description = "Address")
   var address: AddressDTO? = null

) : Company {
   // Factory object to create CompanyDTO from a Company
   companion object Factory {
      fun create(company: Company): CompanyDTO? {
         return when (company) {
            is CompanyDTO -> CompanyDTO(company)
            is CompanyEntity -> CompanyDTO(company)
            else -> null
         }
      }
   }

   constructor(company: CompanyDTO) :
      this(
         id = company.id,
         name = company.name,
         doingBusinessAs = company.doingBusinessAs,
         clientCode = company.clientCode,
         clientId = company.clientId,
         datasetCode = company.datasetCode,
         federalTaxNumber = company.federalTaxNumber,
         address = company.address
      )

   constructor(company: CompanyEntity) :
      this(
         id = company.id,
         name = company.name,
         doingBusinessAs = company.doingBusinessAs,
         clientCode = company.clientCode,
         clientId = company.clientId,
         datasetCode = company.datasetCode,
         federalTaxNumber = company.federalIdNumber,
         address = company.address?.let { AddressDTO(it) }
      )

   constructor(company: Company, federalTaxNumberOverride: String? = null) :
      this(
         id = company.myId(),
         name = if (company is CompanyEntity) company.name else null,
         doingBusinessAs = if (company is CompanyEntity) company.doingBusinessAs else null,
         clientCode = company.myClientCode(),
         clientId = company.myClientId(),
         datasetCode = company.myDataset(),
         federalTaxNumber = federalTaxNumberOverride,
         address = if (company is CompanyEntity && company.address != null) AddressDTO(company.address) else null
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
            .append(this.address, other.address)
            .toComparison()
      } else {
         compareToBuilder.toComparison()
      }
   }
}
