package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import com.github.javafaker.Faker
import com.github.javafaker.Lorem
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

@CompileStatic
class ShipViaTestDataLoader {

   static Stream<ShipViaEntity> stream(int numberIn = 1, CompanyEntity company) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()
      final words = new LinkedHashSet()

      return IntStream.range(0, number).mapToObj {
         new ShipViaEntity(
            null,
            findWordThatHaveNotFoundYet(words, lorem),
            it + 1,
            company
         )
      }
   }

   private static String findWordThatHaveNotFoundYet(LinkedHashSet<String> words, Lorem lorem) {
      var word = lorem.word().capitalize()

      while (words.contains(word) || word.size() < 3) {
         word = lorem.word().capitalize()
      }

      words.add(word)

      return word
   }

   static ShipViaEntity single(CompanyEntity company) {
      return stream(company).findFirst().orElseThrow { new Exception("Unable to create ShipViaEntity") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class ShipViaTestDataLoaderService {
   private final ShipViaRepository shipViaRepository

   ShipViaTestDataLoaderService(ShipViaRepository shipViaRepository) {
      this.shipViaRepository = shipViaRepository
   }

   Stream<ShipViaEntity> stream(int numberIn = 1, CompanyEntity company) {
      return ShipViaTestDataLoader.stream(numberIn, company).map {
         shipViaRepository.insert(it)
      }
   }

   ShipViaEntity single(CompanyEntity company) {
      return stream(1, company).findFirst().orElseThrow { new Exception("Unable to create ShipVia") }
   }
}
