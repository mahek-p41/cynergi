package com.cynergisuite.middleware.darwill.infrastructure

import com.cynergisuite.middleware.company.CompanyService
import com.cynergisuite.middleware.darwill.DarwillManagementDto
import com.cynergisuite.middleware.darwill.DarwillService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.ssh.SftpClientCredentials
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import jakarta.inject.Inject
import java.util.UUID
import javax.validation.Valid

@Secured(IS_ANONYMOUS)
@Controller("/manage/darwill")
class DarwillController @Inject constructor(
   private val companyService: CompanyService,
   private val darwillService: DarwillService,
) {

   @Post(consumes = [APPLICATION_JSON])
   fun enableDarwill(
      @Valid @Body darwillManagementDto: DarwillManagementDto
   ) {
      val company = companyService.fetchOne(darwillManagementDto.companyId!!) ?: throw NotFoundException(darwillManagementDto.companyId)

      darwillService.enableFor(company, SftpClientCredentials(darwillManagementDto))
   }

   @Delete("/{companyId}")
   fun disableDarwill(
      @Parameter("companyId") companyId: UUID
   ) {
      val company = companyService.fetchOne(companyId) ?: throw NotFoundException(companyId)

      darwillService.disableFor(company)
   }
}
