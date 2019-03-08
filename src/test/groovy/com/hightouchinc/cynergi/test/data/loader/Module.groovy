package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Menu
import com.hightouchinc.cynergi.middleware.entity.Module
import com.hightouchinc.cynergi.middleware.repository.ModuleRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class ModuleTestDataLoader {
   static Stream<Module> stream(int number = 1, Menu menu = null) {
      final int value = number > 0 ? number : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final Menu moduleMenu = menu != null ? menu : MenuTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Menu") }

      return IntStream.range(0, value).mapToObj {
         new Module(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            lorem.fixedString(6),
            lorem.characters(1, 50),
            moduleMenu
         )
      }
   }
}

@Singleton
@CompileStatic
class ModuleDataLoaderService {
   private final ModuleRepository moduleRepository
   private final MenuDataLoaderService menuDataLoaderService

   ModuleDataLoaderService(ModuleRepository moduleRepository, MenuDataLoaderService menuDataLoaderService) {
      this.moduleRepository = moduleRepository
      this.menuDataLoaderService = menuDataLoaderService
   }

   Stream<Module> stream(int number = 1, Menu menu = null) {
      final Menu moduleMenu = menu != null ? menu : menuDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Menu") }

      return ModuleTestDataLoader.stream(number, moduleMenu)
         .map {
            moduleRepository.insert(it)
         }
   }
}
