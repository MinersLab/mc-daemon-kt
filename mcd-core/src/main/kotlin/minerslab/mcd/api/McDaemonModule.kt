package minerslab.mcd.api

/**
 * 系统模块，不可在插件中使用
 */
interface McDaemonModule {

    fun start() {}
    fun dispose() {}

}