package minerslab.mcd.common

import minerslab.mcd.api.McDaemonModule
import minerslab.mcd.common.handler.VanillaServerHandler
import minerslab.mcd.mcDaemon

class McDaemonCommon : McDaemonModule {

    override fun start() {
        mcDaemon.handlers["vanilla"] = VanillaServerHandler::class
    }

}
