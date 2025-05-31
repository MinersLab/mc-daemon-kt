package org.example

import minerslab.mcd.api.registry.Registries
import minerslab.mcd.api.registry.createResourceKey
import minerslab.mcd.api.registry.deferred
import org.example.ExamplePlugin.id
import starry.adventure.core.registry.IRegistry
import starry.adventure.core.registry.Identifier
import starry.adventure.core.registry.Registry

object ExampleRegistries {

    @JvmStatic
    private val REGISTRIES = Registries.REGISTRIES.deferred()

    @JvmStatic
    val MY_REGISTRY by REGISTRIES.register("my_registry") {
        Registry(Registries.REGISTRIES.createResourceKey<IRegistry<Any>>(Identifier(id, "my_registry")))
    }

    fun bootstrap() {}

}