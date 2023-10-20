package com.calmwolfs.islazzifarming.utils

import java.awt.Desktop
import java.io.IOException
import java.net.URI

object WebUtils {
    @JvmStatic
    fun openBrowser(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (e: IOException) {
                e.printStackTrace()
                ModUtils.error("[IsLazziFarming] Error opening website: $url!")
            }
        } else {
            ClipboardUtils.copyToClipboard(url)
            ModUtils.warning("[IsLazziFarming] Web browser is not supported! Copied url to clipboard.")
        }
    }
}