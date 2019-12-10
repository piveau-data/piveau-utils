package io.piveau

import io.piveau.utils.ConfigHelper
import io.piveau.utils.forceJsonObject
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigHelperTest {

    companion object {
        val config = JsonObject("{ \"keyString\": \"{ }\", \"keyJson\": {} }")
    }

    @Test
    fun getStringJson() {
        val json = ConfigHelper.forConfig(config).getJson("keyString")
        assert(json == JsonObject("{}"))
        val json2 = config.forceJsonObject("keyString")
        assert(json2 == JsonObject("{}"))
    }

    @Test
    fun getJsonJson() {
        val json = ConfigHelper.forConfig(config).getJson("keyJson")
        assert(json == JsonObject("{}"))
        val json2 = config.forceJsonObject("keyJson")
        assert(json2 == JsonObject("{}"))
    }

    @Test
    fun getNullJson() {
        val json = ConfigHelper.forConfig(config).getJson("anyKey")
        assert(json == JsonObject())
        val json2 = config.forceJsonObject("anyKey")
        assert(json2 == JsonObject())
    }

}