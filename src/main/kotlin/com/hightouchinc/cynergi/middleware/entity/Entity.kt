package com.hightouchinc.cynergi.middleware.entity

import java.util.UUID

/**
 * Defines a simple contract for all Entity implementations where the database id can be retrieved in a generic manner.
 * The advantage of defining this as an interface is that a Kotlin Data Class can implement it and not have any
 * collisions with the getters and setters it will automatically generate.
 */
interface Entity : IdentifiableEntity {

   /**
    * retrieve the system generated UUID assigned to each row
    */
   fun rowId(): UUID
}
