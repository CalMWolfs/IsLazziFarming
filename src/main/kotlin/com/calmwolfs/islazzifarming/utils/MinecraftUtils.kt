package com.calmwolfs.islazzifarming.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution

object MinecraftUtils {
    private fun scaledResolution() = ScaledResolution(Minecraft.getMinecraft())
    fun scaledWidth() = scaledResolution().scaledWidth
}