package minerslab.mcd.api.world.entity

import minerslab.mcd.handler.ServerHandler
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

abstract class Entity {

    protected abstract val handler: ServerHandler<*>

    /**
     * 创建一个实体选择器字符串
     *
     * @return 实体选择器字符串
     */
    @OptIn(ExperimentalUuidApi::class)
    open fun createSelector() = getUuid().toHexDashString()

    /**
     * 获取实体的名称
     *
     * @return 实体的名称
     */
    open fun getName() = handler.commandHelper
        .processDataGet(handler.command("data get entity ${createSelector()}"))
        .first

    /**
     * 获取实体的显示名称
     *
     * @return 实体的显示名称
     */
    open fun getDisplayName() = runCatching {
        retrieveData("CustomName").getString("value")
    }.getOrElse { getName() }

    /**
     * 获取实体的 [Uuid]
     */
    @OptIn(ExperimentalUuidApi::class)
    open fun getUuid(): Uuid {
        val intArray = retrieveData("UUID").getIntArray("value")
        val mostSigBits = (intArray[0].toLong() shl 32) or (intArray[1].toLong() and 0xFFFFFFFF)
        val leastSigBits = (intArray[2].toLong() shl 32) or (intArray[3].toLong() and 0xFFFFFFFF)
        return Uuid.fromLongs(mostSigBits, leastSigBits)
    }

    /**
     * 获取指定路径的数据
     *
     * @return 一个含有 value 字段的 [CompoundBinaryTag]
     */
    open fun retrieveData(path: String): CompoundBinaryTag = handler.commandHelper
        .processDataGet(handler.command("data get entity ${createSelector()} $path"))
        .let { "{value: ${it.second}}" }
        .let { TagStringIO.get().asCompound(it) }

    /**
     * 获取完整的实体数据
     */
    open fun retrieveData(): CompoundBinaryTag = handler.commandHelper
        .processDataGet(handler.command("data get entity ${createSelector()}"))
        .let { TagStringIO.get().asCompound(it.second) }

    /**
     * 获取实体位置
     *
     * @return 实体位置的三元组 (x, y, z)
     */
    open fun getPosition() = retrieveData("Pos").getList("value").let {
        val x = it.getDouble(0)
        val y = it.getDouble(1)
        val z = it.getDouble(2)
        Triple(x, y, z)
    }

}