package minerslab.mcd.util

import starry.adventure.event.Event
import starry.adventure.event.EventBus
import starry.adventure.event.WrappedEvent

/**
 * 添加事件监听器
 */
inline fun <reified T : Event> EventBus.addEventListener(noinline callback: (T) -> Unit): (WrappedEvent<T>) -> Unit =
    on(T::class) { callback(it.unwrap()) }
