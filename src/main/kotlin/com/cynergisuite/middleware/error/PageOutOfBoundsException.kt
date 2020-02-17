package com.cynergisuite.middleware.error

import com.cynergisuite.domain.PageRequest

class PageOutOfBoundsException(
   val pageRequest: PageRequest
) : Exception("$pageRequest")
