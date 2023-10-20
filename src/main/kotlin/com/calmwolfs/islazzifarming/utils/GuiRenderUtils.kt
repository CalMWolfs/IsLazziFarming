package com.calmwolfs.islazzifarming.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

object GuiRenderUtils {
    //From NEU
    fun drawAlphaRectangle(posX: Int, posY: Int, width: Int, height: Int, transparency: Double = 0.9) {
        var alpha = (transparency.coerceIn(0.0, 1.0) * 255).toInt() shl 24

        if (!OpenGlHelper.isFramebufferEnabled()) {
            alpha = 0xff000000.toInt()
        }

        val background = alpha or 0x202026
        val light = alpha or 0x303036
        val dark = alpha or 0x101016
        val shadow = alpha or 0x000000

        Gui.drawRect(posX, posY, posX + 1, posY + height, light) // Left
        Gui.drawRect(posX + 1, posY, posX + width, posY + 1, light) // Top
        Gui.drawRect(posX + width - 1, posY + 1, posX + width, posY + height, dark) // Right
        Gui.drawRect(posX + 1, posY + height - 1, posX + width - 1, posY + height, dark) // Bottom
        Gui.drawRect(posX + 1, posY + 1, posX + width - 1, posY + height - 1, background) // Middle

        Gui.drawRect(posX + width, posY + 2, posX + width + 2, posY + height + 2, shadow) // Right shadow
        Gui.drawRect(posX + 2, posY + height, posX + width, posY + height + 2, shadow) // Bottom shadow
    }

    fun drawString(str: String, x: Int, y: Int) {
        Minecraft.getMinecraft().fontRendererObj.drawString(str, x.toFloat(), y.toFloat(), 0xffffff, true)
    }

    private fun ItemStack.renderOnScreen(x: Float, y: Float, scaleMultiplier: Double = 1.0) {
        val isSkull = this.item === Items.skull

        val baseScale = (if (isSkull) 0.8f else 0.6f)
        val finalScale = baseScale * scaleMultiplier
        val diff = ((finalScale - baseScale) * 10).toFloat()

        val translateX: Float
        val translateY: Float
        if (isSkull) {
            translateX = x - 2 - diff
            translateY = y - 2 - diff
        } else {
            translateX = x - diff
            translateY = y - diff
        }

        GlStateManager.pushMatrix()

        GlStateManager.translate(translateX, translateY, 1F)
        GlStateManager.scale(finalScale, finalScale, 1.0)

        RenderHelper.enableGUIStandardItemLighting()
        Minecraft.getMinecraft().renderItem.renderItemIntoGUI(this, 0, 0)
        RenderHelper.disableStandardItemLighting()

        GlStateManager.popMatrix()
    }

    fun ItemStack.renderOnScreen(x: Int, y: Int, scaleMultiplier: Double = 1.0) {
        this.renderOnScreen(x.toFloat(), y.toFloat(), scaleMultiplier)
    }
}