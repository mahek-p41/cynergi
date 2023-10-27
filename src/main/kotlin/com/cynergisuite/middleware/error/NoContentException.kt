package com.cynergisuite.middleware.error

import org.apache.commons.lang3.StringUtils

class NoContentException(
   private val noContent: String
) : Exception(noContent) {

   constructor(id: Long) :
      this(noContent = id.toString())

   constructor(any: Any = StringUtils.EMPTY) :
      this(noContent = any.toString())
}
