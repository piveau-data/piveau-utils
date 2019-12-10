package io.piveau.utils

import io.piveau.vocabularies.vocabulary.HYDRA
import org.apache.jena.rdf.model.*
import org.apache.jena.vocabulary.RDF
import java.net.MalformedURLException
import java.net.URL

class HydraPaging private constructor(
    private val paging: Resource?,
    private val next: Property,
    val total: Int,
    private val base: String?
) {

    companion object {
        @JvmStatic
        fun findPaging(model: Model, base: String? = null): HydraPaging {
            val it = model.listResourcesWithProperty(RDF.type, HYDRA.PagedCollection)
            return if (it.hasNext()) {
                val paging = it.next()
                HydraPaging(paging, HYDRA.nextPage, paging.getProperty(HYDRA.totalItems).int, base)
            } else {
                val itv = model.listResourcesWithProperty(RDF.type, HYDRA.PartialCollectionView)
                if (itv.hasNext()) {
                    HydraPaging(
                        itv.next(),
                        HYDRA.next,
                        model.listObjectsOfProperty(HYDRA.totalItems).next().asLiteral().int,
                        base
                    )
                } else {
                    HydraPaging(null, HYDRA.nextPage, 0, base)
                }
            }
        }
    }

    fun next(): String? = if (paging == null || !paging.hasProperty(next)) {
        null
    } else handleBroken(
        when (val obj = paging.getProperty(next).`object`) {
            is Resource -> obj.uri
            is Literal -> obj.string
            else -> null
        }
    )

    private fun handleBroken(next: String?): String? = base?.let {
        try {
            val baseUrl = URL(base)
            val nextUrl = URL(next)
            // deal here with base
            "${baseUrl.protocol}://${baseUrl.authority}${baseUrl.path}?${nextUrl.query}"
        } catch (e: MalformedURLException) {
            next
        }
    } ?: next

}
