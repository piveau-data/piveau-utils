@file:JvmName(name = "LanguageTag")
package io.piveau.rdf

fun parseLangTag(tag: String, default: String): Triple<Boolean, String, String?> {
    val elements = tag.split('-')
    return when (elements.size) {
        0 -> Triple(false, default, null)
        1, 2 -> Triple(false, if (elements[0].isBlank()) default else elements[0], null)
        3 -> Triple(false, elements[0], elements[2])
        4, 5 -> Triple(elements[3] == "t0", elements[0], elements[2])
        else -> Triple(false, default, null)
    }
}
