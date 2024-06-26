package com.cynergisuite.middleware.accounting.account

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema


@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "Account", title = "A data transfer object containing account information", description = "An data transfer object containing a account information.")
class AccountDTO{
}
