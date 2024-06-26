package com.cynergisuite.middleware.accounting.account.type


import io.micronaut.http.annotation.Controller
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule


@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/accounting/account/type")
class AccountTypeController {
}
