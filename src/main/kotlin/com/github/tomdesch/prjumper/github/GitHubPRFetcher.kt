package com.github.tomdesch.prjumper.github

import com.github.tomdesch.prjumper.actions.PRContext
import com.github.tomdesch.prjumper.auth.GitHubTokenService
import com.intellij.openapi.diagnostic.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray


object GitHubPRFetcher {
    private val client = OkHttpClient()
    private val logger = Logger.getInstance(GitHubPRFetcher::class.java)


    fun fetchChangedFiles(): List<String> {
        val ref = PRContext.current ?: return emptyList()
        val token = GitHubTokenService.getInstance().getToken() ?: return emptyList()

        val url = "https://api.github.com/repos/${ref.owner}/${ref.repo}/pulls/${ref.number}/files"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "token $token")
            .build()

        logger.info("? Fetching PR files: $url")
        val response = client.newCall(request).execute()

        logger.info("? GitHub Response Code: ${response.code}")

        if (!response.isSuccessful) {
            logger.warn("? GitHub API error: ${response.body?.string()}")
            return emptyList()
        }

        val body = response.body?.string() ?: return emptyList()
        logger.debug("? Raw response body: $body")

        val jsonArray = JSONArray(body)

        return (0 until jsonArray.length()).mapNotNull { i ->
            jsonArray.getJSONObject(i).optString("filename", null)
        }
    }
}