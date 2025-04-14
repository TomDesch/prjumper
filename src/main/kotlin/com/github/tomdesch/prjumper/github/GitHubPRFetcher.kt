package com.github.tomdesch.prjumper.github

import com.github.tomdesch.prjumper.actions.PRContext
import com.github.tomdesch.prjumper.auth.GitHubTokenService
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

object GitHubPRFetcher {
    private val client = OkHttpClient()

    fun fetchChangedFiles(): List<String> {
        val ref = PRContext.current ?: return emptyList()
        val token = GitHubTokenService.getInstance().getToken() ?: return emptyList()

        val url = "https://api.github.com/repos/${ref.owner}/${ref.repo}/pulls/${ref.number}/files"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "token $token")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        val jsonArray = JSONArray(body)

        return (0 until jsonArray.length()).mapNotNull { i ->
            jsonArray.getJSONObject(i).optString("filename", null)
        }
    }
}