package minerslab.mcd.plugin

enum class PluginStatus(val enabled: Boolean = false) {

    IDLE, ERROR, LOADING, LOADED(true), CLOSED;

}