import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.INFO

final def baseLogFileName='cynergi-middleware'
final def logFileHome='/opt/cyn/v01/cynmid/logs'
final String micrnautEnvironments=System.properties['micronaut.environments']
final List<String> appenders = []

if('prod'.equalsIgnoreCase(micrnautEnvironments)) {
   final String appenderName = 'PROD_FILE'

   new File(logFileHome).mkdirs()

   appender(appenderName, RollingFileAppender) {
      file="${logFileHome}/${baseLogFileName}.log"
      append=true
      encoder(PatternLayoutEncoder) {
         pattern='%d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n'
      }
      rollingPolicy(TimeBasedRollingPolicy) {
         FileNamePattern="${logFileHome}/${baseLogFileName}.%d{yyyy-MM-dd}.log"
         maxHistory=7
      }
   }

   appenders.add(appenderName)

   logger('com.hightouchinc', INFO)
}

if(micrnautEnvironments.contains('local') || appenders.isEmpty()) {
   appender('STDOUT', ConsoleAppender) {
      encoder(PatternLayoutEncoder) {
         pattern = '%d{HH:mm:ss.SSS} %-5level %logger{10} - %msg%n'
      }
   }

   appenders.add('STDOUT')

   logger('com.hightouchinc', TRACE)
}

if (System.properties.containsKey('HIGHTOUCH_TRACE_LOGGING')) {
   logger('com.hightouchinc', TRACE)
} else if (System.properties.containsKey('HIGHTOUCH_TRACE_LOGGING')) {
   logger('com.hightouchinc', DEBUG)
}

logger('com.zaxxer', ERROR)
logger('io.netty', ERROR)
logger('io.micronaut', INFO)
logger('org.apache', ERROR)
logger('org.flywaydb', INFO)
logger('org.jboss', ERROR)
logger('org.hibernate', ERROR)
logger('org.springframework', ERROR)

root(DEBUG, appenders)
