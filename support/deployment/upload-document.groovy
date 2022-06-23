#!/opt/cyn/v01/cynmid/groovy/bin/groovy -cp /opt/cyn/v01/cynmid/cynergi-middleware.jar
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.slf4j', module = 'slf4j-simple', version = '1.7.36')
@Grab(group = 'org.slf4j', module = 'jul-to-slf4j', version = '1.7.36')
@Grab(group = 'org.apache.httpcomponents.core5', module = 'httpcore5', version='5.1.3')
@Grab(group = 'org.apache.httpcomponents.client5', module = 'httpclient5', version = '5.1.3')
@Grab(group = 'info.picocli', module = 'picocli', version = '4.6.3')
@Grab(group = 'info.picocli', module = 'picocli-groovy', version = '4.6.3')
@Grab(group = 'org.apache.groovy', module = 'groovy-json', version = '4.0.3')
@Grab(group = 'com.google.guava', module = 'guava', version = '31.1-jre')
@picocli.CommandLine.Command(
   description = "Sign Here Please agreement upload script"
)
@picocli.groovy.PicocliScript
import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.middleware.agreement.signing.AgreementSigningDTO
import com.cynergisuite.middleware.agreement.signing.infrastructure.AgreementSigningPageRequest
import com.cynergisuite.middleware.company.CompanyEntity
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import groovy.transform.Field
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.util.UUID
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

System.setProperty("logback.configurationFile", "/tmp/document-upload.xml")

if (!helpRequested) {
   if (debug) {
      System.properties['org.slf4j.simpleLogger.log.org.apache.hc.client5.http.wire'] = 'trace'
   }
   final escaper = UrlEscapers.urlPathSegmentEscaper()
   final jsonSlurper = new JsonSlurper();
   try (final client = HttpClients.createDefault()) {
      final storeTokenCall = new HttpGet("http://localhost:10900/sign/here/token/store/${storeNumber}/dataset/${dataset}")
      final storeTokenResponse = client.execute(storeTokenCall)
      final storeTokenSlurp = jsonSlurper.parse(storeTokenResponse.entity.content)
      final company = storeTokenSlurp.company
      final companyUUID = UUID.fromString(company.id)
      final currentCompany = new CompanyEntity(companyUUID, company.name, null, company.clientCode, company.clientId, company.datasetCode, company.federalIdNumber)
      println currentCompany
      println "after setting currentCompany"
      final storeToken = storeTokenSlurp.token
      println "after setting storeToken"

      storeTokenResponse.close()
      println storeToken

      if (!signaturePdf.exists() || !signaturePdf.isFile()) {
         println "${signaturePdf} did not exist or is not a file"
      } else {
         final tokenRequest = new JsonBuilder([token: storeToken])
         final tokenLoginRequest = new HttpPost(URI.create("${host}/api/login/token"))
         tokenLoginRequest.setHeader("Content-Type", "application/json")
         tokenLoginRequest.setEntity(new StringEntity(tokenRequest.toString()))
         println tokenLoginRequest
         final accessToken =  client.execute(tokenLoginRequest).withCloseable {
            tokenLoginResponse ->
               if (tokenLoginResponse.code == 200) {
                  return jsonSlurper.parse(tokenLoginResponse.entity.content).access_token
               } else {
                  println "Unable to login with access token"
                  return null
               }
         }
         println accessToken

         if (accessToken != null) {
            final signers = "signer=" + signatories.toList().withIndex().collect { element, index -> "${element}[${index + 1}]" }.join("&signer=")

            //The below Post is hitting DocumentController from high-touch-sign
            final uploadDocumentRequest = new HttpPost("${host}/api/document/${escaper.escape(name)}/${escaper.escape(reason)}/${escaper.escape(location)}/${escaper.escape(contactInfo)}?${signers}")
            println uploadDocumentRequest

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

            if (uploadResponseCode == 200 || uploadResponseCode == 201) {
               println "past 201"

               //This is where we check then insert/update the agreement_signing table. Must handle rto, club, and/or other.
               if (rtoAgreementNumber != null) {
                  final checkExisting = new HttpGet("http://localhost:10900/upsertPrep/${dataset}/${primaryCustomerNumber}/${rtoAgreementNumber}")
                  final existingRtoAgreement = client.execute(checkExisting)

                  final existingRtoAgreementJson = jsonSlurper.parse(existingRtoAgreement.entity.content)

                  final existingRtoAgreementId = existingRtoAgreementJson.id

                  final storeDTO = new SimpleLegacyNumberDTO(storeNumber)

                  final existingRtoAgreementUUID = UUID.fromString(existingRtoAgreementId)
                  //final agreementToUpsert = new AgreementSigningDTO(null, currentCompany, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, rtoAgreementNumber, "R", 1, awsResponse)
                  final agreementToUpsert = new AgreementSigningDTO(existingRtoAgreementUUID, currentCompany, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, rtoAgreementNumber, "R", 1, awsResponse)
                  final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                  println agreementToUpsertJson

                  //TODO Not sure if this check will work yet
                  if (existingRtoAgreementId != null) {
                  //Update the record
                    final updateRtoAgreement = new HttpPut("http://localhost:10900/agreement/signing/${existingRtoAgreementUUID}/dataset/${dataset}")
                    updateRtoAgreement.setHeader("Content-Type", "application/json")
                    updateRtoAgreement.setEntity(new StringEntity(agreementToUpsertJson))
                    final updateResponse = client.execute(updateRtoAgreement)
                    println updateResponse
                    println "Update"
                  } else {
                  //Insert the record
                     final createRtoAgreement = new HttpPost("http://localhost:10900/agreement/signing/dataset/${dataset}")
                     createRtoAgreement.setHeader("Content-Type", "application/json")
                     createRtoAgreement.setEntity(new StringEntity(agreementToUpsertJson))
                     final createResponse = client.execute(createRtoAgreement)
                     println createResponse
                     println "Insert"
                  }
               }

            } else {
               println "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
            }
            uploadResponse.close()
         } else {
            println "Invalid token provided for uploading"
         }
      }
   }
}
