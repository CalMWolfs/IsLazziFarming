package com.calmwolfs.islazzifarming.features

import com.calmwolfs.islazzifarming.IsLazziFarmingMod
import com.calmwolfs.islazzifarming.events.HypixelJoinEvent
import com.calmwolfs.islazzifarming.events.ModTickEvent
import com.calmwolfs.islazzifarming.utils.ApiUtils
import com.calmwolfs.islazzifarming.utils.ChatUtils
import com.calmwolfs.islazzifarming.utils.ModUtils
import com.calmwolfs.islazzifarming.utils.NotificationUtils
import com.calmwolfs.islazzifarming.utils.SimpleTimeMark
import com.calmwolfs.islazzifarming.utils.StringUtils.addSeparators
import kotlinx.coroutines.launch
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object GetLazziStats {
    private val config get() = IsLazziFarmingMod.feature.settings
    private val notificationConfig get() = config.notification

    private var lastFetchTime = SimpleTimeMark.farPast()
    private var lastFetchTimeSmall = SimpleTimeMark.farPast()
    private var lastStats = -1.0
    private var lastFetchedPlayer = ""

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        getStats()
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (!event.repeatSeconds(10)) return
        if (lastFetchTime.passedSince() < 5.minutes) return
        getStats()
    }

    fun getStats() {
        if (lastFetchTimeSmall.passedSince() < 30.seconds) return
        lastFetchTimeSmall = SimpleTimeMark.now()
        IsLazziFarmingMod.coroutineScope.launch {
            val isFirst = (lastStats == -1.0 || config.playerToTrack != lastFetchedPlayer)
            val uuid = ApiUtils.getUuid(config.playerToTrack) ?: run {
                if (notificationConfig.enabled) {
                    val errorMessage = listOf("§c§lERROR", "§c${config.playerToTrack} had no uuid", "§eMaybe you didn't enter a valid username")
                    NotificationUtils.displayNotification(errorMessage, ItemStack(Blocks.barrier), notificationConfig.sound)
                }
                ModUtils.error("§c${config.playerToTrack} does not have a uuid. Make sure you entered a valid username")

                return@launch
            }

            val currentStats = ApiUtils.getPlayerStats(uuid) ?: run {
                if (notificationConfig.enabled) {
                    val errorMessage = listOf("§c§lAPI ERROR", "§c${config.playerToTrack}'s exp was null", "§eMaybe there was an error with the proxy server")
                    NotificationUtils.displayNotification(errorMessage, ItemStack(Blocks.barrier), notificationConfig.sound)
                }
                ModUtils.error("§c${config.playerToTrack}'s exp was null. Maybe the proxy is rate limited!")

                return@launch
            }
            lastFetchedPlayer = config.playerToTrack
            lastFetchTime = SimpleTimeMark.now()
            val difference = currentStats - lastStats
            lastStats = currentStats
            if (isFirst) return@launch

            val gainedExp = difference != 0.0
            val differenceString = difference.addSeparators()
            val status = if (gainedExp) "§a${config.playerToTrack} is farming" else "§e${config.playerToTrack} is not farming"

            if (notificationConfig.enabled) {
                val sound = notificationConfig.sound && gainedExp
                var displayList = listOf("§a§l${config.playerToTrack} Update:", status)
                if (gainedExp) displayList = displayList + "§a$differenceString Farming exp gained"
                NotificationUtils.displayNotification(displayList, ItemStack(Items.reeds), sound)
            }

            if (notificationConfig.message) {
                ChatUtils.chat("§a§l${config.playerToTrack} Update:")
                ChatUtils.chat(status)
                if (gainedExp) ChatUtils.chat("§a$differenceString Farming exp gained")
            }
        }
    }
}