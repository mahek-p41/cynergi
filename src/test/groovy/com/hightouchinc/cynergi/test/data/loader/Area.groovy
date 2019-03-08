package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Area
import com.hightouchinc.cynergi.middleware.entity.Menu
import com.hightouchinc.cynergi.middleware.repository.AreaRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class AreaTestDataLoader {
   static Stream<Area> stream(int number = 1, Menu menu = null) {
      final int value = number > 0 ? number : 1
      final areaMenu = menu != null ? menu : MenuTestDataLoader.single()
      final Faker faker = new Faker()
      final random = faker.number()

      return IntStream.range(0, value).mapToObj {
         new Area(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            areaMenu,
            random.numberBetween(1, 99)
         )
      }
   }

   static Area single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to create Area") }
   }
}

@Singleton
@CompileStatic
class AreaDataLoaderService {
   private final AreaRepository areaRepository
   private final MenuDataLoaderService menuDataLoaderService

   AreaDataLoaderService(AreaRepository areaRepository, MenuDataLoaderService menuDataLoaderService) {
      this.areaRepository = areaRepository
      this.menuDataLoaderService = menuDataLoaderService
   }

   Stream<Area> stream(int number = 1, Menu menu = null) {
      final Menu areaMenu = menu != null ? menu : menuDataLoaderService.single()

      return AreaTestDataLoader.stream(number, areaMenu)
         .map {
            areaRepository.insert(it)
         }
   }

   Area single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to create Area") }
   }
}
