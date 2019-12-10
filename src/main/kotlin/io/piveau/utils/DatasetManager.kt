package io.piveau.utils

import io.piveau.utils.experimental.DCATAPUriSchema
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Promise
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms

class DatasetManager internal constructor(private val tripleStore: TripleStore) {

    fun identify(
        datasetId: String,
        catalogueId: String,
        handler: (AsyncResult<Pair<Resource, Resource>>) -> Unit
    ): Future<Pair<Resource, Resource>> {
        val promise = Promise.promise<Pair<Resource, Resource>>()
        val catalogueUriRef = DCATAPUriSchema.applyFor(catalogueId)
        tripleStore.select("SELECT ?dataset ?record WHERE { GRAPH ?a { <$catalogueUriRef> <${DCAT.record}> ?record . } GRAPH ?b { ?record <${DCTerms.identifier}> \"$datasetId\" ; <${FOAF.primaryTopic}> ?dataset . } }") {
            if (it.succeeded()) {
                it.result().next().run {
                    val pair = Pair<Resource, Resource>(getResource("dataset"), getResource("record"))
                    promise.complete(pair)
                }
            } else {
                promise.fail(it.cause())
            }
        }
        return promise.future().setHandler(handler)
    }

    fun delete(uriRef: String, handler: (AsyncResult<Void>) -> Unit) = tripleStore.deleteGraph(uriRef, handler)
    fun delete(resource: Resource, handler: (AsyncResult<Void>) -> Unit) = delete(resource.uri, handler)

    fun get(uriRef: String, handler: (AsyncResult<Model>) -> Unit) = tripleStore.construct("CONSTRUCT WHERE { GRAPH <$uriRef> { ?s ?p ?o } }", handler)
    fun get(resource: Resource, handler: (AsyncResult<Model>) -> Unit) = get(resource.uri, handler)

    fun distributions(datasetUriRef: String, handler: (AsyncResult<List<Resource>>) -> Unit): Future<List<Resource>> {
        val promise = Promise.promise<List<Resource>>()

        return promise.future().setHandler(handler)
    }

}