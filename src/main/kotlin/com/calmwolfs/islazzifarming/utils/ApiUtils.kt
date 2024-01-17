package com.calmwolfs.islazzifarming.utils

import com.calmwolfs.islazzifarming.IsLazziFarmingMod
import com.calmwolfs.islazzifarming.data.CopyErrorCommand
import com.calmwolfs.islazzifarming.utils.JsonUtils.getBooleanOr
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

    suspend fun getPlayerStats(uuid: String): Double? {
        val url = "https://is-lazzi-farming.cwolfson58.workers.dev/?uuid=$uuid&v2=true"

        try {
            val result = withContext(Dispatchers.IO) { getJSONResponse(url) }.asJsonObject

            val playerJson = result.getJsonArrayOr("profiles")
            for (profile in playerJson) {
                val profileObj = profile.asJsonObject
                if (!profileObj.getBooleanOr("selected")) continue
                val members = profileObj.getAsJsonObject("members")
                val playerStats = members.getJsonObjectOr(uuid)
                val playerData = playerStats.getJsonObjectOr("player_data")
                val experienceData = playerData.getJsonObjectOr("experience")
                val farmingExp = experienceData.getDoubleOr("SKILL_FARMING", -1.0)
                if (farmingExp != -1.0) run {
                    return farmingExp
                }
            }
        } catch (_: Exception) {
            println("Hypixel or proxy server api issue")
        }
        return null
    }

    suspend fun getUuid(playerName: String): String? {
        val fetchedName = playerName.lowercase()
        if (currentlyFetching) return null
        currentlyFetching = true

        val url = "https://api.mojang.com/users/profiles/minecraft/$fetchedName"
        try {
            val result = withContext(Dispatchers.IO) { getJSONResponse(url) }.asJsonObject
            currentlyFetching = false
            return result.getStringOr("id")
        } catch (_: Exception) {
            println("Mojang api issue")
        }

        currentlyFetching = false
        return null
    }
}