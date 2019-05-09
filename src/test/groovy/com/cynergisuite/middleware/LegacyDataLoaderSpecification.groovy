package com.cynergisuite.middleware

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.legacy.load.LegacyCsvLoaderProcessor
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import org.apache.commons.io.IOUtils
import org.apache.commons.io.output.NullOutputStream

import javax.inject.Inject

@MicronautTest(transactional = false)
class LegacyDataLoaderSpecification extends ServiceSpecificationBase {

   @MockBean(LegacyCsvLoaderProcessor)
   LegacyCsvLoaderProcessor legacyCsvLoaderProcessor() {
      def mock = Mock(LegacyCsvLoaderProcessor)

      mock.processCsv << { path, Reader reader ->
         IOUtils.copy(reader, new OutputStreamWriter(new NullOutputStream()))
      }

      return mock
   }

   @Inject LegacyDataLoader legacyDataLoader

   void "test" () {
      expect:
      1 != 1
   }

}
