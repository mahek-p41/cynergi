package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.accounting.account.AccountService
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import io.micronaut.context.annotation.Value
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.ProcessExecutor
import java.io.File
import java.io.FileWriter
import java.util.UUID
import java.util.concurrent.TimeUnit

@Singleton
class ShipViaService @Inject constructor(
   private val shipViaRepository: ShipViaRepository,
   private val shipViaValidator: ShipViaValidator,
   @Value("\${cynergi.process.update.isam.shipvia}") private val processUpdateIsamShipVia: Boolean
) {
   private val logger: Logger = LoggerFactory.getLogger(AccountService::class.java)

   fun fetchById(id: UUID, company: CompanyEntity): ShipViaDTO? =
      shipViaRepository.findOne(id, company)?.let { ShipViaDTO(entity = it) }

   fun fetchAll(pageRequest: PageRequest, company: CompanyEntity): Page<ShipViaDTO> {
      val found = shipViaRepository.findAll(pageRequest, company)

      return found.toPage { shipVia: ShipViaEntity ->
         ShipViaDTO(shipVia)
      }
   }

   fun create(dto: ShipViaDTO, company: CompanyEntity): ShipViaDTO {
      val toCreate = shipViaValidator.validateCreate(dto, company)

      val newShipVia = shipViaRepository.insert(entity = toCreate)
      if (processUpdateIsamShipVia) {
         shipViaToISAM("I", newShipVia, company)
      }
      return ShipViaDTO(
         newShipVia
      )
   }

   fun delete(id: UUID, company: CompanyEntity) {
      val deletedShipVia = shipViaRepository.findOne(id, company)
      shipViaRepository.delete(id, company)
      if (processUpdateIsamShipVia) {
         shipViaToISAM("D", deletedShipVia!!, company)
      }
   }

   fun update(dto: ShipViaDTO, company: CompanyEntity): ShipViaDTO {
      val id = dto.id ?: throw NotFoundException(dto.id?.toString() ?: "") // FIXME need to better handle getting ID.  Should alter the UI to pass id as a path param
      val startingShipVia = shipViaRepository.findOne(dto.id!!, company)
      val startingDescription = startingShipVia!!.description
      val toUpdate = shipViaValidator.validateUpdate(id, dto, company)

      val updatedShipVia = shipViaRepository.update(entity = toUpdate)
      if (processUpdateIsamShipVia) {
         shipViaToISAM("U", updatedShipVia, company, startingDescription)
      }
      return ShipViaDTO(
         updatedShipVia
      )
   }

   fun shipViaToISAM(task: String, shipVia: ShipViaEntity, company: CompanyEntity, beginningDescription: String? = null) {
      var fileWriter: FileWriter? = null
      var csvPrinter: CSVPrinter? = null

      var dataset = company.datasetCode

      val fileName = File.createTempFile("mrshipvia", ".csv")

      try {
         fileWriter = FileWriter(fileName)
         csvPrinter = CSVPrinter(fileWriter, CSVFormat.DEFAULT.withDelimiter('|').withHeader("action", "beginning_description", "description", "dummy_field"))

         var data = listOf("action", "beginning_description", "description", "dummy_field")

         data = listOf(
            task,
            beginningDescription ?: " ",
            shipVia.description,
            "1")
         csvPrinter.printRecord(data)

      } catch (e: Exception) {
         logger.error("Error occurred in creating SHIP-VIA csv file!", e)
      } finally {
         try {
            fileWriter!!.flush()
            fileWriter.close()
            csvPrinter!!.close()
            val processExecutor: ProcessExecutor = ProcessExecutor()
               .command("/bin/bash", "/usr/bin/ht.updt_isam_shipvia.sh", fileName.canonicalPath, dataset)
               .exitValueNormal()
               .timeout(5, TimeUnit.SECONDS)
               .readOutput(true)
            logger.debug(processExecutor.execute().outputString())
         } catch (e: Throwable) {
            logger.error("Error occurred in creating SHIP-VIA csv file!", e)
         }
      }
   }
}
