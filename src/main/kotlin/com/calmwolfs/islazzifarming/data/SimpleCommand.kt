package com.calmwolfs.islazzifarming.data

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class SimpleCommand(private val commandName: String, private val runnable: ProcessCommandRunnable) : CommandBase() {
    private var tabRunnable: TabCompleteRunnable? = null

    abstract class ProcessCommandRunnable {
        abstract fun processCommand(sender: ICommandSender?, args: Array<String>?)
    }

    interface TabCompleteRunnable {
        fun tabComplete(sender: ICommandSender?, args: Array<String>?, pos: BlockPos?): List<String>
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean {
        return true
    }

    override fun getCommandName(): String {
        return commandName
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/$commandName"
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        try {
            runnable.processCommand(sender, args)
        } catch (e: Throwable) {
            CopyErrorCommand.logError(e, "Error while running command /$commandName")
        }
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String>? {
        return if (tabRunnable != null) tabRunnable!!.tabComplete(sender, args, pos) else null
    }
}