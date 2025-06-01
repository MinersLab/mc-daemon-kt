package minerslab.mcd.handler.helper

interface CommandHelper {

    /**
     * 处理命令到当前平台
     * @param command 输入的命令
     * @return 处理后的命令
     */
    fun processCommand(command: String): String = command

    /**
     * 解析 `data get` 命令的运行结果
     *
     * @return `对象名称` 和 `SNBT 文本`
     */
    fun processDataGet(raw: String): Pair<String, String>

    fun stop() = "stop"
    fun tellraw(target: String, raw: String): String = "tellraw $target $raw"

}