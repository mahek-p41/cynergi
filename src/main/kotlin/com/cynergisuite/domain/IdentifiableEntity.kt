package com.cynergisuite.domain

/**
 * Defines a contract where the implementing entity will be required to provide an ID that is a Long
 */
@Deprecated(message = "This class really seems to serve no purpose other than to act as a marker interface.", replaceWith = ReplaceWith("Identifiable", imports = ["com.cynergisuite.domain.Identifiable"]))
interface IdentifiableEntity: Identifiable {

   /**
    * retrieve the primary key ID assigned by the sequence the associated table is using
    *
    * @return Long that is the ID/Primary Key of the table assigned to the row that an Entity represents
    */
   fun entityId(): Long?

   override fun myId(): Long? = entityId()
}
