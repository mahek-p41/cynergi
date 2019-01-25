package com.hightouchinc.cynergi.middleware.entity

interface TypeDomainEntity<ENTITY>: Entity<ENTITY> {

   fun myValue(): String

   fun myDescription(): String

   fun basicEquality(typeDomainEntity: TypeDomainEntity<ENTITY>): Boolean =
      this.entityId() == typeDomainEntity.entityId() &&
         this.myValue() == typeDomainEntity.myValue() &&
         this.myDescription() == typeDomainEntity.myDescription()
}
