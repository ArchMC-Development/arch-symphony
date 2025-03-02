package lol.arch.symphony.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import gg.scala.commons.ScalaCommons
import gg.scala.commons.velocity.VelocityPlugins
import lol.arch.combinator.CombinatorProxyPlugin
import lol.arch.symphony.velocity.command.*
import lol.arch.symphony.velocity.instance.InstanceActionExecutor
import lol.arch.symphony.velocity.instance.LiveInstanceTracker
import lol.arch.symphony.velocity.player.PlayerReconciler
import lol.arch.symphony.velocity.player.PlayerTracker
import lol.arch.symphony.velocity.player.PlayerCatalogue
import java.io.File
import java.nio.file.Path
import java.time.Duration
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
        Dependency(id = "combinator-proxy", optional = true),
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

    val playerCatalogue by lazy { PlayerCatalogue() }
    val playerTracker by lazy { PlayerTracker() }
    val playerReconciler by lazy { PlayerReconciler() }

    val instanceTracker by lazy { LiveInstanceTracker() }
    val instanceActionExecutor by lazy { InstanceActionExecutor() }

    lateinit var config: InstanceConfig

    init
    {
        instance = this
    }

    @Subscribe(order = PostOrder.LATE)
    fun ProxyInitializeEvent.on()
    {
        if (!directory.toFile().exists())
        {
            directory.toFile().mkdirs()
        }

        config = VelocityPlugins.createConfiguration(
            File(
                directory.toFile(),
                "instance.yaml"
            ),
            InstanceConfig.serializer()
        )

        if (server.pluginManager.isLoaded("combinator-proxy"))
        {
            config.id = CombinatorProxyPlugin.selfGameServerID
            logger.info("Using Combinator for the instance ID")
        }

        playerTracker.plugin = this@VelocitySymphonyPlugin
        server.eventManager.register(this@VelocitySymphonyPlugin, playerTracker)

        instanceActionExecutor.startActionTracking(this@VelocitySymphonyPlugin)
        instanceTracker.startTracking(
            this@VelocitySymphonyPlugin,
            config
        )
        playerCatalogue.startTracking(this@VelocitySymphonyPlugin)
        playerReconciler.startReconciliation(this@VelocitySymphonyPlugin)

        val commandManager = VelocityPlugins.createCommands(this@VelocitySymphonyPlugin)
        commandManager.commandCompletions.registerCompletion("instances") {
            instanceTracker.liveInstances()
        }

        commandManager.registerCommand(TrackedPlayerCommand(this@VelocitySymphonyPlugin))
        commandManager.registerCommand(GlobalListCommand(this@VelocitySymphonyPlugin), true)
        commandManager.registerCommand(RunCommandCommand(this@VelocitySymphonyPlugin), true)
        commandManager.registerCommand(ServerIDCommand(this@VelocitySymphonyPlugin), true)
        commandManager.registerCommand(ServerIDListCommand(this@VelocitySymphonyPlugin), true)
        commandManager.registerCommand(PlayerProxyCommand(this@VelocitySymphonyPlugin), true)
    }
}
