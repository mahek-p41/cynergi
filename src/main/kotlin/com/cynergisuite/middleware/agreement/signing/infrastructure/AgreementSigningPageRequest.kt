package com.cynergisuite.middleware.agreement.signing.infrastructure

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.ValidPageSortBy
import com.cynergisuite.middleware.store.StoreEntity
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import javax.validation.constraints.Positive

@Schema(
   name = "AgreementSigningPageRequest",
   title = "Specialized paging for Agreement Signing listing requests",
   description = "Defines the parameters available to for a paging request to the agreementSigning-fetchAll endpoint. Example to come",
   allOf = [PageRequestBase::class]
)
@Introspected
class AgreementSigningPageRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Positive
   @field:Schema(name = "storeNumber", minimum = "1", description = "The Store Number to filter results with, if not provided then the login store will be used")
   var storeNumber: Int? = null,

   @field:Schema(name = "primaryCustomerNumber", minimum = "1", description = "The Customer Number to filter results with")
   var primaryCustomerNumber: Int? = null,

   @field:Schema(name = "agreementNumber", minimum = "1", description = "The Agreement Number to filter results with")
   var agreementNumber: Int? = null

) : PageRequestBase<AgreementSigningPageRequest>(page, size, sortBy, sortDirection) {

   constructor(pageRequest: AgreementSigningPageRequest, store: StoreEntity) :
      this(
         page = pageRequest.page(),
         size = pageRequest.size(),
         sortBy = pageRequest.sortBy(),
         sortDirection = pageRequest.sortDirection(),
         storeNumber = store.myNumber(),
         primaryCustomerNumber = pageRequest.primaryCustomerNumber,
         agreementNumber = pageRequest.agreementNumber
      )

   @ValidPageSortBy("id")
   override fun sortByMe(): String = sortBy()

   override fun equals(other: Any?): Boolean =
      if (other is AgreementSigningPageRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.storeNumber, other.storeNumber)
            .append(this.primaryCustomerNumber, other.primaryCustomerNumber)
            .append(this.agreementNumber, other.agreementNumber)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.storeNumber)
         .append(this.primaryCustomerNumber)
         .append(this.agreementNumber)
         .toHashCode()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AgreementSigningPageRequest =
      AgreementSigningPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         storeNumber = this.storeNumber,
         primaryCustomerNumber = this.primaryCustomerNumber,
         agreementNumber = this.agreementNumber
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "storeNumber" to storeNumber,
         "primaryCustomerNumber" to primaryCustomerNumber,
         "agreementNumber" to agreementNumber
      )
}
