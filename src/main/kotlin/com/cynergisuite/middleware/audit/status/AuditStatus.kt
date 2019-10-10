package com.cynergisuite.middleware.audit.status

import com.cynergisuite.domain.TypeDomainEntity
import org.apache.commons.lang3.builder.HashCodeBuilder

data class AuditStatus(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String,
   val color: String,
   val nextStates: MutableSet<AuditStatus> = LinkedHashSet()
) : TypeDomainEntity<AuditStatus> {
   private val myHashCode: Int = HashCodeBuilder()
      .append(id)
      .append(value)
      .append(description)
      .append(localizationCode)
      .toHashCode()

   override fun entityId(): Long = id
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
