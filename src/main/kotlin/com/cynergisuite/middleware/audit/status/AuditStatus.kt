package com.cynergisuite.middleware.audit.status

import com.cynergisuite.domain.TypeDomain
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
) : TypeDomain() {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun myToString(): String =
      ToStringBuilder(this)
         .appendSuper(super.myToString())
         .append(this.color)
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
