package io.piveau.utils

import io.piveau.rdf.asString
import io.piveau.rdf.toModel
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.apache.jena.query.ResultSetFormatter
import org.apache.jena.riot.Lang
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.SKOS
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Testing triple store")
@ExtendWith(VertxExtension::class)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TripleStoreTest {

    val testGraphName: String = "http://piveau.io/graph/test1"

    val testGraph = """
        @prefix dc:         <http://purl.org/dc/terms/> .
        @prefix skos:       <http://www.w3.org/2004/02/skos/core#> .
        @prefix piveau:     <http://piveau.io/ns/test#> .

        piveau:test
            a                   skos:Concept ;
            skos:prefLabel      "Test"@en ;
            dc:description      "Just simple test."@en ;
            dc:identifier       "TEST" .
    """.trimIndent().toByteArray().toModel(Lang.TURTLE)

    val config: JsonObject = JsonObject()
        .put("address", "https://www.europeandataportal.eu")
        .put("username", "")
        .put("password", "")

//    @Test
    @Order(1)
    fun `Put a graph`(vertx: Vertx, testContext: VertxTestContext) {
        TripleStore(WebClient.create(vertx), config).putGraph(testGraphName, testGraph) {
            if (it.succeeded()) testContext.completeNow() else testContext.failNow(it.cause())
        }
    }

//    @Test
    @Order(2)
    fun `Query a select`(vertx: Vertx, testContext: VertxTestContext) {
        TripleStore(WebClient.create(vertx), config).select("SELECT ?test WHERE { graph <$testGraphName> { ?test a <${SKOS.Concept}> } }") {
            if (it.succeeded()) {
                print(ResultSetFormatter.asText(it.result()))
                testContext.completeNow()
            } else {
                testContext.failNow(it.cause())
            }
        }
    }

//    @Test
    @Order(3)
    fun `Update query`(vertx: Vertx, testContext: VertxTestContext) {
        TripleStore(WebClient.create(vertx), config).update("INSERT DATA { GRAPH <$testGraphName> { <http://piveau.io/ns/test#test> <${DCTerms.title}> \"Testing TripleStore\"@en . } }") {
            if (it.succeeded()) testContext.completeNow() else testContext.failNow(it.cause())
        }
    }

//    @Test
    @Order(4)
    fun `Get a graph`(vertx: Vertx, testContext: VertxTestContext) {
        TripleStore(WebClient.create(vertx), config).getGraph(testGraphName) {
            if (it.succeeded()) {
                print(it.result().asString(Lang.TURTLE))
                testContext.completeNow()
            } else {
                testContext.failNow(it.cause())
            }
        }
    }

//    @Test
    @Order(5)
    fun `Delete a graph`(vertx: Vertx, testContext: VertxTestContext) {
        TripleStore(WebClient.create(vertx), config).deleteGraph(testGraphName) {
            if (it.succeeded()) testContext.completeNow() else testContext.failNow(it.cause())
        }
    }

}
