package minerslab.mcd.handler

enum class CommandExecutingMode {
    /** 优先使用 Rcon */
    RCON,
    /** 只使用 Console */
    CONSOLE;
}