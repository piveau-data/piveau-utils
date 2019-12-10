@file:JvmName("RdfHelper")
package io.piveau.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.util.ResourceUtils

fun renameDCATAPResource(resource: Resource, uriRef: String): Resource {
    val store = mutableMapOf<Property, String>().apply {
        resource.model.listStatements(resource, null, resource).let {
            while (it.hasNext()) {
                it.next().apply {
                    it.remove()
                    put(predicate, `object`.asResource().uri)
                }
            }
        }
    }

    return ResourceUtils.renameResource(resource, uriRef).apply {
        store.forEach { addProperty(it.key, resource.model.createResource(it.value)) }
    }
}
