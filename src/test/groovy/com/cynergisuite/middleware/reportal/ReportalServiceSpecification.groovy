package com.cynergisuite.middleware.reportal

import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.threading.CynergiExecutor
import java.util.concurrent.CountDownLatch
import kotlin.Unit
import kotlin.jvm.functions.Function0
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ReportalServiceSpecification extends Specification {
   @Rule TemporaryFolder temporaryFolder

   void "store reportal doc generation" () {
      setup:
      final store = StoreFactory.random()
      final syncLatch = new CountDownLatch(1)
      final reportalDir = temporaryFolder.newFolder()
      final storeDir = new File(reportalDir, "store${store.number}")
      final cynergiExecutor = new CynergiExecutor(1)
      final executor = Spy(cynergiExecutor)
      executor.execute(_) >> { Function0<Unit> job ->
         cynergiExecutor.executor.execute({
            job.invoke()
            syncLatch.countDown()
         })
      };

      when:
      new ReportalService(executor, reportalDir.getAbsolutePath()).generateReportalDocument(store, "test", "tst") { os ->
         os.write("Test".getBytes())
      }
      syncLatch.await()

      then:
      storeDir.listFiles().length == 1
      storeDir.listFiles()[0].name.startsWith("test")
      storeDir.listFiles()[0].name.endsWith("tst")
      storeDir.listFiles()[0].text == "Test"
   }
}
