package com.cynergisuite.middleware.sign.here.agreement

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.sign.here.DocumentPageRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected

/**
 * Class to map the response from the Sign Here Please /api/document/requested{?pageRequest*}.
 *
 * To get the declarative client mapping to work, the structure has to be defined explicitly, so using
 * the middleware's provided Page<I> won't work.
 */
@Introspected
@JsonInclude(NON_NULL)
data class SignHereAgreementPage(
   val elements: ArrayList<DocumentSignatureRequestDto> = ArrayList(),
   val requested: DocumentPageRequest = DocumentPageRequest(),
   val totalElements: Long = 0,
   val totalPages: Int = 0,
   val first: Boolean = true,
   val last: Boolean = true,
   val notEmpty: Boolean = false,
) {
   fun <N> mapElements(mapper: (i: DocumentSignatureRequestDto) -> N): Page<N> =
      Page(
         elements = elements.map(mapper),
         requested = this.requested,
         totalElements = this.totalElements
      )
}
