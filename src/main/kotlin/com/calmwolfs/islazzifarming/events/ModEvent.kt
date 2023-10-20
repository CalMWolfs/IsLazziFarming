package com.calmwolfs.islazzifarming.events

import com.calmwolfs.islazzifarming.data.CopyErrorCommand
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event

abstract class ModEvent : Event() {
    private val eventName by lazy {
        this::class.simpleName
    }

    fun postAndCatch(): Boolean {
        return runCatching {
            postWithoutCatch()
        }.onFailure {
            CopyErrorCommand.logError(
                it,
                "Caught a ${it::class.simpleName ?: "error"} at ${eventName}: '${it.message}'"
            )
        }.getOrDefault(isCanceled)
    }

    private fun postWithoutCatch() = MinecraftForge.EVENT_BUS.post(this)
}