package io.piveau.rdf

import io.piveau.vocabularies.DCATAP_PREFIXES
import org.apache.jena.query.Dataset
import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdf.model.*
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.riot.RDFParser
import org.apache.jena.riot.RDFParserBuilder
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.StringWriter
import java.text.Normalizer

fun Resource.selectFrom(resources: List<Resource>): Resource? = listProperties(RDF.type).toList().map { it.`object`.asResource() }.firstOrNull { resources.contains(it) }

fun Resource.getIdentifierProperty(): String? = getProperty(DCTerms.identifier)?.let {
    val obj = it.`object`
    when {
        obj.isLiteral -> obj.asLiteral().string
        obj.isURIResource -> obj.asResource().uri
        else -> null
    }
}

fun Resource.identify(removePrefix: Boolean = false, uriRefPrecedence: Boolean = true): String? =
    when (uriRefPrecedence) {
        true -> {
            if (isURIResource) uri else getIdentifierProperty()
        }
        false -> {
            getIdentifierProperty() ?: if (isURIResource) uri else null
        }
    }

private val extractor = ModelExtract(object : StatementBoundaryBase() {
    override fun stopAt(s: Statement): Boolean = s.predicate == FOAF.primaryTopic
})

fun Resource.extract(model: Model = this.model): Model? = this.runCatching {
    extractor.extract(this, model).apply {
        setNsPrefixes(DCATAP_PREFIXES)
    }
}.getOrNull()

fun ByteArray.toModel(lang: Lang, baseUri: String? = null): Model = ModelFactory.createDefaultModel().apply {
    val builder: RDFParserBuilder = RDFParser.create().source(ByteArrayInputStream(this@toModel)).lang(lang)
    baseUri?.apply {
        builder.base(this) // this is baseUri
    }
    builder.parse(this) // this is model
}

fun ByteArray.toModel(contentType: String, baseUri: String? = null): Model =
    toModel(contentType.asRdfLang(), baseUri)

fun ByteArray.toDataset(lang: Lang, baseUri: String? = null): Dataset = DatasetFactory.create().apply {
    val builder: RDFParserBuilder = RDFParser.create().source(ByteArrayInputStream(this@toDataset)).lang(lang)
    baseUri?.apply {
        builder.base(this) // this is baseUri
    }
    builder.parse(this) // this is dataset
}

fun Model.asString(lang: Lang = Lang.TURTLE): String =
    StringWriter().apply { RDFDataMgr.write(this, this@asString, lang) }.toString()

fun Model.asString(contentType: String = RDFMimeTypes.TURTLE): String = asString(contentType.asRdfLang())

fun Dataset.asString(lang: Lang = Lang.TRIG): String = StringWriter().apply { RDFDataMgr.write(this, this@asString, lang) }.toString()

fun String.asNormalized(): String = Normalizer.normalize(this, Normalizer.Form.NFKD).replace("%".toRegex(), "").replace("\\W".toRegex(), "-").replace("-+".toRegex(), "-").toLowerCase()

fun String.asRdfLang(): Lang = when (substringBefore(';', this).trim()) {
    "application/rdf+xml" -> Lang.RDFXML
    "application/ld+json", "application/json" -> Lang.JSONLD
    "text/turtle" -> Lang.TURTLE
    "text/n3" -> Lang.N3
    "application/trig" -> Lang.TRIG
    "application/n-triples" -> Lang.NTRIPLES
    else -> Lang.RDFNULL
}

object RDFMimeTypes {
    const val RDFXML = "application/rdf+xml"
    const val JSONLD = "application/ld+json"
    const val TURTLE = "text/turtle"
    const val N3 = "text/n3"
    const val TRIG = "application/trig"
    const val NTRIPLES = "application/n-triples"
}
