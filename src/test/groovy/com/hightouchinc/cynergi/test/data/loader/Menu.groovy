package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Menu
import com.hightouchinc.cynergi.middleware.repository.MenuRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class MenuTestDataLoader {
   static Stream<Menu> stream(int number = 1) {
      final int value = number > 0 ? number : 1
      final faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, value).mapToObj {
         new Menu(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            lorem.fixedString(6),
            lorem.characters(1, 50)
         )
      }
   }
}

@Singleton
@CompileStatic
class MenuDataLoaderService {
   private final MenuRepository menuRepository

   MenuDataLoaderService(MenuRepository menuRepository) {
      this.menuRepository = menuRepository
   }

   Stream<Menu> stream(int number = 1) {
      return MenuTestDataLoader.stream(number)
         .map {
            menuRepository.insert(it)
         }
   }
}
