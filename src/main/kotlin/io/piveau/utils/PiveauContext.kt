@file:JvmName("Piveau")
package io.piveau.utils

fun createPiveauContext(serviceName: String, moduleName: String) = PiveauContext(serviceName, moduleName)

class PiveauContext constructor(val serviceName: String, val moduleName: String) {
    private var resource = "self"

    private constructor(serviceName: String, moduleName: String, resource: String) : this(serviceName, moduleName) {
        this.resource = resource
    }

    fun extend(resource: String) = PiveauContext(serviceName, moduleName, resource)

    private val log : PiveauLogger by lazy { PiveauLogger(serviceName, moduleName, resource) }

    fun log() = log
}

