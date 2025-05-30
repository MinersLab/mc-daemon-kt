package minerslab.mcd.handler

import kotlinx.serialization.Serializable
import minerslab.mcd.handler.helper.CommandHelper
import org.slf4j.Logger
import starry.adventure.core.event.EventBus
import kotlin.reflect.KClass

/**
 * @param T 配置项目类型，需要标记为 [Serializable]
 */
interface ServerHandler<T : AbstractServerConfig> {

    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Config(val configClass: KClass<*>)

    val eventBus: EventBus
    val logger: Logger
    val config: T
    val process: Process

    /**
     * @param config 配置项目
     */
    fun initialize(config: T)
    fun dispose()

    fun getCommandHelper(): CommandHelper

    /**
     * 在服务器执行命令
     */
    fun command(command: String, mode: CommandExecutingMode = CommandExecutingMode.RCON): String

}
