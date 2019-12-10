package io.piveau.rdf

import io.piveau.utils.JenaUtils
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RdfExtensionsTest {

    @Test
    fun `Test rdf pre-processing and fixing malformed uris`() {
        val rdf = """
            <urn:one \t> <urn:\n two> <urn:
            three\tfour%> .
        """.trimIndent().toByteArray()

        val (content, mimeType) = preProcess(rdf, "application/n-triples", "urn:example")
        val model = content.toModel(mimeType.asRdfLang())
        println(JenaUtils.write(model, Lang.TURTLE))
    }

}
