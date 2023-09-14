package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_ONE
import java.util.UUID

@MappedEntity("module")
data class ModuleEntity(

   @field:Id
   @field:GeneratedValue
   val id: UUID? = null,

   val level: Int,

   @Relation(ONE_TO_ONE)
   val moduleType: ModuleTypeEntity,

   @Relation(ONE_TO_ONE)
   val company: CompanyEntity,
)
