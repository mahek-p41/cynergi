package com.cynergisuite.domain

interface IdentifiableDataTransferObject: Identifiable {

   fun dtoId(): Long?

   override fun myId(): Long? = dtoId()
}
