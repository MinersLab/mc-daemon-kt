package minerslab.mcd.util

import starry.adventure.core.event.Event
import starry.adventure.core.event.EventBus
import starry.adventure.core.event.WrappedEvent

inline fun <reified T : Event> EventBus.addEventListener(noinline callback: (T) -> Unit): (WrappedEvent<T>) -> Unit =
    on(T::class) { callback(it.unwrap()) }
