@picocli.CommandLine.Command(
   description = "Sign Here Please agreement upload script"
)
@picocli.groovy.PicocliScript2
import com.cynergisuite.domain.SimpleLegacyNumberDTO
import com.cynergisuite.middleware.agreement.signing.AgreementSigningDTO
import com.cynergisuite.middleware.company.CompanyDTO
import com.cynergisuite.middleware.company.CompanyEntity
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
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
import com.cynergisuite.middleware.sign.here.SignatoryFinder

@Option(names = ["-H", "--host"], description = "host and protocol to call")
@Field String hostIn = null

@Option(names = ["-h", "--help"], usageHelp = true, defaultValue = "false", description = "Show this help message and exit")
@Field boolean helpRequested

@Option(names = ["-d", "--debug"], defaultValue = "false", description = "Enable debug logging")
@Field boolean debug

@Option(names = ["-c", "--cynmid-port"], defaultValue = "10900", description = "Port that cynergi-middleware is running on")
@Field int cynmidPort

@Parameters(index = "0", arity = "1", paramLabel = "argsFile", description = "Arguments to be passed to the signature service")
@Field File argsFile

static boolean isCst() {
   try {
      return (InetAddress.getLocalHost().getHostName().startsWithIgnoreCase("cst"))
   } catch(Throwable e) {
      return true
   }
}

if (!helpRequested) {
   if (debug) {
      println("Debugging enabled")
      System.setProperty("logback.configurationFile", "logback-debug-stdout.xml")

      SLF4JBridgeHandler.removeHandlersForRootLogger()
      SLF4JBridgeHandler.install()
   }

   final logger = LoggerFactory.getLogger("upload-document.groovy")

   if (argsFile.exists() && argsFile.isFile()) {
      logger.info("Uploading ${argsFile}")

      try (final argsReader = new FileReader(argsFile)) {
         String host

         if (hostIn == null && !isCst()) {
            host = 'https://app.signhereplease.com'
         } else if (hostIn != null) {
            host = hostIn
         } else {
            host = 'https://app.signhereplease.dev'
         }

         logger.info("Uploading to ${host}")

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
            def metaAgreementNumber = 0
            if (rtoAgreementNumber != 0) {
               metaAgreementNumber = rtoAgreementNumber
            } else if (clubAgreementNumber != 0) {
               metaAgreementNumber = clubAgreementNumber
            } else {
               metaAgreementNumber = otherAgreementNumber
            }

            final expirationDate = (csvData["retentionDate"].toString().trim())
            final formatter = DateTimeFormatter.ofPattern("M/d/yy", Locale.ENGLISH);
            final retentionDate = LocalDate.parse(expirationDate, formatter)

            if (signaturePdf.exists() && signaturePdf.isFile()) {
               final signatories = SignatoryFinder.reduceSignatoriesBasedOnProvidedPdf(signaturePdf, StringUtils.split(csvData["signatories"].trim(), ',').collect {it.trim()} )

               if (!signatories.isEmpty()) {
                  final jsonSlurper = new JsonSlurper()

                  try (final client = HttpClients.createDefault()) {
                     final storeTokenCall = new HttpGet("http://localhost:${cynmidPort}/sign/here/token/store/${storeNumber}/dataset/${dataset}")
                     final storeTokenResponse = client.execute(storeTokenCall)

                     if (storeTokenResponse.getCode() == 200) {
                        final storeTokenSlurp = jsonSlurper.parse(storeTokenResponse.entity.content)
                        final company = storeTokenSlurp.company
                        final companyUUID = UUID.fromString(company.id)
                        //final currentCompany = new CompanyEntity(companyUUID, company.name, null, company.clientCode, company.clientId, company.datasetCode, company.federalIdNumber)
                        //final companyDto = new CompanyDTO(currentCompany)
                        final storeToken = storeTokenSlurp.token

                        storeTokenResponse.close()

                        final tokenRequest = new JsonBuilder([token: storeToken])
                        final tokenLoginRequest = new HttpPost(URI.create("${host}/api/login/token"))
                        tokenLoginRequest.setHeader("Content-Type", "application/json")
                        tokenLoginRequest.setEntity(new StringEntity(tokenRequest.toString()))

                        final accessToken = client.execute(tokenLoginRequest).withCloseable { tokenLoginResponse ->
                           if (tokenLoginResponse.code == 200) {
                              return jsonSlurper.parse(tokenLoginResponse.entity.content).access_token
                           } else {
                              logger.info "Unable to login to sign here system with access token. ${tokenLoginResponse.reasonPhrase} ${EntityUtils.toString(tokenLoginResponse.entity)}"
                              return null
                           }
                        }

                        if (accessToken != null) {
                           final signers = "signer=" + signatories.toList().collect { element -> "${element.value}[${element.index}]" }.join("&signer=")

                           //The below Post is hitting DocumentController from high-touch-sign
                           final uploadDocumentRequest = new HttpPost("${host}/api/document/${name}/${reason}/${location}/${contactInfo}/${retentionDate}?${signers}")

                           final pdfBody = new FileBody(signaturePdf, ContentType.APPLICATION_PDF, signaturePdf.name)
                           final requestEntity = MultipartEntityBuilder.create().addPart("file", pdfBody).build()

                           def agreementType = 'R'
                           switch (reason) {
                              case "RTO Agreement":
                                 agreementType = 'R'
                                 break
                              case "Club Agreement":
                                 agreementType = 'C'
                                 break
                              case "Changed Agreement":
                                 agreementType = 'R'
                                 break
                              case "Other Agreement":
                                 agreementType = 'F'
                                 break
                              case "Loaner Agreement":
                                 agreementType = 'L'
                                 break
                              default:
                                 agreementType = 'R'
                                 break
                           }

                           uploadDocumentRequest.setHeader("Authorization", "Bearer ${accessToken}")
                           uploadDocumentRequest.setHeader("X-Sig-Meta-Type", "agreement")
                           uploadDocumentRequest.setHeader("X-Sig-Meta-Store", storeNumber.toString())
                           uploadDocumentRequest.setHeader("X-Sig-Meta-Dataset", dataset)
                           uploadDocumentRequest.setHeader("X-Sig-Meta-Name", name)
                           uploadDocumentRequest.setHeader("X-Sig-Meta-Agreement-No", metaAgreementNumber.toString())
                           uploadDocumentRequest.setHeader("X-Sig-Meta-Customer-No", primaryCustomerNumber.toString())
                           uploadDocumentRequest.setHeader("X-Sig-Meta-Agreement-Type", agreementType)
                           uploadDocumentRequest.setEntity(requestEntity)
                           final uploadResponse = client.execute(uploadDocumentRequest)
                           final uploadResponseCode = uploadResponse.getCode()
                           //will get back json:
                           //requestedDocumentId UUID The id for the root of the whole transaction
                           //nextSignatureUri


                           if (uploadResponseCode >= 200 && uploadResponseCode < 400) {
                              final awsResponse = UUID.fromString(jsonSlurper.parse(uploadResponse.entity.content).requestedDocumentId)

                              if (rtoAgreementNumber != 0) {
                                 //This is where we check then insert/update the agreement_signing table for rto.
                                 final storeDTO = new SimpleLegacyNumberDTO(storeNumber)
                                 logger.info "Look for existing rto with: ${dataset} ${primaryCustomerNumber} ${rtoAgreementNumber}"
                                 final checkExisting = new HttpGet("http://localhost:${cynmidPort}/agreement/signing/upsertPrep/${dataset}/${primaryCustomerNumber}/${rtoAgreementNumber}/${agreementType}")
                                 final existingRtoAgreement = client.execute(checkExisting)
                                 final existingRtoAgreementResponseCode = existingRtoAgreement.getCode()
                                 logger.info "existingRtoAgreementResponseCode: ${existingRtoAgreementResponseCode}"

                                 if (existingRtoAgreementResponseCode == 404) {
                                    //Insert the record
                                    final agreementToUpsert = new AgreementSigningDTO(null, companyUUID, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, rtoAgreementNumber, agreementType, 1, awsResponse.toString())
                                    final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                                    final createRtoAgreement = new HttpPost("http://localhost:${cynmidPort}/agreement/signing/dataset/${dataset}")
                                       createRtoAgreement.setHeader("Content-Type", "application/json")
                                       createRtoAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                                    final createResponse = client.execute(createRtoAgreement)
                                    final responseCode = createResponse.getCode()

                                    if (responseCode >= 200 && responseCode < 400) {
                                       logger.info "Successfully created rto agreement"
                                    } else {
                                       logger.info "Failed to create rto agreement"
                                       logger.info "${uploadResponse.getCode()} -> ${EntityUtils.toString(createResponse.getEntity())}"
                                       System.exit(-2)
                                    }
                                 } else if (existingRtoAgreementResponseCode == 200) {
                                    //Update the record
                                    final existingRtoAgreementJson = jsonSlurper.parse(existingRtoAgreement.entity.content)
                                    final existingRtoAgreementId = existingRtoAgreementJson.id
                                    final existingRtoAgreementUUID = UUID.fromString(existingRtoAgreementId)
                                    logger.info("existingRtoAgreementUUID: ${existingRtoAgreementUUID}")
                                    final agreementToUpsert = new AgreementSigningDTO(existingRtoAgreementUUID.toString(), companyUUID, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, rtoAgreementNumber, agreementType, 1, awsResponse.toString())
                                    final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                                    final updateRtoAgreement = new HttpPut("http://localhost:${cynmidPort}/agreement/signing/${existingRtoAgreementUUID}/dataset/${dataset}")
                                       updateRtoAgreement.setHeader("Content-Type", "application/json")
                                       updateRtoAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                                    final updateResponse = client.execute(updateRtoAgreement)
                                    final responseCode = updateResponse.getCode()

                                    if (responseCode >= 200 && responseCode < 400) {
                                       logger.info "Successfully updated rto agreement"
                                    } else {
                                       logger.info "Failed to update rto agreement"
                                       logger.info "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                                       System.exit(-3)
                                    }
                                 } else {
                                    logger.info "Unhandled response from server $existingRtoAgreementResponseCode"
                                    System.exit(-4)
                                 }
                              }

                              if (clubAgreementNumber != 0) {
                                 //This is where we check then insert/update the agreement_signing table for club.
                                 final storeDTO = new SimpleLegacyNumberDTO(storeNumber)
                                 logger.info "Look for existing club with: ${dataset} ${primaryCustomerNumber} ${rtoAgreementNumber}"
                                 final checkExisting = new HttpGet("http://localhost:${cynmidPort}/agreement/signing/upsertPrep/${dataset}/${primaryCustomerNumber}/${clubAgreementNumber}/${agreementType}")
                                 final existingClubAgreement = client.execute(checkExisting)
                                 final existingClubAgreementResponseCode = existingClubAgreement.getCode()

                                 if (existingClubAgreementResponseCode == 404) {
                                    //Insert the record
                                    final agreementToUpsert = new AgreementSigningDTO(null, companyUUID, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, clubAgreementNumber, agreementType, 1, awsResponse.toString())
                                    final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                                    final createClubAgreement = new HttpPost("http://localhost:${cynmidPort}/agreement/signing/dataset/${dataset}")
                                       createClubAgreement.setHeader("Content-Type", "application/json")
                                       createClubAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                                    final createResponse = client.execute(createClubAgreement)
                                    final responseCode = createResponse.getCode()

                                    if (responseCode >= 200 && responseCode < 400) {
                                       logger.info "Successfully created club agreement"
                                    } else {
                                       logger.info "Failed to create club agreement"
                                       logger.info "${uploadResponse.getCode()} -> ${EntityUtils.toString(createResponse.getEntity())}"
                                       System.exit(-5)
                                    }
                                 } else if (existingClubAgreementResponseCode == 200) {
                                    //Update the record
                                    final existingClubAgreementJson = jsonSlurper.parse(existingClubAgreement.entity.content)
                                    final existingClubAgreementId = existingClubAgreementJson.id
                                    final existingClubAgreementUUID = UUID.fromString(existingClubAgreementId)
                                    logger.info("existingClubAgreementUUID: ${existingClubAgreementUUID}")
                                    final agreementToUpsert = new AgreementSigningDTO(existingClubAgreementUUID.toString(), companyUUID, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, clubAgreementNumber, agreementType, 1, awsResponse.toString())
                                    final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                                    final updateClubAgreement = new HttpPut("http://localhost:${cynmidPort}/agreement/signing/${existingClubAgreementUUID}/dataset/${dataset}")
                                       updateClubAgreement.setHeader("Content-Type", "application/json")
                                       updateClubAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                                    final updateResponse = client.execute(updateClubAgreement)
                                    final responseCode = updateResponse.getCode()

                                    if (responseCode >= 200 && responseCode < 400) {
                                       logger.info "Successfully updated club agreement"
                                    } else {
                                       logger.info "Failed to update club agreement"
                                       logger.info "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                                       System.exit(-6)
                                    }
                                 } else {
                                    logger.info "Unhandled response from server $existingRtoAgreementResponseCode"
                                    System.exit(-7)
                                 }
                              }

                              if (otherAgreementNumber != 0) {
                                 //This is where we check then insert/update the agreement_signing table for other.
                                 final storeDTO = new SimpleLegacyNumberDTO(storeNumber)
                                 final checkExisting = new HttpGet("http://localhost:${cynmidPort}/agreement/signing/upsertPrep/${dataset}/${primaryCustomerNumber}/${otherAgreementNumber}/${agreementType}")
                                 final existingOtherAgreement = client.execute(checkExisting)
                                 final existingOtherAgreementResponseCode = existingOtherAgreement.getCode()

                                 if (existingOtherAgreementResponseCode == 404) {
                                    //Insert the record
                                    final agreementToUpsert = new AgreementSigningDTO(null, companyUUID, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, otherAgreementNumber, agreementType, 1, awsResponse.toString())
                                    final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                                    final createOtherAgreement = new HttpPost("http://localhost:${cynmidPort}/agreement/signing/dataset/${dataset}")
                                       createOtherAgreement.setHeader("Content-Type", "application/json")
                                       createOtherAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                                    final createResponse = client.execute(createOtherAgreement)
                                    final responseCode = createResponse.getCode()

                                    if (responseCode >= 200 && responseCode < 400) {
                                       logger.info "Successfully created other agreement"
                                    } else {
                                       logger.info "${uploadResponse.getCode()} -> ${EntityUtils.toString(createResponse.getEntity())}"
                                       System.exit(-8)
                                    }
                                 } else if (existingOtherAgreementResponseCode == 200) {
                                    //Update the record
                                    final existingOtherAgreementJson = jsonSlurper.parse(existingOtherAgreement.entity.content)
                                    final existingOtherAgreementId = existingOtherAgreementJson.id
                                    final existingOtherAgreementUUID = UUID.fromString(existingOtherAgreementId)
                                    final agreementToUpsert = new AgreementSigningDTO(existingOtherAgreementUUID.toString(), companyUUID, storeDTO, primaryCustomerNumber, secondaryCustomerNumber, otherAgreementNumber, agreementType, 1, awsResponse.toString())
                                    final agreementToUpsertJson = new JsonBuilder(agreementToUpsert).toPrettyString()
                                    final updateOtherAgreement = new HttpPut("http://localhost:${cynmidPort}/agreement/signing/${existingOtherAgreementUUID}/dataset/${dataset}")
                                       updateOtherAgreement.setHeader("Content-Type", "application/json")
                                       updateOtherAgreement.setEntity(new StringEntity(agreementToUpsertJson))

                                    final updateResponse = client.execute(updateOtherAgreement)
                                    final responseCode = updateResponse.getCode()

                                    if (responseCode >= 200 && responseCode < 400) {
                                       logger.info "Successfully updated other agreement"
                                    } else {
                                       logger.info "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                                       System.exit(-9)
                                    }
                                 } else {
                                    logger.info "Unhandled response from server $existingRtoAgreementResponseCode"
                                    System.exit(-10)
                                 }
                              }

                           } else {
                              logger.info "${uploadResponse.getCode()} -> ${EntityUtils.toString(uploadResponse.getEntity())}"
                              System.exit(-11)
                           }

                           uploadResponse.close()
                        } else {
                           logger.info "Invalid token provided for uploading"
                           System.exit(-12)
                        }
                     } else {
                        logger.error "DataSet: {} StoreNumber: {} unable to be loaded, status: ", dataset, storeNumber, storeTokenResponse.getCode()
                        System.exit(-15)
                     }
                  }
               } else {
                  logger.error "No signatories available after double checking requested signatories with {}", signaturePdf
                  System.exit(-16)
               }
            } else {
               logger.info "${signaturePdf} did not exist or is not a file"
               System.exit(-14)
            }
         }
      }
   } else {
      logger.info "${argsFile} is not a csv file"
      System.exit(-13)
   }
}
