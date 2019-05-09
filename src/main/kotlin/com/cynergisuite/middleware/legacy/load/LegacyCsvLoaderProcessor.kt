package com.cynergisuite.middleware.legacy.load

import java.io.Reader
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyCsvLoaderProcessor @Inject constructor(
   private val csvLoadingServices: List<LegacyCsvLoadingService>
) {
   fun processCsv(path: Path, reader: Reader) {
      csvLoadingServices.forEach { loadingService ->
         if (loadingService.canProcess(path)) {
            loadingService.processCsv(reader)
         }
      }
   }
}
