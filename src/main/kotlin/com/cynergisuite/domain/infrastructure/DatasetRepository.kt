package com.cynergisuite.domain.infrastructure

interface DatasetRepository {
   fun findDataset(id: Long): String?
}
