package com.cynergisuite.middleware.localization

import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.StoreEntity
import org.apache.commons.lang3.StringUtils.EMPTY
import org.apache.commons.lang3.builder.ToStringBuilder
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

interface LocalizationCode {
   fun getCode(): String
   fun getArguments(): Array<Any?>
}

open class LocalizationCodeImpl(
   private val code: String,
   private val arguments: Array<Any?>
) : LocalizationCode {
   override fun getCode(): String = code
   override fun getArguments(): Array<Any?> = arguments
   override fun toString(): String {
      return ToStringBuilder(this)
         .append("code", code)
         .append("argument", arguments)
         .build()
   }
}

abstract class Validation(code: String, arguments: Array<Any?>) : LocalizationCodeImpl(code, arguments)
class NotNull(notNullProperty: String) : Validation("javax.validation.constraints.NotNull.message", arrayOf(notNullProperty))
class Size : Validation("javax.validation.constraints.Size.message", emptyArray())
class Positive : Validation("javax.validation.constraints.Positive.message", emptyArray())
class Min : Validation("javax.validation.constraints.Min.message", emptyArray())
class Max : Validation("javax.validation.constraints.Max.message", emptyArray())
class Pattern : Validation("javax.validation.constraints.Pattern.message", emptyArray())

abstract class Cynergi(code: String, arguments: Array<Any?>) : LocalizationCodeImpl(code, arguments)
class Duplicate(duplicateValue: Any?) : Cynergi("cynergi.validation.duplicate", if (duplicateValue != null) arrayOf(duplicateValue) else arrayOf(EMPTY))
class NotUpdatable(notUpdatableValue: Any?) : Cynergi("cynergi.validation.not.updatable", arrayOf(notUpdatableValue))
class MustBeInRangeOf(value: Any?) : Cynergi("cynergi.validation.must.be.in.range.of", arrayOf(value))
class EndDateBeforeStart(endDate: String, startDate: String) : Cynergi("cynergi.validation.end.date.before.start", arrayOf(endDate, startDate))
class NotificationRecipientsRequiredAll(notificationType: String) : Cynergi("cynergi.validation.notification.recipients.not.required", arrayOf(notificationType))
class NotificationRecipientsRequired(notificationType: String?) : Cynergi("cynergi.validation.notification.recipients.required", arrayOf(notificationType))
class ConversionError(valueOne: String, valueTwo: Any?) : Cynergi("cynergi.validation.conversion.error", arrayOf(valueOne, valueTwo))
class ThruDateIsBeforeFrom(from: OffsetDateTime, thru: OffsetDateTime) : Cynergi("cynergi.validation.thru.before.from", arrayOf(from, thru))
class CalendarThruDateIsBeforeFrom(from: LocalDate, thru: LocalDate) : Cynergi("cynergi.validation.calendar.thru.before.from", arrayOf(from, thru))
class CalendarDatesSpanMoreThanTwoYears(from: LocalDate, thru: LocalDate) : Cynergi("cynergi.validation.dates.span.more.than.two.years", arrayOf(from, thru))
class APDatesSelectedOutsideGLDatesSet(from: LocalDate, thru: LocalDate) : Cynergi("cynergi.validation.ap.outside.of.gl.window", arrayOf(from, thru))
class InvalidCompany(company: CompanyEntity) : Cynergi("cynergi.validation.invalid.company", arrayOf(company.datasetCode))
class ConfigAlreadyExist(value: Any?) : Cynergi("cynergi.validation.config.exists", arrayOf(value))
class AddressNeedsUpdated : Cynergi("cynergi.validation.address.needs.updated", emptyArray())
class InvalidPayToVendor(id: UUID?) : Cynergi("cynergi.validation.invalid.pay.to.vendor", arrayOf(id))
class SelectPercentOrPerUnit(percent: BigDecimal?, amountPerUnit: BigDecimal?) : Cynergi("cynergi.validation.select.percent.or.per.unit", arrayOf(percent, amountPerUnit))
class AccountIsRequired(account: AccountEntity?) : Cynergi("cynergi.validation.account.is.required", arrayOf(account))
class PercentTotalGreaterThan100(percent: BigDecimal) : Cynergi("cynergi.validation.percent.total.greater.than.100", arrayOf(percent))
class BalanceMustBeZero(balance: BigDecimal) : Cynergi("cynergi.validation.balance.must.be.zero", arrayOf(balance))
class GLNotOpen(date: LocalDate) : Cynergi("cynergi.validation.gl.not.open", arrayOf(date))
class ProfitCenterMustMatchBankProfitCenter(profitCenter: StoreEntity) : Cynergi("cynergi.validation.profit.center.must.match.bank.profit.center", arrayOf(profitCenter))
class DatesMustBeInSameFiscalYear(startDate: LocalDate, endDate: LocalDate) : Cynergi("cynergi.validation.dates.must.be.in.same.fiscal.year", arrayOf(startDate, endDate))
class ClearedDateMustNotBeFutureDate(date: LocalDate) : Cynergi("cynergi.validation.cleared.date.must.not.be.future.date", arrayOf(date))
class ClearedDateNotPriorTransactionDate(date: LocalDate) : Cynergi("cynergi.validation.cleared.date.not.prior.transaction.date", arrayOf(date))
class AuditStatusNotFound(auditStatus: String) : Cynergi("cynergi.audit.status.not.found", arrayOf(auditStatus))
class AuditUnableToChangeStatusFromTo(auditId: UUID, toStatus: String, fromStatus: String) : Cynergi("cynergi.audit.unable.to.change.status.from.to", arrayOf(auditId, toStatus, fromStatus))
class AuditMustBeInProgressDetails(auditId: UUID) : Cynergi("cynergi.audit.must.be.in.progress.details", arrayOf(auditId))
class AuditMustBeInProgressDiscrepancy(auditId: UUID) : Cynergi("cynergi.audit.must.be.in.progress.exception", arrayOf(auditId))
class AuditScanAreaNotFound(scanArea: String?) : Cynergi("cynergi.audit.scan.area.not.found", arrayOf(scanArea))
class AuditOpenAtStore(storeNumber: Int) : Cynergi("cynergi.audit.open.at.store", arrayOf(storeNumber))
class AuditExceptionMustHaveInventoryOrBarcode() : Cynergi("cynergi.audit.exception.inventory.or.barcode", emptyArray())
class AuditHasBeenApprovedNoNewNotesAllowed(auditId: UUID) : Cynergi("cynergi.audit.has.been.approved.no.new.notes.allowed", arrayOf(auditId))
class AuditUpdateRequiresApprovedOrNote() : Cynergi("cynergi.audit.update.requires.approval.or.note", emptyArray())
class AuditExceptionHasNotBeenApproved(auditExceptionId: UUID) : Cynergi("cynergi.audit.exception.has.been.approved.no.new.notes.allowed", arrayOf(auditExceptionId))
class AuditDueToday(auditNumber: Int) : Cynergi("cynergi.audit.due.today", arrayOf(auditNumber))
class AuditPastDue(auditNumber: Int) : Cynergi("cynergi.audit.past.due", arrayOf(auditNumber))

class VendorPaymentTermDuePercentDoesNotAddUp(percent: String) : Cynergi("vendor.payment.term.does.not.add.up", arrayOf(percent))
class DataConstraintIntegrityViolation : Cynergi("cynergi.data.constraint.violated", emptyArray())

abstract class SystemCode(code: String, arguments: Array<Any?>) : LocalizationCodeImpl(code, arguments)
class NotFound(unfindable: Any) : SystemCode("system.not.found", arrayOf(unfindable)) {
   constructor(user: User) : this(user.myEmployeeNumber())
}
class InternalError : SystemCode("system.internal.error", emptyArray())
class DataAccessError : SystemCode("system.data.access.exception", emptyArray())
class RouteError(routeArgument: String) : SystemCode("system.route.error", arrayOf(routeArgument))
class RouteHeaderError(headerName: String) : SystemCode("system.route.header.error", arrayOf(headerName))
class NotImplemented(pathNotImplemented: String) : SystemCode("system.not.implemented", arrayOf(pathNotImplemented))
class NotLoggedIn : SystemCode("system.not.logged.in", emptyArray())
class AccessDenied : SystemCode("system.access.denied", emptyArray())
class AccessDeniedCredentialsDoNotMatch(user: String) : SystemCode("system.access.denied.creds.do.not.match", arrayOf(user)) {
   constructor(user: Int) : this(user.toString())
}
class AccessDeniedStore(user: String) : SystemCode("system.access.denied.store", arrayOf(user)) {
   constructor(user: Int) : this(user.toString())
}
class Unknown : SystemCode("system.word.unknown", arrayOf())
class UnableToParseJson(jsonParseErrorMessage: String) : SystemCode("system.json.unable.parse", arrayOf(jsonParseErrorMessage))
class PageOutOfBounds(page: Int?, size: Int?, sortBy: String?, sortDirection: String?) : SystemCode("system.page.out.of.bounds", arrayOf(page, size, sortBy, sortDirection))
