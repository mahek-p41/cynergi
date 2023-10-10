package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomain
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_MANY
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_ONE
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder

@MappedEntity("menu_type_domain")
data class MenuTypeEntity(

   @field:Id
   @field:GeneratedValue
   val id: Int,

   val value: String,

   val description: String,

   val localizationCode: String,

   val orderNumber: Int,

   @Relation(ONE_TO_ONE)
   val parent: MenuTypeEntity? = null,

   @Relation(ONE_TO_ONE)
   val areaType: AreaTypeEntity? = null,

   @Relation(ONE_TO_MANY)
   val menus: List<ModuleTypeEntity>

) : TypeDomain() {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode

   override fun myEquality(other: TypeDomain): Boolean {
      return if (other is MenuTypeEntity) {
         EqualsBuilder()
            .appendSuper(super.myEquality(other))
            .append(this.orderNumber, other.orderNumber)
            .append(this.parent, other.parent)
            .append(this.areaType, other.areaType)
            .build()
      } else {
         false
      }
   }

   override fun myHashCode(): Int {
      return HashCodeBuilder()
         .appendSuper(super.myHashCode())
         .append(this.orderNumber)
         .append(this.parent)
         .append(this.areaType)
         .build()
   }
}