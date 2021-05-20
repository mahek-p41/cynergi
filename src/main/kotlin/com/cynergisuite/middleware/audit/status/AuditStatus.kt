package com.cynergisuite.middleware.audit.status

import com.cynergisuite.domain.TypeDomainEntity
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder

/**
 * Defines the shape of an audit status
 */
sealed class AuditStatus(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String,
   val color: String,
   val nextStates: MutableSet<AuditStatus> = mutableSetOf()
) : TypeDomainEntity<AuditStatus> {

   private val myHashCode: Int = HashCodeBuilder()
      .append(id)
      .append(value)
      .append(description)
      .append(localizationCode)
      .append(color)
      .toHashCode()

   override fun myId(): Int = id
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

   override fun toString(): String =
      ToStringBuilder(this)
         .append(id)
         .append(value)
         .append(description)
         .append(localizationCode)
         .append(color)
         .build()
}

/**
 * A container to hold values loaded from the database
 */
class AuditStatusEntity(
   id: Int,
   value: String,
   description: String,
   localizationCode: String,
   color: String,
   nextStates: MutableSet<AuditStatus> = LinkedHashSet()
) : AuditStatus(id, value, description, localizationCode, color, nextStates)

/*
 * these objects define the known to the business logic instances stored in the audit_status_type_domain table
 */
object Created : AuditStatus(1, "CREATED", "Created", "audit.status.created", "A572A7")
object InProgress : AuditStatus(2, "IN-PROGRESS", "In Progress", "audit.status.in-progress", "DB843D")
object Completed : AuditStatus(3, "COMPLETED", "Completed", "audit.status.completed", "12A2DC")
object Canceled : AuditStatus(4, "CANCELED", "Canceled", "audit.status.canceled", "AA4643")
object Approved : AuditStatus(5, "APPROVED", "Approved", "audit.status.approved", "89A54E")

/*
 * aliases to act as alternatives to using the init-case object definition from above.  Kotlin doesn't recommend having
 * class names with the underscores in them so these act as indicators that a constant is being used.
 */
typealias CREATED = Created
typealias IN_PROGRESS = InProgress
typealias COMPLETED = Completed
typealias CANCELED = Canceled
typealias APPROVED = Approved
