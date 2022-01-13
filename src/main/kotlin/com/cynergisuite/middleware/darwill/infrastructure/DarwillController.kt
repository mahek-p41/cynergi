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
      val company = companyService.fetchByDatasetCode(darwillManagementDto.dataset!!) ?: throw NotFoundException(darwillManagementDto.dataset)

      darwillService.enableFor(company, SftpClientCredentials(darwillManagementDto))
   }

   @Delete("/{dataset}")
   fun disableDarwill(
      @Parameter("dataset") dataset: String
   ) {
      val company = companyService.fetchByDatasetCode(dataset) ?: throw NotFoundException(dataset)

      darwillService.disableFor(company)
   }
}
