package io.piveau

import io.piveau.rdf.renameDCATAPResource
import io.piveau.utils.JenaUtils
import io.piveau.vocabularies.readXmlResource
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.RDF
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RdfHelperTest {
    @Test
    fun `Test rename resource with self references`() {
        ModelFactory.createDefaultModel().apply {
            readXmlResource("rename-test.rdf")
            val dataset = listResourcesWithProperty(RDF.type, DCAT.Dataset).nextResource()

            val renamed = renameDCATAPResource(dataset, "https://piveau.io/set/data/1")

            var i = 1
            listResourcesWithProperty(RDF.type, DCAT.Distribution).forEachRemaining {
                renameDCATAPResource(it, "https://piveau.io/set/distribution/${i++}")
            }

            print(JenaUtils.write(this, Lang.TURTLE))

            Assertions.assertNotEquals(renamed.getPropertyResourceValue(DCAT.landingPage).uri, renamed.uri)
        }
    }
}
