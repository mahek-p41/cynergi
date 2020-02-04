package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.CSVParsingService
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.load.legacy.LegacyCsvLoadingService
import com.cynergisuite.middleware.shipvia.infrastructure.ShipViaRepository
import io.micronaut.validation.Validated
import org.apache.commons.csv.CSVRecord
import java.nio.file.FileSystems
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class ShipViaService @Inject constructor(
   private val shipViaRepository: ShipViaRepository,
   private val shipViaValidator: ShipViaValidator
) : CSVParsingService(), LegacyCsvLoadingService {
   private val shipViaMatcher = FileSystems.getDefault().getPathMatcher("glob:eli-shipVia*csv")

   fun fetchById(id: Long): ShipViaValueObject? =
      shipViaRepository.findOne(id = id)?.let{ShipViaValueObject(entity = it)}

   @Validated
   fun fetchAll(@Valid pageRequest: PageRequest): Page<ShipViaValueObject> {
      val found = shipViaRepository.findAll(pageRequest)

      return found.toPage { shipVia: ShipViaEntity ->
         ShipViaValueObject(shipVia)
      }
   }

   fun exists(id: Long): Boolean =
      shipViaRepository.exists(id = id)

   @Validated
   fun create(@Valid vo: ShipViaValueObject): ShipViaValueObject {
      shipViaValidator.validateCreate(vo)

      return ShipViaValueObject(
         entity = shipViaRepository.insert(entity = ShipViaEntity(vo = vo))
      )
   }

   @Validated
   fun update(@Valid vo: ShipViaValueObject): ShipViaValueObject {
      shipViaValidator.validateUpdate(vo)

      return ShipViaValueObject(
         entity = shipViaRepository.update(entity = ShipViaEntity(vo = vo))
      )
   }

   override fun canProcess(path: Path): Boolean =
      shipViaMatcher.matches(path.fileName)

   override fun processCsvRow(record: CSVRecord) {
      create (
         ShipViaValueObject(
            description = record.get("description")
         )
      )
   }
}
