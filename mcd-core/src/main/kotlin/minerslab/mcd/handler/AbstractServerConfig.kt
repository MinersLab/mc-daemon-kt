package minerslab.mcd.handler

import io.ktor.utils.io.charsets.name
import kotlinx.serialization.Serializable

@Serializable
open class AbstractServerConfig {
    open var commandLine = "cmd /c run.bat"
    open var inputCharset: String = Charsets.UTF_8.name
    open var outputCharset: String = Charsets.UTF_8.name
    open var rconCharset: String = Charsets.UTF_8.name
    open var rcon: RconConfig = RconConfig()
    open var server: ServerConfig = ServerConfig()

    @Serializable
    open class ServerConfig {
        open var daemonCommandPrefix = "!!"
        open var gameCommandPrefix = "/"
    }

    @Serializable
    open class RconConfig {
        open var enabled: Boolean = true
        open var port: Int = 25575
        open var password: String = "123456"
        open var host: String = "localhost"
    }

}