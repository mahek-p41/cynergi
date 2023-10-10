package com.cynergisuite.middleware.sign.here.agreement.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.agreement.signing.infrastructure.AgreementSigningRepository
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.sign.here.DocumentPageRequest
import com.cynergisuite.middleware.sign.here.agreement.SignHereAgreementDto
import com.cynergisuite.middleware.sign.here.agreement.SignHereAgreementService
import com.cynergisuite.middleware.sign.here.associated.SignHereAssociatedDto
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import jakarta.inject.Inject
import org.reactivestreams.Publisher
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/sign/here/agreement")
class SignHereAgreementProxyController @Inject constructor(
   private val agreementSigningRepository: AgreementSigningRepository,
   private val signHereAgreementService: SignHereAgreementService,
   private val userService: UserService,
) {

   @Get("{?pageRequest*}")
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") @Valid pageRequest: DocumentPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>,
   ): Page<SignHereAgreementDto> {
      val user = userService.fetchUser(authentication) // grab the store user
      val docs = signHereAgreementService.findRequestsByStore(user.myLocation(), user.myCompany(), pageRequest)

      return if (docs.notEmpty()) {
         docs.mapElements { doc ->
            SignHereAgreementDto(
               id = doc.id!!,
               agreementNumber = doc.meta["Agreement-No"]?.toString() ?: agreementSigningRepository.findAgreementNumberFromSignatureId(doc.id!!),
               customerNumber = doc.meta["Customer-No"]?.toString() ?: agreementSigningRepository.findCustomerNumberFromSignatureId(doc.id!!),
               agreementType = doc.meta["Agreement-Type"]?.toString() ?: agreementSigningRepository.findAgreementTypeFromSignatureId(doc.id!!),
               timeCreated = doc.timeCreated!!,
               customerName = doc.signingDetail?.name,
               status = doc.status?.value
            )
         }
      } else {
         throw PageOutOfBoundsException(pageRequest)
      }
   }

   @Get("/requested/associated/{signatureRequestedId}{?pageRequest*}")
   fun fetchAssociated(
      @Parameter(description = "Primary Key to lookup the Agreement associated records", `in` = PATH) @QueryValue("signatureRequestedId")
      signatureRequestedId: UUID,
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") @Valid pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>,
   ): Page<SignHereAssociatedDto> {
      val user = userService.fetchUser(authentication) // grab the store user
      val docs = signHereAgreementService.findAssociated(user.myLocation(), user.myCompany(), signatureRequestedId, pageRequest)

      return if (docs.notEmpty()) {
         docs.mapElements { doc ->
            SignHereAssociatedDto(
               id = doc.id,
               type = doc.type,
               signatories = doc.signatories,
               name = doc.signingDetail?.name,
               timeCreated = doc.timeCreated,
               signatureUrl = doc.signatureUrl,
            )
         }
      } else {
         throw PageOutOfBoundsException(pageRequest)
      }
   }

   @Get("/document/detail/archive/{documentId}", processes = [MediaType.APPLICATION_PDF])
   fun fetchArchivedDocument(
      @Parameter(description = "Primary Key to lookup the requested document", `in` = PATH) @QueryValue("documentId")
      documentId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>,
   ): Publisher<MutableHttpResponse<*>> {
      val user = userService.fetchUser(authentication) // grab the store user

      return signHereAgreementService.retrieveDocument(user.myLocation(), user.myCompany(), documentId, httpRequest)
   }

   @Delete("/document/cancel/{signatureRequestedId}")
   fun cancelAssociated(
      @Parameter(description = "Primary Key to cancel the Agreement associated records", `in` = PATH) @QueryValue("signatureRequestedId")
      signatureRequestedId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>,
   ) {
      val user = userService.fetchUser(authentication) // grab the store user

      signHereAgreementService.cancelAssociated(user.myLocation(), user.myCompany(), signatureRequestedId)
   }
}
