package com.calmwolfs.islazzifarming.utils

import com.calmwolfs.islazzifarming.IsLazziFarmingMod
import com.calmwolfs.islazzifarming.data.CopyErrorCommand
import com.calmwolfs.islazzifarming.utils.JsonUtils.getDoubleOr
import com.calmwolfs.islazzifarming.utils.JsonUtils.getJsonArrayOr
import com.calmwolfs.islazzifarming.utils.JsonUtils.getJsonObjectOr
import com.calmwolfs.islazzifarming.utils.JsonUtils.getStringOr
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils

object ApiUtils {
    private val config get() = IsLazziFarmingMod.feature.settings
    private val parser = JsonParser()

    private var currentlyFetching = false

    private val builder: HttpClientBuilder =
        HttpClients.custom().setUserAgent("IsLazziFarming/${IsLazziFarmingMod.version}")
            .setDefaultHeaders(
                mutableListOf(
                    BasicHeader("Pragma", "no-cache"),
                    BasicHeader("Cache-Control", "no-cache")
                )
            )
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .build()
            )
            .useSystemProperties()

    private fun getJSONResponse(urlString: String): JsonObject {
        val client = builder.build()

        try {
            val response = client.execute(HttpGet(urlString))
            val entity = response.entity
            val retSrc = EntityUtils.toString(entity)

            return try {
                parser.parse(retSrc) as JsonObject
            } catch (e: JsonSyntaxException) {
                CopyErrorCommand.logError(e, "Api JSON syntax error")
                JsonObject()
            }
        } catch (e: Exception) {
            CopyErrorCommand.logError(e, "Api error")
            JsonObject()
        } finally {
            client.close()
        }
        return JsonObject()
    }

    suspend fun getLazziStats(): Double? {
        if (config.apiKey.isEmpty()) return null
        if (currentlyFetching) return null
        currentlyFetching = true

        val uuid = "3b1cd284767a4a5a92db253ead0621b2"
        val profileId = "a37e31f9-6ecb-4cae-8e08-d575a2e19d7e"
        val url = "https://api.hypixel.net/skyblock/profiles?key=${config.apiKey}&uuid=$uuid"

        try {
            val result = withContext(Dispatchers.IO) { getJSONResponse(url) }.asJsonObject
            val playerJson = result.getJsonArrayOr("profiles")
            for (profile in playerJson) {
                val profileObj = profile.asJsonObject
                if (profileObj.getStringOr("profile_id") != profileId) continue
                val members = profileObj.getAsJsonObject("members")
                val lazziStats = members.getJsonObjectOr(uuid)
                val farmingExp = lazziStats.getDoubleOr("experience_skill_farming")
                if (farmingExp != 0.0) run {
                    currentlyFetching = false
                    return farmingExp
                }
            }
        } catch (_: Exception) {
            println("Hypixel api issue")
        }
        currentlyFetching = false
        return null
    }
}