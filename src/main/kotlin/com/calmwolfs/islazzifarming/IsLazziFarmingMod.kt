package com.calmwolfs.islazzifarming

import com.calmwolfs.islazzifarming.config.Features
import com.calmwolfs.islazzifarming.config.UpdateManager
import com.calmwolfs.islazzifarming.config.guistuff.ConfigManager
import com.calmwolfs.islazzifarming.data.Commands
import com.calmwolfs.islazzifarming.data.MinecraftData
import com.calmwolfs.islazzifarming.data.ScoreboardData
import com.calmwolfs.islazzifarming.events.ModTickEvent
import com.calmwolfs.islazzifarming.features.GetLazziStats
import com.calmwolfs.islazzifarming.utils.HypixelUtils
import com.calmwolfs.islazzifarming.utils.NotificationUtils
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(
    modid = IsLazziFarmingMod.MODID,
    clientSideOnly = true,
    useMetadata = true,
    guiFactory = "com.calmwolfs.islazzifarming.config.guistuff.ConfigGuiForgeInterop",
    version = "1.2",
)
class IsLazziFarmingMod {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        loadModule(this)

        loadModule(HypixelUtils)
        loadModule(MinecraftData())
        loadModule(ScoreboardData)
        loadModule(GetLazziStats)
        loadModule(NotificationUtils)
        loadModule(UpdateManager)

        Commands.init()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        configManager = ConfigManager
        configManager.firstLoad()
        Runtime.getRuntime().addShutdownHook(Thread {
            configManager.saveConfig("shutdown-hook")
        })
    }

    private fun loadModule(obj: Any) {
        modules.add(obj)
        MinecraftForge.EVENT_BUS.register(obj)
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (screenToOpen != null) {
            Minecraft.getMinecraft().displayGuiScreen(screenToOpen)
            screenToOpen = null
        }
    }

    companion object {
        const val MODID = "islazzifarming"

        @JvmStatic
        val version: String get() = Loader.instance().indexedModList[MODID]!!.version

        @JvmStatic
        val feature: Features get() = configManager.features

        lateinit var configManager: ConfigManager

        private val logger: Logger = LogManager.getLogger("IsLazziFarming")
        fun consoleLog(message: String) {
            logger.log(Level.INFO, message)
        }

        private val modules: MutableList<Any> = ArrayList()
        private val globalJob: Job = Job(null)

        val coroutineScope = CoroutineScope(
            CoroutineName("IsLazziFarming") + SupervisorJob(globalJob)
        )

        var screenToOpen: GuiScreen? = null
    }
}