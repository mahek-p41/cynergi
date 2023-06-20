package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Relation
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_ONE
import java.util.UUID

@Introspected
@MappedEntity("security_group")
data class SecurityGroup (

@field:Id
val id: UUID? = null,
val value: String,
val description: String,

@Relation(ONE_TO_ONE)
val company: CompanyEntity
) : Identifiable {
    constructor(
        dto: SecurityGroupDTO
    ) :
    this(
        id = dto.id,
        value = dto.value!!,
        description = dto.description!!,
        company = dto.company!!,
    )
    override fun myId(): UUID? = id
}