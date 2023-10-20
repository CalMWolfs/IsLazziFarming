package com.calmwolfs.islazzifarming.data

import com.calmwolfs.islazzifarming.utils.SimpleTimeMark
import net.minecraft.item.ItemStack

data class Notification(
    val notificationLines: List<String>,
    val notificationItem: ItemStack?,
    val startTime: SimpleTimeMark,
    var endTime: SimpleTimeMark
)