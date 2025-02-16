package lol.arch.symphony

import com.google.inject.Inject
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import java.util.logging.Logger
import kotlin.properties.Delegates

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
@Plugin(
    id = "symphony",
    name = "Symphony",
    version = "1.0.0",
    url = "https://arch.lol/",
    authors = ["GrowlyX"],
    dependencies = [
        Dependency(id = "scala-commons"),
        Dependency(id = "store-velocity"),
    ]
)
class VelocitySymphonyPlugin
@Inject
constructor(
    val server: ProxyServer,
    val logger: Logger,
    @DataDirectory
    private val directory: Path
)
{
    companion object
    {
        @JvmStatic
        var instance by Delegates.notNull<VelocitySymphonyPlugin>()
    }


}
