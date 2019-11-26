package com.cynergisuite.middleware.localization

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
}

abstract class Validation(code: String, arguments: Array<Any?>): LocalizationCodeImpl(code, arguments)
class NotNull(notNullProperty: String) : Validation("javax.validation.constraints.NotNull.message", arrayOf(notNullProperty))
class Size : Validation("javax.validation.constraints.Size.message", emptyArray())
class Positive : Validation("javax.validation.constraints.Positive.message", emptyArray())
class Min : Validation("javax.validation.constraints.Min.message", emptyArray())
class Max : Validation("javax.validation.constraints.Max.message", emptyArray())
class Pattern : Validation("javax.validation.constraints.Pattern.message", emptyArray())

abstract class Cynergi(code: String, arguments: Array<Any?>): LocalizationCodeImpl(code, arguments)
class Duplicate(duplicateValue: Any?) : Cynergi("cynergi.validation.duplicate", arrayOf(duplicateValue))
class NotUpdatable(notUpdatableValue: Any?) : Cynergi("cynergi.validation.not.updatable", arrayOf(notUpdatableValue))
class EndDateBeforeStart(endDate: String, startDate: String) : Cynergi("cynergi.validation.end.date.before.start", arrayOf(endDate, startDate))
class NotificationRecipientsRequiredAll(notificationType: String) : Cynergi("cynergi.validation.notification.recipients.not.required", arrayOf(notificationType))
class NotificationRecipientsRequired(notificationType: String?) : Cynergi("cynergi.validation.notification.recipients.required", arrayOf(notificationType))
class ConversionError(valueOne: String, valueTwo: Any?) : Cynergi("cynergi.validation.conversion.error", arrayOf(valueOne, valueTwo))
class ThruDateIsBeforeFrom(from: OffsetDateTime, thru: OffsetDateTime) : Cynergi("cynergi.validation.thru.before.from", arrayOf(from, thru))

class AuditStatusNotFound(auditStatus: String):  Cynergi("cynergi.audit.status.not.found", arrayOf(auditStatus))
class AuditUnableToChangeStatusFromTo(auditId: Long, toStatus: String, fromStatus: String): Cynergi("cynergi.audit.unable.to.change.status.from.to", arrayOf(auditId, toStatus, fromStatus))
class AuditMustBeInProgressDetails(auditId: Long): Cynergi("cynergi.audit.must.be.in.progress.details", arrayOf(auditId))
class AuditMustBeInProgressDiscrepancy(auditId: Long): Cynergi("cynergi.audit.must.be.in.progress.exception", arrayOf(auditId))
class AuditScanAreaNotFound(scanArea: String?): Cynergi("cynergi.audit.scan.area.not.found", arrayOf(scanArea))
class AuditOpenAtStore(storeNumber: Int): Cynergi("cynergi.audit.open.at.store", arrayOf(storeNumber))
class AuditExceptionMustHaveInventoryOrBarcode(): Cynergi("cynergi.audit.exception.inventory.or.barcode", emptyArray())
class AuditHasBeenSignedOffNoNewNotesAllowed(auditId: Long): Cynergi("cynergi.audit.has.been.signed.off.no.new.notes.allowed", arrayOf(auditId))


abstract class SystemCode(code: String, arguments: Array<Any?>): LocalizationCodeImpl(code, arguments)
class NotFound(unfindable: Any): SystemCode("system.not.found", arrayOf(unfindable))
class InternalError: SystemCode("system.internal.error", emptyArray())
class RouteError(routeArgument: String): SystemCode("system.route.error", arrayOf(routeArgument))
class NotImplemented(pathNotImplemented: String): SystemCode("system.not.implemented", arrayOf(pathNotImplemented))
class LoggedIn(user: String): SystemCode("system.logged.in", arrayOf(user))
class NotLoggedIn: SystemCode("system.not.logged.in", emptyArray())
class AccessDenied(user: String): SystemCode("system.access.denied", arrayOf(user))
class Unknown: SystemCode("system.word.unknown", arrayOf())
class UnableToParseJson(jsonParseErrorMessage: String): SystemCode("system.json.unable.parse", arrayOf(jsonParseErrorMessage))
class PageOutOfBounds(page: Int?, size: Int?, sortBy: String?, sortDirection: String?): SystemCode("system.page.out.of.bounds", arrayOf(page, size, sortBy, sortDirection))
