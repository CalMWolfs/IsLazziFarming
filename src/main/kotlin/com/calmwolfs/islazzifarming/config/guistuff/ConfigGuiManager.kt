package com.calmwolfs.islazzifarming.config.guistuff

import com.calmwolfs.islazzifarming.IsLazziFarmingMod
import io.github.moulberry.moulconfig.gui.GuiScreenElementWrapper
import io.github.moulberry.moulconfig.gui.MoulConfigEditor

object ConfigGuiManager {
    val editor by lazy { MoulConfigEditor(ConfigManager.processor) }
    fun openConfigGui(search: String? = null) {
        if (search != null) {
            editor.search(search)
        }
        IsLazziFarmingMod.screenToOpen = GuiScreenElementWrapper(editor)
    }
}