package com.cynergisuite.domain

/**
 * Defines a simple contract where a DTO must provide an ID in the form of a long or null if it hasn't been assigned one yet
 */
interface IdentifiableValueObject {

   /**
    * retrieve the primary key ID assigned by the sequence the associated table is using
    *
    * @return Long that is the ID/Primary Key of the table assigned to the row that an Entity represents
    */
   fun valueObjectId(): Long?
}
