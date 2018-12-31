package com.hightouchinc.cynergi.middleware.extensions

import org.eclipse.collections.api.factory.map.MutableMapFactory
import org.eclipse.collections.api.map.MutableMap

fun <K, V> MutableMapFactory.ofPairs(vararg pairs: Pair<K, V>): MutableMap<K, V> {
   val map: MutableMap<K, V> = this.of()

   map.putAll(pairs)

   return map
}
