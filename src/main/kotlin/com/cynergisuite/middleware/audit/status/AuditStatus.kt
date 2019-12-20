package com.cynergisuite.middleware.audit.status

import com.cynergisuite.domain.TypeDomainEntity
import org.apache.commons.lang3.builder.HashCodeBuilder

/**
 * Defines the shape of an audit status
 */
sealed class AuditStatus(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String,
   val color: String,
   val nextStates: MutableSet<AuditStatus> = mutableSetOf<AuditStatus>()
) : TypeDomainEntity<AuditStatus> {

   private val myHashCode: Int = HashCodeBuilder()
      .append(id)
      .append(value)
      .append(description)
      .append(localizationCode)
      .toHashCode()

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun hashCode(): Int = myHashCode

   override fun equals(other: Any?): Boolean =
      if (other is AuditStatus) {
         basicEquality(other) && other.color == this.color
      } else {
         false
      }
}

/**
 * A container to hold values loaded from the database
 */
class AuditStatusEntity(
   id: Long,
   value: String,
   description: String,
   localizationCode: String,
   color: String,
   nextStates: MutableSet<AuditStatus> = LinkedHashSet()
) : AuditStatus(id, value, description, localizationCode, color, nextStates)

/*
 * these objects define the known to the business logic instances stored in the audit_status_type_domain table
 */
object Created : AuditStatus(1, "CREATED", "Created", "audit.status.opened", "A572A7")
object InProgress : AuditStatus(2, "IN-PROGRESS", "In Progress", "audit.status.in-progress", "DB843D")
object Completed: AuditStatus(3, "COMPLETED", "Completed", "audit.status.completed", "12A2DC")
object Canceled: AuditStatus(4, "CANCELED", "Canceled", "audit.status.canceled", "AA4643")
object SignedOff: AuditStatus(5, "SIGNED-OFF", "Signed Off", "audit.status.signed-off", "89A54E")

/*
 * aliases to act as alternatives to using the init-case object definition from above.  Kotlin doesn't recommend having
 * class names with the underscores in them so these act as indicators that a constant is being used.
 */
typealias CREATED = Created
typealias IN_PROGRESS = InProgress
typealias COMPLETED = Completed
typealias CANCELED = Canceled
typealias SIGNED_OFF = SignedOff
