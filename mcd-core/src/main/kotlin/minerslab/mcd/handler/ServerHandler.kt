package minerslab.mcd.handler

import kotlinx.serialization.Serializable
import minerslab.mcd.McDaemon
import minerslab.mcd.handler.helper.CommandHelper
import org.glavo.rcon.Rcon
import org.slf4j.Logger
import starry.adventure.core.event.EventBus
import kotlin.reflect.KClass


/**
 * @param T 配置项目类型，需要标记为 [Serializable]
 */
interface ServerHandler<T : AbstractServerHandler.AbstractServerConfig> {


    @MustBeDocumented
    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class Config(val configClass: KClass<*>)

    val eventBus: EventBus
    val logger: Logger

    /**
     * 创建一个进程，使用 [getProcess] 获取
     * @param config 配置项目
     */
    fun initialize(config: T, mcDaemon: McDaemon)
    fun dispose()

    fun getConfig(): T
    fun getProcess(): Process
    fun getDaemon(): McDaemon
    fun getRcon(): Rcon?

    fun isPlayerMessage(line: String): Boolean
    fun handlePlayerMessage(line: String)
    fun isServerMessage(line: String): Boolean
    fun handleServerMessage(line: String)
    fun isRconStarted(line: String): Boolean
    fun parseConsoleCommandFeedback(line: String): String

    fun getCommandHelper(): CommandHelper
    fun command(command: String)

}

fun ServerHandler<*>.prompt(command: String): String {
    command(command)
    val reader = getProcess().inputStream.bufferedReader()
    return parseConsoleCommandFeedback(reader.readLine().also(::println))
}

fun ServerHandler<*>.retrieve(command: String): String {

    return getCommandHelper().processCommand(command).let {
        logger.debug("Retrieving: $it")
        getRcon()?.command(it) ?: prompt(it)
    }
}
