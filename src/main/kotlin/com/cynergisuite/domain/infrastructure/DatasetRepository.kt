package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.company.Company

interface DatasetRepository {
   fun existsForCompany(id: Long, company: Company): Boolean
}
