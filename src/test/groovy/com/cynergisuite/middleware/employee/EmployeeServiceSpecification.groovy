package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import io.micronaut.test.annotation.MicronautTest
import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.springframework.jdbc.core.JdbcTemplate

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Paths

@MicronautTest(transactional = false)
class EmployeeServiceSpecification extends ServiceSpecificationBase {
   @Rule TemporaryFolder temporaryFolder

   @Inject EmployeeService employeeService
   @Inject JdbcTemplate jdbc

   void "eli path glob"() {
      expect:
      check == employeeService.canProcess(path)

      where:
      path                                                           | check
      Paths.get("import/eli-employee-new-stuff.csv")            | true
      Paths.get("import/eli-address.csv")                       | false
      Paths.get("import/eli-employee-new-stuff.csv.processed")  | false
   }

   void "csv process empty file"() {
      given:
      def tempDirectory = temporaryFolder.newFolder("eli-employees").toPath()
      def tempFile = tempDirectory.resolve("eli-employees.csv")
      FileUtils.touch(tempFile.toFile()) // create an empty file
      def reader = Files.newBufferedReader(tempFile)

      when:
      employeeService.processCsv(reader)

      then:
      0 == jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer)

      cleanup:
      reader.close()
   }

   void "csv process file with 3 record" () {
      given:
      def tempDirectory = temporaryFolder.newFolder("eli-employees").toPath()
      def tempFile = tempDirectory.resolve("eli-employees.csv")
      def testEmployeeFile = Paths.get(EmployeeServiceSpecification.class.classLoader.getResource("legacy/load/employee/eli-employee.csv").toURI())
      Files.copy(testEmployeeFile, tempFile)
      def reader = Files.newBufferedReader(tempFile)

      when:
      employeeService.processCsv(reader)

      then:
      3 == jdbc.queryForObject("SELECT COUNT(*) FROM employee", Integer)
      [
          ["id": 1, "number": "123", "pass_code": "tryme", "active": true ],
          ["id": 2, "number": "987", "pass_code": "hrdpas", "active": true ],
          ["id": 3, "number": "4500", "pass_code": "psswrd", "active": true ]
      ] == jdbc.queryForList("SELECT id, number, pass_code, active FROM employee")

      cleanup:
      reader.close()
   }
}
