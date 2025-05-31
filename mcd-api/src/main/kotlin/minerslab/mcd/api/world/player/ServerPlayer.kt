package minerslab.mcd.api.world.player

import minerslab.mcd.handler.ServerHandler
import minerslab.mcd.util.Namespaces
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import starry.adventure.core.registry.identifierOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

open class ServerPlayer(protected val handler: ServerHandler<*>, val name: String) {

    /**
     * 获取指定路径的数据
     *
     * @return 一个含有 value 字段的 [CompoundBinaryTag]
     */
    open fun retrieveData(path: String): CompoundBinaryTag = handler.getCommandHelper()
        .processDataGet(handler.command("data get entity $name $path"))
        .let { "{value: $it}" }
        .let { TagStringIO.get().asCompound(it) }

    /**
     * 获取完整的玩家数据
     */
    open fun retrieveData(): CompoundBinaryTag = handler.getCommandHelper()
        .processDataGet(handler.command("data get entity $name"))
        .let { TagStringIO.get().asCompound(it) }

    @OptIn(ExperimentalUuidApi::class)
    open fun getUuid(): Uuid {
        val intArray = retrieveData("UUID").getIntArray("value")
        val mostSigBits = (intArray[0].toLong() shl 32) or (intArray[1].toLong() and 0xFFFFFFFF)
        val leastSigBits = (intArray[2].toLong() shl 32) or (intArray[3].toLong() and 0xFFFFFFFF)
        return Uuid.fromLongs(mostSigBits, leastSigBits)
    }

    /**
     * 获取玩家所处维度
     */
    open fun getDimension() = identifierOf(retrieveData("Dimension").getString("value"), Namespaces.MINECRAFT)

    /**
     * 获取玩家位置
     */
    open fun getPosition() = retrieveData("Pos").getList("value").let {
        val x = it.getDouble(0)
        val y = it.getDouble(1)
        val z = it.getDouble(2)
        Triple(x, y, z)
    }

}