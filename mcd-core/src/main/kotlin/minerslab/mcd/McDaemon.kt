package minerslab.mcd

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.serializer
import minerslab.mcd.api.McDaemonModule
import minerslab.mcd.event.EmbeddedServerEvent
import minerslab.mcd.handler.ServerHandler
import minerslab.mcd.plugin.PluginManager
import starry.adventure.core.event.EventBus
import java.nio.file.Path
import java.util.*
import kotlin.io.path.div
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.time.Duration.Companion.seconds

typealias EmbeddedServerType = EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

class McDaemon(val args: Array<out String>, val path: Path) {

    init {
        currentMcDaemon = this
    }

    val eventBus = EventBus(this::class)

    @OptIn(ExperimentalSerializationApi::class)
    val config = (path / "config.conf").toFile().run {
        if (!isFile) {
            createNewFile()
            writeBytes(McDaemon::class.java.getResourceAsStream("/templates/config.conf")!!.readAllBytes())
        }
        Hocon.decodeFromConfig<McDaemonConfig>(ConfigFactory.parseFile(this))
    }

    val embeddedServer: EmbeddedServerType = embeddedServer(
        Netty,
        configure = {
            connectors.add(
                EngineConnectorBuilder().apply {
                    host = config.server.host
                    port = config.server.port
                }
            )
        }
    ) {
        install(WebSockets) {
            pingPeriod = 15.seconds
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        eventBus.emit(EmbeddedServerEvent.StartEvent(embeddedServer, this))
    }

    val pluginManager = PluginManager(this)
    val modules = ServiceLoader.load(McDaemonModule::class.java).toList()
    val handlers = mutableMapOf<String, KClass<out ServerHandler<*>>>()

    val handler: ServerHandler<*> by lazy {
        handlers[config.handler]!!.primaryConstructor!!.call()
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalSerializationApi::class)
    private fun initHandler() {
        val handlerClass = handler::class
        val configClass = handlerClass.findAnnotation<ServerHandler.Config>()!!.configClass
        val config = (path / "handler.conf").toFile().run {
            if (!isFile) {
                createNewFile()
                writeBytes(McDaemon::class.java.getResourceAsStream("/templates/handler.conf")!!.readAllBytes())
            }
            Hocon.decodeFromConfig(serializer(configClass.createType()), ConfigFactory.parseFile(this))
        }
        (handler as ServerHandler<Any?>).initialize(config)
    }

    fun start() {
        isStopped = false
        modules.forEach(McDaemonModule::start)
        pluginManager.scan().forEach {
            pluginManager.add(it.first, it.second)
        }
        pluginManager.construct()
        pluginManager.load()
        initHandler()
        if (config.server.enabled) embeddedServer.start(true)
    }

    var isStopped = false

    fun stop() {
        if (isStopped) return
        isStopped = true
        modules.forEach { runCatching { it.dispose() } }
        runCatching(handler::dispose)
        runCatching(pluginManager::dispose)
        if (config.server.enabled) runCatching(embeddedServer::stop)
    }

}

/**
 * 版本信息
 */
object McDaemonVersion {

    fun toMap() = meta.toMap().mapKeys { it.key.toString() }.mapValues { it.value.toString() }

    private val meta = Properties().apply {
        load(McDaemon::class.java.getResourceAsStream("/META-INF/mcd-core.properties"))
    }

    val version = meta["version"].toString()
    val gitLastTag = meta["git-last-tag"].toString()
    val gitBranch = meta["git-branch"].toString()
    val gitHash = meta["git-hash"].toString()

}

internal lateinit var currentMcDaemon: McDaemon

/**
 * 获取 [McDaemon] 实例
 */
val mcDaemon: McDaemon
    get() = currentMcDaemon

/**
 * 在 [McDaemon] 实例中查找 [McDaemonModule] 模块
 */
inline fun <reified T : McDaemonModule> McDaemon.findModule(): T = modules.filterIsInstance<T>().first()
