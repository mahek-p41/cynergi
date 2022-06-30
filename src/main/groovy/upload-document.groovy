@picocli.CommandLine.Command(
   description = "Sign Here Please agreement upload script"
)
@picocli.groovy.PicocliScript2
import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.middleware.agreement.signing.AgreementSigningDTO
import com.cynergisuite.middleware.company.CompanyEntity
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import groovy.transform.Field
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
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

@Parameters(index = "0", arity = "1", paramLabel = "dataset", description = "Customer number used to look up agreement")
@Field String dataset

@Parameters(index = "1", arity = "1", paramLabel = "storeNumber", description = "Documents must be stored at least until the retention date")
@Field Integer storeNumber

@Parameters(index = "2", arity = "1", paramLabel = "pdf", description = "PDF to be uploaded for signature")
@Field File signaturePdf

@Parameters(index = "3", arity = "1", paramLabel = "name", description = "Name to be put on the signed document")
@Field String name

@Parameters(index = "4", arity = "1", paramLabel = "reason", description = "Reason to be put on the signed document")
@Field String reason

@Parameters(index = "5", arity = "1", paramLabel = "location", description = "Location to be put on the signed document")
@Field String location

@Parameters(index = "6", arity = "1", paramLabel = "contactInfo", description = "Contact Info to be put on the signed document")
@Field String contactInfo

@Parameters(index = "7", arity = "1", paramLabel = "primaryCustomerNumber", description = "Primary customer number used to look up agreement")
@Field Integer primaryCustomerNumber

@Parameters(index = "8", arity = "1", paramLabel = "secondaryCustomerNumber", description = "Secondary customer number used to look up agreement")
@Field Integer secondaryCustomerNumber

@Parameters(index = "9", arity = "1", paramLabel = "rtoAgreementNumber", description = "RTO agreement number")
@Field Integer rtoAgreementNumber

@Parameters(index = "10", arity = "1..*", paramLabel = "signatories", description = "The people who need to sign the document")
@Field String[] signatories

if (!helpRequested) {
   if (debug) {
      System.properties['org.slf4j.simpleLogger.log.org.apache.hc.client5.http.wire'] = 'trace'
      System.properties['logback.configurationFile'] = 'logback-debug-stdout.xml'
   }

   final escaper = UrlEscapers.urlPathSegmentEscaper()
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

         final accessToken =  client.execute(tokenLoginRequest).withCloseable { tokenLoginResponse ->
            if (tokenLoginResponse.code == 200) {
               return jsonSlurper.parse(tokenLoginResponse.entity.content).access_token
            } else {
               println "Unable to login with access token"
               return null
            }
         }

         if (accessToken != null) {
            final signers = "signer=" + signatories.toList().withIndex().collect { element, index -> "${element}[${index + 1}]" }.join("&signer=")

            //The below Post is hitting DocumentController from high-touch-sign
            final uploadDocumentRequest = new HttpPost("${host}/api/document/${escaper.escape(name)}/${escaper.escape(reason)}/${escaper.escape(location)}/${escaper.escape(contactInfo)}?${signers}")

            final pdfBody = new FileBody(signaturePdf, ContentType.APPLICATION_PDF, signaturePdf.name)
            final requestEntity = MultipartEntityBuilder.create().addPart("file", pdfBody).build()

            uploadDocumentRequest.setHeader("Authorization", "Bearer ${accessToken}")
            uploadDocumentRequest.setEntity(requestEntity)
            final uploadResponse = client.execute(uploadDocumentRequest)
            final uploadResponseCode = uploadResponse.getCode()
            //will get back json:
            //requestedDocumentId UUID The id for the root of the whole transaction
            //nextSignatureUri

            final awsResponse = jsonSlurper.parse(uploadResponse.entity.content).nextSignatureUri

            if (uploadResponseCode >= 200 && uploadResponseCode < 400) {
               if (rtoAgreementNumber != null) { //This is where we check then insert/update the agreement_signing table. Must handle rto, club, and/or other.
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
                        println "Successfully created agreement"
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
                        println "Successfully updated agreement"
                     } else {
                        println "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                        System.exit(-1)
                     }
                  } else {
                     println "Unhandled response from server $existingRtoAgreementResponseCode"
                     System.exit(-3)
                  }
               }

            } else {
               println "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
            }

            uploadResponse.close()
         } else {
            println "Invalid token provided for uploading"
            System.exit(-3)
         }
      }
   }
}
