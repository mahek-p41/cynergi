package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyDTO
import com.cynergisuite.middleware.company.CompanyEntity
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Account", title = "A data transfer object containing account information", description = "An data transfer object containing a account information.")
data class SecurityGroupDTO(

    var id: UUID? = null,

    @field:NotNull
    @field:Schema(name = "name", description = "Description for a Vendor.")
    var value: String,

    @field:Valid
    @field:NotNull
    @field:Schema(name = "balance", description = "Vendor Balance")
    var description: String,

    @field:NotNull
    @field:Positive
    @field:Schema(name = "number", description = "Vendor number")
    var company: CompanyEntity

    ) : Identifiable {
    constructor(securityGroupDto: SecurityGroup) :
            this(
                id = securityGroupDto.id,
                value = securityGroupDto.value,
                description = securityGroupDto.description,
                company = securityGroupDto.company
            )

    override fun myId(): UUID? = id
}
