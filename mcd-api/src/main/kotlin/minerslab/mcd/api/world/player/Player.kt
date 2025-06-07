package minerslab.mcd.api.world.player

import minerslab.mcd.api.world.entity.LivingEntity
import minerslab.mcd.handler.ServerHandler
import minerslab.mcd.util.Namespaces
import starry.adventure.registry.identifierOf

open class Player(override val handler: ServerHandler<*>, private val selector: String) : LivingEntity() {

    override fun createSelector() = selector


    /**
     * 获取实体所处维度
     */
    open fun getDimension() = identifierOf(retrieveData("Dimension").getString("value"), Namespaces.MINECRAFT)

}
