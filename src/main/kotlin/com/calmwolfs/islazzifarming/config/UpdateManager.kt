package com.calmwolfs.islazzifarming.config

import com.calmwolfs.islazzifarming.IsLazziFarmingMod
import com.calmwolfs.islazzifarming.config.updater.ConfigVersionDisplay
import com.calmwolfs.islazzifarming.config.updater.GuiOptionEditorUpdateCheck
import com.calmwolfs.islazzifarming.events.HypixelJoinEvent
import com.calmwolfs.islazzifarming.events.ModTickEvent
import com.calmwolfs.islazzifarming.utils.ChatUtils
import com.calmwolfs.islazzifarming.utils.MinecraftExecutor
import com.calmwolfs.islazzifarming.utils.ModUtils.onToggle
import io.github.moulberry.moulconfig.processor.MoulConfigProcessor
import moe.nea.libautoupdate.CurrentVersion
import moe.nea.libautoupdate.PotentialUpdate
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateSource
import moe.nea.libautoupdate.UpdateTarget
import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.CompletableFuture

object UpdateManager {
    private var _activePromise: CompletableFuture<*>? = null
    private var activePromise: CompletableFuture<*>?
        get() = _activePromise
        set(value) {
            _activePromise?.cancel(true)
            _activePromise = value
        }

    var updateState: UpdateState = UpdateState.NONE
        private set

    fun getNextVersion(): String? {
        return potentialUpdate?.update?.versionNumber?.asString
    }

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        IsLazziFarmingMod.feature.settings.updateStream.onToggle {
            reset()
        }
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        Minecraft.getMinecraft().thePlayer ?: return
        MinecraftForge.EVENT_BUS.unregister(this)
        if (config.autoUpdates)
            checkUpdate()
    }

    fun getCurrentVersion(): String {
        return IsLazziFarmingMod.version
    }

    fun injectConfigProcessor(processor: MoulConfigProcessor<*>) {
        processor.registerConfigEditor(ConfigVersionDisplay::class.java) { option, _ ->
            GuiOptionEditorUpdateCheck(option)
        }
    }

    private fun isPreRelease(): Boolean {
        return getCurrentVersion().contains("pre", ignoreCase = true)
    }

    private val config get() = IsLazziFarmingMod.feature.settings

    private fun reset() {
        updateState = UpdateState.NONE
        _activePromise = null
        potentialUpdate = null
        println("Reset update state")
    }

    fun checkUpdate() {
        if (updateState != UpdateState.NONE) {
            println("Trying to perform update check while another update is already in progress")
            return
        }
        println("Starting update check")
        var updateStream = config.updateStream.get()
        if (updateStream == SettingsConfig.UpdateStream.RELEASES && isPreRelease()) {
            updateStream = SettingsConfig.UpdateStream.PRE
        }
        activePromise = context.checkUpdate(updateStream.stream)
            .thenAcceptAsync({
                println("Update check completed")
                if (updateState != UpdateState.NONE) {
                    println("This appears to be the second update check. Ignoring this one")
                    return@thenAcceptAsync
                }
                potentialUpdate = it
                if (it.isUpdateAvailable) {
                    updateState = UpdateState.AVAILABLE
                    ChatUtils.clickableChat(
                        "§e[IsLazziFarming] §afound a new update: ${getNextVersion()} " +
                                "§aGo check §b/lz download §afor more info.",
                        "lz download"
                    )
                }
            }, MinecraftExecutor.OnThread)
    }

    fun queueUpdate() {
        if (updateState != UpdateState.AVAILABLE) {
            println("Trying to enqueue an update while another one is already downloaded or none is present")
        }
        updateState = UpdateState.QUEUED
        activePromise = CompletableFuture.supplyAsync {
            println("Update download started")
            potentialUpdate!!.prepareUpdate()
        }.thenAcceptAsync({
            println("Update download completed, setting exit hook")
            updateState = UpdateState.DOWNLOADED
            potentialUpdate!!.executeUpdate()
        }, MinecraftExecutor.OnThread)
    }

    private val context = UpdateContext(
        UpdateSource.githubUpdateSource("CalMWolfs", "IsLazziFarming"),
        UpdateTarget.deleteAndSaveInTheSameFolder(UpdateManager::class.java),
        CurrentVersion.ofTag(IsLazziFarmingMod.version),
        IsLazziFarmingMod.MODID,
    )

    init {
        context.cleanup()
    }

    enum class UpdateState {
        AVAILABLE,
        QUEUED,
        DOWNLOADED,
        NONE
    }

    private var potentialUpdate: PotentialUpdate? = null
}