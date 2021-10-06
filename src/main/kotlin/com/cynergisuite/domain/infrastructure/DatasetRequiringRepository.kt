package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.company.CompanyEntity

interface DatasetRequiringRepository {
   fun exists(id: Long, company: CompanyEntity): Boolean
}
