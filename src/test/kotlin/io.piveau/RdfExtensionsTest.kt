package io.piveau

import io.piveau.rdf.asRdfLang
import io.piveau.utils.JenaUtils
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RdfExtensionsTest {
    @Test
    fun `Test mime type to Lang mapper`() {
        assert("blabla".asRdfLang() == Lang.RDFNULL)
        assert(JenaUtils.mimeTypeToLang("blabla") == Lang.NTRIPLES)

        assert("application/rdf+xml".asRdfLang() == Lang.RDFXML)
        assert("application/rdf+xml; UTF-8".asRdfLang() == Lang.RDFXML)
        assert("application/rdf+xml;".asRdfLang() == Lang.RDFXML)
        assert("application/rdf+xml; ".asRdfLang() == Lang.RDFXML)
        assert("application/rdf+xml ;".asRdfLang() == Lang.RDFXML)
        assert(" application/rdf+xml ; UTF-8".asRdfLang() == Lang.RDFXML)

        assert(JenaUtils.mimeTypeToLang("application/rdf+xml") == Lang.RDFXML)
        assert(JenaUtils.mimeTypeToLang("application/rdf+xml; UTF-8") == Lang.RDFXML)
        assert(JenaUtils.mimeTypeToLang("application/rdf+xml;") == Lang.RDFXML)
        assert(JenaUtils.mimeTypeToLang("application/rdf+xml; ") == Lang.RDFXML)
        assert(JenaUtils.mimeTypeToLang("application/rdf+xml ;") == Lang.RDFXML)
        assert(JenaUtils.mimeTypeToLang(" application/rdf+xml ; UTF-8") == Lang.RDFXML)

        assert("application/ld+json".asRdfLang() == Lang.JSONLD)
        assert("application/json".asRdfLang() == Lang.JSONLD)
        assert(JenaUtils.mimeTypeToLang("application/ld+json") == Lang.JSONLD)
        assert(JenaUtils.mimeTypeToLang("application/json") == Lang.JSONLD)

        assert("text/turtle".asRdfLang() == Lang.TURTLE)
        assert(JenaUtils.mimeTypeToLang("text/turtle") == Lang.TURTLE)

        assert("text/n3".asRdfLang() == Lang.N3)
        assert(JenaUtils.mimeTypeToLang("text/n3") == Lang.N3)

        assert("application/trig".asRdfLang() == Lang.TRIG)
        assert(JenaUtils.mimeTypeToLang("application/trig") == Lang.TRIG)

        assert("application/n-triples".asRdfLang() == Lang.NTRIPLES)
        assert(JenaUtils.mimeTypeToLang("application/n-triples") == Lang.NTRIPLES)
    }
}
