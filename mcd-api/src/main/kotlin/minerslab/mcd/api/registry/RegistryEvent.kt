package minerslab.mcd.api.registry

import starry.adventure.core.event.Event
import starry.adventure.core.registry.Registry

sealed class RegistryEvent<T>(val registry: Registry<T>) : Event() {

    class RegisterEvent<T>(registry: Registry<T>) : RegistryEvent<T>(registry)

}