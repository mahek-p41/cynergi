package com.cynergisuite.domain.infrastructure

/**
 * Most services that interact with a [com.cynergisuite.middleware.repository.CrudRepository] are going to want to
 * provide a way to look up the DTO that the caller is interacting with via an ID which this interface defines as being
 * a long.
 *
 * @param DTO defines a DTO of some type
 */
interface IdentifiableService<DTO> {

   /**
    * search for a DTO via some underlying mechanism by an ID defined as a Long
    *
    * @param id the ID of the DTO to be searched for
    * @return DTO an instance of that DTO or null if it was unable to be located
    */
   fun fetchById(id: Long): DTO?

   /**
    * search for the DTO via some underlying mechanism by an ID defined as a Long
    *
    * @param id the ID of the DTO to be searched for
    * @return Boolean with true if a DTO could be found using the provided ID or false if it could not
    */
   fun exists(id: Long): Boolean
}
