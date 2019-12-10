@file:JvmName("PreProcessing")
package io.piveau.rdf

import org.eclipse.rdf4j.model.*
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import java.io.ByteArrayOutputStream

val factory: ValueFactory = SimpleValueFactory.getInstance()

fun preProcess(content: ByteArray, mimeType: String, url: String): Pair<ByteArray, String> {
    val model = LinkedHashModel()
    val format = Rio.getParserFormatForMIMEType(mimeType)
    if (!format.isPresent) return Pair(content, mimeType)
    val parser = Rio.createParser(format.get()).apply {
        parserConfig.set(BasicParserSettings.VERIFY_URI_SYNTAX, false)
        setRDFHandler(URIFixer(model))
    }
    parser.parse(content.inputStream(), url)
    val output = ByteArrayOutputStream()
    Rio.write(model, output, RDFFormat.NTRIPLES)
    return Pair(output.toByteArray(), "application/n-triples")
}

class URIFixer(model: Model) : StatementCollector(model) {
    override fun handleStatement(st: Statement) {
        val fixedSubject = if (st.subject is IRI) st.subject.fix() else st.subject
        val fixedObject = if (st.`object` is IRI) (st.`object` as IRI).fix() else st.`object`
        val fixedStatement = factory.createStatement(fixedSubject, st.predicate.fix(), fixedObject)
        super.handleStatement(fixedStatement)
    }
}

val encodingMap = mapOf(
    " " to "%20",
    "{" to "%7B",
    "}" to "%7D",
    "[" to "%5B",
    "]" to "%5D",
    "\r" to "%0D",
    "\n" to "%0A",
    "\t" to "%09")

fun Resource.fix(): IRI = factory.createIRI(stringValue()).fix()
fun IRI.fix(): IRI = factory.createIRI(
    stringValue().removeSuffix("%").replace("[{}\\[\\]\\s\n\r\t]".toRegex()) {
        encodingMap[it.value] ?: ""
    })
