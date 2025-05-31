package minerslab.mcd.util

import starry.adventure.core.registry.identifierOf
import starry.adventure.core.registry.Identifier

/**
 * 命名空间
 * @see Identifier
 */
object Namespaces {

    const val MC_DAEMON = "mcd"
    const val MINECRAFT = "minecraft"

    fun mcDaemon(path: String) = identifierOf(path, MC_DAEMON)
    fun minecraft(path: String) = identifierOf(path, MINECRAFT)

}