package com.cynergisuite.middleware.reportal

import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.store.StoreTestDataLoader
import com.cynergisuite.middleware.threading.CynergiExecutor
import kotlin.Unit
import kotlin.jvm.functions.Function0
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

class ReportalServiceSpecification extends Specification {

   void "store reportal doc generation" () {
      setup:
      final company = CompanyFactory.tstds1()
      final store = StoreTestDataLoader.store(3, company)
      final syncLatch = new CountDownLatch(1)
      final reportalDir = File.createTempDir()
      final storeDir = new File(reportalDir.getAbsolutePath(), "store${store.number}")
      final cynergiExecutor = new CynergiExecutor(1)
      final executor = Spy(cynergiExecutor)
      executor.execute(_) >> { Function0<Unit> job ->
         cynergiExecutor.executor.execute({
            job.invoke()
            syncLatch.countDown()
         })
      }

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
