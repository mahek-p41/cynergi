package com.cynergisuite.common.exceptions

import com.cynergisuite.domain.PageRequest

class PageOutOfBoundsException(
   val pageRequest: PageRequest
) : Exception("$pageRequest")
