package com.cynergisuite.middleware.authentication.user

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyDTO
import com.fasterxml.jackson.annotation.JsonInclude
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "SecurityGroupDTO", title = "A data transfer object containing security group information", description = "An data transfer object containing security group information.")
data class SecurityGroupDTO(

    @field:NotNull
    @field:Schema(name = "id", required = false, nullable = true, description = "System generated ID")
    var id: UUID? = null,

    @field:NotNull
    @field:Schema(name = "value", description = "Security Group value")
    var value: String? = null,

    @field:NotNull
    @field:Schema(name = "description", description = "Security Group description")
    var description: String? = null,

    @field:NotNull
    @field:Schema(name = "description", description = "Security Group description")
    var types: List<SecurityType>? = null,

    @field:NotNull
    @field:Schema(name = "company", description = "Company")
    var company: CompanyDTO? = null

) : Identifiable {

    constructor(securityGroup: SecurityGroup) :
            this(
                id = securityGroup.id,
                value = securityGroup.value,
                description = securityGroup.description,
                types = securityGroup.types,
                company = CompanyDTO(securityGroup.company)
            )

    override fun myId(): UUID? = id
}
