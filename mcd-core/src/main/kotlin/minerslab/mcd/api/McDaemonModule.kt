package minerslab.mcd.api

import minerslab.mcd.McDaemon

/**
 * 系统模块
 *
 * @suppress 不可在插件中使用
 */
interface McDaemonModule {

    fun start(daemon: McDaemon) {}
    fun dispose(daemon: McDaemon) {}

}