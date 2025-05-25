package minerslab.mcd.api.entity

import minerslab.mcd.handler.ServerHandler
import minerslab.mcd.handler.retrieve
import minerslab.mcd.util.Namespaces.MINECRAFT
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import starry.adventure.core.registry.identifierOf

class ServerPlayer(private val handler: ServerHandler<*>, val name: String) {

    /**
     * 获取指定路径的数据
     *
     * @return 一个含有 value 字段的 [CompoundBinaryTag]
     */
    fun retrieveData(path: String): CompoundBinaryTag = handler.getCommandHelper()
        .processDataGet(handler.retrieve("data get entity $name $path"))
        .let { "{value: $it}" }
        .let { TagStringIO.get().asCompound(it) }

    /**
     * 获取完整的玩家数据
     */
    fun retrieveData(): CompoundBinaryTag = handler.getCommandHelper()
        .processDataGet(handler.retrieve("data get entity $name"))
        .let { TagStringIO.get().asCompound(it) }


    fun getDimension() = identifierOf(retrieveData("Dimension").getString("value"), MINECRAFT)
    fun getPosition() = retrieveData("Pos").getList("value").let {
        val x = it.getDouble(0)
        val y = it.getDouble(1)
        val z = it.getDouble(2)
        Triple(x, y, z)
    }

}