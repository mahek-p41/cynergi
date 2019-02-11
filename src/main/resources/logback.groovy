import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.INFO

final def baseLogFileName='cynergi-middleware'
final def logFileHome='/opt/cyn/v01/cynmid/logs'
final String micrnautEnvironments=System.properties['micronaut.environments']
final List<String> appenders = []

if(micrnautEnvironments.contains('prod'))
{
   new File(logFileHome).mkdirs()

   final String appenderName = 'PROD_FILE'

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
}

if(micrnautEnvironments.contains('local') || appenders.isEmpty()) {
   final String appenderName = 'LOCAL_STDOUT'

   appender(appenderName, ConsoleAppender) {
      encoder(PatternLayoutEncoder) {
         pattern = '%d{HH:mm:ss.SSS} %-5level %logger{10} - %msg%n'
      }
   }

   appenders.add(appenderName)

   logger('com.hightouchinc', TRACE)
}

if (System.properties.containsKey('HIGHTOUCH_TRACE_LOGGING')) {
   logger('com.hightouchinc', TRACE)
}

logger('com.zaxxer', INFO)
logger('io.netty', ERROR)
logger('io.micronaut', ERROR)
logger('org.apache', ERROR)
logger('org.flywaydb', INFO)
logger('org.jboss', ERROR)
logger('org.springframework', ERROR)

root(DEBUG, appenders)
