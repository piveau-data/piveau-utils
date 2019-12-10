package io.piveau.utils

import io.piveau.rdf.RDFMimeTypes
import io.piveau.rdf.asString
import io.piveau.rdf.toModel
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.predicate.ResponsePredicate
import org.apache.jena.query.ResultSet
import org.apache.jena.query.ResultSetFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang

private val ACCEPT = HttpHeaders.ACCEPT.toString()
private val CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toString()
private val AUTHORIZATION = HttpHeaders.AUTHORIZATION.toString()
private const val WWW_AUTHENTICATE = "WWW-Authenticate"

private val NTRIPLES = Lang.NTRIPLES.headerString

class TripleStore(val client: WebClient, val config: JsonObject) {

    private val address = config.getString("address")

    private val queryEndpoint = config.getString("queryEndpoint", "sparql")
    private val queryAuthEndpoint = config.getString("queryAuthEndpoint", "sparql-auth")
    private val graphEndpoint = config.getString("graphEndpoint", "sparql-graph-crud")
    private val graphAuthEndpoint = config.getString("graphAuthEndpoint", "sparql-graph-crud-auth")

    private val queryRequest = client.getAbs("$address/$queryEndpoint")
        .putHeader(ACCEPT, "application/json")
        .expect(ResponsePredicate.SC_SUCCESS)

    private val graphRequest = client.getAbs("$address/$graphEndpoint")
        .putHeader(ACCEPT, NTRIPLES)
        .expect(ResponsePredicate.SC_SUCCESS)

    private val username = config.getString("username", "dba")
    private val password = config.getString("password", "dba")

    val datasetManager = DatasetManager(this)
    val catalogueManager = CatalogueManager(this)

    fun getGraph(graphUri: String, handler: (AsyncResult<Model>) -> Unit): Future<Model> =
        Promise.promise<Model>().apply {
            graphRequest.addQueryParam("graph", graphUri).send {
                when {
                    it.succeeded() -> it.result().apply {
                        complete(bodyAsString().toByteArray().toModel(NTRIPLES))
                    }
                    else -> fail(it.cause())
                }
            }
        }.future().setHandler(handler)

    fun putGraph(graphUri: String, model: Model, handler: (AsyncResult<Void>) -> Unit): Future<Void> =
        Promise.promise<Void>().apply {
            client.putAbs("$address/$graphAuthEndpoint")
                .putHeader(CONTENT_TYPE, NTRIPLES)
                .addQueryParam("graph", graphUri)
                .sendBufferDigestAuth(
                    "$address/$graphAuthEndpoint",
                    HttpMethod.PUT,
                    username,
                    password,
                    Buffer.buffer(model.asString(NTRIPLES))
                ) {
                    if (it.succeeded()) complete() else fail(it.cause())
                }
        }.future().setHandler(handler)

    fun deleteGraph(graphUri: String, handler: (AsyncResult<Void>) -> Unit): Future<Void> =
        Promise.promise<Void>().apply {
            client.deleteAbs("$address/$graphAuthEndpoint")
                .addQueryParam("graph", graphUri)
                .sendDigestAuth(
                    "$address/$graphAuthEndpoint",
                    HttpMethod.DELETE,
                    username,
                    password
                ) { if (it.succeeded()) complete() else fail(it.cause()) }
        }.future().setHandler(handler)

    fun select(query: String, handler: (AsyncResult<ResultSet>) -> Unit): Future<ResultSet> =
        Promise.promise<ResultSet>().apply {
            queryRequest.addQueryParam("query", query).send {
                when {
                    it.succeeded() -> {
                        val resultSet = ResultSetFactory.fromJSON(it.result().body().bytes.inputStream())
                        complete(resultSet)
                    }
                    else -> fail(it.cause())
                }
            }
        }.future().setHandler(handler)

    fun update(query: String, handler: (AsyncResult<Void>) -> Unit): Future<Void> =
        Promise.promise<Void>().apply {
            client.getAbs("$address/$queryAuthEndpoint")
                .addQueryParam("query", query)
                .sendDigestAuth("$address/$queryAuthEndpoint", HttpMethod.GET, username, password) {
                    if (it.succeeded()) complete() else fail(it.cause())
                }
        }.future().setHandler(handler)

    fun ask(query: String, handler: (AsyncResult<Boolean>) -> Unit): Future<Boolean> =
        Promise.promise<Boolean>().apply {
            queryRequest.putHeader(ACCEPT, "text/html").addQueryParam("query", query).send {

            }
        }.future().setHandler(handler)

    fun construct(query: String, handler: (AsyncResult<Model>) -> Unit): Future<Model> =
        Promise.promise<Model>().apply {
            queryRecursive(query, 0, ModelFactory.createDefaultModel(), this)
        }.future().setHandler(handler)

    private fun queryRecursive(query: String, offset: Int, model: Model, promise: Promise<Model>) {
        client.getAbs("$address/$queryEndpoint")
            .putHeader(ACCEPT, "application/n-triples")
            .expect(ResponsePredicate.SC_SUCCESS)
            .addQueryParam("query", "$query OFFSET $offset LIMIT 5000").send {
                when {
                    it.succeeded() -> {
                        val body: String = it.result().bodyAsString()
                        if (!body.contains("# Empty NT")) {
                            model.add(JenaUtils.read(body.toByteArray(), RDFMimeTypes.NTRIPLES))
                            queryRecursive(query, offset + 5000, model, promise)
                        } else {
                            promise.complete(model)
                        }
                    }
                    else -> promise.fail(it.cause())
                }
            }
    }

}

fun HttpRequest<Buffer>.sendDigestAuth(
    uri: String,
    method: HttpMethod,
    username: String,
    password: String,
    handler: (AsyncResult<HttpResponse<Buffer>>) -> Unit
) = send {
    if (it.succeeded()) {
        when (it.result().statusCode()) {
            401 -> {
                DigestAuth.authenticate(
                    it.result().getHeader(WWW_AUTHENTICATE),
                    uri,
                    method.name,
                    username,
                    password
                )?.apply {
                    this@sendDigestAuth.putHeader(AUTHORIZATION, this)
                    send(handler)
                } ?: handler(Future.failedFuture("DigestAuth authentication failure"))
            }
            in 200..299 -> handler(it)
            else -> handler(Future.failedFuture("${it.result().statusCode()} ${it.result().statusMessage()}"))
        }
    }
}

fun HttpRequest<Buffer>.sendBufferDigestAuth(
    uri: String,
    method: HttpMethod,
    username: String,
    password: String,
    buffer: Buffer,
    handler: (AsyncResult<HttpResponse<Buffer>>) -> Unit
) = sendBuffer(buffer) {
    if (it.succeeded()) {
        when (it.result().statusCode()) {
            401 -> {
                DigestAuth.authenticate(
                    it.result().getHeader(WWW_AUTHENTICATE),
                    uri,
                    method.name,
                    username,
                    password
                )?.apply {
                    this@sendBufferDigestAuth.putHeader(AUTHORIZATION, this)
                    sendBuffer(buffer, handler)
                } ?: handler(Future.failedFuture("DigestAuth authentication failure"))
            }
            in 200..299 -> handler(it)
            else -> handler(Future.failedFuture("${it.result().statusCode()} ${it.result().statusMessage()}"))
        }
    }
}
