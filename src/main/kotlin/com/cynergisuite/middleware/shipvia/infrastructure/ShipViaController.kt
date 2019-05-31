package com.cynergisuite.middleware.shipvia.infrastructure

import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.shipvia.*
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid

@Validated
@Secured(IS_ANONYMOUS)
@Controller("/api/shipvia")
class ShipViaController @Inject constructor(
   private val shipViaService: ShipViaService,
   private val shipViaValidator: ShipViaValidator
) {
   private val logger: Logger = LoggerFactory.getLogger(ShipViaController::class.java)

   @Throws(NotFoundException::class)
   @Get("/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): ShipViaValueObject {
      logger.info("Fetching ShipVia by {}", id)
      val response = shipViaService.fetchById(id = id) ?: throw NotFoundException(id)
      logger.debug("Fetch ShipVia by {} resulted {}", id, response)
      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun save(
      @Valid @Body vo: ShipViaValueObject
   ): ShipViaValueObject {
      logger.info("Requested Save ShipVia {}", vo)
      shipViaValidator.validateSave(vo = vo)
      val response = shipViaService.create(vo = vo)
      logger.debug("Requested Save ShipVia {} resulted in {}", vo, response)
      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun update(
      @Valid @Body vo: ShipViaValueObject
   ): ShipViaValueObject {
      logger.info("Requested Update ShipVia {}", vo)
      shipViaValidator.validateUpdate(vo = vo)
      val response = shipViaService.update(vo = vo)
      logger.debug("Requested Update ShipVia {} resulted in {}", vo, response)
      return response
   }

}
