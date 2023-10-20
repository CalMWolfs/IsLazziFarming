package com.calmwolfs.islazzifarming.utils

import com.calmwolfs.islazzifarming.IsLazziFarmingMod
import com.calmwolfs.islazzifarming.utils.StringUtils.unformat
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText

object ChatUtils {
    fun chat(message: String) {
        internalChat(message)
    }

    fun internalChat(message: String): Boolean {
        val minecraft = Minecraft.getMinecraft()
        if (minecraft == null) {
            IsLazziFarmingMod.consoleLog(message.unformat())
            return false
        }

        val thePlayer = minecraft.thePlayer
        if (thePlayer == null) {
            IsLazziFarmingMod.consoleLog(message.unformat())
            return false
        }

        messagePlayer(ChatComponentText(message))
        return true
    }

    fun clickableChat(message: String, command: String) {
        val text = ChatComponentText(message)
        val fullCommand = "/" + command.removePrefix("/")
        text.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, fullCommand)
        text.chatStyle.chatHoverEvent =
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§eExecute $fullCommand"))
        messagePlayer(text)
    }

    private fun messagePlayer(message: ChatComponentText) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(message)
    }
}