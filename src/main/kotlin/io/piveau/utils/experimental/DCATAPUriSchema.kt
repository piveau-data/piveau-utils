package io.piveau.utils.experimental

import io.piveau.rdf.asNormalized
import io.vertx.core.json.JsonObject

class DCATAPUriRef(val id: String) {

    val datasetGraphName: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.datasetGraphContext + id

    val catalogueGraphName: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.catalogueGraphContext + id

    val validationGraphName: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.validationGraphContext + id

    val metricsGraphName: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.metricsGraphContext + id

    val datasetUriRef: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.datasetContext + id

    val distributionUriRef: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.distributionContext + id

    val recordUriRef: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.recordContext + id

    val catalogueUriRef: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.catalogueContext + id

    val validationUriRef: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.validationContext + id

    val metricsUriRef: String
        get() = DCATAPUriSchema.baseUri + DCATAPUriSchema.metricsContext + id

}

object DCATAPUriSchema {
    var config: JsonObject? = null

    val baseUri: String by lazy { config?.getString("baseUri", "https://piveau.io/") ?: "https://piveau.io/" }

    val datasetContext: String by lazy { config?.getString("datasetContext", "set/data/") ?: "set/data/" }

    val distributionContext: String by lazy { config?.getString("distributionContext", "set/distribution/") ?: "set/distribution/" }

    val recordContext: String by lazy { config?.getString("recordContext", "set/record/") ?: "set/record/" }

    val catalogueContext: String by lazy { config?.getString("catalogueContext", "id/catalogue/") ?: "id/catalogue/" }

    val validationContext by lazy { config?.getString("validationContext", "id/validation/") ?: "id/validation/" }

    val metricsContext by lazy { config?.getString("metricsContext", "id/metrics/") ?: "id/metrics/" }

    val datasetGraphContext: String
        get() = datasetContext

    val catalogueGraphContext: String
        get() = catalogueContext

    val validationGraphContext: String
        get() = validationContext

    val metricsGraphContext: String
        get() = metricsContext

    @JvmStatic
    fun applyFor(id: String): DCATAPUriRef = DCATAPUriRef(id.asNormalized())

    @JvmStatic
    fun parseUriRef(uriRef: String): DCATAPUriRef? =
        if (uriRef.startsWith(baseUri)) DCATAPUriRef(uriRef.substringAfterLast('/')) else null

}
