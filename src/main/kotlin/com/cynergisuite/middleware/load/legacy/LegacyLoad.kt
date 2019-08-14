package com.cynergisuite.middleware.load.legacy

import com.cynergisuite.domain.Entity
import java.nio.file.Path
import java.time.OffsetDateTime
import java.util.UUID

data class LegacyLoad(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val filename: Path,
   val hash: String
) : Entity<LegacyLoad> {

   constructor(
      filename: Path,
      hash: String
   ) : this(
         id = null,
         filename = filename,
         hash = hash
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): LegacyLoad = copy()
}
