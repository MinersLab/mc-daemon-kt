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

    /**
     * 事件总线
     */
    val eventBus: EventBus
    val logger: Logger

    /**
     * 配置项目
     */
    val config: T

    /**
     * 服务器进程
     */
    val process: Process

    /**
     * @param config 配置项目
     */
    fun initialize(config: T)

    /**
     * 停止 [ServerHandler]
     */
    fun dispose()

    /**
     * 获取命令助手
     */
    fun getCommandHelper(): CommandHelper

    /**
     * 在服务器执行命令
     */
    fun command(command: String, mode: CommandExecutingMode = CommandExecutingMode.RCON): String

}
