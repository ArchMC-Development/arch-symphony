package lol.arch.symphony.velocity.instance.actor

import com.velocitypowered.api.permission.Tristate
import com.velocitypowered.api.proxy.ConsoleCommandSource
import net.kyori.adventure.permission.PermissionChecker
import net.kyori.adventure.util.TriState

object SymphonyCommandSource : ConsoleCommandSource
{
    private val permissionChecker = PermissionChecker.always(TriState.TRUE)

    override fun hasPermission(permission: String) = true
    override fun getPermissionValue(permission: String) = Tristate.TRUE
    override fun getPermissionChecker() = permissionChecker
}
