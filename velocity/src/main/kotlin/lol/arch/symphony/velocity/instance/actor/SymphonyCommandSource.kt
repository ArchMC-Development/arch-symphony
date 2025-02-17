package lol.arch.symphony.velocity.instance.actor

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.permission.Tristate
import net.kyori.adventure.permission.PermissionChecker
import net.kyori.adventure.util.TriState

object SymphonyCommandSource : CommandSource
{
    private val permissionChecker = PermissionChecker.always(TriState.TRUE)

    override fun hasPermission(permission: String) = permissionChecker.test(permission)
    override fun getPermissionValue(permission: String) = Tristate.TRUE
    override fun getPermissionChecker() = permissionChecker
}
