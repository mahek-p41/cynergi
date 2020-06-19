package com.cynergisuite.domain

import com.cynergisuite.middleware.localization.LocalizationService
import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.util.Locale

interface TypeDomainEntity<ENTITY> : Comparable<TypeDomainEntity<ENTITY>>, Identifiable {

   fun myValue(): String

   fun myDescription(): String

   fun myLocalizationCode(): String

   fun localizeMyDescription(locale: Locale, localizationService: LocalizationService): String =
      localizationService.localize(messageKey = myLocalizationCode(), locale = locale, ifNotFound = myDescription())

   fun basicEquality(typeDomainEntity: TypeDomainEntity<ENTITY>): Boolean =
      this.myId() == typeDomainEntity.myId() &&
         this.myValue() == typeDomainEntity.myValue() &&
         this.myDescription() == typeDomainEntity.myDescription() &&
         this.myLocalizationCode() == typeDomainEntity.myLocalizationCode()

   fun basicHashCode(): Int =
      HashCodeBuilder()
         .append(myId())
         .append(myValue())
         .append(myDescription())
         .append(myLocalizationCode())
         .toHashCode()

   override fun compareTo(other: TypeDomainEntity<ENTITY>): Int =
      CompareToBuilder()
         .append(myId(), other.myId())
         .append(myValue(), other.myValue())
         .append(myDescription(), other.myDescription())
         .append(myLocalizationCode(), other.myLocalizationCode())
         .toComparison()
}
