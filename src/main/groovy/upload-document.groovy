@picocli.CommandLine.Command(
   description = "Sign Here Please agreement upload script"
)
@picocli.groovy.PicocliScript2
import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.middleware.agreement.signing.AgreementSigningDTO
import com.cynergisuite.middleware.company.CompanyEntity
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.lang3.StringUtils
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import groovy.transform.Field
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import com.google.common.net.UrlEscapers
import org.slf4j.bridge.SLF4JBridgeHandler
import org.apache.hc.client5.http.entity.mime.FileBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.classic.methods.HttpPut
import org.apache.hc.core5.http.io.entity.StringEntity

SLF4JBridgeHandler.removeHandlersForRootLogger()
SLF4JBridgeHandler.install()

@Option(names = ["-H", "--host"], defaultValue = "https://app.htsigntest.click", description = "host and protocol to call")
@Field String host = "https://app.htsigntest.click"

@Option(names = ["-h", "--help"], usageHelp = true, description = "Show this help message and exit")
@Field boolean helpRequested = false

@Option(names = ["-d", "--debug"], description = "Enable debug logging")
@Field boolean debug = false

@Parameters(index = "0", arity = "1", paramLabel = "argsFile", description = "Arguments to be passed to the signature service")
@Field File argsFile

if (!helpRequested) {
   if (debug) {
      System.properties['org.slf4j.simpleLogger.log.org.apache.hc.client5.http.wire'] = 'trace'
      System.properties['logback.configurationFile'] = 'logback-debug-stdout.xml'
   }

   if (argsFile.exists() && argsFile.isFile()) {
      try (final argsReader = new FileReader(argsFile)) {
         final escaper = UrlEscapers.urlPathSegmentEscaper()

         try (final csvParser = new CSVParser(argsReader, CSVFormat.EXCEL.builder().setHeader().setDelimiter('|').build())) {
            final csvData = csvParser.first() // just grab the first record after the head
            final dataset = csvData["dataset"].toString().trim()
            final storeNumber = Integer.valueOf(csvData["storeNumber"].trim())
            final pdfFile = csvData["signaturePdf"].toString().trim()
            final signaturePdf = new File(pdfFile)
            final name = escaper.escape(csvData["name"].toString().trim())
            final reason = escaper.escape(csvData["reason"].toString().trim())
            final location = escaper.escape(csvData["location"].toString().trim())
            final contactInfo = escaper.escape(csvData["contactInfo"].toString().trim())
            final primaryCustomerNumber = Integer.valueOf(csvData["primaryCustomerNumber"].trim())
            final secondaryCustomerNumber = Integer.valueOf(csvData["secondaryCustomerNumber"].trim())
            final rtoAgreementNumber = Integer.valueOf(csvData["rtoAgreementNumber"].trim())
            final clubAgreementNumber = Integer.valueOf(csvData["clubAgreementNumber"].trim())
            final otherAgreementNumber = Integer.valueOf(csvData["otherAgreementNumber"].trim())
            final signatories = StringUtils.split(csvData["signatories"].trim(), ',').collect {it.trim()}

            final expirationDate = (csvData["retentionDate"].toString().trim())
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy", Locale.ENGLISH);
            LocalDate retentionDate = LocalDate.parse(expirationDate, formatter);

            final jsonSlurper = new JsonSlurper()

            try (final client = HttpClients.createDefault()) {
               final storeTokenCall = new HttpGet("http://localhost:10900/sign/here/token/store/${storeNumber}/dataset/${dataset}")
               final storeTokenResponse = client.execute(storeTokenCall)
               final storeTokenSlurp = jsonSlurper.parse(storeTokenResponse.entity.content)
               final company = storeTokenSlurp.company
               final companyUUID = UUID.fromString(company.id)
               final currentCompany = new CompanyEntity(companyUUID, company.name, null, company.clientCode, company.clientId, company.datasetCode, company.federalIdNumber)
               final storeToken = storeTokenSlurp.token

               storeTokenResponse.close()

               if (!signaturePdf.exists() || !signaturePdf.isFile()) {
                  println "${signaturePdf} did not exist or is not a file"
               } else {
                  final tokenRequest = new JsonBuilder([token: storeToken])
                  final tokenLoginRequest = new HttpPost(URI.create("${host}/api/login/token"))
                  tokenLoginRequest.setHeader("Content-Type", "application/json")
                  tokenLoginRequest.setEntity(new StringEntity(tokenRequest.toString()))

                  final accessToken = client.execute(tokenLoginRequest).withCloseable {tokenLoginResponse ->
                     if (tokenLoginResponse.code == 200) {
                        return jsonSlurper.parse(tokenLoginResponse.entity.content).access_token
                     } else {
                        println "Unable to login to sign here system with access token. ${tokenLoginResponse.reasonPhrase} ${EntityUtils.toString(tokenLoginResponse.entity)}"
                     }
                  }

                  if (accessToken != null) {
                     final signers = "signer=" + signatories.toList().withIndex().collect {element, index -> "${element}[${index + 1}]" }.join("&signer=")

                     //The below Post is hitting DocumentController from high-touch-sign
                     final uploadDocumentRequest = new HttpPost("${host}/api/document/${name}/${reason}/${location}/${contactInfo}/${retentionDate}?${signers}")

                     final pdfBody = new FileBody(signaturePdf, ContentType.APPLICATION_PDF, signaturePdf.name)
                     final requestEntity = MultipartEntityBuilder.create().addPart("file", pdfBody).build()

                     uploadDocumentRequest.setHeader("Authorization", "Bearer ${accessToken}")
                     uploadDocumentRequest.setHeader("X-Sig-Meta-Type", "agreement")
                     uploadDocumentRequest.setHeader("X-Sig-Meta-Store", storeNumber.toString())
                     uploadDocumentRequest.setHeader("X-Sig-Meta-Dataset", dataset)
                     uploadDocumentRequest.setHeader("X-Sig-Meta-Name", name)
                     uploadDocumentRequest.setHeader("X-Sig-Meta-Agreement-No", rtoAgreementNumber.toString())
                     uploadDocumentRequest.setHeader("X-Sig-Meta-Customer-No", primaryCustomerNumber.toString())
                     uploadDocumentRequest.setEntity(requestEntity)
                     final uploadResponse = client.execute(uploadDocumentRequest)
                     final uploadResponseCode = uploadResponse.getCode()
                     //will get back json:
                     //requestedDocumentId UUID The id for the root of the whole transaction
                     //nextSignatureUri

                     final awsResponse = UUID.fromString(jsonSlurper.parse(uploadResponse.entity.content).requestedDocumentId)

                     if (uploadResponseCode >= 200 && uploadResponseCode < 400) {
                        if (rtoAgreementNumber != 0) { //This is where we check then insert/update the agreement_signing table for rto.
                           final storeDTO = new SimpleLegacyNumberDTO(storeNumber)
                           final checkExisting = new HttpGet("http://localhost:10900/agreement/signing/upsertPrep/${dataset}/${primaryCustomerNumber}/${rtoAgreementNumber}")
                           final existingRtoAgreement = client.execute(checkExisting)
                           final existingRtoAgreementResponseCode = existingRtoAgreement.getCode()

                           if (existingRtoAgreementResponseCode == 404) {
                              //Insert the record
                              final agreementToUpsert = new AgreementSigningDTO(null, currentCompany, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, rtoAgreementNumber, "R", 1, awsResponse)
                              final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                              final createRtoAgreement = new HttpPost("http://localhost:10900/agreement/signing/dataset/${dataset}")
                              createRtoAgreement.setHeader("Content-Type", "application/json")
                              createRtoAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                              final createResponse = client.execute(createRtoAgreement)
                              final responseCode = createResponse.getCode()

                              if (responseCode >= 200 && responseCode < 400) {
                                 println "Successfully created rto agreement"
                              } else {
                                 println "${uploadResponse.getCode()} -> ${EntityUtils.toString(createResponse.getEntity())}"
                                 System.exit(-2)
                              }
                           } else if (existingRtoAgreementResponseCode == 200) {
                              //Update the record
                              final existingRtoAgreementJson = jsonSlurper.parse(existingRtoAgreement.entity.content)
                              final existingRtoAgreementId = existingRtoAgreementJson.id
                              final existingRtoAgreementUUID = UUID.fromString(existingRtoAgreementId)
                              final agreementToUpsert = new AgreementSigningDTO(existingRtoAgreementUUID, currentCompany, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, rtoAgreementNumber, "R", 1, awsResponse)
                              final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                              final updateRtoAgreement = new HttpPut("http://localhost:10900/agreement/signing/${existingRtoAgreementUUID}/dataset/${dataset}")
                              updateRtoAgreement.setHeader("Content-Type", "application/json")
                              updateRtoAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                              final updateResponse = client.execute(updateRtoAgreement)
                              final responseCode = updateResponse.getCode()

                              if (responseCode >= 200 && responseCode < 400) {
                                 println "Successfully updated rto agreement"
                              } else {
                                 println "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                                 System.exit(-3)
                              }
                           } else {
                              println "Unhandled response from server $existingRtoAgreementResponseCode"
                              System.exit(-4)
                           }
                        }

                        if (clubAgreementNumber != 0) { //This is where we check then insert/update the agreement_signing table for club.
                           final storeDTO = new SimpleLegacyNumberDTO(storeNumber)
                           final checkExisting = new HttpGet("http://localhost:10900/agreement/signing/upsertPrep/${dataset}/${primaryCustomerNumber}/${clubAgreementNumber}")
                           final existingClubAgreement = client.execute(checkExisting)
                           final existingClubAgreementResponseCode = existingClubAgreement.getCode()
                           var clubOrLoaner = "C"
                           if (reason == "Loaner Agreement") {
                              clubOrLoaner = "L"
                           }

                           if (existingClubAgreementResponseCode == 404) {
                              //Insert the record
                              final agreementToUpsert = new AgreementSigningDTO(null, currentCompany, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, clubAgreementNumber, clubOrLoaner, 1, awsResponse)
                              final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                              final createClubAgreement = new HttpPost("http://localhost:10900/agreement/signing/dataset/${dataset}")
                              createClubAgreement.setHeader("Content-Type", "application/json")
                              createClubAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                              final createResponse = client.execute(createClubAgreement)
                              final responseCode = createResponse.getCode()

                              if (responseCode >= 200 && responseCode < 400) {
                                 println "Successfully created club agreement"
                              } else {
                                 println "${uploadResponse.getCode()} -> ${EntityUtils.toString(createResponse.getEntity())}"
                                 System.exit(-5)
                              }
                           } else if (existingClubAgreementResponseCode == 200) {
                              //Update the record
                              final existingClubAgreementJson = jsonSlurper.parse(existingClubAgreement.entity.content)
                              final existingClubAgreementId = existingClubAgreementJson.id
                              final existingClubAgreementUUID = UUID.fromString(existingClubAgreementId)
                              final agreementToUpsert = new AgreementSigningDTO(existingClubAgreementUUID, currentCompany, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, clubAgreementNumber, clubOrLoaner, 1, awsResponse)
                              final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                              final updateClubAgreement = new HttpPut("http://localhost:10900/agreement/signing/${existingClubAgreementUUID}/dataset/${dataset}")
                              updateClubAgreement.setHeader("Content-Type", "application/json")
                              updateClubAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                              final updateResponse = client.execute(updateClubAgreement)
                              final responseCode = updateResponse.getCode()

                              if (responseCode >= 200 && responseCode < 400) {
                                 println "Successfully updated club agreement"
                              } else {
                                 println "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                                 System.exit(-6)
                              }
                           } else {
                              println "Unhandled response from server $existingRtoAgreementResponseCode"
                              System.exit(-7)
                           }
                        }

                        if (otherAgreementNumber != 0) { //This is where we check then insert/update the agreement_signing table for other.
                           final storeDTO = new SimpleLegacyNumberDTO(storeNumber)
                           final checkExisting = new HttpGet("http://localhost:10900/agreement/signing/upsertPrep/${dataset}/${primaryCustomerNumber}/${otherAgreementNumber}")
                           final existingOtherAgreement = client.execute(checkExisting)
                           final existingOtherAgreementResponseCode = existingOtherAgreement.getCode()

                           if (existingOtherAgreementResponseCode == 404) {
                              //Insert the record
                              final agreementToUpsert = new AgreementSigningDTO(null, currentCompany, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, otherAgreementNumber, "O", 1, awsResponse)
                              final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                              final createOtherAgreement = new HttpPost("http://localhost:10900/agreement/signing/dataset/${dataset}")
                              createOtherAgreement.setHeader("Content-Type", "application/json")
                              createOtherAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                              final createResponse = client.execute(createOtherAgreement)
                              final responseCode = createResponse.getCode()

                              if (responseCode >= 200 && responseCode < 400) {
                                 println "Successfully created other agreement"
                              } else {
                                 println "${uploadResponse.getCode()} -> ${EntityUtils.toString(createResponse.getEntity())}"
                                 System.exit(-8)
                              }
                           } else if (existingOtherAgreementResponseCode == 200) {
                              //Update the record
                              final existingOtherAgreementJson = jsonSlurper.parse(existingOtherAgreement.entity.content)
                              final existingOtherAgreementId = existingOtherAgreementJson.id
                              final existingOtherAgreementUUID = UUID.fromString(existingOtherAgreementId)
                              final agreementToUpsert = new AgreementSigningDTO(existingOtherAgreementUUID, currentCompany, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, otherAgreementNumber, "O", 1, awsResponse)
                              final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                              final updateOtherAgreement = new HttpPut("http://localhost:10900/agreement/signing/${existingOtherAgreementUUID}/dataset/${dataset}")
                              updateOtherAgreement.setHeader("Content-Type", "application/json")
                              updateOtherAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                              final updateResponse = client.execute(updateOtherAgreement)
                              final responseCode = updateResponse.getCode()

                              if (responseCode >= 200 && responseCode < 400) {
                                 println "Successfully updated other agreement"
                              } else {
                                 println "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                                 System.exit(-9)
                              }
                           } else {
                              println "Unhandled response from server $existingRtoAgreementResponseCode"
                              System.exit(-10)
                           }
                        }

                     } else {
                        println "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                        System.exit(-11)
                     }

                     uploadResponse.close()
                  } else {
                     println "Invalid token provided for uploading"
                     System.exit(-12)
                  }
               }
            }
         }
      }
   } else {
      println "${argsFile} is not a csv file"
      System.exit(-13)
   }
}
