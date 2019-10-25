package com.cynergisuite.domain

@Deprecated(message = "This class really seems to serve no purpose other than to act as a marker interface.", replaceWith = ReplaceWith("Identifiable", imports = ["com.cynergisuite.domain.Identifiable"]))
interface IdentifiableDataTransferObject: Identifiable {

   fun dtoId(): Long?

   override fun myId(): Long? = dtoId()
}
