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

System.setProperty("logback.configurationFile", "/tmp/document-upload.xml")

//println reason
//println location
//println contactInfo

if (!helpRequested) {
   if (debug) {
      System.properties['org.slf4j.simpleLogger.log.org.apache.hc.client5.http.wire'] = 'trace'
   }
   final escaper = UrlEscapers.urlPathSegmentEscaper()
   final jsonSlurper = new JsonSlurper();
   try (final client = HttpClients.createDefault()) {
      final storeTokenCall = new HttpGet("http://localhost:10900/sign/here/token/store/${storeNumber}/dataset/${dataset}")
      final storeTokenResponse = client.execute(storeTokenCall)
      final storeToken = jsonSlurper.parse(storeTokenResponse.entity.content).token

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
         //println signatories

         if (accessToken != null) {
            //TODO Where do the email addresses come into play?
            //Changing signers to be the email addresses, when emailAddresses is not null.
            final signers = "signer=" + signatories.toList().withIndex().collect { element, index -> "${element}[${index + 1}]" }.join("&signer=")
            //println signers
            //println reason
            //println location
            //println contactInfo
            //The below Post is hitting DocumentController from high-touch-sign
            final uploadDocumentRequest = new HttpPost("${host}/api/document/${escaper.escape(name)}/${escaper.escape(reason)}/${escaper.escape(location)}/${escaper.escape(contactInfo)}?${signers}")
            println uploadDocumentRequest
            //println signaturePdf.name
            final pdfBody = new FileBody(signaturePdf, ContentType.APPLICATION_PDF, signaturePdf.name)
            final requestEntity = MultipartEntityBuilder.create().addPart("file", pdfBody).build()

            uploadDocumentRequest.setHeader("Authorization", "Bearer ${accessToken}")
            //uploadDocumentRequest.setHeader("Authentication", "Bearer"+"${accessToken}")
            //println "pdf body"
            //println pdfBody
            //println "request Entity"
            //println requestEntity
            uploadDocumentRequest.setEntity(requestEntity)
            //println "upload Document Request"
            //println uploadDocumentRequest
            final uploadResponse = client.execute(uploadDocumentRequest)
            final uploadResponseCode = uploadResponse.getCode()
            //will get back json:
            //requestedDocumentId UUID The id for the root of the whole transaction
            //nextSignatureUri
            //TODO Do we need to store the requestedDocumentId in the postgres table along with the nextSignatureUri?
            //TODO Or do we only save the requestedDocumentId, and with that, find the latest when needed?

            println uploadResponse
            final awsResponse = jsonSlurper.parse(uploadResponse.entity.content).nextSignatureUri
            println  awsResponse
            //println EntityUtils.toString(awsResponse.nextSignatureUri)
            //println (awsResponse.nextSignatureUri).toString()
            //println awsResponse.nextSignatureUri

            //if (uploadResponse.getCode() == 200 || uploadResponse.getCode() == 201) {
            if (uploadResponseCode == 200 || uploadResponseCode == 201) {
               //println EntityUtils.toString(uploadResponse.getEntity())
               println "past 201"
               println rtoAgreementNumber
               println primaryCustomerNumber
               println secondaryCustomerNumber
               //This is where we check then insert/update the agreement_signing table. Must handle rto, club, and/or other.
               if (rtoAgreementNumber != null) {
                  //final checkRtoAgreementPageRequest = new AgreementSigningPageRequest(1, 5, "asn_agreement_number", "ASC", null, primaryCustomerNumber, rtoAgreementNumber)
                  final checkRtoAgreementPageRequest = new AgreementSigningPageRequest(1, 5, "asn_agreement_number", "ASC", 1, primaryCustomerNumber, rtoAgreementNumber)
                  final checkExisting = new HttpGet("${host}/agreement/signing/paged/dataset/${dataset}?${checkRtoAgreementPageRequest}")
                  final existingRtoAgreement = client.execute(checkExisting)
                  println existingRtoAgreement
                  final existingAgreementId = jsonSlurper.parse(existingRtoAgreement.entity.content).id
                  //final existingAgreementId = existingRtoAgreement.id

                  final storeDTO = new SimpleLegacyNumberDTO(storeNumber)

                  //Move fields here from the params to construct the dto
                  final agreementToUpsert = new AgreementSigningDTO(null, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, rtoAgreementNumber, "R", 1, awsResponse.nextSignatureUri)

                  //try this if the import does not work
                  //final agreementToUpsert = [key1: companyId, key2: storeNumber, etc.]
                  //
                  //Will a paged Get return null if no record found? If not, what do I check for? Do I need a non-paged findByCustomerAndAgreement?
                  //TODO updateAgreementTable(customerNumber, agreementNumber, dataset, location, "NEW" (status), uploadResponse.LINK?)
                  if (existingRtoAgreement != null) {
                     //Update the record
                     final updateRtoAgreement = new HttpPut("${host}/agreement/signing/${existingAgreementId}/dataset/${dataset}, ${agreementToUpsert}")
                     final updateResponse = client.execute(updateRtoAgreement)
                     println "Update"
                  } else {
                     //Insert the record
                     final createRtoAgreement = new HttpPost("${host}/agreement/signing/dataset/${dataset}, ${agreementToUpsert}")
                     final createResponse = client.execute(createRtoAgreement)
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
