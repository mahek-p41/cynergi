package com.cynergisuite.domain

/**
 * Defines a contract where the implementing entity will be required to provide an ID that is a Long
 */
interface IdentifiableEntity {

   /**
    * retrieve the primary key ID assigned by the sequence the associated table is using
    *
    * @return Long that is the ID/Primary Key of the table assigned to the row that an Entity represents
    */
   fun entityId(): Long?
}
