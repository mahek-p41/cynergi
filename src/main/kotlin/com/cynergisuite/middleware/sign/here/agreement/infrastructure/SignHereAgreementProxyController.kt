package com.cynergisuite.middleware.sign.here.agreement.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.sign.here.DocumentPageRequest
import com.cynergisuite.middleware.sign.here.agreement.SignHereAgreementDto
import com.cynergisuite.middleware.sign.here.agreement.SignHereAgreementService
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import jakarta.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/sign/here/agreement")
class SignHereAgreementProxyController @Inject constructor(
   private val signHereAgreementService: SignHereAgreementService,
   private val userService: UserService,
) {

   @Get("{?pageRequest*}")
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") @Valid pageRequest: DocumentPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<SignHereAgreementDto> {
      val user = userService.fetchUser(authentication) // grab the store user
      val docs = signHereAgreementService.findRequestsByStore(user.myLocation(), user.myCompany(), pageRequest)

      return if (docs.notEmpty) {
         docs.mapElements(::SignHereAgreementDto) // map the elements of the page to a new page instance, using the default constructor
      } else {
         throw PageOutOfBoundsException(pageRequest)
      }
   }
}
