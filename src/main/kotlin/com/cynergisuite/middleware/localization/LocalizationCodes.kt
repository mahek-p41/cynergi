package com.cynergisuite.middleware.localization

import com.cynergisuite.middleware.address.AddressEntity
import com.cynergisuite.middleware.authentication.user.User
import com.cynergisuite.middleware.company.Company
import org.apache.commons.lang3.builder.ToStringBuilder
import java.time.OffsetDateTime

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

abstract class Cynergi(code: String, arguments: Array<Any?>) : LocalizationCodeImpl(code, arguments)
class Duplicate(duplicateValue: Any?) : Cynergi("cynergi.validation.duplicate", arrayOf(duplicateValue))
class NotUpdatable(notUpdatableValue: Any?) : Cynergi("cynergi.validation.not.updatable", arrayOf(notUpdatableValue))
class MustBeInRangeOf(value: Any?) : Cynergi("cynergi.validation.must.be.in.range.of", arrayOf(value))
class EndDateBeforeStart(endDate: String, startDate: String) : Cynergi("cynergi.validation.end.date.before.start", arrayOf(endDate, startDate))
class NotificationRecipientsRequiredAll(notificationType: String) : Cynergi("cynergi.validation.notification.recipients.not.required", arrayOf(notificationType))
class NotificationRecipientsRequired(notificationType: String?) : Cynergi("cynergi.validation.notification.recipients.required", arrayOf(notificationType))
class ConversionError(valueOne: String, valueTwo: Any?) : Cynergi("cynergi.validation.conversion.error", arrayOf(valueOne, valueTwo))
class ThruDateIsBeforeFrom(from: OffsetDateTime, thru: OffsetDateTime) : Cynergi("cynergi.validation.thru.before.from", arrayOf(from, thru))
class InvalidCompany(company: Company): Cynergi("cynergi.validation.invalid.company", arrayOf(company.myDataset()))
class MustMatchPathVariable(value: Any?): Cynergi("cynergi.validation.must.match.path.variable", arrayOf(value))
class ConfigAlreadyExist(value: Any?) : Cynergi("cynergi.validation.config.exists", arrayOf(value))
class AddressNeedsUpdated(address: AddressEntity) : Cynergi("cynergi.validation.address.needs.updated", arrayOf(address))

class AuditStatusNotFound(auditStatus: String) : Cynergi("cynergi.audit.status.not.found", arrayOf(auditStatus))
class AuditUnableToChangeStatusFromTo(auditId: Long, toStatus: String, fromStatus: String) : Cynergi("cynergi.audit.unable.to.change.status.from.to", arrayOf(auditId, toStatus, fromStatus))
class AuditMustBeInProgressDetails(auditId: Long) : Cynergi("cynergi.audit.must.be.in.progress.details", arrayOf(auditId))
class AuditMustBeInProgressDiscrepancy(auditId: Long) : Cynergi("cynergi.audit.must.be.in.progress.exception", arrayOf(auditId))
class AuditScanAreaNotFound(scanArea: String?) : Cynergi("cynergi.audit.scan.area.not.found", arrayOf(scanArea))
class AuditOpenAtStore(storeNumber: Int) : Cynergi("cynergi.audit.open.at.store", arrayOf(storeNumber))
class AuditExceptionMustHaveInventoryOrBarcode() : Cynergi("cynergi.audit.exception.inventory.or.barcode", emptyArray())
class AuditHasBeenApprovedNoNewNotesAllowed(auditId: Long) : Cynergi("cynergi.audit.has.been.approved.no.new.notes.allowed", arrayOf(auditId))
class AuditUpdateRequiresApprovedOrNote() : Cynergi("cynergi.audit.update.requires.approval.or.note", emptyArray())
class AuditExceptionHasNotBeenApproved(auditExceptionId: Long) : Cynergi("cynergi.audit.exception.has.been.approved.no.new.notes.allowed", arrayOf(auditExceptionId))
class AuditDueToday(auditNumber: Int) : Cynergi("cynergi.audit.due.today", arrayOf(auditNumber))
class AuditPastDue(auditNumber: Int) : Cynergi("cynergi.audit.past.due", arrayOf(auditNumber))

class VendorPaymentTermDuePercentDoesNotAddUp(percent: String) : Cynergi("vendor.payment.term.does.not.add.up", arrayOf(percent))

abstract class SystemCode(code: String, arguments: Array<Any?>) : LocalizationCodeImpl(code, arguments)
class NotFound(unfindable: Any) : SystemCode("system.not.found", arrayOf(unfindable)) {
   constructor(user: User) : this(user.myEmployeeNumber())
}
class InternalError : SystemCode("system.internal.error", emptyArray())
class DataAccessError : SystemCode("system.data.access.exception", emptyArray())
class RouteError(routeArgument: String) : SystemCode("system.route.error", arrayOf(routeArgument))
class NotImplemented(pathNotImplemented: String) : SystemCode("system.not.implemented", arrayOf(pathNotImplemented))
class NotLoggedIn : SystemCode("system.not.logged.in", emptyArray())
class AccessDenied : SystemCode("system.access.denied", emptyArray())
class AccessDeniedCredentialsDoNotMatch(user: String) : SystemCode("system.access.denied.creds.do.not.match", arrayOf(user))
class AccessDeniedStore(user: String) : SystemCode("system.access.denied.store", arrayOf(user))
class Unknown : SystemCode("system.word.unknown", arrayOf())
class UnableToParseJson(jsonParseErrorMessage: String) : SystemCode("system.json.unable.parse", arrayOf(jsonParseErrorMessage))
