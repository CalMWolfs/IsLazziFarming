package com.calmwolfs.islazzifarming.utils

import com.calmwolfs.islazzifarming.IsLazziFarmingMod
import com.calmwolfs.islazzifarming.data.Notification
import com.calmwolfs.islazzifarming.events.NotificationRenderEvent
import com.calmwolfs.islazzifarming.events.WorldChangeEvent
import com.calmwolfs.islazzifarming.utils.GuiRenderUtils.renderOnScreen
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object NotificationUtils {
    private val config get() = IsLazziFarmingMod.feature.settings.notification

    private lateinit var currentNotification: Notification
    private var notificationActive = false
    private val expiredNotifications = mutableListOf<Notification>()

    fun displayNotification(text: List<String>, item: ItemStack?, soundEnabled: Boolean) {
        if (soundEnabled) SoundUtils.playBeepSound()
        cancelNotification(false)
        currentNotification = Notification(text, item, SimpleTimeMark.now(), SimpleTimeMark.now().plus(7.seconds))
        notificationActive = true
    }

    private fun cancelNotification(immediately: Boolean) {
        if (!notificationActive) return
        notificationActive = false
        if (!config.animations) return
        if (immediately) return
        currentNotification.endTime = SimpleTimeMark.now()
        expiredNotifications.add(currentNotification)
    }

    @SubscribeEvent
    fun onNotificationRender(event: NotificationRenderEvent) {
        if (notificationActive && currentNotification.endTime.isInPast()) {
            cancelNotification(false)
        }

        val expired = expiredNotifications
        val notificationsToRemove = mutableListOf<Notification>()

        for (notification in expired) {
            if (notification.endTime.passedSince() >= 350.milliseconds) {
                notificationsToRemove.add(notification)
                continue
            }

            renderNotification(notification)
        }

        for (notification in notificationsToRemove)  {
            expiredNotifications.remove(notification)
        }


        if (!notificationActive) return

        renderNotification(currentNotification)
    }

    private fun renderNotification(notification: Notification) {
        var offsetX = 0
        var offsetY = 0
        val width = notification.getWidth()
        val height = notification.getHeight()
        val shownFor = notification.startTime.passedSince()
        val expiredFor = notification.endTime.passedSince()
        val expired = expiredFor.isPositive()
        var animationProgress = 0.0


        var status = when {
            expired && expiredFor < 350.milliseconds -> {
                animationProgress = expiredFor.inWholeMilliseconds / 350.0
                NotificationStatus.LEAVING
            }
            shownFor < 500.milliseconds -> {
                animationProgress = shownFor.inWholeMilliseconds / 500.0
                NotificationStatus.ENTERING
            }
            else -> {
                NotificationStatus.DISPLAYING
            }
        }

        if (!config.animations) status = NotificationStatus.DISPLAYING

        if (status != NotificationStatus.DISPLAYING) {
            if (config.direction == 0) {
                val distance = (notification.getWidth() * (1 - animationProgress)).toInt()
                offsetX = if (config.corner == 0) - distance else distance
            } else {
                offsetY = - ((notification.getHeight()) * (1 - animationProgress)).toInt()
            }
        }

        if (status == NotificationStatus.LEAVING) {
            if (config.direction == 0) {
                if (config.corner == 0) offsetX = offsetX * -1 - width
                if (config.corner == 1) offsetX = offsetX * -1 + width
            }
            if (config.direction == 1) offsetY = offsetY * -1 - height
        }


        val posX = if (config.corner == 0) 5 else MinecraftUtils.scaledWidth() - 5 - width
        val posY = 5

        GuiRenderUtils.drawAlphaRectangle(posX + offsetX, posY + offsetY, width, height)

        for ((index, line) in notification.notificationLines.withIndex()) {
            var textOffset = 0

            if (notification.itemActive() && config.corner == 0) {
                textOffset = 30
            }

            GuiRenderUtils.drawString(line, posX + 4 + offsetX + textOffset, posY + 5 + offsetY + index * 10)
        }

        if (notification.itemActive()) {
            var itemPosX = posX + 4 + offsetX + 9
            val itemPosY = posY + 5 + offsetY + height / 2 - 8
            if (config.corner == 1) itemPosX += width - 35
            notification.notificationItem!!.renderOnScreen(itemPosX, itemPosY, 2.0)
        }
    }

    private fun Notification.getHeight(): Int {
        val height = this.notificationLines.size * 10 + 10
        return if (this.itemActive() && height < 30) 30 else height
    }

    private fun Notification.getWidth(): Int {
        var width = 0
        for (line in this.notificationLines) {
            val length = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line) + 8
            if (length > width) width = length
        }

        return if (this.itemActive()) width + 30 else width
    }

    private fun Notification.itemActive() = config.items && this.notificationItem != null

    @SubscribeEvent
    fun onWorldChange(event: WorldChangeEvent) {
        cancelNotification(true)
        expiredNotifications.clear()
    }
}

enum class NotificationStatus {
    ENTERING,
    DISPLAYING,
    LEAVING
}