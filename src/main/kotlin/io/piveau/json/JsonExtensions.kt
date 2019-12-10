package io.piveau.json

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.jena.rdf.model.*
import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.vocabulary.RDF
import java.io.InputStream

fun JsonArray.addIfNotEmpty(value: JsonObject): JsonArray = if (value.isEmpty) this else add(value)
fun JsonArray.addIfNotNull(value: Any?): JsonArray = value?.let { add(value) } ?: this

fun JsonObject.withJsonObject(key: String): JsonObject = getJsonObject(key) ?: JsonObject().apply { this@withJsonObject.put(key, this) }
fun JsonObject.withJsonArray(key: String): JsonArray = getJsonArray(key) ?: JsonArray().apply { this@withJsonArray.put(key, this) }

fun JsonObject.putIfNotEmpty(key: String, value: JsonObject): JsonObject = if (value.isEmpty) this else put(key, value)
fun JsonObject.putIfNotNull(key: String, value: Any?): JsonObject = value?.let { put(key, value) } ?: this
fun JsonObject.putPair(pair: Pair<String, JsonObject?>): JsonObject = putIfNotNull(pair.first, pair.second)

