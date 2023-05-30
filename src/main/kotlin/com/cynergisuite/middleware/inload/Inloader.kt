package com.cynergisuite.middleware.inload

import java.io.BufferedReader
import java.nio.file.Path

interface Inloader {
   fun canProcess(path: Path): Boolean
   fun inload(reader: BufferedReader): Int
}
