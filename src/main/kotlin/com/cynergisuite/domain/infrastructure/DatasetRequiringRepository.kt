package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.company.Company

interface DatasetRequiringRepository {
   fun exists(id: Long, company: Company): Boolean
}
