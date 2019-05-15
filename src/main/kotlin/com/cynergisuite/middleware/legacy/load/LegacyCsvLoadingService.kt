package com.cynergisuite.middleware.legacy.load

import java.io.Reader
import java.nio.file.Path

interface LegacyCsvLoadingService {
   fun canProcess(path: Path): Boolean
   fun processCsv(reader: Reader)
}
