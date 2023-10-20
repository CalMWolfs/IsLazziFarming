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

object GetLazziStats {
    private val config get() = IsLazziFarmingMod.feature.settings.notification

    private var lastFetchTime = SimpleTimeMark.farPast()
    private var lastLazziStats = 0.0

    @SubscribeEvent
    fun onHypixelJoin(event: HypixelJoinEvent) {
        IsLazziFarmingMod.coroutineScope.launch {
            val currentStats = ApiUtils.getLazziStats()
            if (currentStats != null) {
                lastLazziStats = currentStats
            } else {
                if (config.enabled) {
                    val errorMessage = listOf("§c§lAPI ERROR", "§cLazzi02's exp was null", "§eMaybe no api key set")
                    NotificationUtils.displayNotification(errorMessage, ItemStack(Blocks.barrier), config.sound)
                }
                ModUtils.error("§cLazzi02's exp was null. Maybe you have no api key set")
            }
            lastFetchTime = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onTick(event: ModTickEvent) {
        if (!event.repeatSeconds(10)) return
        if (lastFetchTime.passedSince() < 5.minutes) return
        loadStats()
    }

    fun loadStats() {

        IsLazziFarmingMod.coroutineScope.launch {
            val currentStats = ApiUtils.getLazziStats()
            if (currentStats == null) {
                if (config.enabled) {
                    val errorMessage = listOf("§c§lAPI ERROR", "§cLazzi02's exp was null", "§eMaybe no api key set")
                    NotificationUtils.displayNotification(errorMessage, ItemStack(Blocks.barrier), config.sound)
                }
                ModUtils.error("§cLazzi02's exp was null. Maybe you have no api key set!")
            } else {
                val difference = currentStats - lastLazziStats
                val gainedExp = difference != 0.0
                val differenceString = difference.addSeparators()
                lastLazziStats = currentStats
                val status = if (gainedExp) "§aLazzi02 is farming" else "§eLazzi02 is not farming"

                if (config.enabled) {
                    val sound = config.sound && gainedExp
                    var displayList = listOf("§a§lLazzi02 Update:", status)
                    if (gainedExp) displayList = displayList + "§a$differenceString Farming exp gained"
                    NotificationUtils.displayNotification(displayList, ItemStack(Items.reeds), sound)
                }

                ChatUtils.chat("§a§lLazzi02 Update:")
                ChatUtils.chat(status)
                if (gainedExp) ChatUtils.chat("§a$differenceString Farming exp gained")
            }
            lastFetchTime = SimpleTimeMark.now()
        }
    }
}