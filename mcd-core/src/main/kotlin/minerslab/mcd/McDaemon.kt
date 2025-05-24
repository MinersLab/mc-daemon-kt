package minerslab.mcd

import com.typesafe.config.ConfigFactory
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.serializer
import minerslab.mcd.api.McDaemonModule
import minerslab.mcd.event.EmbeddedServerEvent
import minerslab.mcd.handler.ServerHandler
import minerslab.mcd.handler.VanillaServerHandler
import minerslab.mcd.plugin.PluginManager
import starry.adventure.core.event.EventBus
import java.nio.file.Path
import java.util.*
import kotlin.io.path.div
import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

typealias EmbeddedServerType = EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

class McDaemon(val args: Array<out String>, val path: Path) {

    companion object {
        const val DEFAULT_ID = "mcd"
    }

    init {
        currentMcDaemon = this
    }

    val eventBus = EventBus(this::class)

    val embeddedServer: EmbeddedServerType = embeddedServer(Netty, configure = {
        connectors.add(EngineConnectorBuilder().apply {
            host = "localhost"
            port = 8080
        })
    }) {
        eventBus.emit(EmbeddedServerEvent.StartEvent(embeddedServer, this))
    }

    @OptIn(ExperimentalSerializationApi::class)
    val config = (path / "config.conf").toFile().run {
        if (!isFile) {
            createNewFile()
            writeBytes(McDaemon::class.java.getResourceAsStream("/templates/config.conf")!!.readAllBytes())
        }
        Hocon.decodeFromConfig<McDaemonConfig>(ConfigFactory.parseFile(this))
    }

    val pluginManager = PluginManager(this)
    val modules = ServiceLoader.load(McDaemonModule::class.java).toList()
    val handlers = mutableMapOf<String, KClass<out ServerHandler<*>>>().apply {
        this["vanilla"] = VanillaServerHandler::class
    }

    val handler: ServerHandler<*> = handlers[config.handler]!!.primaryConstructor!!.call()

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
        (handler as ServerHandler<Any?>).initialize(config, this)
    }

    fun start() {
        modules.forEach { it.start(this) }
        pluginManager.scan().forEach {
            pluginManager.add(it.first, it.second)
        }
        pluginManager.construct()
        pluginManager.load()
        initHandler()
        embeddedServer.start(true)
    }

    fun stop() {
        modules.forEach { it.dispose(this) }
        handler.dispose()
        pluginManager.dispose()
        embeddedServer.stop()
    }

}

internal lateinit var currentMcDaemon: McDaemon
val mcDaemon: McDaemon
    get() = currentMcDaemon

inline fun <reified T : McDaemonModule> McDaemon.findModule(): T = modules.filterIsInstance<T>().first()
