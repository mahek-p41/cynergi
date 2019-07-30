package com.cynergisuite.middleware.load.legacy

import java.io.Reader
import java.nio.file.Path

interface LegacyCsvLoadingService {
   fun canProcess(path: Path): Boolean
   fun processCsv(reader: Reader)
}
