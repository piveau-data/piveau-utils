package io.piveau.utils

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@DisplayName("Testing dataset manager")
@ExtendWith(VertxExtension::class)
class DatasetManagerTest {

//    @Test
    fun `Identify a dataset`(vertx: Vertx, testContext: VertxTestContext) {
        val tripleStore = TripleStore(WebClient.create(vertx), JsonObject())
        tripleStore.datasetManager.identify("", "") {
            if (it.succeeded()) {
                val (dataset, record) = it.result()
                testContext.completeNow()
            } else {
                testContext.failNow(it.cause())
            }
        }
    }

}
