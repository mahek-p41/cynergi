package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.math.BigDecimal
import java.time.LocalDate

@Schema(
   name = "BankReconciliationTransactionsFilterRequest",
   title = "Bank Reconciliation Transactions Filter Request",
   description = "Filter request for Bank Reconciliation Transactions",
   allOf = [PageRequestBase::class]
)
class BankReconciliationTransactionsFilterRequest(
   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "Bank", description = "Bank Number")
   var bank: Long? = null,

   @field:Schema(name = "bankType", description = "Bank Reconciliation Type Value")
   var bankReconciliationType: String? = null,

   @field:Schema(name = "fromTransactionDate", description = "From transaction date for bank reconciliation transactions")
   var fromTransactionDate: LocalDate? = null,

   @field:Schema(name = "thruTransactionDate", description = "Thru transaction date for bank reconciliation transactions")
   var thruTransactionDate: LocalDate? = null,

   @field:Schema(name = "beginDocNum", description = "Beginning Document Number")
   var beginDocNum: String? = null,

   @field:Schema(name = "endDocNum", description = "Ending Document Number")
   var endDocNum: String? = null,

   @field:Schema(name = "description", description = "Description of documents to be viewed")
   var description: String? = null,

   @field:Schema(name = "status", description = "Bank Reconciliation Status")
   var status: String? = null,

   @field:Schema(name = "fromClearedDate", description = "From cleared date for bank reconciliation transactions")
   var fromClearedDate: LocalDate? = null,

   @field:Schema(name = "thruClearedDate", description = "Thru cleared date for bank reconciliation transactions")
   var thruClearedDate: LocalDate? = null,

   @field:Schema(name = "amount", description = "Amount")
   var amount: BigDecimal? = null,

   ) : PageRequestBase<BankReconciliationTransactionsFilterRequest>(page, size, sortBy, sortDirection) {

      @ValidPageSortBy("id")
      override fun sortByMe(): String = sortBy()

      override fun equals(other: Any?): Boolean =
         if (other is BankReconciliationTransactionsFilterRequest) {
            EqualsBuilder()
               .appendSuper(super.equals(other))
               .append(this.bank, other.bank)
               .append(this.bankReconciliationType, other.bankReconciliationType)
               .append(this.fromTransactionDate, other.fromTransactionDate)
               .append(this.thruTransactionDate, other.thruTransactionDate)
               .append(this.beginDocNum, other.beginDocNum)
               .append(this.endDocNum, other.endDocNum)
               .append(this.description, other.description)
               .append(this.status, other.status)
               .append(this.fromClearedDate, other.fromClearedDate)
               .append(this.thruClearedDate, other.thruClearedDate)
               .append(this.amount, other.amount)
               .isEquals
         } else {
            false
         }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.bank)
         .append(this.bankReconciliationType)
         .append(this.fromTransactionDate)
         .append(this.thruTransactionDate)
         .append(this.beginDocNum)
         .append(this.endDocNum)
         .append(this.description)
         .append(this.status)
         .append(this.fromClearedDate)
         .append(this.thruClearedDate)
         .append(this.amount)
         .toHashCode()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): BankReconciliationTransactionsFilterRequest =
      BankReconciliationTransactionsFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         bank = this.bank,
         bankReconciliationType = this.bankReconciliationType,
         fromTransactionDate = this.fromTransactionDate,
         thruTransactionDate = this.thruTransactionDate,
         beginDocNum = this.beginDocNum,
         endDocNum = this.endDocNum,
         description = this.description,
         status = this.status,
         fromClearedDate = this.fromClearedDate,
         thruClearedDate = this.thruClearedDate,
         amount = this.amount
      )
   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "bank" to bank,
         "bankType" to bankReconciliationType,
         "fromTransactionDate" to fromTransactionDate,
         "thruTransactionDate" to thruTransactionDate,
         "beginDocNum" to beginDocNum,
         "endDocNum" to endDocNum,
         "description" to description,
         "status" to status,
         "fromClearedDate" to fromClearedDate,
         "thruClearedDate" to thruClearedDate,
         "amount" to amount
      )
}
