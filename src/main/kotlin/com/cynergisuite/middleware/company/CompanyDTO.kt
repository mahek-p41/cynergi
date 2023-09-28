package com.cynergisuite.middleware.company

import com.cynergisuite.middleware.address.AddressDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.CompareToBuilder
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Company", title = "An entity containing a rental company", description = "An entity containing a rental company.", requiredProperties = ["clientId", "name", "clientId", "datasetCode"])
data class CompanyDTO(

   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: UUID? = null,

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

) : Comparable<CompanyDTO> {

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

   override fun compareTo(other: CompanyDTO): Int =
      CompareToBuilder()
         .append(this.id, other.id)
         .append(this.clientCode, other.clientCode)
         .append(this.clientId, other.clientId)
         .append(this.datasetCode, other.datasetCode)
         .append(this.name, other.name)
         .append(this.doingBusinessAs, other.doingBusinessAs)
         .append(this.federalTaxNumber, other.federalTaxNumber)
         .append(this.address, other.address)
         .toComparison()
}
