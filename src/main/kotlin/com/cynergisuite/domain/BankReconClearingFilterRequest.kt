package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
   name = "BankReconClearingFilterRequest",
   title = "Resulting list for filtering results",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [PageRequestBase::class]
)
class BankReconClearingFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "Bank", description = "Bank Number")
   var bank: Int? = null,

   @field:Schema(name = "status", description = "Bank Reconciliation Status")
   var status: String? = null,

   @field:Schema(name = "bankType", description = "Bank Reconciliation Type Value")
   var bankType: String? = null,

   @field:Schema(name = "fromTransactionDate", description = "From Date for Transaction")
   var fromTransactionDate: LocalDate? = null,

   @field:Schema(name = "thruTransactionDate", description = "Thru Date for Transaction")
   var thruTransactionDate: LocalDate? = null,

   @field:Schema(name = "beginDocNum", description = "Beginning Document Number")
   var beginDocNum: String? = null,

   @field:Schema(name = "endDocNum", description = "Ending Document Number")
   var endDocNum: String? = null,

   @field:Schema(name = "description", description = "Description of documents to be viewed")
   var description: String? = null,

   @field:Schema(name = "amount", description = "Amount")
   var amount: BigDecimal? = null,

   @field:Schema(name = "statementDate", description = "Statement Date")
   var statementDate: LocalDate? = null,

   ) : PageRequestBase<BankReconClearingFilterRequest>(page, size, sortBy, sortDirection) {

      @ValidPageSortBy("id")
      override fun sortByMe(): String = sortBy()

      override fun equals(other: Any?): Boolean =
         if (other is BankReconClearingFilterRequest) {
            EqualsBuilder()
               .appendSuper(super.equals(other))
               .append(this.bank, other.bank)
               .append(this.status, other.status)
               .append(this.bankType, other.bankType)
               .append(this.fromTransactionDate, other.fromTransactionDate)
               .append(this.thruTransactionDate, other.thruTransactionDate)
               .append(this.beginDocNum, other.beginDocNum)
               .append(this.endDocNum, other.endDocNum)
               .append(this.description, other.description)
               .append(this.amount, other.amount)
               .append(this.statementDate, other.statementDate)
               .isEquals
         } else {
            false
         }

      override fun hashCode(): Int =
         HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(this.bank)
            .append(this.status)
            .append(this.bankType)
            .append(this.fromTransactionDate)
            .append(this.thruTransactionDate)
            .append(this.beginDocNum)
            .append(this.endDocNum)
            .append(this.description)
            .append(this.amount)
            .append(this.statementDate)
            .toHashCode()

      override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): BankReconClearingFilterRequest =
         BankReconClearingFilterRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection,
            bank = this.bank,
            status = this.status,
            bankType = this.bankType,
            fromTransactionDate = this.fromTransactionDate,
            thruTransactionDate = this.thruTransactionDate,
            beginDocNum = this.beginDocNum,
            endDocNum = this.endDocNum,
            description = this.description,
            amount = this.amount,
            statementDate = this.statementDate
         )

      override fun myToStringValues(): List<Pair<String, Any?>> =
         listOf(
            "bank" to bank,
            "status" to status,
            "bankType" to bankType,
            "fromTransactionDate" to fromTransactionDate,
            "thruTransactionDate" to thruTransactionDate,
            "beginDocNum" to beginDocNum,
            "endDocNum" to endDocNum,
            "description" to description,
            "amount" to amount,
            "statementDate" to statementDate
         )
}
