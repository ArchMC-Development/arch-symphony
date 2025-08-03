package lol.arch.symphony.velocity.rpc

import lol.arch.symphony.velocity.instance.requests.RunCommandRequest
import lol.arch.symphony.velocity.player.requests.PlayerReconcileRequest
import mc.arch.commons.communications.rpc.CommunicationGateway
import mc.arch.commons.communications.rpc.createOneWayRemoteService

/**
 * @author Subham
 * @since 8/3/25
 */
object SymphonyRPC
{
    val gateway = CommunicationGateway("symphony")
    val reconcilePlayerRPC = gateway.createOneWayRemoteService<PlayerReconcileRequest>("playerReconcile")
    val runCommandRPC = gateway.createOneWayRemoteService<RunCommandRequest>("runCommand")
}
