package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Schema(
   name = "GeneralLedgerSearchReportFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available to for a sortable request. Example ?banks=1,3&status=P",
   allOf = [PageRequestBase::class]
)
class GeneralLedgerSearchReportFilterRequest(

   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "startingAccount", description = "Beginning account number")
   var startingAccount: Int? = null,

   @field:Schema(name = "endingAccount", description = "Ending account number")
   var endingAccount: Int? = null,

   @field:Schema(name = "profitCenter", description = "Profit Center")
   var profitCenter: Int? = null,

   @field:Schema(name = "sourceCode", description = "General ledger source code (for transfer or report)")
   var sourceCode: String? = null,

   @field:Schema(name = "typeEntry", description = "General ledger recurring type (for transfer or report)")
   var typeEntry: String? = null,

   @field:Schema(name = "lowAmount", description = "Low amount")
   var lowAmount: BigDecimal? = null,

   @field:Schema(name = "highAmount", description = "High amount")
   var highAmount: BigDecimal? = null,

   @field:Schema(name = "description", description = "description")
   var description: String? = null,

   @field:Schema(name = "jeNumber", description = "Journal entry number")
   var jeNumber: Int? = null,

   @field:Schema(name = "frmPmtDt", description = "Beginning payment date")
   var frmPmtDt: OffsetDateTime? = null,

   @field:Schema(name = "thruPmtDt", description = "Ending payment date")
   var thruPmtDt: OffsetDateTime? = null,



   ) : PageRequestBase<GeneralLedgerSearchReportFilterRequest>(page, size, sortBy, sortDirection) {

      @ValidPageSortBy("id")
      override fun sortByMe(): String = sortBy()

      override fun equals(other: Any?): Boolean =
         if (other is GeneralLedgerSearchReportFilterRequest) {
            EqualsBuilder()
               .appendSuper(super.equals(other))

               .append(this.startingAccount, other.startingAccount)
               .append(this.endingAccount, other.endingAccount)
               .append(this.profitCenter, other.profitCenter)
               .append(this.sourceCode, other.sourceCode)
               .append(this.typeEntry, other.typeEntry)
               .append(this.lowAmount, other.lowAmount)
               .append(this.highAmount, other.highAmount)
               .append(this.description, other.description)
               .append(this.jeNumber, other.jeNumber)
               .append(this.frmPmtDt, other.frmPmtDt)
               .append(this.thruPmtDt, other.thruPmtDt)
               .isEquals
         } else {
            false
         }

      override fun hashCode(): Int =
         HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(this.startingAccount)
            .append(this.endingAccount)
            .append(this.profitCenter)
            .append(this.sourceCode)
            .append(this.typeEntry)
            .append(this.lowAmount)
            .append(this.highAmount)
            .append(this.description)
            .append(this.jeNumber)
            .append(this.frmPmtDt)
            .append(this.thruPmtDt)
            .toHashCode()

      protected override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): GeneralLedgerSearchReportFilterRequest =
         GeneralLedgerSearchReportFilterRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection,
            startingAccount = this.startingAccount,
            endingAccount = this.endingAccount,
            profitCenter = this.profitCenter,
            sourceCode = this.sourceCode,
            typeEntry = this.typeEntry,
            lowAmount = this.lowAmount,
            highAmount = this.highAmount,
            description = this.description,
            jeNumber = this.jeNumber,
            frmPmtDt = this.frmPmtDt,
            thruPmtDt = this.thruPmtDt
         )

      protected override fun myToStringValues(): List<Pair<String, Any?>> =
         listOf(
            "startingAccount" to startingAccount,
            "endingAccount" to endingAccount,
            "profitCenter" to profitCenter,
            "sourceCode" to sourceCode,
            "typeEntry" to typeEntry,
            "lowAmount" to lowAmount,
            "highAmount" to highAmount,
            "description" to description,
            "jeNumber" to jeNumber,
            "frmPmtDt" to frmPmtDt,
            "thruPmtDt" to thruPmtDt
         )
   }
