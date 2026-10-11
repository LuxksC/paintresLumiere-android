package com.luxkasTech.PaintresLumiere.core.common

import java.text.Normalizer

private val diacritics = "\\p{Mn}+".toRegex()

/** Lowercases and strips accents, so "Árvore" matches "arvore" (like iOS `localizedStandardContains`). */
fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(diacritics, "").lowercase()

/** True when every whitespace-separated term of [query] appears in any of [fields]. */
fun matchesSearch(query: String, vararg fields: String?): Boolean {
    val terms = query.normalizedForSearch().split(' ').filter { it.isNotBlank() }
    if (terms.isEmpty()) return true
    val haystack = fields.filterNotNull().joinToString(" ") { it.normalizedForSearch() }
    return terms.all { it in haystack }
}
