package com.calmwolfs.islazzifarming.data

import com.calmwolfs.islazzifarming.config.guistuff.ConfigGuiManager
import com.calmwolfs.islazzifarming.features.GetLazziStats
import net.minecraft.command.ICommandSender
import net.minecraftforge.client.ClientCommandHandler

object Commands {
    private val openConfig: (Array<String>) -> Unit = {
        if (it.isNotEmpty()) {
            ConfigGuiManager.openConfigGui(it.joinToString(" "))
        } else {
            ConfigGuiManager.openConfigGui()
        }
    }

    fun init() {
        registerCommand("lz", openConfig)
        registerCommand("lazzi", openConfig)
        registerCommand("islazzifarming", openConfig)

        registerCommand("lzcopyerror") { CopyErrorCommand.command(it) }
        registerCommand("lzrefresh") { GetLazziStats.loadStats() }
    }

    private fun registerCommand(name: String, function: (Array<String>) -> Unit) {
        ClientCommandHandler.instance.registerCommand(SimpleCommand(name, createCommand(function)))
    }

    private fun createCommand(function: (Array<String>) -> Unit) = object : SimpleCommand.ProcessCommandRunnable() {
        override fun processCommand(sender: ICommandSender?, args: Array<String>?) {
            if (args != null) function(args.asList().toTypedArray())
        }
    }
}