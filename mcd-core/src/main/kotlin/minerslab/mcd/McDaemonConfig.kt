package minerslab.mcd

import kotlinx.serialization.Serializable

@Serializable
data class McDaemonConfig(
    val handler: String = "vanilla",
    val server: Server = Server()
) {

    @Serializable
    data class Server(val host: String = "localhost", val port: Int = 8080)

}
