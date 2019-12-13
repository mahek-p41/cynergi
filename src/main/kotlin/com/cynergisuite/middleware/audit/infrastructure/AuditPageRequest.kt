package com.cynergisuite.middleware.audit.infrastructure

import com.cynergisuite.domain.PageRequestBase
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_PAGE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SIZE
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_BY
import com.cynergisuite.domain.PageRequestDefaults.DEFAULT_SORT_DIRECTION
import com.cynergisuite.domain.ValidPageSortBy
import com.cynergisuite.extensions.beginningOfWeek
import com.cynergisuite.extensions.endOfWeek
import com.cynergisuite.middleware.audit.status.CANCELED
import com.cynergisuite.middleware.audit.status.COMPLETED
import com.cynergisuite.middleware.audit.status.CREATED
import com.cynergisuite.middleware.audit.status.IN_PROGRESS
import com.cynergisuite.middleware.audit.status.SIGNED_OFF
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.validation.constraints.Min
import javax.validation.constraints.Positive

@Schema(
   name = "AuditPageRequest",
   title = "Specialized paging for listing audits",
   description = "Defines the parameters available to for a paging request to the audit-fetchAll endpoint. Example ?page=1&size=10&sortBy=id&sortDirection=ASC&storeNumber=1&status=CREATED&status=IN-PROGRESS",
   allOf = [PageRequestBase::class]
)
class AuditPageRequest(
   page: Int?, size: Int?, sortBy: String?, sortDirection: String?,

   @field:Schema(name = "from", description = "Bottom end of the range which will be used to filter audits.  If from is found thru is required.  If both from and thru are empty then the result will include all audits")
   var from: OffsetDateTime? = null,

   @field:Schema(name = "thru", description = "Top end of the range which will be used to filter audits.  If from is found thru is required.  If both from and thru are empty then the result will include all audits")
   var thru: OffsetDateTime? = null,

   @field:Positive
   @field:Min(1)
   @field:Schema(minimum = "1", description = "The Store Number to filter results with")
   var storeNumber: Int? = null,

   @field:Schema(name = "status", description = "Collection of statues that an audit must be in")
   var status: Set<String>? = emptySet()

) : PageRequestBase<AuditPageRequest>(page, size, sortBy, sortDirection) {

   constructor(pageRequestIn: AuditPageRequest? = null) :
      this(
         page = pageRequestIn?.page ?: DEFAULT_PAGE,
         size = pageRequestIn?.size ?: DEFAULT_SIZE,
         sortBy = pageRequestIn?.sortBy ?: DEFAULT_SORT_BY,
         sortDirection = pageRequestIn?.sortDirection ?: DEFAULT_SORT_DIRECTION
      ) {
         val statusesIn = pageRequestIn?.status

         this.status = if ( !statusesIn.isNullOrEmpty() ) statusesIn else setOf(CREATED.value, IN_PROGRESS.value, COMPLETED.value, CANCELED.value, SIGNED_OFF.value)
         this.from = buildFrom(this.status!!, pageRequestIn)
         this.thru = buildThru(from, this.status!!, pageRequestIn)
         this.storeNumber = pageRequestIn?.storeNumber
      }

   protected override fun myNextPage(page: Int, size: Int, sortBy: String, sortDirection: String): AuditPageRequest =
      AuditPageRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         from = this.from,
         thru = this.thru,
         storeNumber = this.storeNumber,
         status = this.status
      )

   @ValidPageSortBy("id", "storeNumber")
   override fun sortByMe(): String = sortBy()

   protected override fun myToString(stringBuilder: StringBuilder, separatorIn: String) {
      val status = this.status
      var separator = separatorIn

      separator = from?.apply { stringBuilder.append(separator).append("from=").append(this) }?.let { "&" } ?: separator
      separator = thru?.apply { stringBuilder.append(separator).append("thru=").append(this) }?.let { "&" } ?: separator
      separator = storeNumber?.apply { stringBuilder.append(separator).append("storeNumber=").append(this) }?.let { "&" } ?: separator

      if ( !status.isNullOrEmpty() ) {
         stringBuilder.append(status.joinToString(separator = "${separator}status=", prefix = "&status="))
      }
   }

   protected override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "from" to from,
         "thru" to thru,
         "storeNumber" to storeNumber,
         "status" to status
      )

   override fun equals(other: Any?): Boolean =
      if (other is AuditPageRequest) {
         EqualsBuilder()
            .appendSuper(super.equals(other))
            .append(this.storeNumber, other.storeNumber)
            .append(this.status, other.status)
            .isEquals
      } else {
         false
      }

   override fun hashCode(): Int =
      HashCodeBuilder()
         .appendSuper(super.hashCode())
         .append(this.storeNumber)
         .append(this.status)
         .toHashCode()

   private fun buildFrom(statuses: Set<String>, pageRequestIn: AuditPageRequest?): OffsetDateTime? {
      val fromIn = pageRequestIn?.from

      return if (statuses.size < 3 && (statuses.contains(CREATED.value) || statuses.contains(IN_PROGRESS.value)) && fromIn == null) {
         null
      } else {
         fromIn ?: OffsetDateTime.now(ZoneId.of("UTC")).beginningOfWeek()
      }
   }

   private fun buildThru(from: OffsetDateTime?, statuses: Set<String>, pageRequestIn: AuditPageRequest?): OffsetDateTime? {
      val thruIn = pageRequestIn?.thru ?: from?.endOfWeek()

      return if (statuses.size < 3 && (statuses.contains(CREATED.value) || statuses.contains(IN_PROGRESS.value)) && thruIn == null) {
         null
      } else {
         thruIn
      }
   }
}
