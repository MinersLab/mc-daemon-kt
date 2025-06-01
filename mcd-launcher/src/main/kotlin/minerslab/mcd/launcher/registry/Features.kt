package minerslab.mcd.launcher.registry

import minerslab.mcd.api.data.Feature
import minerslab.mcd.api.registry.Registries
import minerslab.mcd.util.IBootstrap
import minerslab.mcd.util.Namespaces.mcDaemon

object Features : IBootstrap {

    object Commands : IBootstrap {

        private fun create(name: String, defaultEnabled: Boolean = true) =
            Registries.FEATURES.register(mcDaemon("command/$name")) { Feature(defaultEnabled) }

        @JvmStatic val HELP = create("help")
        @JvmStatic val SUDO = create("sudo")
        @JvmStatic val HERE = create("here")

    }

    override fun bootstrap() {
        Commands.bootstrap()
    }

}