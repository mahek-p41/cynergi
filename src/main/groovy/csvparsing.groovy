import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser

try (final csvReader = new FileReader("/tmp/test.csv")) {
   try (final csvParser = new CSVParser(csvReader, CSVFormat.EXCEL.builder().setHeader().setDelimiter('|').build())) {
      final csvData = csvParser.first()
      final dataset = csvData["dataset"].trim()
      final storeNumber = Integer.valueOf(csvData["storeNumber"].trim())
      final signatories = org.apache.commons.lang3.StringUtils.split(csvData["signatories"].trim(), ',')

      println(dataset)
      println(storeNumber)
      println(signatories)
      println(signatories[0])
      println(signatories[1])
   }
}
