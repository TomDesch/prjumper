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

    data class PullRequestSummary(val number: Int, val title: String)

    data class RepoSummary(val owner: String, val name: String)

    fun fetchUserRepos(): List<RepoSummary> {
        val token = GitHubTokenService.getInstance().getToken() ?: return emptyList()

        val url = "https://api.github.com/user/repos?per_page=100"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "token $token")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        val jsonArray = JSONArray(body)

        return (0 until jsonArray.length()).mapNotNull { i ->
            val obj = jsonArray.getJSONObject(i)
            val name = obj.optString("name")
            val owner = obj.getJSONObject("owner").optString("login")
            if (name.isNotBlank() && owner.isNotBlank()) RepoSummary(owner, name) else null
        }
    }


    fun fetchOpenPRs(owner: String, repo: String): List<PullRequestSummary> {
        val token = GitHubTokenService.getInstance().getToken() ?: return emptyList()

        val url = "https://api.github.com/repos/$owner/$repo/pulls"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "token $token")
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val body = response.body?.string() ?: return emptyList()
        val jsonArray = JSONArray(body)

        return (0 until jsonArray.length()).mapNotNull { i ->
            val obj = jsonArray.getJSONObject(i)
            val number = obj.optInt("number")
            val title = obj.optString("title", "")
            if (number != 0) PullRequestSummary(number, title) else null
        }
    }

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


        val raw = response.body?.string()
        logger.info("/user/repos response: $raw")


        val jsonArray = JSONArray(raw)

        return (0 until jsonArray.length()).mapNotNull { i ->
            jsonArray.getJSONObject(i).optString("filename", null)
        }
    }

    fun debugCheckToken(): String {
        val token = GitHubTokenService.getInstance().getToken() ?: return "No token stored"

        val request = Request.Builder()
            .url("https://api.github.com/user")
            .header("Authorization", "token $token")
            .build()

        return try {
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful) "Token OK: $body" else "Token failed: $body"
        } catch (e: Exception) {
            "Request error: ${e.message}"
        }
    }
}