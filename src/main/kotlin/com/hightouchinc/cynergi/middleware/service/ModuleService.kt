package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.entity.Module
import com.hightouchinc.cynergi.middleware.repository.ModuleRepository
import org.apache.commons.lang3.RandomUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleService @Inject constructor(
   val moduleRepository: ModuleRepository
) {
   fun randomModule(): Module {
      val modules = moduleRepository.findAll()
      val random: Int = RandomUtils.nextInt(0, modules.size)

      return modules[random]
   }
}
