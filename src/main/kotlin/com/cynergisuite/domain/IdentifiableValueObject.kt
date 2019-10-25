package com.cynergisuite.domain

/**
 * Defines a simple contract where a DTO must provide an ID in the form of a long or null if it hasn't been assigned one yet
 */
@Deprecated(message = "This class really seems to serve no purpose other than to act as a marker interface.", replaceWith = ReplaceWith("Identifiable", imports = ["com.cynergisuite.domain.Identifiable"]))
interface IdentifiableValueObject: Identifiable {

   /**
    * retrieve the primary key ID assigned by the sequence the associated table is using
    *
    * @return Long that is the ID/Primary Key of the table assigned to the row that an Entity represents
    */
   fun valueObjectId(): Long?

   override fun myId(): Long? = valueObjectId()
}
