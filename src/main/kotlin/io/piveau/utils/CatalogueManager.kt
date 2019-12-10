package io.piveau.utils

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Promise
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource

class CatalogueManager internal constructor(private val tripleStore: TripleStore) {

    fun records(catalogueUriRef: String, handler: (AsyncResult<List<Resource>>) -> Unit): Future<List<Resource>> {
        val promise = Promise.promise<List<Resource>>()

        return promise.future().setHandler(handler)
    }
    fun records(catalogue: Resource, handler: (AsyncResult<List<Resource>>) -> Unit): Future<List<Resource>> =
        records(catalogue.uri, handler)

    fun datasets(catalogueUriRef: String, handler: (AsyncResult<List<Resource>>) -> Unit): Future<List<Resource>> {
        val promise = Promise.promise<List<Resource>>()

        return promise.future().setHandler(handler)
    }
    fun datasets(catalogue: Resource, handler: (AsyncResult<List<Resource>>) -> Unit): Future<List<Resource>> =
        datasets(catalogue.uri, handler)

    fun delete(uriRef: String, handler: (AsyncResult<Void>) -> Unit) = tripleStore.deleteGraph(uriRef, handler)
    fun delete(resource: Resource, handler: (AsyncResult<Void>) -> Unit) = delete(resource.uri, handler)

    fun get(uriRef: String, handler: (AsyncResult<Model>) -> Unit) = tripleStore.construct("CONSTRUCT WHERE { GRAPH <$uriRef> { ?s ?p ?o } }", handler)
    fun get(resource: Resource, handler: (AsyncResult<Model>) -> Unit) = get(resource.uri, handler)

    fun add(uriRef: String, model: Model, handler: (AsyncResult<Void>) -> Unit) = tripleStore.putGraph(uriRef, model, handler)
    fun add(resource: Resource, handler: (AsyncResult<Void>) -> Unit) = tripleStore.putGraph(resource.uri, resource.model, handler)

}