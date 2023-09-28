package com.cynergisuite.domain

import com.cynergisuite.middleware.localization.LocalizationService
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.Locale

abstract class TypeDomain {

   abstract fun myId(): Int

   abstract fun myValue(): String

   abstract fun myDescription(): String

   abstract fun myLocalizationCode(): String

   fun localizeMyDescription(locale: Locale, localizationService: LocalizationService): String =
      localizationService.localize(messageKey = myLocalizationCode(), locale = locale, ifNotFound = myDescription())

   protected open fun myEquality(typeDomainEntity: TypeDomain): Boolean =
      this.myId() == typeDomainEntity.myId() &&
         this.myValue() == typeDomainEntity.myValue() &&
         this.myDescription() == typeDomainEntity.myDescription() &&
         this.myLocalizationCode() == typeDomainEntity.myLocalizationCode()

   protected open fun myHashCode(): Int =
      HashCodeBuilder()
         .append(myId())
         .append(myValue())
         .append(myDescription())
         .append(myLocalizationCode())
         .build()

   protected open fun myCompareTo(other: TypeDomain): Int =
      CompareToBuilder()
         .append(other.myId(), myId())
         .append(other.myValue(), myValue())
         .append(other.myDescription(), myDescription())
         .append(other.myLocalizationCode(), myLocalizationCode())
         .build()

   protected open fun myToString(): String =
      ToStringBuilder(this)
         .append(myId())
         .append(myValue())
         .append(myDescription())
         .append(myLocalizationCode())
         .build()

   final override fun hashCode(): Int {
      return myHashCode()
   }

   final override fun equals(other: Any?): Boolean {
      return if (other != null && other is TypeDomain) {
         myEquality(other)
      } else {
         false
      }
   }

   final override fun toString(): String {
      return myToString()
   }
}
