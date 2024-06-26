package com.cynergisuite.middleware.accounting.account.type

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountType", title = "Account type", description = "Account type")
class AccountTypeDTO {
}
