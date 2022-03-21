#!/usr/bin/env /opt/cyn/v01/cynmid/groovy/bin/groovy.sh -cp /opt/cyn/v01/cynmid/cynergi-middleware.jar
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.slf4j', module = 'slf4j-simple', version = '1.7.36')
@Grab(group='org.slf4j', module='jul-to-slf4j', version='1.7.36')
@Grab(group='org.apache.httpcomponents.client5', module='httpclient5', version='5.1.3')
@Grab(group='info.picocli', module='picocli', version='4.6.3')
@Grab(group='info.picocli', module='picocli-groovy', version='4.6.3')
@Grab(group='org.apache.groovy', module='groovy-json', version='3.0.8') //may need to change this to 3.0.8 elsewhere
@Grab(group='com.google.guava', module='guava', version='31.1-jre')
@Command(
   description = "Simple script for creating an organization in the signature service"
)
@picocli.groovy.PicocliScript
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import groovy.transform.Field
import groovy.json.JsonSlurper
import com.google.common.net.UrlEscapers
import org.slf4j.bridge.SLF4JBridgeHandler
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.FileBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.EntityUtils
import com.cynergisuite.domain.SimpleLegacyNumberDTO

SLF4JBridgeHandler.removeHandlersForRootLogger();
SLF4JBridgeHandler.install();

final scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent
final libFile = new File("$scriptDir/lib/Lib.groovy")
final Class libClass = new GroovyClassLoader(getClass().classLoader).parseClass(libFile)
final lib = libClass.getDeclaredConstructor().newInstance()

@Option(names = ["-H", "--host"], defaultValue = "http://localhost:8080", description = "host and protocol to call")
@Field String host = "http://localhost:8080"

@Option(names = ["-h", "--help"], usageHelp = true, description = "Show this help message and exit")
@Field boolean helpRequested = false

@Option(names = ["-d", "--debug"], description = "Enable debug logging")
@Field boolean debug = false

@Parameters(index = "0", arity = "1", paramLabel = "pdf", description = "PDF to be uploaded for signature")
@Field File signaturePdf

@Parameters(index = "1", arity = "1", paramLabel = "name", description = "Name to be put on the signed document")
@Field String name

@Parameters(index = "2", arity = "1", paramLabel = "primaryCustomerNumber", description = "Primary customer number used to look up agreement")
@Field String primaryCustomerNumber

@Parameters(index = "3", arity = "1", paramLabel = "secondaryCustomerNumber", description = "Secondary customer number used to look up agreement")
@Field String secondaryCustomerNumber

@Parameters(index = "4", arity = "1", paramLabel = "rtoAgreementNumber", description = "RTO agreement number")
@Field String rtoAgreementNumber

@Parameters(index = "5", arity = "1", paramLabel = "clubAgreementNumber", description = "Club agreement number")
@Field String clubAgreementNumber

@Parameters(index = "6", arity = "1", paramLabel = "otherAgreementNumber", description = "Other agreement number")
@Field String otherAgreementNumber

@Parameters(index = "7", arity = "1", paramLabel = "dataset", description = "Customer number used to look up agreement")
@Field String dataset

@Parameters(index = "8", arity = "1", paramLabel = "reason", description = "Reason to be put on the signed document")
@Field String reason

@Parameters(index = "9", arity = "1", paramLabel = "location", description = "Location to be put on the signed document")
@Field String location

@Parameters(index = "10", arity = "1", paramLabel = "contactInfo", description = "Contact Info to be put on the signed document")
@Field String contactInfo

@Parameters(index = "11", arity = "1", paramLabel = "retentionDate", description = "Documents must be stored at least until the retention date")
@Field Date retentionDate

@Parameters(index = "12", arity = "1", paramLabel = "storeNumber", description = "Documents must be stored at least until the retention date")
@Field Integer storeNumber

@Parameters(index = "13", arity = "1..*", paramLabel = "signatories", description = "The people who need to sign the document")
@Field String[] signatories

@Parameters(index = "14", arity = "1..*", paramLabel = "emailAddresses", description = "The email addresses for the people who need to sign the document")
@Field String[] emailAddresses

//Add parameters for all pieces of info we need to store in the agreement table.

if (!helpRequested) {
   if (debug) {
      System.properties['jdk.internal.httpclient.debug'] = 'true'
      System.properties['jdk.httpclient.HttpClient.log'] = 'all'
      System.properties['org.slf4j.simpleLogger.log.org.apache.hc.client5.http.wire'] = 'trace'
   }

   final escaper = UrlEscapers.urlPathSegmentEscaper()
   final jsonSlurper = new JsonSlurper();

   final tokenCall = new HttpGet("${host}/sign/here/token/store/${storeNumber}/dataset/${dataset}")
   final tokenResponse = client.execute(tokenCall)
   final token = tokenResponse.token

   if (!signaturePdf.exists() || !signaturePdf.isFile()) {
      println "${signaturePdf} did not exist or is not a file"
   } else {
      try (final client = HttpClients.createDefault()) {
         //This accessToken is a token from AWS?
         final accessToken = lib.loginWithAccessToken(client, token, host)

         if (accessToken != null) {
            //TODO Where do the email addresses come into play?
            //Changing signers to be the email addresses, when emailAddresses is not null.
            if (emailAddresses != null) {
               final signers = "signer=" + emailAddresses.toList().withIndex().collect { element, index -> "${element}[${index + 1}]" }.join("&signer=")
            } else {
               final signers = "signer=" + signatories.toList().withIndex().collect { element, index -> "${element}[${index + 1}]" }.join("&signer=")
            }
            //The below Post is hitting DocumentController from high-touch-sign
            final uploadDocumentRequest = new HttpPost("${host}/api/document/${escaper.escape(name)}/${escaper.escape(reason)}/${escaper.escape(location)}/${escaper.escape(contactInfo)}?${signers}")
            final pdfBody = new FileBody(signaturePdf, ContentType.APPLICATION_PDF, signaturePdf.name)
            final requestEntity = MultipartEntityBuilder.create().addPart("file", pdfBody).build()

            uploadDocumentRequest.setHeader("Authorization", "Bearer ${accessToken}")
            //uploadDocumentRequest.setHeader("X-Notify-Hook", "patricks@hightouchinc.com") //removed for now
            uploadDocumentRequest.setEntity(requestEntity)

            final uploadResponse = client.execute(uploadDocumentRequest)
            //will get back json:
            //requestedDocumentId UUID The id for the root of the whole transaction
            //nextSignatureURL
            //TODO Do we need to store the requestedDocumentId in the postgres table along with the nextSignatureURL?
            //TODO Or do we only save the requestedDocumentId, and with that, find the latest when needed?

            final awsResponse = jsonSlurper.parseText(uploadResponse)
            println EntityUtils.toString(awsResponse.nextSignatureURL)

            if (uploadResponse.getCode() == 200) {
               println EntityUtils.toString(uploadResponse.getEntity())
               println EntityUtils.toString(rtoAgreementNumber)
               println EntityUtils.toString(primaryCustomerNumber)
               println EntityUtils.toString(secondaryCustomerNumber)
               //This is where we check then insert/update the agreement_signing table. Must handle rto, club, and/or other.
               if (rtoAgreementNumber != null) {
                  final checkRtoAgreementPageRequest = new AgreementSigningPageRequest(1, 5, "asn_agreement_number", "ASC", null, primaryCustomerNumber, rtoAgreementNumber)
                  final checkExisting = new HttpGet("${host}/agreement/signing/paged/dataset/${dataset}${checkRtoAgreementPageRequest}")
                  final existingRtoAgreement = client.execute(checkExisting)
                  final existingAgreementId = checkExistingRtoAgreement.id

                  final storeDTO = new SimpleLegacyNumberDTO(storeNumber)

                  //Move fields here from the params to construct the dto
                  final agreementToUpsert = new AgreementSigningDTO(null, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, rtoAgreementNumber, "R", 1, awsResponse.nextSignatureURL)

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
         } else {
            println "Invalid token provided for uploading"
         }
      }
   }
}
