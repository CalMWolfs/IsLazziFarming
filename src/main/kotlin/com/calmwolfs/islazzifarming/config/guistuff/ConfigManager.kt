package com.calmwolfs.islazzifarming.config.guistuff

import com.calmwolfs.islazzifarming.IsLazziFarmingMod
import com.calmwolfs.islazzifarming.config.Features
import com.calmwolfs.islazzifarming.config.UpdateManager
import com.calmwolfs.islazzifarming.data.CopyErrorCommand
import com.calmwolfs.islazzifarming.utils.SimpleTimeMark
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.moulberry.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.moulberry.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.moulberry.moulconfig.processor.ConfigProcessorDriver
import io.github.moulberry.moulconfig.processor.MoulConfigProcessor
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.concurrent.fixedRateTimer

object ConfigManager {
    val gson = GsonBuilder().setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
        .enableComplexMapKeySerialization()
        .create()

    lateinit var features: Features

    private var configDirectory = File("config/islazzifarming")
    private var configFile: File? = null
    lateinit var processor: MoulConfigProcessor<Features>

    fun firstLoad() {
        if (ConfigManager::features.isInitialized) {
            println("Loading config despite config being already loaded?")
        }
        configDirectory.mkdir()

        configFile = File(configDirectory, "config.json")

        println("Trying to load config from $configFile")

        if (configFile!!.exists()) {
            try {
                val builder = StringBuilder()

                println("load-config-now")
                BufferedReader(FileReader(configFile!!)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        builder.append(line).append('\n')
                    }
                }
                val configJson = gson.fromJson(builder.toString(), JsonObject::class.java)
                features = gson.fromJson(configJson, Features::class.java)

                println("Loaded config from file")
            } catch (error: Exception) {
                error.printStackTrace()
                val backupFile = configFile!!.resolveSibling("config-${SimpleTimeMark.now().toMillis()}-backup.json")
                println("Exception while reading $configFile. Will load blank config and save backup to $backupFile")
                println("Exception was $error")
                try {
                    configFile!!.copyTo(backupFile)
                } catch (e: Exception) {
                    println("Could not create backup for config file")
                    e.printStackTrace()
                }
            }
        }

        if (!ConfigManager::features.isInitialized) {
            println("Creating blank config and saving to file")
            features = Features()
            saveConfig("blank config")
        }

        fixedRateTimer(name = "islazzifarming-config-auto-save", period = 60_000L, initialDelay = 60_000L) {
            try {
                saveConfig("auto-save-60s")
            } catch (error: Throwable) {
                CopyErrorCommand.logError(error, "Error auto-saving config!")
            }
        }

        val features = IsLazziFarmingMod.feature
        processor = MoulConfigProcessor(IsLazziFarmingMod.feature)
        BuiltinMoulConfigGuis.addProcessors(processor)
        UpdateManager.injectConfigProcessor(processor)
        ConfigProcessorDriver.processConfig(
            features.javaClass,
            features,
            processor
        )
    }

    fun saveConfig(reason: String) {
        println("saveConfig: $reason")
        val file = configFile ?: throw Error("Can not save config, configFile is null!")
        try {
            println("Saving config file")
            file.parentFile.mkdirs()
            val unit = file.parentFile.resolve("config.json.write")
            unit.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream(unit), StandardCharsets.UTF_8)).use { writer ->
                writer.write(gson.toJson(IsLazziFarmingMod.feature))
            }

            Files.move(
                unit.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (e: IOException) {
            println("Could not save config file to $file")
            e.printStackTrace()
        }
    }
}