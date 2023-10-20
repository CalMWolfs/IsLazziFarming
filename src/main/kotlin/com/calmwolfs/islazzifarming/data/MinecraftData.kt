package com.calmwolfs.islazzifarming.data

import com.calmwolfs.islazzifarming.events.ModTickEvent
import com.calmwolfs.islazzifarming.events.NotificationRenderEvent
import com.calmwolfs.islazzifarming.events.WorldChangeEvent
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

class MinecraftData {
    private var tick = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        Minecraft.getMinecraft().thePlayer ?: return
        tick++
        ModTickEvent(tick).postAndCatch()
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        WorldChangeEvent().postAndCatch()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR) return

        NotificationRenderEvent().postAndCatch()
    }
}