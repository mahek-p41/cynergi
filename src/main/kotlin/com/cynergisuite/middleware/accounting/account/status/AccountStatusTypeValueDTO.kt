package com.cynergisuite.middleware.accounting.account.status

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountStatusType", title = "Account status type", description = "Currencies that the banks support")
class AccountStatusTypeValueDTO {

}
