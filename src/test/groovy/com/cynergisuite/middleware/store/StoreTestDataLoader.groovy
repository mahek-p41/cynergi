package com.cynergisuite.middleware.store

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.store.infrastructure.StoreRepository
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import kotlin.Pair

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.stream.Stream

import static java.util.stream.Collectors.toList

class StoreTestDataLoader {

   // list of stores defined in cynergi-inittestdb.sql that aren't HOME OFFICE
   private static final List<StoreEntity> stores = [
      new StoreEntity(
         1,
         1,
         "KANSAS CITY",
         null,
         CompanyFactory.tstds1(),
      ),
      new StoreEntity(
         2,
         3,
         "INDEPENDENCE",
         null,
         CompanyFactory.tstds1(),
      ),
      new StoreEntity(
         4,
         1,
         "Pelham Trading Post, Inc",
         null,
         CompanyFactory.tstds2(),
      ),
      new StoreEntity(
         5,
         2,
         "Camilla Trading Post, Inc.",
         null,
         CompanyFactory.tstds2(),
      ),
      new StoreEntity(
         6,
         3,
         "Arlington Trading Post",
         null,
         CompanyFactory.tstds2(),
      ),
      new StoreEntity(
         7,
         4,
         "Moultrie Trading Post, Inc",
         null,
         CompanyFactory.tstds2(),
      ),
      new StoreEntity(
         8,
         5,
         "Bainbridge Trading Post",
         null,
         CompanyFactory.tstds2(),
      )
   ]

   static Store random(CompanyEntity company) {
      return stores.findAll { it.myCompany().datasetCode == company.datasetCode }.random()
   }

   static Store store(int number, CompanyEntity company) {
      return stores.find { it.company.datasetCode == company.datasetCode && it.number == number }
   }

   static List<Store> stores(CompanyEntity company) {
      return stores.findAll { it.company.datasetCode == company.datasetCode } as List<Store>
   }

   static List<Store> storesDevelop(CompanyEntity company) {
      return stores.stream()
         .map { store ->
            switch (store.company.datasetCode) {
               case CompanyFactory.tstds1().datasetCode: return store.copyWithNewCompany(CompanyFactory.corrto())
               case CompanyFactory.tstds2().datasetCode:  return store.copyWithNewCompany(CompanyFactory.corptp())
               default: return store
            }
         }
         .filter { it.company.datasetCode == company.datasetCode }
         .collect(toList())
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class StoreTestDataLoaderService {
   private final StoreRepository storeRepository

   @Inject
   StoreTestDataLoaderService(StoreRepository storeRepository) {
      this.storeRepository = storeRepository
   }

   Store store(int storeNumber, CompanyEntity company) {
      final StoreEntity toReturn = storeRepository.findOne(storeNumber, company)
         ?.with { StoreEntity e ->
            return new StoreEntity(
               e.myId(),
               e.myNumber(),
               e.myName(),
               null,
               e.myCompany(),
            )
         }

      if (toReturn != null) {
         return toReturn
      } else {
         throw new Exception("Unable to find Store")
      }
   }

   Stream<Pair<RegionEntity, Location>> companyStoresToRegion(RegionEntity region, Long limit) {
      return StoreTestDataLoader.stores(region.division.company)
         .limit(limit)
         .map { storeRepository.assignToRegion(it, region, region.division.company.id) }
   }

   List<Pair<RegionEntity, Location>> companyStoresToRegion(RegionEntity region, Store ...stores) {
      return Stream.of(stores).map { storeRepository.assignToRegion(it, region, region.division.company.id) }.collect(toList())
   }

   StoreEntity random(CompanyEntity company) {
      final randomStore = StoreTestDataLoader.random(company)

      assert(company.datasetCode == randomStore.myCompany().datasetCode)

      final store = storeRepository.findOne(randomStore.myNumber(), company)

      if (store == null) {
         throw new Exception("Unable to find StoreEntity")
      }

      assert(store.myCompany().datasetCode == company.datasetCode)

      return store
   }
}
