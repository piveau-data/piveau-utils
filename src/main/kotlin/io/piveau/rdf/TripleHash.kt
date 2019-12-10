@file:JvmName(name = "TripleHash")
package io.piveau.rdf

import org.apache.commons.codec.digest.DigestUtils
import org.apache.jena.graph.Triple

fun tripleHash(triple: Triple): String {
    val (subject, predicate, `object`) = triple
    return DigestUtils.md5Hex("$subject$predicate$`object`")
}

operator fun Triple.component1() = if (subject.isBlank) "Magic_S" else subject.toString()
operator fun Triple.component2() = predicate.toString()
operator fun Triple.component3() = if (`object`.isBlank) "Magic_O" else `object`.toString()
