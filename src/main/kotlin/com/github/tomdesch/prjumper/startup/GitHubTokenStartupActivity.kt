package com.github.tomdesch.prjumper.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.github.tomdesch.prjumper.ui.GitHubTokenDialog
import okhttp3.OkHttpClient
import okhttp3.Request

class GitHubTokenStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        val token = System.getenv("GITHUB_TOKEN")

        if (token.isNullOrBlank() || !isTokenValid(token)) {
            ApplicationManager.getApplication().invokeLater {
                GitHubTokenDialog().show()
            }
        }
    }

    private fun isTokenValid(token: String): Boolean {
        val request = Request.Builder()
            .url("https://api.github.com/user")
            .header("Authorization", "token $token")
            .build()

        return try {
            OkHttpClient().newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }
}