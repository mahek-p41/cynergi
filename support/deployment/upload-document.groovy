#!/opt/cyn/v01/cynmid/groovy/bin/groovy -cp /opt/cyn/v01/cynmid/cynergi-middleware.jar
@GrabConfig(systemClassLoader = true)
@Grab(group = 'org.slf4j', module = 'slf4j-simple', version = '1.7.36')
@Grab(group = 'org.slf4j', module = 'jul-to-slf4j', version = '1.7.36')
@Grab(group = 'org.apache.httpcomponents.client5', module = 'httpclient5', version = '5.1.3')
@Grab(group = 'info.picocli', module = 'picocli', version = '4.6.3')
@Grab(group = 'info.picocli', module = 'picocli-groovy', version = '4.6.3')
@Grab(group = 'org.apache.groovy', module = 'groovy-json', version = '4.0.3')
@Grab(group = 'com.google.guava', module = 'guava', version = '31.1-jre')
@picocli.CommandLine.Command(
   description = "Sign Here Please agreement upload script"
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
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.classic.methods.HttpPost

SLF4JBridgeHandler.removeHandlersForRootLogger()
SLF4JBridgeHandler.install()

@Option(names = ["-h", "--help"], usageHelp = true, description = "Show this help message and exit")
@Field boolean helpRequested = false

@Option(names = ["-d", "--debug"], description = "Enable debug logging")
@Field boolean debug = false

@Parameters(index = "0", arity = "1", paramLabel = "dataset", description = "Customer number used to look up agreement")
@Field String dataset

@Parameters(index = "1", arity = "1", paramLabel = "storeNumber", description = "Documents must be stored at least until the retention date")
@Field Integer storeNumber


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

      println storeToken
   }
}
