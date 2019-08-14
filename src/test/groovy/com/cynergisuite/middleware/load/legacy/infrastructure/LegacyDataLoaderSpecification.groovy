package com.cynergisuite.middleware.load.legacy.infrastructure

import com.cynergisuite.middleware.load.legacy.LegacyCsvLoaderProcessor
import io.micronaut.configuration.dbmigration.flyway.event.MigrationFinishedEvent
import io.micronaut.context.event.ApplicationEventPublisher
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.io.output.NullOutputStream
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LegacyDataLoaderSpecification extends Specification {
   @Rule
   TemporaryFolder temporaryFolder

   void "process valid csv file"() {
      given:
      def migrationFinishedEvent = Mock(MigrationFinishedEvent)
      def applicationEventPublisher = Mock(ApplicationEventPublisher)
      def tempDirectory = temporaryFolder.newFolder()
      def legacyLoadRepository = Mock(LegacyLoadRepository)
      def legacyCsvLoaderProcessor = Mock(LegacyCsvLoaderProcessor)
      def legacyLoader = new LegacyDataLoader(tempDirectory.getAbsolutePath(), true, "processed", true, legacyLoadRepository, legacyCsvLoaderProcessor, applicationEventPublisher)
      def tempCsvFile = new File(tempDirectory, "eli-test.csv")

      when:
      tempCsvFile << "header1,header2\nval1,val2"
      legacyLoader.onApplicationEvent(migrationFinishedEvent)

      then:
      1 * legacyCsvLoaderProcessor.processCsv(_, _) >> {path, Reader reader -> IOUtils.copy(reader, new OutputStreamWriter(new NullOutputStream()))}
      1 * legacyLoadRepository.insert(_)
      !tempCsvFile.exists()
      new File(tempDirectory, "eli-test.csv.processed").exists()
   }

   void "process empty csv file"() {
      def migrationFinishedEvent = Mock(MigrationFinishedEvent)
      def applicationEventPublisher = Mock(ApplicationEventPublisher)
      def tempDirectory = temporaryFolder.newFolder()
      def legacyLoadRepository = Mock(LegacyLoadRepository)
      def legacyCsvLoaderProcessor = Mock(LegacyCsvLoaderProcessor)
      def legacyLoader = new LegacyDataLoader(tempDirectory.getAbsolutePath(), true, "processed", true, legacyLoadRepository, legacyCsvLoaderProcessor, applicationEventPublisher)
      def tempCsvFile = new File(tempDirectory, "eli-test.csv")

      when:
      FileUtils.touch(tempCsvFile)
      legacyLoader.onApplicationEvent(migrationFinishedEvent)

      then:
      0 * legacyCsvLoaderProcessor.processCsv(_, _)
      0 * legacyLoadRepository.insert(_)
      tempCsvFile.exists()
      !new File(tempDirectory, "eli-test.csv.processed").exists()
   }
}
