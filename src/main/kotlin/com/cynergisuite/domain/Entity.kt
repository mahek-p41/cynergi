package com.cynergisuite.domain

import java.util.UUID

/**
 * Defines a simple contract for all Entity implementations where the database id can be retrieved in a generic manner.
 * The advantage of defining this as an interface is that a Kotlin Data Class can implement it and not have any
 * collisions with the getters and setters it will automatically generate.
 */
interface Entity<ENTITY> : IdentifiableEntity {

   /**
    * retrieve the system generated UUID assigned to each row
    */
   fun rowId(): UUID

   /**
    * Creates a copy of this instance of an Entity.  Contract is the same as a Kotlin data class's copy method with
    * no paramerters.
    */
   fun copyMe(): ENTITY
}
