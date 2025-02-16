package lol.arch.symphony.player.requests

import java.util.UUID

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
data class PlayerReconcileRequest(
    val player: UUID,
    val instance: String
)
