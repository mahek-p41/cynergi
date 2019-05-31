package com.cynergisuite.middleware.error

import com.cynergisuite.domain.PageRequest

class PageOutOfBoundsException(
   val extra: String? = null,
   val pageRequest: PageRequest
): Exception("$pageRequest ${extra ?: ""}")
