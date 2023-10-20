package com.calmwolfs.islazzifarming.events

class ModTickEvent(private val tick: Int) : ModEvent() {
    private fun isMod(i: Int) = tick % i == 0

    fun repeatSeconds(i: Int) = isMod(i * 20)
}